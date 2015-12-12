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

package org.optaplannerdelirium.sss.solver.score;

import org.optaplanner.core.api.score.buildin.hardsoftlong.HardSoftLongScore;
import org.optaplanner.core.impl.score.director.incremental.AbstractIncrementalScoreCalculator;
import org.optaplannerdelirium.sss.domain.GiftAssignment;
import org.optaplannerdelirium.sss.domain.Reindeer;
import org.optaplannerdelirium.sss.domain.ReindeerRoutingSolution;
import org.optaplannerdelirium.sss.domain.Standstill;

public class ReindeerRoutingIncrementalScoreCalculator extends AbstractIncrementalScoreCalculator<ReindeerRoutingSolution> {

    private long hardScore;
    private long softScore;

    public void resetWorkingSolution(ReindeerRoutingSolution solution) {
        hardScore = 0L;
        softScore = 0L;
        for (GiftAssignment giftAssignment : solution.getGiftAssignmentList()) {
            insertReindeer(giftAssignment);
            insertNextGiftAssignment(giftAssignment);
            insertTransportationWeight(giftAssignment);
        }
    }

    public void beforeEntityAdded(Object entity) {
        // Do nothing
    }

    public void afterEntityAdded(Object entity) {
        if (entity instanceof Reindeer) {
            return;
        }
        insertReindeer((Standstill) entity);
        insertNextGiftAssignment((Standstill) entity);
        insertTransportationWeight((Standstill) entity);
    }

    public void beforeVariableChanged(Object entity, String variableName) {
        if (variableName.equals("previousStandstill"))   {
            // Do nothing
        } else if (variableName.equals("reindeer"))   {
            retractReindeer((Standstill) entity);
        } else if (variableName.equals("nextGiftAssignment"))   {
            retractNextGiftAssignment((Standstill) entity);
        } else if (variableName.equals("transportationWeight"))   {
            retractTransportationWeight((Standstill) entity);
        } else {
            throw new IllegalArgumentException("Unsupported variableName (" + variableName + ").");
        }
    }

    public void afterVariableChanged(Object entity, String variableName) {
        if (variableName.equals("previousStandstill"))   {
            // Do nothing
        } else if (variableName.equals("reindeer"))   {
            insertReindeer((Standstill) entity);
        } else if (variableName.equals("nextGiftAssignment"))   {
            insertNextGiftAssignment((Standstill) entity);
        } else if (variableName.equals("transportationWeight"))   {
            insertTransportationWeight((Standstill) entity);
        } else {
            throw new IllegalArgumentException("Unsupported variableName (" + variableName + ").");
        }
    }

    public void beforeEntityRemoved(Object entity) {
        retractReindeer((Standstill) entity);
        retractNextGiftAssignment((Standstill) entity);
        retractTransportationWeight((Standstill) entity);
    }

    public void afterEntityRemoved(Object entity) {
        // Do nothing
    }

    private void insertReindeer(Standstill standstill) {
    }

    private void retractReindeer(Standstill standstill) {
    }

    private void insertNextGiftAssignment(Standstill standstill) {
        Standstill toStandstill = standstill.getNextGiftAssignment();
        if (toStandstill == null) {
            toStandstill = standstill.getReindeer();
            if (toStandstill == null) {
                return;
            }
        }
        Long transportationWeight = standstill.getTransportationWeight();
        if (transportationWeight == null) {
            return;
        }
        softScore -= ReindeerRoutingCostCalculator.multiplyWeightAndDistance(transportationWeight,
                standstill.getDistanceTo(toStandstill));
    }

    private void retractNextGiftAssignment(Standstill standstill) {
        Standstill toStandstill = standstill.getNextGiftAssignment();
        if (toStandstill == null) {
            toStandstill = standstill.getReindeer();
            if (toStandstill == null) {
                return;
            }
        }
        Long transportationWeight = standstill.getTransportationWeight();
        if (transportationWeight == null) {
            return;
        }
        softScore += ReindeerRoutingCostCalculator.multiplyWeightAndDistance(transportationWeight,
                standstill.getDistanceTo(toStandstill));
    }

    private void insertTransportationWeight(Standstill standstill) {
        Long transportationWeight = standstill.getTransportationWeight();
        if (transportationWeight == null) {
            return;
        }
        long weightCapacity = Reindeer.WEIGHT_CAPACITY - transportationWeight;
        if (weightCapacity < 0L) {
            hardScore += weightCapacity;
        }
    }

    private void retractTransportationWeight(Standstill standstill) {
        Long transportationWeight = standstill.getTransportationWeight();
        if (transportationWeight == null) {
            return;
        }
        long weightCapacity = Reindeer.WEIGHT_CAPACITY - transportationWeight;
        if (weightCapacity < 0L) {
            hardScore -= weightCapacity;
        }
    }

    public HardSoftLongScore calculateScore() {
        return HardSoftLongScore.valueOf(hardScore, softScore);
    }

}
