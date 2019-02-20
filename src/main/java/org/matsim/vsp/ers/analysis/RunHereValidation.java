/* *********************************************************************** *
 * project: org.matsim.*
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2017 by the members listed in the COPYING,        *
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

/**
 *
 */
package org.matsim.vsp.ers.analysis;
/**
 * @author jbischoff
 */

import org.matsim.contrib.analysis.vsp.traveltimedistance.RunTraveltimeValidationExample;

/**
 *
 */
public class RunHereValidation {
    public static void main(String[] args) {
        String folder = "D:\\runs-svn\\ers_sweden/se_14.0.1/";
        String run = "se_14.0.1";

        RunTraveltimeValidationExample.main(new String[]{folder + run + ".output_plans.xml.gz", folder + run + ".output_events.xml.gz", folder + run + ".output_network.xml.gz", "EPSG:3006", "kiuJ25yDM4IxExx48vI6", "PYKgSi3HWhS6Ua0ESSndkA", folder, "2018-09-16", "5000"});
    }
}
