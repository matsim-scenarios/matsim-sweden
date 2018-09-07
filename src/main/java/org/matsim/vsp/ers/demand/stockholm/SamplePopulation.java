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

package org.matsim.vsp.ers.demand.stockholm;/*
 * created by jbischoff, 05.09.2018
 */

import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.population.Activity;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Population;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.gbl.MatsimRandom;
import org.matsim.core.population.PopulationUtils;
import org.matsim.core.population.io.PopulationReader;
import org.matsim.core.population.io.PopulationWriter;
import org.matsim.utils.objectattributes.ObjectAttributesXmlWriter;

import java.util.Random;

import static org.matsim.core.config.ConfigUtils.createConfig;
import static org.matsim.core.scenario.ScenarioUtils.createScenario;

public class SamplePopulation {

    public static void main(String[] args) {

        String inputPopulation = "D:/ers/stockholm/100PctAllModes.xml.gz";
        double sampleSize = 0.1;
        String outputPopulation = "D:/ers/stockholm/stockholm_" + sampleSize + ".xml.gz";
        String outputPopulationAttributes = "D:/ers/stockholm/stockholm_" + sampleSize + "_attributes.xml.gz";
        Random random = MatsimRandom.getRandom();
        Config config = createConfig();
        Scenario scenario = createScenario(config);
        new PopulationReader(scenario).readFile(inputPopulation);

        Population population2 = PopulationUtils.createPopulation(ConfigUtils.createConfig());
        for (Person p : scenario.getPopulation().getPersons().values()) {
            if (random.nextDouble() < sampleSize) {
                p.getSelectedPlan().getPlanElements().stream().filter(Activity.class::isInstance).forEach(a -> ((Activity) a).setLinkId(null));
                population2.addPerson(p);

                population2.getPersonAttributes().putAttribute(p.getId().toString(), "subpopulation", "commuters");
            }
        }
        new PopulationWriter(population2).write(outputPopulation);
        new ObjectAttributesXmlWriter(population2.getPersonAttributes()).writeFile(outputPopulationAttributes);
    }
}
