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


import com.vividsolutions.jts.geom.Geometry;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.population.Population;
import org.matsim.core.gbl.MatsimRandom;
import org.matsim.vsp.ers.demand.commuters.CreateCommuterDemand;
import playground.vsp.corineLandcover.CorineLandCoverData;

import java.util.Map;
import java.util.Random;

public class CreateLongDistanceDemand {
    private Map<Id<ReadSampersMatrices.PassengerFlow>, ReadSampersMatrices.PassengerFlow> flowMap;
    private CorineLandCoverData corineLandCoverData;

    final String zonesFile = "D:/ers/Sampers/Resultat/zoneconversion.txt";
    final String matricesFolder = "D:\\ers\\Sampers\\Resultat\\relevant_matrices\\";
    final String clcFile = "D:/ers/clc/landcover_rel_se.shp";
    private Map<String, Geometry> communities;
    private Population population;


    public static final String COMMUNITYSHAPE = "D:/ers/commuters/Kommun_Sweref99TM_region.shp";
    private Random random = MatsimRandom.getRandom();

    final double max_sameday_return_threshold_m = 250000;

    public static void main(String[] args) {

        new CreateLongDistanceDemand().run();

    }

    public void run() {
        ReadSampersMatrices readSampersMatrices = new ReadSampersMatrices();
        readSampersMatrices.run(zonesFile, matricesFolder);
        communities = CreateCommuterDemand.readShapeFileAndExtractGeometry(COMMUNITYSHAPE, "KnKod");
        corineLandCoverData = new CorineLandCoverData(clcFile);


    }

    private double getDayReturnProbability(double distance) {
        double p = 1.0 - Math.sqrt(distance / max_sameday_return_threshold_m);
        return Math.min(p, 0.0);
    }
}
