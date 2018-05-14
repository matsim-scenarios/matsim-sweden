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

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.population.Activity;
import org.matsim.core.utils.io.tabularFileParser.TabularFileHandler;
import org.matsim.core.utils.io.tabularFileParser.TabularFileParser;
import org.matsim.core.utils.io.tabularFileParser.TabularFileParserConfig;
import org.matsim.facilities.ActivityFacility;

import java.util.HashMap;
import java.util.Map;

public class ParseNodeLocations {


    String inputFile ="C:/Users/Joschka/ownCloud/ers/nodes.csv";
    Map<Id<ActivityFacility>,SamGodsNode> nodes = new HashMap<>();

    public static void main(String[] args) {
        new ParseNodeLocations().run();
    }

    private void run() {
        TabularFileParserConfig tabularFileParserConfig = new TabularFileParserConfig();
        tabularFileParserConfig.setFileName(inputFile);
        tabularFileParserConfig.setDelimiterTags(new String[]{";"});

        new TabularFileParser().parse(tabularFileParserConfig, new TabularFileHandler() {
            boolean headerRead =false;
            @Override
            public void startRow(String[] row) {
                if (headerRead){
                    Coord coord = new Coord(Double.parseDouble(row[20]),Double.parseDouble(row[21]));
                    Id<ActivityFacility> activityFacilityId = Id.create(row[3],ActivityFacility.class);
                    String name = row[4];
                    String zone = row[3].substring(0,4)+"00";
                    SamGodsNode node = new SamGodsNode(coord,activityFacilityId,name,zone);
                    System.out.println(node);
                }

                else {
                    headerRead =true;
                }
            }
        });
    }


    class SamGodsNode{

        public SamGodsNode(Coord coord, Id<ActivityFacility> facilityId, String name, String zoneId) {
            this.coord = coord;
            this.facilityId = facilityId;
            this.name = name;
            this.zoneId = zoneId;
        }

        Coord coord;
        Id<ActivityFacility> facilityId;
        String name;
        String zoneId;

        @Override
        public String toString() {
            return "SamGodsNode{" +
                    "coord=" + coord +
                    ", facilityId=" + facilityId +
                    ", name='" + name + '\'' +
                    ", zoneId='" + zoneId + '\'' +
                    '}';
        }
    }

}
