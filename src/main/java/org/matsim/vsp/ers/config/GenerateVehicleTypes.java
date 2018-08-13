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
 * created by jbischoff, 17.07.2018
 */

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.core.config.Config;
import org.matsim.vehicles.VehicleType;
import org.matsim.vehicles.VehicleWriterV1;

import static org.matsim.core.config.ConfigUtils.createConfig;
import static org.matsim.core.scenario.ScenarioUtils.createScenario;

public class GenerateVehicleTypes {
    public static void main(String[] args) {
        Config config = createConfig();
        Scenario scenario = createScenario(config);
        VehicleType car = scenario.getVehicles().getFactory().createVehicleType(Id.create(TransportMode.car, VehicleType.class));
        car.setDescription("passenger Car");
        car.setMaximumVelocity(120 / 3.6);
        scenario.getVehicles().addVehicleType(car);

        VehicleType truck = scenario.getVehicles().getFactory().createVehicleType(Id.create(TransportMode.truck, VehicleType.class));
        truck.setMaximumVelocity(85 / 3.6);
        truck.setLength(18);
        truck.setPcuEquivalents(3);
        scenario.getVehicles().addVehicleType(truck);

        new VehicleWriterV1(scenario.getVehicles()).writeFile("D:/ers/scenario/vehicleTypes.xml");
    }
}
