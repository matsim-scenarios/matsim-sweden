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

package org.matsim.vsp.ers.config;/*
 * created by jbischoff, 06.06.2018
 */

import org.matsim.api.core.v01.TransportMode;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.config.ConfigWriter;
import org.matsim.core.config.groups.*;
import org.matsim.core.controler.OutputDirectoryHierarchy;
import org.matsim.core.replanning.strategies.DefaultPlanStrategiesModule;

public class GenerateSwedenConfig {

    private static double storageCap = 0.05;
    private static double flowCap = 0.01;

    public static void main(String[] args) {

        Config config = ConfigUtils.createConfig();

        //network
        config.network().setInputFile("network/network-pt.xml.gz");

        config.transit().setTransitScheduleFile("transit/transitSchedule.xml.gz");
        config.transit().setUseTransit(true);
        config.transit().setVehiclesFile("transit/transitVehicles.xml.gz");

        ControlerConfigGroup ccg = config.controler();
        ccg.setRunId("se_05" + "." + flowCap);
        ccg.setOutputDirectory("output/" + ccg.getRunId() + "/");
        ccg.setFirstIteration(0);
        int lastIteration = 200;
        ccg.setLastIteration(lastIteration);
        ccg.setMobsim("qsim");
        ccg.setOverwriteFileSetting(OutputDirectoryHierarchy.OverwriteFileSetting.overwriteExistingFiles);
        ccg.setWriteEventsInterval(100);
        ccg.setWritePlansInterval(100);
        config.global().setNumberOfThreads(8);

        QSimConfigGroup qsc = config.qsim();
        qsc.setUsingFastCapacityUpdate(true);
        qsc.setTrafficDynamics(QSimConfigGroup.TrafficDynamics.withHoles);
        qsc.setLinkDynamics(QSimConfigGroup.LinkDynamics.PassingQ);

        qsc.setNumberOfThreads(6);
        qsc.setStorageCapFactor(storageCap);
        qsc.setFlowCapFactor(flowCap);
        qsc.setEndTime(30 * 3600);

        config.parallelEventHandling().setNumberOfThreads(6);


        config.plans().setInputFile("commuters/commuter_population_0.01.xml.gz");

        config.planCalcScore().setMarginalUtilityOfMoney(0.1); //10 SEK  --- 1 EUR

        PlanCalcScoreConfigGroup.ActivityParams work = new PlanCalcScoreConfigGroup.ActivityParams();
        work.setActivityType("work");
        work.setOpeningTime(7.5 * 3600);
        work.setClosingTime(17.5 * 3600);
        work.setTypicalDuration(7 * 3600);
        config.planCalcScore().addActivityParams(work);

        PlanCalcScoreConfigGroup.ActivityParams home = new PlanCalcScoreConfigGroup.ActivityParams();
        home.setActivityType("home");
        home.setTypicalDuration(14 * 3600);
        config.planCalcScore().addActivityParams(home);

        PlanCalcScoreConfigGroup.ModeParams car = config.planCalcScore().getModes().get(TransportMode.car);
        car.setMonetaryDistanceRate(-0.0001);
        car.setMarginalUtilityOfTraveling(-3);
        car.setConstant(-3);

        PlanCalcScoreConfigGroup.ModeParams pt = config.planCalcScore().getModes().get(TransportMode.pt);
        pt.setMarginalUtilityOfTraveling(-1.75);
        pt.setConstant(-2);

        PlanCalcScoreConfigGroup.ModeParams walk = config.planCalcScore().getModes().get(TransportMode.walk);
        walk.setMarginalUtilityOfTraveling(-1);

        PlanCalcScoreConfigGroup.ModeParams bike = config.planCalcScore().getModes().get(TransportMode.bike);
        bike.setMarginalUtilityOfTraveling(-3);
        bike.setConstant(-2);

        PlansCalcRouteConfigGroup.ModeRoutingParams bikeP = config.plansCalcRoute().getOrCreateModeRoutingParams(TransportMode.bike);
        bikeP.setBeelineDistanceFactor(1.3);
        bikeP.setTeleportedModeSpeed(3.333);

        PlansCalcRouteConfigGroup.ModeRoutingParams walkP = config.plansCalcRoute().getOrCreateModeRoutingParams(TransportMode.walk);
        walkP.setBeelineDistanceFactor(1.3);
        walkP.setTeleportedModeSpeed(1.2);

        StrategyConfigGroup.StrategySettings subtour = new StrategyConfigGroup.StrategySettings();
        subtour.setStrategyName(DefaultPlanStrategiesModule.DefaultStrategy.SubtourModeChoice.toString());
        subtour.setWeight(0.1);
        config.strategy().addStrategySettings(subtour);

        StrategyConfigGroup.StrategySettings reroute = new StrategyConfigGroup.StrategySettings();
        reroute.setStrategyName(DefaultPlanStrategiesModule.DefaultStrategy.ReRoute.toString());
        reroute.setWeight(0.1);
        config.strategy().addStrategySettings(reroute);

        StrategyConfigGroup.StrategySettings timeAllocation = new StrategyConfigGroup.StrategySettings();
        timeAllocation.setStrategyName(DefaultPlanStrategiesModule.DefaultStrategy.TimeAllocationMutator.toString());
        timeAllocation.setWeight(0.1);
        config.strategy().addStrategySettings(timeAllocation);

        StrategyConfigGroup.StrategySettings timeAllocationReroute = new StrategyConfigGroup.StrategySettings();
        timeAllocationReroute.setStrategyName(DefaultPlanStrategiesModule.DefaultStrategy.TimeAllocationMutator_ReRoute.toString());
        timeAllocationReroute.setWeight(0.1);
        config.strategy().addStrategySettings(timeAllocationReroute);

        StrategyConfigGroup.StrategySettings changeExpBeta = new StrategyConfigGroup.StrategySettings();
        changeExpBeta.setStrategyName(DefaultPlanStrategiesModule.DefaultSelector.ChangeExpBeta.toString());
        changeExpBeta.setWeight(0.6);
        config.strategy().addStrategySettings(changeExpBeta);


        config.strategy().setFractionOfIterationsToDisableInnovation(.8);

        config.strategy().setMaxAgentPlanMemorySize(5);
        config.timeAllocationMutator().setMutationRange(3600);


        config.subtourModeChoice().setConsiderCarAvailability(true);
        config.subtourModeChoice().setModes(new String[]{"car", "bike", "walk", "pt"});

        new ConfigWriter(config).write("D:/ers/config_" + flowCap + ".xml");


    }

}
