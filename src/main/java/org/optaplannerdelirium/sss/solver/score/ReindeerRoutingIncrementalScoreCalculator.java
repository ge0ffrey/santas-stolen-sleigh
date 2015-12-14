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
            insertTransportationWeight(giftAssignment);
            insertTransportationToNextPenalty(giftAssignment);
        }
    }

    public void beforeEntityAdded(Object entity) {
        // Do nothing
    }

    public void afterEntityAdded(Object entity) {
        Standstill standstill = (Standstill) entity;
        insertTransportationWeight(standstill);
        insertTransportationToNextPenalty(standstill);
    }

    public void beforeVariableChanged(Object entity, String variableName) {
        Standstill standstill = (Standstill) entity;
        if (variableName.equals("transportationWeight")) {
            retractTransportationWeight(standstill);
        } else if (variableName.equals("transportationToNextPenalty")) {
            retractTransportationToNextPenalty(standstill);
        }
    }

    public void afterVariableChanged(Object entity, String variableName) {
        Standstill standstill = (Standstill) entity;
        if (variableName.equals("transportationWeight")) {
            insertTransportationWeight(standstill);
        } else if (variableName.equals("transportationToNextPenalty")) {
            insertTransportationToNextPenalty(standstill);
        }
    }

    public void beforeEntityRemoved(Object entity) {
        Standstill standstill = (Standstill) entity;
        retractTransportationWeight(standstill);
        retractTransportationToNextPenalty(standstill);
    }

    public void afterEntityRemoved(Object entity) {
        // Do nothing
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

    private void insertTransportationToNextPenalty(Standstill standstill) {
        Long transportationToNextPenalty = standstill.getTransportationToNextPenalty();
        if (transportationToNextPenalty == null) {
            return;
        }
        softScore -= transportationToNextPenalty;
    }

    private void retractTransportationToNextPenalty(Standstill standstill) {
        Long transportationToNextPenalty = standstill.getTransportationToNextPenalty();
        if (transportationToNextPenalty == null) {
            return;
        }
        softScore += transportationToNextPenalty;
    }

    public HardSoftLongScore calculateScore() {
        return HardSoftLongScore.valueOf(hardScore, softScore);
    }

}
