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
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Population;
import org.matsim.api.core.v01.population.PopulationWriter;
import org.matsim.core.config.Config;
import org.matsim.core.gbl.MatsimRandom;
import org.matsim.core.utils.geometry.CoordUtils;
import org.matsim.core.utils.geometry.geotools.MGC;
import org.matsim.core.utils.gis.ShapeFileReader;
import org.opengis.feature.simple.SimpleFeature;
import playground.vsp.corineLandcover.CorineLandCoverData;
import playground.vsp.openberlinscenario.cemdap.input.CommuterRelationV2;

import java.util.*;
import java.util.stream.Collectors;

import static org.matsim.core.config.ConfigUtils.createConfig;
import static org.matsim.core.scenario.ScenarioUtils.createScenario;

public class CreateCommuterDemand {

    public static final String COMMUTERS = "D:/ers/commuters/commuters2016.csv";
    public static final String COMMUNITYSHAPE = "D:/ers/commuters/Kommun_Sweref99TM_region.shp";
    private double maxCommutingBeelineDistance = 80000;
    private int agentCounter = 0;
    private double fraction = 0.1;
    private List<CommuterRelationV2> commuterRelations;
    private Map<String, Geometry> communities;
    private Population population;
    private CorineLandCoverData corineLandCoverData;
    private Random random = MatsimRandom.getRandom();
    private int noOfThreads = 4;

    private List<String> metropolitanRegions = Arrays.asList(new String[]{"0114", "0115", "1402", "0117", "1407", "0120", "0123", "0125", "0126", "0127", "0128", "0136", "0138", "0139", "1440", "0140", "1441", "0160", "0162", "0163", "0180", "0181", "0182", "0183", "0184", "0186", "0187", "0191", "0192", "1230", "1480", "1231", "1481", "1233", "1482", "1261", "1262", "1263", "1280", "1281", "1285", "1287", "1384"});
    private List<String> stockholm = Arrays.asList(new String[]{"0188", "0114", "0115", "1402", "0117", "1407", "0120", "0123", "0125", "0126", "0127", "0128", "0136", "0138", "0139", "1440", "0140", "1441", "0160", "0162", "0163", "0180", "0181", "0182", "0183", "0184", "0186", "0187", "0191", "0192",});

    public static void main(String[] args) {
        new CreateCommuterDemand().run();
    }

    public void run() {
        Config config = createConfig();
        Scenario scenario = createScenario(config);
        this.population = scenario.getPopulation();
        ReadCommuterFile readCommuterFile = new ReadCommuterFile();
        readCommuterFile.readCommuterFile(COMMUTERS);
        communities = readShapeFileAndExtractGeometry(COMMUNITYSHAPE, "KnKod");
        commuterRelations = readCommuterFile.getCommuterRelations();
        commuterRelations = filterCommuterRelations(commuterRelations);

        corineLandCoverData = new CorineLandCoverData("D:/ers/clc/landcover_rel_se.shp");
        ParallelDemandGenerator generator = new ParallelDemandGenerator(communities, corineLandCoverData, metropolitanRegions, MatsimRandom.getLocalInstance(), fraction, population.getFactory());
        List<Person> personList = commuterRelations.parallelStream().flatMap(r -> generator.generatePersons(r).stream()).collect(Collectors.toList());
        personList.forEach(population::addPerson);

        new PopulationWriter(population).write("D:/ers/commuters/commuter_population_shrinked_noStockholm_" + fraction + ".xml.gz");
    }

    private List<CommuterRelationV2> filterCommuterRelations(List<CommuterRelationV2> commuterRelations) {
        List<CommuterRelationV2> relations = new ArrayList<>();
        for (CommuterRelationV2 relation : commuterRelations) {
            if (stockholm.contains(relation.getFrom())) continue;

            Coord c1 = MGC.point2Coord(communities.get(relation.getFrom()).getCentroid());
            Coord c2 = MGC.point2Coord(communities.get(relation.getTo()).getCentroid());
            if (CoordUtils.calcEuclideanDistance(c1, c2) <= maxCommutingBeelineDistance) {
                relations.add(relation);
            }
        }
        return relations;
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
