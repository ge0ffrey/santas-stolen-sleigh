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
import org.optaplanner.examples.common.domain.AbstractPersistable;
import org.optaplannerdelirium.sss.domain.location.Location;

@XStreamAlias("Reindeer")
public class Reindeer extends AbstractPersistable implements Standstill {

    protected int capacity;
    protected Location startingLocation;

    // Shadow variables
    protected GiftAssignment nextGiftAssignment;

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public Location getStartingLocation() {
        return startingLocation;
    }

    public void setStartingLocation(Location startingLocation) {
        this.startingLocation = startingLocation;
    }

    public GiftAssignment getNextGiftAssignment() {
        return nextGiftAssignment;
    }

    public void setNextGiftAssignment(GiftAssignment nextCustomer) {
        this.nextGiftAssignment = nextCustomer;
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

    public long getDistanceTo(Standstill standstill) {
        return startingLocation.getDistanceTo(standstill.getLocation());
    }

}
