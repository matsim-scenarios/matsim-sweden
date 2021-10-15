/* *********************************************************************** *
 * project: org.matsim.*
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2018 by the members listed in the COPYING,        *
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

package org.matsim.vsp.ers.scoring;

import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.population.Person;
import org.matsim.core.scoring.ScoringFunction;
import org.matsim.core.scoring.ScoringFunctionFactory;
import org.matsim.core.scoring.SumScoringFunction;
import org.matsim.core.scoring.functions.*;

import javax.inject.Inject;
import javax.inject.Singleton;

public class AgentSpecificASCScoring implements ScoringFunctionFactory {

    private final Scenario scenario;

    @Singleton
    @Inject
    public AgentSpecificASCScoring(Scenario scenario) {
        this.scenario = scenario;
    }

    @Override
    public ScoringFunction createNewScoringFunction(Person person) {
        SumScoringFunction sumScoringFunction = new SumScoringFunction();

        // Score activities, legs, payments and being stuck
        // with the default MATSim scoring based on utility parameters in the config file.
        ScoringParameters.Builder builder = new ScoringParameters.Builder(scenario, person );

        Boolean metropolitanAgent = (Boolean) person.getAttributes().getAttribute("metropolitanRegion");
        if (metropolitanAgent != null) {
            if (metropolitanAgent) {
                double constant = scenario.getConfig().planCalcScore().getModes().get(TransportMode.car).getConstant() * 1.5;
                ModeUtilityParameters car = builder.getModeParameters(TransportMode.car);
                ModeUtilityParameters params = new ModeUtilityParameters(car.marginalUtilityOfTraveling_s,car.marginalUtilityOfDistance_m,car.monetaryDistanceCostRate,constant,constant,car.dailyUtilityConstant);
                builder.setModeParameters(TransportMode.car,params);
            }
        }

        ScoringParameters params = builder.build();
        sumScoringFunction.addScoringFunction(new CharyparNagelActivityScoring(params));
        sumScoringFunction.addScoringFunction(new CharyparNagelLegScoring(params, scenario.getNetwork()));
        sumScoringFunction.addScoringFunction(new CharyparNagelMoneyScoring(params));
        sumScoringFunction.addScoringFunction(new CharyparNagelAgentStuckScoring(params));
        return sumScoringFunction;
    }

}
