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
 * created by jbischoff, 12.10.2018
 */

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.network.filter.NetworkFilterManager;
import org.matsim.core.network.filter.NetworkLinkFilter;
import org.matsim.core.network.io.MatsimNetworkReader;
import org.matsim.core.utils.geometry.CoordinateTransformation;
import org.matsim.core.utils.geometry.transformations.TransformationFactory;
import org.matsim.vsp.ev.EvUnitConversions;
import org.matsim.vsp.ev.data.Charger;
import org.matsim.vsp.ev.data.ChargerImpl;
import org.matsim.vsp.ev.data.file.ChargerWriter;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GenerateChargersFromGasStations {

    public static void main(String[] args) throws IOException, ParseException {
        String folder = "D:/ers/ev-test/";

        CoordinateTransformation ct = TransformationFactory.getCoordinateTransformation(TransformationFactory.WGS84, "EPSG:3006");
        Network network = NetworkUtils.createNetwork();
        new MatsimNetworkReader(network).readFile(folder + "network-osm.xml.gz");
        NetworkFilterManager nfm = new NetworkFilterManager(network);
        nfm.addLinkFilter(new NetworkLinkFilter() {
            @Override
            public boolean judgeLink(Link l) {
                if (l.getAllowedModes().contains(TransportMode.car)) return true;
                else return false;
            }
        });
        Network filteredNet = nfm.applyFilters();
        BufferedReader in = new BufferedReader(new FileReader(folder + "gas-stations-sweden.json"));
        List<Charger> chargers = new ArrayList<>();


        JSONParser jp = new JSONParser();

        JSONObject jsonObject = (JSONObject) jp.parse(in);
        JSONArray elements = ((JSONArray) (jsonObject.get("elements")));
        for (Object o : elements) {
            JSONObject jo = (JSONObject) o;
            double y = Double.parseDouble(jo.get("lat").toString());
            double x = Double.parseDouble(jo.get("lon").toString());
            Coord c = ct.transform(new Coord(x, y));
            Link l = NetworkUtils.getNearestLink(filteredNet, c);
            Charger fastCharger = new ChargerImpl(Id.create(l.getId().toString() + "fast", Charger.class), 50 * EvUnitConversions.W_PER_kW, 10, l, c, "fast");
            chargers.add(fastCharger);
            Charger truckCharger = new ChargerImpl(Id.create(l.getId().toString() + "truck", Charger.class), 200 * EvUnitConversions.W_PER_kW, 2, l, c, "truck");
            chargers.add(truckCharger);
        }
        new ChargerWriter(chargers).write(folder + "chargers_gasstations.xml");

    }


}
