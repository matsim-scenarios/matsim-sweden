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

package org.matsim.vsp.ers.demand.sampers;/*
 * created by jbischoff, 05.07.2018
 */

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.core.utils.io.tabularFileParser.TabularFileHandler;
import org.matsim.core.utils.io.tabularFileParser.TabularFileParser;
import org.matsim.core.utils.io.tabularFileParser.TabularFileParserConfig;

import java.util.HashMap;
import java.util.Map;

public class ReadSampersMatrices {

    Map<Id<PassengerFlow>, PassengerFlow> flowMap = new HashMap<>();
    Map<String, String> zoneConverterMap = new HashMap<>();

    public static void main(String[] args) {
        final String zonesFile = "D:/ers/Sampers/Resultat/zoneconversion.txt";
        final String matricesFolder = "D:\\ers\\Sampers\\Resultat\\relevant_matrices\\";
        new ReadSampersMatrices().run(zonesFile, matricesFolder);
    }


    private void run(String zonesFile, String matricesFolder) {
        readZoneTransformation(zonesFile);
        readSampersFlows(matricesFolder + "nat_bil_pri_amd_2014.txt", TransportMode.car, false);
        readSampersFlows(matricesFolder + "nat_bil_tjn_amd_2014.txt", TransportMode.car, true);
        readSampersFlows(matricesFolder + "nat_buss_tjn_amd_2014.txt", "bus", true);
        readSampersFlows(matricesFolder + "nat_buss_pri_amd_2014.txt", "bus", false);
        readSampersFlows(matricesFolder + "nat_flyg_pri_amd_2014.txt", TransportMode.airplane, true);
        readSampersFlows(matricesFolder + "nat_flyg_pri_amd_2014.txt", TransportMode.airplane, false);
        readSampersFlows(matricesFolder + "nat_tag_pri_amd_2014.txt", "train", false);
        readSampersFlows(matricesFolder + "nat_tag_tjn_amd_2014.txt", "train", true);

//        flowMap.forEach((k,v)->System.out.println(v.toString()));

    }


    private void readSampersFlows(String file, String mode, boolean isBusiness) {
        TabularFileParserConfig tabularFileParserConfig = new TabularFileParserConfig();
        tabularFileParserConfig.setDelimiterTags(new String[]{" ", "\t", ":"});
        tabularFileParserConfig.setFileName(file);
        new TabularFileParser().parse(tabularFileParserConfig, new TabularFileHandler() {
            int rowN = 0;

            @Override
            public void startRow(String[] row) {
                if ((rowN < 4)) {
                    rowN++;
                } else {
                    if (row.length < 3) return;

                    String fromZone = zoneConverterMap.get(row[1]);
                    String toZone1 = zoneConverterMap.get(row[2]);
                    double flow1 = Double.parseDouble(row[3]);
                    addFlow(fromZone, toZone1, flow1);
                    if (row.length > 5) {
                        String toZone2 = zoneConverterMap.get(row[4]);
                        double flow2 = Double.parseDouble(row[5]);
                        addFlow(fromZone, toZone2, flow2);
                    }
                    if (row.length > 7) {
                        String toZone3 = zoneConverterMap.get(row[6]);
                        double flow3 = Double.parseDouble(row[7]);
                        addFlow(fromZone, toZone3, flow3);
                    }


                }
            }

            private void addFlow(String fromZone, String toZone, double flow) {
                Id<PassengerFlow> flowId = generateId(fromZone, toZone);
                PassengerFlow passengerFlow = flowMap.getOrDefault(flowId, new PassengerFlow(flowId, fromZone, toZone));
                if (isBusiness) {
                    if (mode.equals(TransportMode.car)) passengerFlow.carTripsBusiness += flow;
                    if (mode.equals("bus")) passengerFlow.busTripsBusiness += flow;
                    if (mode.equals(TransportMode.train)) passengerFlow.trainTripsBusiness += flow;
                    if (mode.equals(TransportMode.airplane)) passengerFlow.airTripsBusiness += flow;
                } else {
                    if (mode.equals(TransportMode.car)) passengerFlow.carTripsPrivate += flow;
                    if (mode.equals("bus")) passengerFlow.busTripsPrivate += flow;
                    if (mode.equals(TransportMode.train)) passengerFlow.trainTripsPrivate += flow;
                    if (mode.equals(TransportMode.airplane)) passengerFlow.airTripsPrivate += flow;

                }
                flowMap.put(flowId, passengerFlow);
            }
        });
    }

    private Id<PassengerFlow> generateId(String fromZone, String toZone) {
        return Id.create(fromZone + "_" + toZone, PassengerFlow.class);

    }

    private void readZoneTransformation(String zonesFile) {
        TabularFileParserConfig tabularFileParserConfig = new TabularFileParserConfig();
        tabularFileParserConfig.setDelimiterRegex("\t");
        tabularFileParserConfig.setFileName(zonesFile);
        new TabularFileParser().parse(tabularFileParserConfig, row -> {
            String emmeZone = row[1];
            String scbZone = row[0].substring(0, 4);
            zoneConverterMap.put(emmeZone, scbZone);
        });

    }


    class PassengerFlow {
        Id<PassengerFlow> flowId;
        String fromZone;
        String toZone;
        double carTripsPrivate = 0;
        double trainTripsPrivate = 0;
        double busTripsPrivate = 0;
        double airTripsPrivate = 0;

        double carTripsBusiness = 0;
        double trainTripsBusiness = 0;
        double busTripsBusiness = 0;
        double airTripsBusiness = 0;

        public PassengerFlow(Id<PassengerFlow> flowId, String fromZone, String toZone) {
            this.flowId = flowId;
            this.fromZone = fromZone;
            this.toZone = toZone;
        }

        @Override
        public String toString() {
            return "PassengerFlow{" +
                    "flowId=" + flowId +
                    ", fromZone='" + fromZone + '\'' +
                    ", toZone='" + toZone + '\'' +
                    ", carTripsPrivate=" + carTripsPrivate +
                    ", trainTripsPrivate=" + trainTripsPrivate +
                    ", busTripsPrivate=" + busTripsPrivate +
                    ", airTripsPrivate=" + airTripsPrivate +
                    ", carTripsBusiness=" + carTripsBusiness +
                    ", trainTripsBusiness=" + trainTripsBusiness +
                    ", busTripsBusiness=" + busTripsBusiness +
                    ", airTripsBusiness=" + airTripsBusiness +
                    '}';
        }
    }
}
