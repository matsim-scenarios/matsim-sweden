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

package org.matsim.vsp.ers.consumption;/*
 * created by jbischoff, 23.08.2018
 */

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.Node;
import org.matsim.core.network.NetworkUtils;
import org.matsim.vehicles.VehicleType;
import org.matsim.vsp.ev.data.file.LTHConsumptionModelReader;
import org.matsim.vsp.ev.discharging.LTHDriveEnergyConsumption;

public class TestConsumption {
    public static void main(String[] args) {
        String filename = "D:/ers/energyconsumption/CityCarMap.csv";
        LTHDriveEnergyConsumption c = new LTHConsumptionModelReader(Id.create("a", VehicleType.class)).readFile(filename);
        Network n = NetworkUtils.createNetwork();
        Node n1 = NetworkUtils.createNode(Id.createNodeId("1"));
        Node n2 = NetworkUtils.createNode(Id.createNodeId("2"));
        Link l = NetworkUtils.createLink(Id.createLinkId("b"), n1, n2, n, 1000, 0.3, 300, 2);
        l.getAttributes().putAttribute("slopes", new double[]{0.05});
        System.out.println(c.calcEnergyConsumption(l, 33.33333333333));
    }
}
