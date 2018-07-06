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

package org.matsim.vsp.ers.demand.commuters;/*
 * created by jbischoff, 04.06.2018
 */

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.population.*;
import org.matsim.core.config.Config;
import org.matsim.core.gbl.MatsimRandom;
import org.matsim.core.utils.gis.ShapeFileReader;
import org.opengis.feature.simple.SimpleFeature;
import playground.vsp.corineLandcover.CorineLandCoverData;
import playground.vsp.corineLandcover.LandCoverUtils;
import playground.vsp.openberlinscenario.cemdap.input.CommuterRelationV2;

import java.util.*;

import static org.matsim.core.config.ConfigUtils.createConfig;
import static org.matsim.core.scenario.ScenarioUtils.createScenario;

public class CreateCommuterDemand {

    public static final String COMMUTERS = "D:/ers/commuters/commuters2016.csv";
    public static final String COMMUNITYSHAPE = "D:/ers/commuters/Kommun_Sweref99TM_region.shp";
    private int agentCounter = 0;
    private double fraction = 1.0;
    private List<CommuterRelationV2> commuterRelations;
    private Map<String, Geometry> communities;
    private Population population;
    private CorineLandCoverData corineLandCoverData;
    private Random random = MatsimRandom.getRandom();

    private List<String> metropolitanRegions = Arrays.asList(new String[]{"0114", "0115", "1402", "0117", "1407", "0120", "0123", "0125", "0126", "0127", "0128", "0136", "0138", "0139", "1440", "0140", "1441", "0160", "0162", "0163", "0180", "0181", "0182", "0183", "0184", "0186", "0187", "0191", "0192", "1230", "1480", "1231", "1481", "1233", "1482", "1261", "1262", "1263", "1280", "1281", "1285", "1287", "1384"});

    public static void main(String[] args) {
        new CreateCommuterDemand().run();
    }

    public void run() {
        Config config = createConfig();
        Scenario scenario = createScenario(config);
        this.population = scenario.getPopulation();
        ReadCommuterFile readCommuterFile = new ReadCommuterFile();
        readCommuterFile.readCommuterFile(COMMUTERS);
        commuterRelations = readCommuterFile.getCommuterRelations();
        communities = readShapeFileAndExtractGeometry(COMMUNITYSHAPE, "KnKod");
        corineLandCoverData = new CorineLandCoverData("D:/ers/clc/landcover_rel_se.shp");
        commuterRelations.forEach(commuterRelationV2 -> createAgents(commuterRelationV2));
        new PopulationWriter(population).write("D:/ers/commuters/commuter_population_" + fraction + ".xml.gz");
    }

    private void createAgents(CommuterRelationV2 commuterRelation) {
        PopulationFactory f = population.getFactory();
        Geometry fromGeo = communities.get(commuterRelation.getFrom());
        if (fromGeo == null) {
            throw new RuntimeException(commuterRelation.getFrom() + " - community not found in Shape.");
        }
        Geometry toGeo = communities.get(commuterRelation.getTo());
        if (toGeo == null) {
            throw new RuntimeException(commuterRelation.getTo() + " - community not found in Shape.");
        }

        for (int i = 0; i < commuterRelation.getTrips(); i++) {
            if (agentCounter % (1.00 / fraction) != 0) {
                agentCounter++;
                continue;
            }
            boolean metropolitanRegion = (metropolitanRegions.contains(commuterRelation.getFrom()) && metropolitanRegions.contains(commuterRelation.getTo()));

            Person person = f.createPerson(Id.createPersonId(commuterRelation.getFrom() + "_" + commuterRelation.getTo() + "_" + agentCounter));
            person.getAttributes().putAttribute("metropolitanRegion", metropolitanRegion);
            person.getAttributes().putAttribute("homeZone", commuterRelation.getFrom());
            person.getAttributes().putAttribute("workZone", commuterRelation.getTo());
            agentCounter++;
            population.addPerson(person);
            Plan plan = f.createPlan();
            person.addPlan(plan);
            Coord homeCoord = corineLandCoverData.getRandomCoord(fromGeo, LandCoverUtils.LandCoverActivityType.home);
            Coord workCoord = corineLandCoverData.getRandomCoord(fromGeo, LandCoverUtils.LandCoverActivityType.other);
            Activity h1 = f.createActivityFromCoord("home", homeCoord);
            h1.setEndTime(6.5 * 3600 + random.nextInt(9000));
            plan.addActivity(h1);
            Leg l1 = f.createLeg(TransportMode.car);
            plan.addLeg(l1);
            Activity w = f.createActivityFromCoord("work", workCoord);
            w.setEndTime(h1.getEndTime() + 5 * 3600 + random.nextInt(10800));
            plan.addActivity(w);
            Leg l2 = f.createLeg(TransportMode.car);
            plan.addLeg(l2);

            Activity h2 = f.createActivityFromCoord("home", homeCoord);
            plan.addActivity(h2);
        }
    }

    public static Map<String, Geometry> readShapeFileAndExtractGeometry(String filename, String key) {

        Map<String, Geometry> geometry = new TreeMap<>();
        for (SimpleFeature ft : ShapeFileReader.getAllFeatures(filename)) {

            GeometryFactory geometryFactory = new GeometryFactory();
            WKTReader wktReader = new WKTReader(geometryFactory);

            try {
                Geometry geo = wktReader.read((ft.getAttribute("the_geom")).toString());
                String lor = ft.getAttribute(key).toString();
                geometry.put(lor, geo);
            } catch (ParseException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }


        }
        return geometry;
    }
}
