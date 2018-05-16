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

package org.matsim.vsp.ers.demand.freight;/*
 * created by jbischoff, 14.05.2018
 */



/*
 * This class parses the Node Location from the SamGods model
 */

import org.matsim.api.core.v01.Id;
import org.matsim.core.utils.io.tabularFileParser.TabularFileHandler;
import org.matsim.core.utils.io.tabularFileParser.TabularFileParser;
import org.matsim.core.utils.io.tabularFileParser.TabularFileParserConfig;
import org.matsim.vehicles.VehicleType;
import org.matsim.vehicles.VehicleUtils;
import org.matsim.vehicles.VehicleWriterV1;
import org.matsim.vehicles.Vehicles;

public class ParseVehicleTypes {


    private Vehicles vehicles;

    public static void main(String[] args) {
        String inputFile = "C:/Users/Joschka/ownCloud/ers/vehicletypes.csv";
        new ParseVehicleTypes().run(inputFile);
    }

    private void run(String inputFile) {

        TabularFileParserConfig tabularFileParserConfig = new TabularFileParserConfig();
        tabularFileParserConfig.setFileName(inputFile);
        tabularFileParserConfig.setDelimiterTags(new String[]{";"});
        vehicles = VehicleUtils.createVehiclesContainer();

        new TabularFileParser().parse(tabularFileParserConfig, new TabularFileHandler() {
            boolean headerRead = false;

            @Override
            public void startRow(String[] row) {
                if (headerRead) {
                    VehicleType vehicleType = vehicles.getFactory().createVehicleType(Id.create(row[0], VehicleType.class));
                    vehicleType.setDescription(row[2] + "_" + row[1]);
                    vehicles.addVehicleType(vehicleType);
                } else {
                    headerRead = true;
                }
            }
        });

        new VehicleWriterV1(vehicles).writeFile(inputFile + "vehicles.xml");
    }

    public Vehicles getVehicles() {
        return vehicles;
    }
}


