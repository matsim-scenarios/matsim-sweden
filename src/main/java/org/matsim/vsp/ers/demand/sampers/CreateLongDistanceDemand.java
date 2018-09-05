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

package org.matsim.vsp.ers.demand.sampers;/*
 * created by jbischoff, 05.07.2018
 */


import com.vividsolutions.jts.geom.Geometry;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.population.*;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.population.PopulationUtils;
import org.matsim.core.utils.geometry.CoordUtils;
import org.matsim.vsp.ers.demand.commuters.CreateCommuterDemand;
import playground.vsp.corineLandcover.CorineLandCoverData;
import playground.vsp.corineLandcover.LandCoverUtils;

import java.util.Map;
import java.util.Random;

public class CreateLongDistanceDemand {
    private Map<Id<ReadSampersMatrices.PassengerFlow>, ReadSampersMatrices.PassengerFlow> flowMap;
    private CorineLandCoverData corineLandCoverData;

    final String zonesFile = "D:/ers/Sampers/Resultat/zoneconversion.txt";
    final String matricesFolder = "D:\\ers\\Sampers\\Resultat\\relevant_matrices\\";
    final String clcFile = "D:/ers/clc/landcover_rel_se.shp";
    private Map<String, Geometry> communities;
    private Population population;
    double sampleSize = 0.07;
    double distanceCar = 0;
    private int agentCount = 0;


    public static final String COMMUNITYSHAPE = "D:/ers/commuters/Kommun_Sweref99TM_region.shp";
    private Random random = new Random(42);

    final double max_sameday_return_threshold_m = 250000;

    public static void main(String[] args) {

        new CreateLongDistanceDemand().run();

    }

    public void run() {
        ReadSampersMatrices readSampersMatrices = new ReadSampersMatrices();
        readSampersMatrices.run(zonesFile, matricesFolder);
        this.flowMap = readSampersMatrices.getFlowMap();
        communities = CreateCommuterDemand.readShapeFileAndExtractGeometry(COMMUNITYSHAPE, "KnKod");
        corineLandCoverData = new CorineLandCoverData(clcFile);
        this.population = PopulationUtils.createPopulation(ConfigUtils.createConfig());
        createDemand();
        new PopulationWriter(population).write("D:/ers/Sampers/Resultat/sampers_trips_" + sampleSize + ".xml.gz");
        System.out.print(distanceCar / 1000);
    }

    private void createDemand() {

        for (ReadSampersMatrices.PassengerFlow flow : flowMap.values()) {

            Geometry fromGeo = communities.get(flow.fromZone);
            if (fromGeo == null) {
                throw new RuntimeException(flow.fromZone + " - community not found in Shape.");
            }
            Geometry toGeo = communities.get(flow.toZone);
            if (toGeo == null) {
                throw new RuntimeException(flow.toZone + " - community not found in Shape.");
            }
            int carTripsPrivate = (int) flow.carTripsPrivate;
            if (random.nextDouble() < (flow.carTripsPrivate - carTripsPrivate)) carTripsPrivate++;

            int carTripsBusiness = (int) flow.carTripsBusiness;
            if (random.nextDouble() < (flow.carTripsBusiness - carTripsBusiness)) carTripsBusiness++;

            double ptTripsPrivate = flow.busTripsPrivate + flow.trainTripsPrivate;
            if (random.nextDouble() < (ptTripsPrivate - Math.floor(ptTripsPrivate))) ptTripsPrivate++;
            ptTripsPrivate = Math.floor(ptTripsPrivate);

            double ptTripsBusiness = flow.busTripsBusiness + flow.trainTripsBusiness;
            if (random.nextDouble() < (ptTripsBusiness - Math.floor(ptTripsBusiness))) ptTripsBusiness++;
            ptTripsBusiness = Math.floor(ptTripsBusiness);
            generateAgents(flow.fromZone, fromGeo, flow.toZone, toGeo, carTripsPrivate, "private", TransportMode.car);
            generateAgents(flow.fromZone, fromGeo, flow.toZone, toGeo, carTripsBusiness, "business", TransportMode.car);
            generateAgents(flow.fromZone, fromGeo, flow.toZone, toGeo, ptTripsBusiness, "business", TransportMode.pt);
            generateAgents(flow.fromZone, fromGeo, flow.toZone, toGeo, ptTripsPrivate, "private", TransportMode.pt);


        }

    }

    private void generateAgents(String fromZone, Geometry fromGeo, String toZone, Geometry toGeo, double numberofTrips, String activityType, String mode) {
        PopulationFactory f = population.getFactory();

        for (int i = 0; i < numberofTrips * sampleSize; i++) {
            Person person = f.createPerson(Id.createPersonId("ldt_" + fromZone + "_" + toZone + "_" + agentCount));
            person.getAttributes().putAttribute("homeZone", fromZone);
            person.getAttributes().putAttribute("destinationZone", toZone);
            agentCount++;
            population.addPerson(person);
            Plan plan = f.createPlan();
            person.addPlan(plan);

            Coord homeCoord = corineLandCoverData.getRandomCoord(fromGeo, LandCoverUtils.LandCoverActivityType.home);
            Coord toCoord = corineLandCoverData.getRandomCoord(toGeo, LandCoverUtils.LandCoverActivityType.other);
//            Coord homeCoord = MGC.point2Coord(fromGeo.getCentroid());
//            Coord toCoord = MGC.point2Coord(toGeo.getCentroid());
            Activity h1 = f.createActivityFromCoord("home", homeCoord);
            h1.setEndTime(5.5 * 3600 + random.nextInt(10800));
            plan.addActivity(h1);
            Leg l1 = f.createLeg(mode);
            plan.addLeg(l1);
            Activity w = f.createActivityFromCoord(activityType, toCoord);
            plan.addActivity(w);

            double distance = CoordUtils.calcEuclideanDistance(homeCoord, toCoord) * 1.3;
            if (mode.equals(TransportMode.car)) distanceCar += 2 * distance;
            double dayReturnProbability = getDayReturnProbability(distance);

            if (random.nextDouble() < dayReturnProbability) {
                w.setType(activityType + "_sameday");
                w.setEndTime(h1.getEndTime() + 6 * 3600 + random.nextInt(10800));
                Leg l2 = f.createLeg(mode);
                plan.addLeg(l2);
                Activity h2 = f.createActivityFromCoord("home", homeCoord);
                plan.addActivity(h2);

            }
//            else {
//                Person bperson = f.createPerson(Id.createPersonId(fromZone + "_" + toZone + "b_" + agentCount));
//                bperson.getAttributes().putAttribute("homeZone", toZone);
//                bperson.getAttributes().putAttribute("destinationZone", fromZone);
//                agentCount++;
//                population.addPerson(bperson);
//                Plan bplan = f.createPlan();
//                bperson.addPlan(bplan);
//                Activity bh1 = f.createActivityFromCoord("home", toCoord);
//                bh1.setEndTime(5.5 * 3600 + random.nextInt(10800));
//                bplan.addActivity(bh1);
//                Leg bl1 = f.createLeg(mode);
//                bplan.addLeg(bl1);
//                Activity bw = f.createActivityFromCoord(activityType, homeCoord);
//                bplan.addActivity(bw);
//            }

        }


    }

    private double getDayReturnProbability(double distance) {
        double p = 1.0 - Math.sqrt(distance / max_sameday_return_threshold_m);
        return Math.max(p, 0.0);
    }
}
