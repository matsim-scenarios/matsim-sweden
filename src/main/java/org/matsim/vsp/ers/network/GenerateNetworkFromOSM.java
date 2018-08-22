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
 * created by jbischoff, 21.08.2018
 */

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.NetworkWriter;
import org.matsim.api.core.v01.network.Node;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.network.algorithms.NetworkSimplifier;
import org.matsim.core.utils.geometry.CoordUtils;
import org.matsim.core.utils.geometry.transformations.TransformationFactory;
import org.matsim.core.utils.io.OsmNetworkReader;
import org.matsim.run.NetworkCleaner;

import java.util.HashSet;
import java.util.Set;

public class GenerateNetworkFromOSM {

    public static void main(String[] args) {
        String networkFile = "D:/ers/network/osm/sweden-merged-matsim.xml";
        String networkFileC = "D:/ers/network/osm/sweden-merged-matsim-cleaned.xml";

        Network network = NetworkUtils.createNetwork();
        OsmNetworkReader osmNetworkReader = new OsmNetworkReader(network, TransformationFactory.getCoordinateTransformation(TransformationFactory.WGS84, "EPSG:3006"), true, true);

        osmNetworkReader.parse("D:/ers/network/osm/sweden-merged.osm");

        Node vvik = network.getNodes().get(Id.createNodeId(566539));
        Node visby = network.getNodes().get(Id.createNodeId(2045331593));
        Node ohamm = network.getNodes().get(Id.createNodeId("4908049077"));

        Link l1 = network.getFactory().createLink(Id.createLinkId("f1"), vvik, visby);
        Link l3 = network.getFactory().createLink(Id.createLinkId("f3"), ohamm, visby);
        Link l4 = network.getFactory().createLink(Id.createLinkId("f4"), visby, ohamm);
        Link l2 = network.getFactory().createLink(Id.createLinkId("f2"), visby, vvik);


        l1.setLength(CoordUtils.calcEuclideanDistance(visby.getCoord(), vvik.getCoord()));
        l1.setCapacity(500);
        l1.setFreespeed(25 / 3.6);
        l1.setNumberOfLanes(1);

        l3.setLength(CoordUtils.calcEuclideanDistance(visby.getCoord(), ohamm.getCoord()));
        l3.setCapacity(500);
        l3.setFreespeed(25 / 3.6);
        l3.setNumberOfLanes(1);

        l4.setLength(CoordUtils.calcEuclideanDistance(visby.getCoord(), ohamm.getCoord()));
        l4.setCapacity(500);
        l4.setFreespeed(25 / 3.6);
        l4.setNumberOfLanes(1);


        l2.setLength(CoordUtils.calcEuclideanDistance(visby.getCoord(), vvik.getCoord()));
        l2.setCapacity(500);
        l2.setFreespeed(25 / 3.6);
        l2.setNumberOfLanes(1);

        network.addLink(l1);
        network.addLink(l2);
        network.addLink(l3);
        network.addLink(l4);
        Set<String> modes = new HashSet<>();
        modes.add(TransportMode.car);
        modes.add(TransportMode.truck);
        network.getLinks().values().forEach(l -> l.setAllowedModes(modes));

        new NetworkSimplifier().run(network);

        new NetworkWriter(network).write(networkFile);
        new NetworkCleaner().run(networkFile, networkFileC);

    }
}
