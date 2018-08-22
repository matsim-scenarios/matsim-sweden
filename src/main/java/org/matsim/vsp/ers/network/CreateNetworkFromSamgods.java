/* *********************************************************************** *
 * project: org.matsim.*
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2016 by the members listed in the COPYING,        *
 *                   LICENSE and WARRANTY file.                            *
 * email           : info at matsim dot org                                *
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *   See also COPYING, LICENSE and WARRANTY file                           *
 *                                                                         *
 * *********************************************************************** */

package org.matsim.vsp.ers.network;/*
 * created by jbischoff, 30.05.2018
 */

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.NetworkWriter;
import org.matsim.api.core.v01.network.Node;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.network.filter.NetworkFilterManager;
import org.matsim.core.network.filter.NetworkLinkFilter;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.utils.geometry.CoordinateTransformation;
import org.matsim.core.utils.geometry.transformations.TransformationFactory;
import org.matsim.core.utils.io.tabularFileParser.TabularFileHandler;
import org.matsim.core.utils.io.tabularFileParser.TabularFileParser;
import org.matsim.core.utils.io.tabularFileParser.TabularFileParserConfig;
import org.matsim.run.NetworkCleaner;

import java.util.HashSet;
import java.util.Set;

public class CreateNetworkFromSamgods {

    private static final String nodesFile = "D:\\ers\\network/Base2012_Node.csv";
    private static final String linksFile = "D:\\ers\\network/Base2012_Link.csv";
    private static final String networkFile = "D:\\ers\\network/Base2012_network_car.xml";
    private static final String networkFileC = "D:\\ers\\network/Base2012_network_car_cleaned.xml";
    Network network;
    private CoordinateTransformation ct = TransformationFactory.getCoordinateTransformation("EPSG:3021", "EPSG:3006");
    private double carlinkdetourfactor = 1.2;

    public static void main(String[] args) {
        CreateNetworkFromSamgods createNetworkFromSamgods = new CreateNetworkFromSamgods();
        createNetworkFromSamgods.run();


    }

    private void run() {
        Scenario scenario = ScenarioUtils.createScenario(ConfigUtils.createConfig());
        network = scenario.getNetwork();
        createNodes();
        createLinks();
        NetworkFilterManager networkFilterManager = new NetworkFilterManager(network);
        networkFilterManager.addLinkFilter(new NetworkLinkFilter() {
            @Override
            public boolean judgeLink(Link l) {
                if (l.getCoord().getX() > -533409 && l.getCoord().getX() < 1874924 && l.getCoord().getY() > 5666078 && l.getCoord().getY() < 7913083)
                    return true;
                else return false;
            }
        });
        Network network2 = networkFilterManager.applyFilters();
        new NetworkWriter(network2).write(networkFile);

        new NetworkCleaner().run(networkFile, networkFileC);
    }


    private void createNodes() {
        TabularFileParserConfig tbc = new TabularFileParserConfig();
        tbc.setDelimiterTags(new String[]{";"});
        tbc.setFileName(nodesFile);
        new TabularFileParser().parse(tbc, new TabularFileHandler() {
            boolean headerRead = false;

            @Override
            public void startRow(String[] row) {
                if (headerRead) {
                    Id<Node> nodeId = Id.createNodeId(row[2]);
                    Coord coord = ct.transform(new Coord(Double.parseDouble(row[3]), Double.parseDouble(row[4])));
                    Node node = network.getFactory().createNode(nodeId, coord);
                    node.getAttributes().putAttribute("NORIG", row[5]);
                    network.addNode(node);
                } else {
                    headerRead = true;
                }
            }
        });
    }

