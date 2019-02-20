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

package org.matsim.vsp.ers.analysis;/*
 * created by jbischoff, 12.02.2019
 */

import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.events.*;
import org.matsim.api.core.v01.events.handler.*;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.events.EventsUtils;
import org.matsim.core.events.MatsimEventsReader;
import org.matsim.core.utils.misc.Time;

public class TravelTimeAnalyzer {

    public static void main(String[] args) {

        EventsManager eventsManager = EventsUtils.createEventsManager();
        TTEventHandler tts = new TTEventHandler();
        eventsManager.addHandler(tts);
//        new MatsimEventsReader(eventsManager).readFile("D:/runs-svn/ers_sweden/15-bev/se_15.0.1.output_events.xml.gz");
        new MatsimEventsReader(eventsManager).readFile("D:/runs-svn/ers_sweden/se_14.0.1/se_14.0.1.output_events.xml.gz");
        System.out.println("truck tt: " + Time.writeTime(tts.truckTT));
        System.out.println("car tt: " + Time.writeTime(tts.carTT));
    }

}

class TTEventHandler implements ActivityStartEventHandler, ActivityEndEventHandler, PersonDepartureEventHandler, PersonArrivalEventHandler, PersonStuckEventHandler {

    double carTT = 0;
    double truckTT = 0;

    @Override
    public void handleEvent(ActivityEndEvent event) {
        if (event.getActType().equals("truck charging")) {
            truckTT += event.getTime();
        } else if (event.getActType().equals("car charging")) {
            carTT += event.getTime();
        }
    }

    @Override
    public void handleEvent(ActivityStartEvent event) {
        if (event.getActType().equals("truck charging")) {
            truckTT -= event.getTime();
        } else if (event.getActType().equals("car charging")) {
            carTT -= event.getTime();
        }
    }

    @Override
    public void handleEvent(PersonArrivalEvent event) {
        if (event.getTime() > 29 * 3600) return;
        if (event.getLegMode().equals(TransportMode.truck)) {
            truckTT += event.getTime();
        } else if (event.getLegMode().equals(TransportMode.car)) {
            carTT += event.getTime();
        }
    }

    @Override
    public void handleEvent(PersonDepartureEvent event) {
        if (event.getLegMode().equals(TransportMode.truck)) {
            truckTT -= event.getTime();
        } else if (event.getLegMode().equals(TransportMode.car)) {
            carTT -= event.getTime();
        }
    }

    @Override
    public void handleEvent(PersonStuckEvent event) {
        if (String.valueOf(event.getLegMode()).equals(TransportMode.truck)) {
            truckTT += event.getTime();
        } else if (String.valueOf(event.getLegMode()).equals(TransportMode.car)) {
            carTT += event.getTime();
        }
    }
}