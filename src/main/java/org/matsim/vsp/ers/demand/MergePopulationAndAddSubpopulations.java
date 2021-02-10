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

package org.matsim.vsp.ers.demand;/*
 * created by jbischoff, 17.07.2018
 */

import org.matsim.api.core.v01.Scenario;
import org.matsim.core.config.Config;
import org.matsim.core.gbl.MatsimRandom;
import org.matsim.core.population.PopulationUtils;
import org.matsim.core.population.io.PopulationReader;
import org.matsim.core.population.io.PopulationWriter;
import org.matsim.utils.objectattributes.ObjectAttributesXmlWriter;

import java.util.Random;

import static org.matsim.core.config.ConfigUtils.createConfig;
import static org.matsim.core.scenario.ScenarioUtils.createScenario;

public class MergePopulationAndAddSubpopulations {

    private static final String commuterDemand = "D:/ers/commuters/commuter_population_0.1.xml.gz";
    private static final String freightDemand = "D:/ers/samgods/samgoodspopulation0.1.xml";
    private static final String sampersDemand = "D:/ers/Sampers/Resultat/sampers_trips_0.07.xml.gz";
    private static final String stockholmPop = "D:/ers/stockholm/stockholm_0.1.xml.gz";

    private static final String outputPopulation = "D:/ers/scenario/merged_population.xml.gz";
    private static final String outputPopulationAttributes = "D:/ers/scenario/merged_population_attributes.xml.gz";

    public static final String COMMUTERS = "commuters";

    public static void main(String[] args) {
        Config config = createConfig();
        final Scenario scenario = createScenario(config);
        Random random = MatsimRandom.getRandom();
        Scenario cscenario = createScenario(config);
        Scenario fscenario = createScenario(config);
        Scenario sscenario = createScenario(config);
        Scenario stscenario = createScenario(config);

        new PopulationReader(cscenario).readFile(commuterDemand);
        new PopulationReader(fscenario).readFile(freightDemand);
        new PopulationReader(sscenario).readFile(sampersDemand);
        new PopulationReader(stscenario).readFile(stockholmPop);

        cscenario.getPopulation().getPersons().values().forEach(p ->
        {
            if (random.nextDouble() < 0.5) {
                scenario.getPopulation().addPerson(p);
//                scenario.getPopulation().getPersonAttributes().putAttribute(p.getId().toString(), "subpopulation", "commuters");
                PopulationUtils.putSubpopulation( p, COMMUTERS );
            }
        });
        stscenario.getPopulation().getPersons().values().forEach(p ->
        {
            scenario.getPopulation().addPerson(p);
//            scenario.getPopulation().getPersonAttributes().putAttribute(p.getId().toString(), "subpopulation", "commuters");
            PopulationUtils.putSubpopulation( p, COMMUTERS );
        });
        fscenario.getPopulation().getPersons().values().forEach(p ->
        {
            scenario.getPopulation().addPerson(p);
//            scenario.getPopulation().getPersonAttributes().putAttribute(p.getId().toString(), "subpopulation", "freight");
            PopulationUtils.putSubpopulation( p, COMMUTERS );
        });
        sscenario.getPopulation().getPersons().values().forEach(p ->
        {
            if (random.nextDouble() < 0.8) {
                scenario.getPopulation().addPerson(p);
//                scenario.getPopulation().getPersonAttributes().putAttribute(p.getId().toString(), "subpopulation", "longdistance");
                PopulationUtils.putSubpopulation( p, COMMUTERS );
            }
        });

        new PopulationWriter(scenario.getPopulation()).write(outputPopulation);
//        new ObjectAttributesXmlWriter(scenario.getPopulation().getPersonAttributes()).writeFile(outputPopulationAttributes);
    }
}
