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

package org.matsim.vsp.ers;/*
 * created by jbischoff, 06.06.2018
 */

import org.matsim.api.core.v01.Scenario;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.controler.AbstractModule;
import org.matsim.core.controler.Controler;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.pt.transitSchedule.api.TransitSchedule;
import org.matsim.vsp.ers.ptrouting.SwissRailRaptorModuleAsTeleportedMode;
import org.matsim.vsp.ers.scoring.AgentSpecificASCScoring;

public class RunSweden {

    public static void main(String[] args) {
        String configFile = "D:\\ers\\config_0.01.xml";
        if (args.length > 0) {
            configFile = args[0];
        }
        Config config = ConfigUtils.loadConfig(configFile);
        config.transit().setUseTransit(false);
        Scenario scenario = ScenarioUtils.loadScenario(config);

        Controler controler = new Controler(scenario);
        controler.addOverridingModule(new AbstractModule() {
            @Override
            public void install() {
                bindScoringFunctionFactory().to(AgentSpecificASCScoring.class);
                bind(TransitSchedule.class).toInstance(scenario.getTransitSchedule());
            }
        });
        controler.addOverridingModule(new SwissRailRaptorModuleAsTeleportedMode());

        controler.run();
    }

}
