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
 * created by jbischoff, 15.11.2018
 */

import org.matsim.core.config.ConfigUtils;
import org.matsim.core.population.io.StreamingPopulationReader;
import org.matsim.core.population.io.StreamingPopulationWriter;
import org.matsim.core.scenario.ScenarioUtils;

public class DownscaleDemand {

    public static void main(String[] args) {
        String inputDemand = "C:/Users/Joschka/git/matsim-sweden/scenario/se_14.0.1/input/merged_population.xml.gz";

        String outputDemand = "C:/Users/Joschka/git/matsim-sweden/scenario/se_14.0.1/input/merged_population_0.01.xml.gz";
        double sample = 0.1;
        StreamingPopulationWriter spw = new StreamingPopulationWriter(sample);
        StreamingPopulationReader spr = new StreamingPopulationReader(ScenarioUtils.createScenario(ConfigUtils.createConfig()));
        spw.startStreaming(outputDemand);
        spr.addAlgorithm(spw);
        spr.readFile(inputDemand);
        spw.closeStreaming();

    }
}
