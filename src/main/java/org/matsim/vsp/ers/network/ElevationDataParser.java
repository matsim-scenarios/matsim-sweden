/* *********************************************************************** *
 * project: org.matsim.*												   *
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2008 by the members listed in the COPYING,        *
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
package org.matsim.vsp.ers.network;

import org.geotools.coverage.grid.GridCoordinates2D;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridGeometry2D;
import org.geotools.coverage.grid.InvalidGridGeometryException;
import org.geotools.data.DataSourceException;
import org.geotools.gce.geotiff.GeoTiffReader;
import org.geotools.geometry.DirectPosition2D;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.Node;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.network.io.MatsimNetworkReader;
import org.matsim.core.network.io.NetworkWriter;
import org.matsim.core.utils.geometry.CoordUtils;
import org.matsim.core.utils.geometry.CoordinateTransformation;
import org.matsim.core.utils.geometry.transformations.TransformationFactory;
import org.matsim.utils.objectattributes.DoubleArrayConverter;
import org.opengis.referencing.operation.TransformException;

import java.awt.image.Raster;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * @author smetzler, dziemke
 */
public class ElevationDataParser {

    private double minimumLength = 500;
    private double minimumSlope = 0.005;

    private static GridCoverage2D grid;
    private static Raster gridData;
    private CoordinateTransformation ct;

    public static void main(String[] args) {
        // Data sources:
        // SRTM1:  http://earthexplorer.usgs.gov/ (login in required)
        // SRTM3:  http://srtm.csi.cgiar.org/SELECTION/inputCoord.asp
        // EU-DEM: http://data.eox.at/eudem

        List<String> tiffFiles = Arrays.asList(new String[]{"D:/ers/network/dem/eu_dem_v11_E40N50/eu_dem_v11_E40N50.TIF", "D:/ers/network/dem/eu_dem_v11_E40N40/eu_dem_v11_E40N40.TIF", "D:/ers/network/dem/eu_dem_v11_E40N30/eu_dem_v11_E40N30.TIF"});

        String scenarioCRS = "EPSG:3006"; // WGS84 as the coorinates to test below are stated like this
        Network network = NetworkUtils.createNetwork();
        new MatsimNetworkReader(network).readFile("D:/ers/network/Base2012_network_car_cleaned.xml");
        int zs = 0;
        for (String tiffFile : tiffFiles) {
            ElevationDataParser elevationDataParser = new ElevationDataParser(tiffFile, scenarioCRS);
            for (Node n : network.getNodes().values()) {
                if (!n.getCoord().hasZ()) {
                    Double z = elevationDataParser.getElevation(n.getCoord());
                    if (z != null) {
                        if (z < -10) {
                            z = 0.0;
                        }
                        n.setCoord(new Coord(n.getCoord().getX(), n.getCoord().getY(), z));
                        zs++;
                        for (Link l : n.getOutLinks().values()) {
                            elevationDataParser.addSlopes(l);
                        }

                    }
                }
            }
        }
        System.out.println(zs + " z coordinates set");
        NetworkWriter networkWriter = new NetworkWriter(network);
        networkWriter.putAttributeConverter(double[].class, new DoubleArrayConverter());
        networkWriter.write("D:/ers/network/Base2012_network_car_cleaned_zs.xml");
    }

    private void addSlopes(Link l) {
        int bins = Math.max(1, (int) (l.getLength() / minimumLength));
        double[] slopes = new double[bins];
        boolean hasRelevantSlope = false;
        Coord lastCoord = l.getFromNode().getCoord();
        double lastElevation = l.getFromNode().getCoord().getZ();
        for (int i = 0; i < bins; i++) {
            double fraction = ((double) i + 1 / ((double) bins));
            double nextX = l.getFromNode().getCoord().getX() + fraction * (l.getToNode().getCoord().getX() - l.getFromNode().getCoord().getX());
            double nextY = l.getFromNode().getCoord().getY() + fraction * (l.getToNode().getCoord().getY() - l.getFromNode().getCoord().getY());
            Coord nextCoord = new Coord(nextX, nextY);
            Double nextElevation = getElevation(nextCoord);
            if (nextElevation == null) {
                hasRelevantSlope = false;
                break;
            }
            if (nextElevation < -20) {
                hasRelevantSlope = false;
                break;
            }
            double slope = (nextElevation - lastElevation) / CoordUtils.calcEuclideanDistance(nextCoord, lastCoord);
            slopes[i] = slope;
            if (Math.abs(slope) >= minimumSlope) {
                hasRelevantSlope = true;
            }

            lastCoord = nextCoord;
            lastElevation = nextElevation;
        }
        if (hasRelevantSlope) {
            l.getAttributes().putAttribute("slopes", slopes);
        }

    }


    public ElevationDataParser(String tiffFile, String scenarioCRS) {
        this.ct = TransformationFactory.getCoordinateTransformation(scenarioCRS, "EPSG:3035");

        GeoTiffReader reader = null;
        try {
            reader = new GeoTiffReader(tiffFile);
        } catch (DataSourceException e) {
            e.printStackTrace();
        }

        try {
            grid = reader.read(null);
        } catch (IOException e) {
            e.printStackTrace();
        }
        gridData = grid.getRenderedImage().getData();
        System.out.println(gridData.getBounds());
    }


    public Double getElevation(Coord coord) {
        GridGeometry2D gg = grid.getGridGeometry();

        Coord transformedCoord = ct.transform(coord);
//		System.out.println(transformedCoord);
        GridCoordinates2D posGrid = null;
        try {
            posGrid = gg.worldToGrid(new DirectPosition2D(transformedCoord.getX(), transformedCoord.getY()));
        } catch (InvalidGridGeometryException e) {
            e.printStackTrace();
        } catch (TransformException e) {
            e.printStackTrace();
        }

        double[] pixel = new double[1];
        try {
            double[] data = gridData.getPixel(posGrid.x, posGrid.y, pixel);
//		System.out.println(posGrid.x +" "+ posGrid.y +" "+ data[0]);
            return data[0];
        } catch (ArrayIndexOutOfBoundsException e) {
            return null;

        }
    }
}