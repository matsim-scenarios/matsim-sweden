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
import org.matsim.facilities.ActivityFacility;
import org.matsim.vehicles.VehicleType;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ParseODs {

    private Set<GoodsFlow> goodsFlows = new HashSet<>();

    public static void main(String[] args) {
        String inputFolder = "C:/Users/Joschka/ownCloud/ers/Loaded/";
        new ParseODs().run(inputFolder, true);
        String inputFolderEmpty = "C:/Users/Joschka/ownCloud/ers/Empty/";
        new ParseODs().run(inputFolderEmpty, false);
    }

    void run(String inputFolder, final boolean loaded) {
        List<String> files = listFilesForFolder(new File(inputFolder));
        files.forEach(f -> runFile(f, loaded));
        goodsFlows.forEach(goodsFlow -> System.out.println(goodsFlow));
    }


    private void runFile(String inputFile, final boolean loaded) {
        TabularFileParserConfig tabularFileParserConfig = new TabularFileParserConfig();
        tabularFileParserConfig.setFileName(inputFile);
        tabularFileParserConfig.setDelimiterTags(new String[]{":", " "});
        String target = (loaded ? "Vhcl" : "Emp");
        String vehicleType = inputFile.split("_")[1].replace(target, "");
        System.out.println(vehicleType);
        Id<VehicleType> vehicleTypeId = Id.create(vehicleType, VehicleType.class);

        new TabularFileParser().parse(tabularFileParserConfig, new TabularFileHandler() {
            @Override
            public void startRow(String[] row) {
                Id<ActivityFacility> fromId = Id.create(row[0], ActivityFacility.class);
                Id<ActivityFacility> toId = Id.create(row[1], ActivityFacility.class);
                double annualFlow = Double.parseDouble(row[2]);
                GoodsFlow goodsFlow = new GoodsFlow(fromId, toId, annualFlow, loaded, vehicleTypeId);
                goodsFlows.add(goodsFlow);
            }
        });
    }

    public Set<GoodsFlow> getGoodsFlows() {
        return goodsFlows;
    }

    private List<String> listFilesForFolder(final File folder) {
        List<String> files = new ArrayList<>();
        for (final File fileEntry : folder.listFiles()) {
            if (fileEntry.isDirectory()) {
                listFilesForFolder(fileEntry);
            } else {
                if (!fileEntry.getName().endsWith(".314"))
                    continue;
                System.out.println(fileEntry.getName());
                files.add(fileEntry.getPath());
            }
        }
        return files;

    }

    class GoodsFlow {
        Id<ActivityFacility> fromId;
        Id<ActivityFacility> toId;
        double annualFlow;
        boolean loaded;
        Id<VehicleType> vehicleTypeId;
        double dailyTrips = 0.;

        public GoodsFlow(Id<ActivityFacility> fromId, Id<ActivityFacility> toId, double annualFlow, boolean loaded, Id<VehicleType> vehicleTypeId) {
            this.fromId = fromId;
            this.toId = toId;
            this.annualFlow = annualFlow;
            this.loaded = loaded;
            this.vehicleTypeId = vehicleTypeId;
        }

        @Override
        public String toString() {
            return "GoodsFlow{" +
                    "fromId=" + fromId +
                    ", toId=" + toId +
                    ", annualFlow=" + annualFlow +
                    ", loaded=" + loaded +
                    ", vehicleTypeId=" + vehicleTypeId +
                    '}';
        }
    }


}
