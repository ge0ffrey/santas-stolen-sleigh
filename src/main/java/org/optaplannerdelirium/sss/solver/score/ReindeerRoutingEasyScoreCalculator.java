/*
 * Copyright 2013 JBoss Inc
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
import org.optaplanner.core.impl.score.director.easy.EasyScoreCalculator;
import org.optaplannerdelirium.sss.domain.GiftAssignment;
import org.optaplannerdelirium.sss.domain.Reindeer;
import org.optaplannerdelirium.sss.domain.ReindeerRoutingSolution;
import org.optaplannerdelirium.sss.domain.Standstill;

public class ReindeerRoutingEasyScoreCalculator implements EasyScoreCalculator<ReindeerRoutingSolution> {

    public HardSoftLongScore calculateScore(ReindeerRoutingSolution solution) {
        long hardScore = 0L;
        long softScore = 0L;
        for (GiftAssignment giftAssignment : solution.getGiftAssignmentList()) {
            if (giftAssignment.getPreviousStandstill() != null) {
                Standstill toStandstill = giftAssignment.getNextGiftAssignment();
                if (toStandstill == null) {
                    toStandstill = giftAssignment.getReindeer();
                }
                softScore -= ReindeerRoutingCostCalculator.multiplyWeightAndDistance(giftAssignment.getTransportationWeight(),
                        giftAssignment.getDistanceTo(toStandstill));
                if (giftAssignment.getTransportationWeight() > Reindeer.WEIGHT_CAPACITY) {
                    hardScore -= giftAssignment.getTransportationWeight() - Reindeer.WEIGHT_CAPACITY;
                }
            }
        }
        for (Reindeer reindeer : solution.getReindeerList()) {
            GiftAssignment toStandstill = reindeer.getNextGiftAssignment();
            if (toStandstill != null) {
                softScore -= ReindeerRoutingCostCalculator.multiplyWeightAndDistance(reindeer.getTransportationWeight(),
                        reindeer.getDistanceTo(toStandstill));
            }
        }
        return HardSoftLongScore.valueOf(hardScore, softScore);
    }

}
