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

package org.matsim.vsp.ers.demand.commuters;/*
 * created by jbischoff, 04.06.2018
 */

import org.matsim.core.utils.io.tabularFileParser.TabularFileHandler;
import org.matsim.core.utils.io.tabularFileParser.TabularFileParser;
import org.matsim.core.utils.io.tabularFileParser.TabularFileParserConfig;
import playground.vsp.openberlinscenario.cemdap.input.CommuterRelationV2;

import java.util.ArrayList;
import java.util.List;

public class ReadCommuterFile {

    public static void main(String[] args) {
        new ReadCommuterFile().readCommuterFile("D:/ers/commuters/commuters2016.csv");
    }

    private List<CommuterRelationV2> commuterRelations = new ArrayList<>();

    public void readCommuterFile(String filename) {
        TabularFileParserConfig tabularFileParserConfig = new TabularFileParserConfig();
        tabularFileParserConfig.setFileName(filename);
        tabularFileParserConfig.setDelimiterTags(new String[]{";"});
        new TabularFileParser().parse(tabularFileParserConfig, new TabularFileHandler() {
            String currentFrom = "";

            @Override
            public void startRow(String[] row) {
                if (row[0].length() > 0) {
                    currentFrom = row[0].split(" ")[0];

                }
                String to = row[1].split(" ")[0];
                Integer commuters = Integer.parseInt(row[2]);
                CommuterRelationV2 relation = new CommuterRelationV2(currentFrom, to, commuters, null, null);
                commuterRelations.add(relation);
                System.out.println(relation);
            }
        });
    }

    public List<CommuterRelationV2> getCommuterRelations() {
        return commuterRelations;
    }
}
