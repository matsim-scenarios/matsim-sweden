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

import com.google.inject.Inject;
import com.google.inject.Provider;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.population.Leg;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Plan;
import org.matsim.api.core.v01.population.Population;
import org.matsim.vsp.ev.EvUnitConversions;
import org.matsim.vsp.ev.data.*;

import java.util.Collections;
import java.util.List;


public class VehiclesAsEVFleet implements Provider<ElectricFleet> {

    @Inject
    Population population;

    private ElectricFleetImpl electricFleet;

    private String truckType = "truck";
    private double truckCapacity = 400 * EvUnitConversions.J_PER_kWh;
    private List<String> truckChargers = Collections.singletonList("truck");

    private String carType = "smallCar";
    private double carCapacity = 60 * EvUnitConversions.J_PER_kWh;
    private List<String> carChargers = Collections.singletonList("fast");

    @Override
    public ElectricFleet get() {
        electricFleet = new ElectricFleetImpl();
        for (Person person : population.getPersons().values()) {
            Plan plan = person.getSelectedPlan();
            plan.getPlanElements().stream().filter(Leg.class::isInstance).forEach(pl -> {
                if (((Leg) pl).getMode().equals(TransportMode.car))
                    generateCar(person.getId());
                else if (((Leg) pl).getMode().equals(TransportMode.truck))
                    generateTruck(person.getId());
            });
        }

        return electricFleet;
    }

    private void generateTruck(Id<Person> id) {
        generateAndAddVehicle(id, TransportMode.truck, truckCapacity, truckCapacity, truckType, truckChargers);
    }

    private void generateCar(Id<Person> id) {
        generateAndAddVehicle(id, TransportMode.car, carCapacity, carCapacity, carType, carChargers);
    }

    private void generateAndAddVehicle(Id<Person> id, String mode, double batteryCapa, double soc, String vehicleType, List<String> chargerTypes) {
        Id<ElectricVehicle> evId = mode.equals(TransportMode.car) ? Id.create(id, ElectricVehicle.class) : Id.create(id.toString() + "_" + mode, ElectricVehicle.class);
        if (!electricFleet.getElectricVehicles().containsKey(evId)) {
            ElectricVehicle ev = new ElectricVehicleImpl(evId, new BatteryImpl(batteryCapa, soc), chargerTypes, vehicleType);
            electricFleet.addElectricVehicle(ev);
        }
    }


}
