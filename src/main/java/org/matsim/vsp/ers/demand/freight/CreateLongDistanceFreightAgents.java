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

package org.matsim.vsp.ers.demand.freight;/*
 * created by jbischoff, 16.05.2018
 */

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.population.*;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.gbl.MatsimRandom;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.facilities.ActivityFacility;
import org.matsim.vehicles.VehicleType;

import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.function.Predicate;

public class CreateLongDistanceFreightAgents {


    public static final double NUMBEROFDAYS = 365 - 52 - 26 - 10; //assuming almost no freight traffic on sundays and holidays and half the load on saturdays.

    private Map<Id<ActivityFacility>, ParseNodeLocations.SamGodsNode> nodes;
    private Set<ParseODs.GoodsFlow> goodsFlows;
    private Random r = MatsimRandom.getRandom();
    private static double samplesize = 0.1;

    public static void main(String[] args) {
        new CreateLongDistanceFreightAgents().run();
    }

    private Scenario scenario;

    private void run() {

        scenario = ScenarioUtils.createScenario(ConfigUtils.createConfig());

        ParseODs ods = new ParseODs();
        String inputFolder = "D:/ers/samgods/Loaded/";
        ods.run(inputFolder, true);
        String inputFolderEmpty = "D:/ers/samgods/Empty/";
        ods.run(inputFolderEmpty, false);
        String nodesFile = "D:/ers/samgods/nodes.csv";

        this.goodsFlows = ods.getGoodsFlows();
        ParseNodeLocations parseNodeLocations = new ParseNodeLocations();
        parseNodeLocations.run(nodesFile);
        this.nodes = parseNodeLocations.getNodes();
        calculateDailyTripNumbers(goodsFlows, r);
        goodsFlows.stream().filter(new TouchesSweden()).forEach(flow -> generateAgents(flow));
        new PopulationWriter(scenario.getPopulation()).write("D:/ers/samgods/samgoodspopulation" + samplesize + ".xml");
    }

    private void generateAgents(ParseODs.GoodsFlow flow) {
        Population population = scenario.getPopulation();
        PopulationFactory pf = population.getFactory();
        String mode = getMode(flow.vehicleTypeId);
        if (!mode.equals(TransportMode.truck)) return;
        ParseNodeLocations.SamGodsNode fromNode = nodes.get(flow.fromId);
        ParseNodeLocations.SamGodsNode toNode = nodes.get(flow.toId);

        String loaded = (flow.loaded) ? "l" : "e";
        for (int i = 0; i < flow.dailyTrips * samplesize; i++) {
            Id<Person> personId = Id.createPersonId(flow.fromId.toString() + "_" + flow.toId.toString() + "_" + flow.vehicleTypeId.toString() + "_" + loaded + "_" + i);
            Person person = pf.createPerson(personId);
            population.addPerson(person);
            Plan plan = pf.createPlan();
            person.addPlan(plan);
            Activity a1 = pf.createActivityFromCoord("freight", fromNode.coord);
            a1.setEndTime(r.nextInt(20 * 3600));

            Leg leg = pf.createLeg(mode);
            leg.getAttributes().putAttribute("vehicleType", flow.vehicleTypeId.toString());
            Activity a2 = pf.createActivityFromCoord("freight", toNode.coord);

            plan.addActivity(a1);
            plan.addLeg(leg);
            plan.addActivity(a2);
        }

    }


    private void calculateDailyTripNumbers(Set<ParseODs.GoodsFlow> flows, Random random) {
        for (ParseODs.GoodsFlow od : flows) {
            double dailyFlow = od.annualFlow / NUMBEROFDAYS;
            double fullTrips = Math.floor(dailyFlow);
            double rest = dailyFlow - fullTrips;
            if (random.nextDouble() < rest) {
                fullTrips++;
            }
            od.dailyTrips = fullTrips;

        }
    }


    private String getMode(Id<VehicleType> vehicleTypeId) {
        if (vehicleTypeId.toString().startsWith("1")) return TransportMode.truck;
        if (vehicleTypeId.toString().startsWith("2")) return TransportMode.train;
        if (vehicleTypeId.toString().startsWith("3")) return TransportMode.ship;
        if (vehicleTypeId.toString().startsWith("4")) return TransportMode.airplane;
        throw new RuntimeException("No mode found for vehicleType: " + vehicleTypeId.toString());
    }

    class TouchesSweden implements Predicate<ParseODs.GoodsFlow> {

        @Override
        public boolean test(ParseODs.GoodsFlow flow) {
            ParseNodeLocations.SamGodsNode fromNode = nodes.get(flow.fromId);
            ParseNodeLocations.SamGodsNode toNode = nodes.get(flow.toId);
            if (fromNode.domestic || toNode.domestic) return true;
            else return false;
        }
    }


}
