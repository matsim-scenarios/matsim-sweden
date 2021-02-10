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
 * created by jbischoff, 28.08.2018
 */

import org.locationtech.jts.geom.Geometry;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.population.*;
import playground.vsp.corineLandcover.CorineLandCoverData;
import playground.vsp.corineLandcover.LandCoverUtils;
import playground.vsp.openberlinscenario.cemdap.input.CommuterRelationV2;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;


public class ParallelDemandGenerator


{
    private Map<String, Geometry> communities;
    private PopulationFactory factory;
    private CorineLandCoverData corineLandCoverData;

    private List<String> metropolitanRegions;
    private int counterOffset;
    private Random random;
    private double fraction;

    public ParallelDemandGenerator(Map<String, Geometry> communities, CorineLandCoverData corineLandCoverData, List<String> metropolitanRegions, Random random, double fraction, PopulationFactory factory) {

        this.communities = communities;
        this.corineLandCoverData = corineLandCoverData;
        this.factory = factory;
        this.metropolitanRegions = metropolitanRegions;
        this.counterOffset = 0;
        this.random = random;
        this.fraction = fraction;
    }


    public List<Person> generatePersons(CommuterRelationV2 commuterRelation) {
        List<Person> personList = new ArrayList<>();
        int agentCounter = counterOffset;

        PopulationFactory f = factory;
        Geometry fromGeo = communities.get(commuterRelation.getFrom());
        if (fromGeo == null) {
            throw new RuntimeException(commuterRelation.getFrom() + " - community not found in Shape.");
        }
        Geometry toGeo = communities.get(commuterRelation.getTo());
        if (toGeo == null) {
            throw new RuntimeException(commuterRelation.getTo() + " - community not found in Shape.");
        }

        for (int i = 0; i < commuterRelation.getTrips() * fraction; i++) {

            boolean metropolitanRegion = (metropolitanRegions.contains(commuterRelation.getFrom()) && metropolitanRegions.contains(commuterRelation.getTo()));

            Person person = f.createPerson(Id.createPersonId(commuterRelation.getFrom() + "_" + commuterRelation.getTo() + "_" + agentCounter));
            person.getAttributes().putAttribute("metropolitanRegion", metropolitanRegion);
            person.getAttributes().putAttribute("homeZone", commuterRelation.getFrom());
            person.getAttributes().putAttribute("workZone", commuterRelation.getTo());
            agentCounter++;
            personList.add(person);
            Plan plan = f.createPlan();
            person.addPlan(plan);
            Coord homeCoord = corineLandCoverData.getRandomCoord(fromGeo, LandCoverUtils.LandCoverActivityType.home);
            Coord workCoord = corineLandCoverData.getRandomCoord(toGeo, LandCoverUtils.LandCoverActivityType.other);
            Activity h1 = f.createActivityFromCoord("home", homeCoord);
            h1.setEndTime(6.5 * 3600 + random.nextInt(9000));
            plan.addActivity(h1);
            Leg l1 = f.createLeg(TransportMode.car);
            plan.addLeg(l1);
            Activity w = f.createActivityFromCoord("work", workCoord);
            w.setEndTime(h1.getEndTime().seconds() + 5 * 3600 + random.nextInt(10800));
            plan.addActivity(w);
            Leg l2 = f.createLeg(TransportMode.car);
            plan.addLeg(l2);

            Activity h2 = f.createActivityFromCoord("home", homeCoord);
            plan.addActivity(h2);

        }
        return personList;
    }
}
