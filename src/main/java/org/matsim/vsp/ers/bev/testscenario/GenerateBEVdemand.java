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

package org.matsim.vsp.ers.bev.testscenario;/*
 * created by jbischoff, 15.10.2018
 */

import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.population.Leg;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Plan;
import org.matsim.core.config.Config;
import org.matsim.core.population.PersonUtils;
import org.matsim.core.population.algorithms.PersonAlgorithm;
import org.matsim.core.population.io.StreamingPopulationReader;
import org.matsim.core.population.io.StreamingPopulationWriter;

import java.util.Arrays;
import java.util.List;

import static org.matsim.core.config.ConfigUtils.createConfig;
import static org.matsim.core.scenario.ScenarioUtils.createScenario;

public class GenerateBEVdemand {

    public static void main(String[] args) {
        Config config = createConfig();

        Scenario scenario = createScenario(config);
        StreamingPopulationReader spr = new StreamingPopulationReader(scenario);
        StreamingPopulationWriter spw = new StreamingPopulationWriter();
        List<String> relevantModes = Arrays.asList(new String[]{TransportMode.car, TransportMode.truck});
        spw.startStreaming("D:/ers/ev-test/13.01-noroutes.xml.gz");
        spr.addAlgorithm(new PersonAlgorithm() {
            @Override
            public void run(Person person) {
                PersonUtils.removeUnselectedPlans(person);
                Plan plan = person.getSelectedPlan();
                plan.getPlanElements().stream().filter(Leg.class::isInstance).filter(l -> relevantModes.contains(((Leg) l).getMode())).forEach(leg -> ((Leg) leg).setRoute(null));
            }
        });
        spr.addAlgorithm(spw);
        spr.readFile("D:/runs-svn/ers_sweden/basecase/se_13.0.1/se_13.0.1.output_plans.xml.gz");
        spw.closeStreaming();
    }
}
