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
        Standstill standstill = (Standstill) entity;
        insertReindeer(standstill);
        insertNextGiftAssignment(standstill);
        insertTransportationWeight(standstill);
    }

    public void beforeVariableChanged(Object entity, String variableName) {
        Standstill standstill = (Standstill) entity;
        if (variableName.equals("previousStandstill")) {
            // Do nothing
        } else if (variableName.equals("reindeer")) {
            retractReindeer(standstill);
        } else if (variableName.equals("nextGiftAssignment")) {
            retractNextGiftAssignment(standstill);
        } else if (variableName.equals("transportationWeight")) {
            retractTransportationWeight(standstill);
        } else {
            throw new IllegalArgumentException("Unsupported variableName (" + variableName + ").");
        }
    }

    public void afterVariableChanged(Object entity, String variableName) {
        Standstill standstill = (Standstill) entity;
        if (variableName.equals("previousStandstill")) {
            // Do nothing
        } else if (variableName.equals("reindeer")) {
            insertReindeer(standstill);
        } else if (variableName.equals("nextGiftAssignment")) {
            insertNextGiftAssignment(standstill);
        } else if (variableName.equals("transportationWeight")) {
            insertTransportationWeight(standstill);
        } else {
            throw new IllegalArgumentException("Unsupported variableName (" + variableName + ").");
        }
    }

    public void beforeEntityRemoved(Object entity) {
        Standstill standstill = (Standstill) entity;
        retractReindeer(standstill);
        retractNextGiftAssignment(standstill);
        retractTransportationWeight(standstill);
    }

    public void afterEntityRemoved(Object entity) {
        // Do nothing
    }

    private void insertReindeer(Standstill standstill) {
    }

    private void retractReindeer(Standstill standstill) {
    }

    private void insertNextGiftAssignment(Standstill standstill) {
        softScore -= standstill.getSoftNextDistanceWeightCost();
    }

    private void retractNextGiftAssignment(Standstill standstill) {
        softScore += standstill.getSoftNextDistanceWeightCost();
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
        softScore -= standstill.getSoftNextDistanceWeightCost();
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
        softScore += standstill.getSoftNextDistanceWeightCost();
    }

    public HardSoftLongScore calculateScore() {
        return HardSoftLongScore.valueOf(hardScore, softScore);
    }

}
