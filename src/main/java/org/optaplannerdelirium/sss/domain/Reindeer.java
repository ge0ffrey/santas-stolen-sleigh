/*
 * Copyright 2012 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.optaplannerdelirium.sss.domain;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import org.optaplanner.core.api.domain.variable.CustomShadowVariable;
import org.optaplanner.core.api.domain.variable.PlanningVariableReference;
import org.optaplanner.examples.common.domain.AbstractPersistable;
import org.optaplannerdelirium.sss.domain.location.Location;
import org.optaplannerdelirium.sss.solver.score.ReindeerRoutingCostCalculator;

@XStreamAlias("Reindeer")
public class Reindeer extends AbstractPersistable implements Standstill {

    public static final long SLEIGH_WEIGHT = 10L * ReindeerRoutingCostCalculator.MICROS_PER_ONE_AS_LONG;
    public static final long WEIGHT_CAPACITY = 1010L * ReindeerRoutingCostCalculator.MICROS_PER_ONE_AS_LONG;

    private Location startingLocation;

    // Shadow variables
    private GiftAssignment nextGiftAssignment;
    private Long transportationToNextPenalty;

    public Location getStartingLocation() {
        return startingLocation;
    }

    public void setStartingLocation(Location startingLocation) {
        this.startingLocation = startingLocation;
    }

    public GiftAssignment getNextGiftAssignment() {
        return nextGiftAssignment;
    }

    public void setNextGiftAssignment(GiftAssignment nextGiftAssignment) {
        this.nextGiftAssignment = nextGiftAssignment;
    }

    // ************************************************************************
    // Complex methods
    // ************************************************************************

    public Reindeer getReindeer() {
        return this;
    }

    public Location getLocation() {
        return startingLocation;
    }

    public double getDistanceToNextGiftAssignment() {
        if (nextGiftAssignment == null) {
            return 0.0;
        }
        return getDistanceTo(nextGiftAssignment);
    }

    public double getDistanceTo(Standstill standstill) {
        return startingLocation.getDistanceTo(standstill.getLocation());
    }

    public Long getTransportationWeight() {
        return SLEIGH_WEIGHT;
    }

    public Long getTransportationToNextPenalty() {
        return transportationToNextPenalty;
    }

    public void setTransportationToNextPenalty(Long transportationToNextPenalty) {
        this.transportationToNextPenalty = transportationToNextPenalty;
    }

}
