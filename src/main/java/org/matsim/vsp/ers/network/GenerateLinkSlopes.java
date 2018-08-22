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

package org.matsim.vsp.ers.network;/*
 * created by jbischoff, 21.08.2018
 */

import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.network.io.MatsimNetworkReader;
import org.matsim.core.network.io.NetworkWriter;

public class GenerateLinkSlopes {

    public static void main(String[] args) {
        Network network = NetworkUtils.createNetwork();
        new MatsimNetworkReader(network).readFile("D:/ers/network/Base2012_network_car_cleaned.xml");

        for (Link l : network.getLinks().values()) {
            l.getAttributes().putAttribute("slope", new double[]{1.0, 0.1, 0.0});

        }
        new NetworkWriter(network).write("D:/ers/network/testnet.xml");
    }

}
