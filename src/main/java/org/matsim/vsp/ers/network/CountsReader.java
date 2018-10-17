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
 * created by jbischoff, 17.10.2018
 */

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.core.utils.io.tabularFileParser.TabularFileHandler;
import org.matsim.core.utils.io.tabularFileParser.TabularFileParser;
import org.matsim.core.utils.io.tabularFileParser.TabularFileParserConfig;
import org.matsim.counts.Count;
import org.matsim.counts.Counts;
import org.matsim.counts.CountsWriter;

import java.util.HashMap;
import java.util.Map;

public class CountsReader {

    public static void main(String[] args) {

        Counts<Link> counts = new Counts<>();

        Map<String, String> mapping = new HashMap<>();
        TabularFileParserConfig tb1 = new TabularFileParserConfig();
        tb1.setDelimiterTags(new String[]{"\t"});
        tb1.setFileName("D:/ers/counts/mapping.txt");
        new TabularFileParser().parse(tb1, row -> mapping.put(row[0], row[1]));

        TabularFileParserConfig tbc = new TabularFileParserConfig();
        tbc.setDelimiterTags(new String[]{";"});
        tbc.setFileName("D:/ers/counts/counts.csv");
        new TabularFileParser().parse(tbc, new TabularFileHandler() {
            Count<Link> currentCount = null;

            @Override
            public void startRow(String[] row) {
                if (row.length < 1) return;
                if (row[0].equals("LinkID")) {

                    if (mapping.containsKey(row[1])) {

                        Id<Link> linkId = Id.createLinkId(mapping.get(row[1]));
                        counts.createAndAddCount(linkId, linkId.toString());
                        currentCount = counts.getCount(linkId);
                    } else {
                        currentCount = null;
                    }
                } else {
                    try {
                        int time = Integer.parseInt(row[0]) + 1;
                        double overAllCount = Double.parseDouble(row[1]) + Double.parseDouble(row[2]);
                        if (currentCount != null) currentCount.createVolume(time, overAllCount);
                    } catch (NumberFormatException e) {

                    }


                }
            }
        });
        new CountsWriter(counts).write("D:/ers/counts/counts.xml");


    }
}
