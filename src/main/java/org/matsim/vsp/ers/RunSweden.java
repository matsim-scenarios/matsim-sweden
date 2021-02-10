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

        String configFile = "scenario/se_14.0.01/input/config_0.01.xml";


        //for running on  a server
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
        // yyyy I am not sure if this is a good way of doing it (not using the default execution path, thus always running the risk that it behaves
        // differently from the default version).  I also think there is (now?) a switch to have pt for routing only, and to teleport it in the
        // mobsim.  kai, feb'21

        controler.run();
    }

}