    private void createLinks() {
        TabularFileParserConfig tbc = new TabularFileParserConfig();
        tbc.setDelimiterTags(new String[]{";"});
        tbc.setFileName(linksFile);
        new TabularFileParser().parse(tbc, new TabularFileHandler() {
            boolean headerRead = false;

            @Override
            public void startRow(String[] row) {
                if (headerRead) {
                    Id<Link> linkId = Id.createLinkId(row[0]);
                    Id<Node> fromnodeId = Id.createNodeId(row[2]);
                    Id<Node> tonodeId = Id.createNodeId(row[3]);
                    Node fromNode = network.getNodes().get(fromnodeId);
                    Node toNode = network.getNodes().get(tonodeId);
                    if (fromNode == null) {
                        throw new RuntimeException("Node " + fromnodeId.toString() + " not found.");
                    }
                    if (toNode == null) {
                        throw new RuntimeException("Node " + fromnodeId.toString() + " not found.");
                    }
                    Link l = network.getFactory().createLink(linkId, fromNode, toNode);
                    l.setLength(Double.parseDouble(row[13]));
                    double speed = Double.parseDouble(row[6]) / 3.6;
                    double lanes = Double.parseDouble(row[9]);
                    int category = Integer.parseInt(row[7]);
                    l.setFreespeed(speed);
                    l.setNumberOfLanes(lanes);
                    setCapacityAndMode(category, l);

                    l.getAttributes().putAttribute("category", category);
                    if (l.getAllowedModes().contains(TransportMode.car)) network.addLink(l);
                } else {
                    headerRead = true;
                }
            }
        });
    }

    private void setCapacityAndMode(int category, Link link) {
        Set<String> modes = new HashSet<>();
        double capacity = 0;
        double freeSpeed = link.getFreespeed();
        double lanes = link.getNumberOfLanes();
        switch (category) {
            //major motorway, e.g. E4

            case 11:
            case 501: {
                modes.add(TransportMode.truck);
                modes.add(TransportMode.car);
                capacity = 1800 * lanes;
                freeSpeed = 110 / 3.6;
                break;
            }
            case 12:
            case 502: { //freeway
                modes.add(TransportMode.truck);
                modes.add(TransportMode.car);
                freeSpeed = 90 / 3.6;
                capacity = 1500 * lanes;
                break;
            }
            case 13:
            case 503: { //highway
                modes.add(TransportMode.truck);
                modes.add(TransportMode.car);
                freeSpeed = 90 / 3.6;
                capacity = 1200 * lanes;
                break;
            }

            case 14:
            case 15:
            case 16:
            case 17:
            case 18:
            case 504: { //highway
                modes.add(TransportMode.truck);
                modes.add(TransportMode.car);
                capacity = 900 * lanes;
                break;
            }
            case 110:
            case 201:
            case 610:
            case 701: {
                //connectors
                modes.add(TransportMode.truck);
                modes.add(TransportMode.car);
                capacity = 1500;
                break;

            }
            case 70:
            case 71:
            case 72:
            case 73:
            case 74:
            case 75:
            case 76:
            case 77:
            case 211:
            case 570:
            case 571:
            case 572:
            case 573:
            case 574:
            case 578:
            case 579:
            case 670:
            case 678:
            case 679:
            case 711: {
                modes.add(TransportMode.train);
                capacity = 30;
                freeSpeed = 13;
                break;


            }
            case 241:
            case 560:
            case 741: {
                modes.add(TransportMode.airplane);
                capacity = 60;
                freeSpeed = 120;
                break;
            }
            case 80:
            case 81:
            case 85:
            case 86:
            case 89:
            case 221:
            case 540:
            case 580:
            case 581:
            case 586:
            case 589:
            case 721: {
                modes.add(TransportMode.ship);
                capacity = 300;
                freeSpeed = 6;
                break;
            }
            case 90:
            case 91:
            case 96:
            case 231:
            case 590:
            case 595:
            case 599:
            case 731: {
                modes.add(TransportMode.ship);
                modes.add(TransportMode.car);
                modes.add(TransportMode.truck);
                capacity = 500;
                freeSpeed = 3;
                break;
//                These are ferry lines
            }

            default:
                throw new RuntimeException("Category: " + category + " for Link " + link.getId() + " is undefined.");
        }

        link.setFreespeed(freeSpeed);
        if (freeSpeed == 0.0) {
            System.err.println("Category: " + category + " for Link " + link.getId() + " has 0.0 freespeded");

        }
        link.setAllowedModes(modes);
        link.setCapacity(capacity);
        if (modes.contains(TransportMode.car) && (!modes.contains(TransportMode.ship))) {
            link.setLength(link.getLength() * carlinkdetourfactor + 1.0);
        }


    }

}
