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
import org.optaplanner.core.api.domain.entity.PlanningEntity;
import org.optaplanner.core.api.domain.variable.AnchorShadowVariable;
import org.optaplanner.core.api.domain.variable.PlanningVariable;
import org.optaplanner.core.api.domain.variable.PlanningVariableGraphType;
import org.optaplanner.examples.common.domain.AbstractPersistable;
import org.optaplannerdelirium.sss.domain.location.Location;
import org.optaplannerdelirium.sss.domain.solver.NorthPoleAngleGiftAssignmentDifficultyWeightFactory;

@PlanningEntity(difficultyWeightFactoryClass = NorthPoleAngleGiftAssignmentDifficultyWeightFactory.class)
@XStreamAlias("GiftAssignment")
public class GiftAssignment extends AbstractPersistable implements Standstill {

    protected Gift gift;

    // Planning variables: changes during planning, between score calculations.
    protected Standstill previousStandstill;

    // Shadow variables
    protected GiftAssignment nextGiftAssignment;
    protected Reindeer reindeer;

    public Gift getGift() {
        return gift;
    }

    public void setGift(Gift gift) {
        this.gift = gift;
    }

    @PlanningVariable(valueRangeProviderRefs = {"reindeerRange", "giftAssignmentRange"},
            graphType = PlanningVariableGraphType.CHAINED)
    public Standstill getPreviousStandstill() {
        return previousStandstill;
    }

    public void setPreviousStandstill(Standstill previousStandstill) {
        this.previousStandstill = previousStandstill;
    }

    public GiftAssignment getNextGiftAssignment() {
        return nextGiftAssignment;
    }

    public void setNextGiftAssignment(GiftAssignment nextGiftAssignment) {
        this.nextGiftAssignment = nextGiftAssignment;
    }

    @AnchorShadowVariable(sourceVariableName = "previousStandstill")
    public Reindeer getReindeer() {
        return reindeer;
    }

    public void setReindeer(Reindeer reindeer) {
        this.reindeer = reindeer;
    }

    // ************************************************************************
    // Complex methods
    // ************************************************************************

    public Location getLocation() {
        return gift.getLocation();
    }

    public long getGiftWeight() {
        return gift.getWeight();
    }

    /**
     * @return a positive number, the distance multiplied by 1000 to avoid floating point arithmetic rounding errors
     */
    public long getDistanceFromPreviousStandstill() {
        if (previousStandstill == null) {
            return 0;
        }
        return getDistanceFrom(previousStandstill);
    }

    /**
     * @param standstill never null
     * @return a positive number, the distance multiplied by 1000 to avoid floating point arithmetic rounding errors
     */
    public long getDistanceFrom(Standstill standstill) {
        return standstill.getLocation().getDistanceTo(getLocation());
    }

    /**
     * @param standstill never null
     * @return a positive number, the distance multiplied by 1000 to avoid floating point arithmetic rounding errors
     */
    public long getDistanceTo(Standstill standstill) {
        return getLocation().getDistanceTo(standstill.getLocation());
    }

}
