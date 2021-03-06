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
import org.optaplanner.core.api.domain.variable.CustomShadowVariable;
import org.optaplanner.core.api.domain.variable.PlanningVariable;
import org.optaplanner.core.api.domain.variable.PlanningVariableGraphType;
import org.optaplanner.examples.common.domain.AbstractPersistable;
import org.optaplannerdelirium.sss.domain.location.Location;
import org.optaplannerdelirium.sss.domain.solver.NorthPoleAngleGiftAssignmentDifficultyWeightFactory;
import org.optaplannerdelirium.sss.domain.solver.WeightAndCostUpdatingVariableListener;

 @PlanningEntity(difficultyWeightFactoryClass = NorthPoleAngleGiftAssignmentDifficultyWeightFactory.class)
//@PlanningEntity(difficultyWeightFactoryClass = NorthPoleDistanceGiftAssignmentDifficultyWeightFactory.class) // Partitioning still happens on angle though
@XStreamAlias("GiftAssignment")
public class GiftAssignment extends AbstractPersistable implements Standstill {

    private Gift gift;

    // Planning variables: changes during planning, between score calculations.
    private Standstill previousStandstill; // Because the reindeer drop off gifts, the previousStandstill is actually where they go to...

    // caches
    private double distanceFromPreviousStandstill;
    private double distanceToNextGiftAssignmentOrReindeer;

    // Shadow variables
    private GiftAssignment nextGiftAssignment; // Because the reindeer drop off gifts, the previousStandstill is actually where they come from...
    private Reindeer reindeer;
    private Long transportationWeight;
    private Long transportationToNextPenalty; // Because the reindeer drop off gifts, the penalty is actually they weariness of the distance from where they come from...

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

    private double calculateDistanceFromPreviousStandstill() {
        if (previousStandstill == null) {
            return 0;
        }
        return getDistanceTo(previousStandstill);
    }

    private double calculateDistanceToNextGiftAssignmentOrReindeer() {
        if (nextGiftAssignment == null) {
            if (reindeer == null) {
             return 0.0;
            }
            return getDistanceTo(reindeer);
        }
        return getDistanceTo(nextGiftAssignment);
    }

    public void setPreviousStandstill(Standstill previousStandstill) {
        this.previousStandstill = previousStandstill;
        this.distanceFromPreviousStandstill = calculateDistanceFromPreviousStandstill();
    }

    public GiftAssignment getNextGiftAssignment() {
        return nextGiftAssignment;
    }

    public void setNextGiftAssignment(GiftAssignment nextGiftAssignment) {
        this.nextGiftAssignment = nextGiftAssignment;
        this.distanceToNextGiftAssignmentOrReindeer = calculateDistanceToNextGiftAssignmentOrReindeer();
    }

    @AnchorShadowVariable(sourceVariableName = "previousStandstill")
    public Reindeer getReindeer() {
        return reindeer;
    }

    public void setReindeer(Reindeer reindeer) {
        this.reindeer = reindeer;
        this.distanceToNextGiftAssignmentOrReindeer = calculateDistanceToNextGiftAssignmentOrReindeer();
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

    public double getDistanceFromPreviousStandstill() {
        return this.distanceFromPreviousStandstill;
    }

    @Override
    public double getDistanceToNextGiftAssignmentOrReindeer() {
        return this.distanceToNextGiftAssignmentOrReindeer;
    }

    public double getDistanceTo(Standstill standstill) {
        return getLocation().getDistanceTo(standstill.getLocation());
    }

    @CustomShadowVariable(variableListenerClass = WeightAndCostUpdatingVariableListener.class,
            sources = {@CustomShadowVariable.Source(variableName = "previousStandstill"),
                     @CustomShadowVariable.Source(variableName = "nextGiftAssignment"),
                    @CustomShadowVariable.Source(variableName = "reindeer")})
    public Long getTransportationWeight() {
        return transportationWeight;
    }

     public void setTransportationWeight(Long transportationWeight) {
        this.transportationWeight = transportationWeight;
    }

    public Long getTransportationToNextPenalty() {
        return transportationToNextPenalty;
    }

    public void setTransportationToNextPenalty(Long transportationToNextPenalty) {
        this.transportationToNextPenalty = transportationToNextPenalty;
    }

}
