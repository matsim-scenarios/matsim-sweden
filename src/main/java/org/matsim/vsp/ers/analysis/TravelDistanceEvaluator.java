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

import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.population.*;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.population.algorithms.PersonAlgorithm;
import org.matsim.core.population.io.StreamingPopulationReader;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.utils.geometry.CoordUtils;
import org.matsim.core.utils.io.IOUtils;
import org.matsim.pt.router.TransitActsRemover;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

/**
 * @author jbischoff
 */


public class TravelDistanceEvaluator {
    private Map<String, PersonValidator> groups = new HashMap<>();


    public static void main(String[] args) throws IOException {
        TravelDistanceEvaluator tde = new TravelDistanceEvaluator();

        tde.run("D:/runs-svn/ers_sweden/basecase/se.0.01/se.0.01.output_plans.xml.gz");

    }


    public void run(String populationFile) throws IOException {
        groups.put("metropolitan", new LivesMetropolitan());
        groups.put("non_metropolitan", new LivesNotMetropolitan());
        groups.put("everyone", new AnyPerson());

        for (Entry<String, PersonValidator> e : groups.entrySet()) {
            Scenario scenario = ScenarioUtils.createScenario(ConfigUtils.createConfig());
            StreamingPopulationReader spr = new StreamingPopulationReader(scenario);
            Map<String, TravelDistanceActivity> distanceActivityPerMode = new HashMap<>();
            distanceActivityPerMode.put(TransportMode.car, new TravelDistanceActivity(TransportMode.car));
            distanceActivityPerMode.put(TransportMode.ride, new TravelDistanceActivity(TransportMode.ride));
            distanceActivityPerMode.put(TransportMode.bike, new TravelDistanceActivity(TransportMode.bike));
            distanceActivityPerMode.put(TransportMode.pt, new TravelDistanceActivity(TransportMode.pt));
            distanceActivityPerMode.put(TransportMode.walk, new TravelDistanceActivity(TransportMode.walk));
            spr.addAlgorithm(new PersonAlgorithm() {

                @Override
                public void run(Person person) {
                    if (e.getValue().isValidPerson(person)) {
                        Plan plan = person.getSelectedPlan();

                        Leg prevLeg = null;
                        Activity prevAct = null;
                        for (PlanElement pe : plan.getPlanElements()) {
                            //convert pure transit_walks to walk
                            if (pe instanceof Activity) {
                                if (prevLeg != null && prevAct != null) {
                                    if (!((Activity) pe).getType().equals("pt interaction") && !prevAct.getType().equals("pt interaction") && prevLeg.getMode().equals(TransportMode.access_walk)) {
                                        prevLeg.setMode("walk");
                                    }
                                }
                                prevAct = (Activity) pe;
                            } else if (pe instanceof Leg) {
                                prevLeg = (Leg) pe;
                            }

                        }

                        new TransitActsRemover().run(plan, true);
                        Activity lastAct = null;
                        Leg lastLeg = null;
                        for (PlanElement pe : plan.getPlanElements()) {
                            if (pe instanceof Activity) {
                                if (lastAct != null) {
                                    int distance_km;
                                    if (lastLeg.getMode().equals(TransportMode.pt)) {
                                        distance_km = (int) ((CoordUtils.calcEuclideanDistance(((Activity) pe).getCoord(), lastAct.getCoord()) * 1.3) / 1000.0);

                                    } else {
                                        distance_km = (int) (lastLeg.getRoute().getDistance() / 1000.0);
                                    }
                                    distanceActivityPerMode.get(lastLeg.getMode()).addLeg(((Activity) pe).getType(), distance_km);
                                    lastAct = (Activity) pe;

                                } else {
                                    lastAct = (Activity) pe;

                                }
                            } else if (pe instanceof Leg) {
                                lastLeg = (Leg) pe;
                            }
                        }
                    }
                }


            });

            spr.readFile(populationFile);
            BufferedWriter bw = IOUtils.getBufferedWriter(populationFile.replace(".xml.gz", "_modeStats_" + e.getKey() + ".csv"));
            bw.write(e.getKey() + ";");
            bw.newLine();
            bw.write("Mode;trips");


            for (TravelDistanceActivity tda : distanceActivityPerMode.values()) {
                tda.writeTable(populationFile.replace(".xml.gz", "_distanceStats_" + tda.mode + "_" + e.getKey() + ".csv"));
                bw.newLine();
                bw.write(tda.mode + ";" + tda.totalTrips);
            }
            bw.flush();
            bw.close();
        }


    }


    class TravelDistanceActivity {
        String mode;
        Map<String, int[]> activityDistanceBins = new TreeMap<>();
        int totalTrips = 0;

        /**
         *
         */
        public TravelDistanceActivity(String mode) {
            this.mode = mode;
        }

        public void addLeg(String activity, int distance) {
            totalTrips++;
            activity = activity.split("_")[0];
            if (!activityDistanceBins.containsKey(activity)) {
                activityDistanceBins.put(activity, new int[51]);
            }
            if (distance > 50) distance = 50;
            activityDistanceBins.get(activity)[distance]++;
        }

        public void writeTable(String filename) {
            BufferedWriter bw = IOUtils.getBufferedWriter(filename);
            try {
                bw.write("Mode;" + mode);
                bw.newLine();
                bw.write("distance");
                for (String s : activityDistanceBins.keySet()) {
                    bw.write(";" + s);


                }
                for (int i = 0; i < 51; i++) {
                    bw.newLine();
                    bw.write(Integer.toString(i));
                    for (int[] v : activityDistanceBins.values()) {
                        bw.write(";" + v[i]);
                    }

                }
                bw.flush();
                bw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    class LivesMetropolitan implements PersonValidator {

        /* (non-Javadoc)
         * @see analysis.traveldistances.PersonValidator#isValidPerson(org.matsim.api.core.v01.population.Person)
         */
        @Override
        public boolean isValidPerson(Person person) {
            Boolean metropolitanAgent = (Boolean) person.getAttributes().getAttribute("metropolitanRegion");
            if (metropolitanAgent != null) {
                if (metropolitanAgent) {
                    return true;
                }
            }
            return false;


        }
    }

    class LivesNotMetropolitan implements PersonValidator {

        /* (non-Javadoc)
         * @see analysis.traveldistances.PersonValidator#isValidPerson(org.matsim.api.core.v01.population.Person)
         */
        @Override
        public boolean isValidPerson(Person person) {
            Boolean metropolitanAgent = (Boolean) person.getAttributes().getAttribute("metropolitanRegion");
            if (metropolitanAgent != null) {
                if (metropolitanAgent) {
                    return false;
                } else return true;
            }
            return false;


        }
    }

    class AnyPerson implements PersonValidator {

        /* (non-Javadoc)
         * @see analysis.traveldistances.PersonValidator#isValidPerson(org.matsim.api.core.v01.population.Person)
         */
        @Override
        public boolean isValidPerson(Person person) {
            Boolean metropolitanAgent = (Boolean) person.getAttributes().getAttribute("metropolitanRegion");
            if (metropolitanAgent != null) {
                return true;
            } else return false;
        }


    }
}
	

