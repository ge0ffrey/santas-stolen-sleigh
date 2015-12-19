/*
 * Copyright 2015 JBoss Inc
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

package org.optaplannerdelirium.sss.domain.solver;

import org.apache.commons.lang3.ObjectUtils;
import org.optaplanner.core.impl.domain.variable.listener.VariableListener;
import org.optaplanner.core.impl.score.director.ScoreDirector;
import org.optaplannerdelirium.sss.domain.GiftAssignment;
import org.optaplannerdelirium.sss.domain.Reindeer;
import org.optaplannerdelirium.sss.domain.Standstill;
import org.optaplannerdelirium.sss.solver.score.ReindeerRoutingCostCalculator;

public class WeightAndCostUpdatingVariableListener implements VariableListener<Standstill> {

    public void beforeEntityAdded(ScoreDirector scoreDirector, Standstill standstill) {
        // Do nothing
    }

    public void afterEntityAdded(ScoreDirector scoreDirector, Standstill standstill) {
        updateVariable(scoreDirector, standstill);
    }

    public void beforeVariableChanged(ScoreDirector scoreDirector, Standstill standstill) {
        // Do nothing
    }

    public void afterVariableChanged(ScoreDirector scoreDirector, Standstill standstill) {
        updateVariable(scoreDirector, standstill);
    }

    public void beforeEntityRemoved(ScoreDirector scoreDirector, Standstill standstill) {
        // Do nothing
    }

    public void afterEntityRemoved(ScoreDirector scoreDirector, Standstill standstill) {
        // Do nothing
    }

    protected void updateVariable(ScoreDirector scoreDirector, Standstill sourceStandstill) {
        if (sourceStandstill instanceof GiftAssignment) {
            GiftAssignment sourceGiftAssignment = (GiftAssignment) sourceStandstill;
            Standstill previousStandstill = sourceGiftAssignment.getPreviousStandstill();
            Long transportationWeight = previousStandstill == null ? null
                    : previousStandstill.getTransportationWeight() + sourceGiftAssignment.getGiftWeight();
            if (ObjectUtils.equals(sourceGiftAssignment.getTransportationWeight(), transportationWeight)) {
                // Only nextGiftAssignment changed
                Long transportationToNextPenalty =
                        calculateTransportationToNextPenalty(transportationWeight, sourceGiftAssignment);
                if (!ObjectUtils.equals(sourceStandstill.getTransportationToNextPenalty(), transportationToNextPenalty)) {
                    scoreDirector.beforeVariableChanged(sourceGiftAssignment, "transportationToNextPenalty");
                    sourceGiftAssignment.setTransportationToNextPenalty(transportationToNextPenalty);
                    scoreDirector.afterVariableChanged(sourceGiftAssignment, "transportationToNextPenalty");
                }
            } else {
                GiftAssignment shadowGiftAssignment = sourceGiftAssignment;
                while (shadowGiftAssignment != null
                        && !ObjectUtils.equals(shadowGiftAssignment.getTransportationWeight(), transportationWeight)) {
                    scoreDirector.beforeVariableChanged(shadowGiftAssignment, "transportationWeight");
                    shadowGiftAssignment.setTransportationWeight(transportationWeight);
                    scoreDirector.afterVariableChanged(shadowGiftAssignment, "transportationWeight");
                    Long transportationToNextPenalty =
                            calculateTransportationToNextPenalty(transportationWeight, sourceGiftAssignment);
                    scoreDirector.beforeVariableChanged(shadowGiftAssignment, "transportationToNextPenalty");
                    shadowGiftAssignment.setTransportationToNextPenalty(transportationToNextPenalty);
                    scoreDirector.afterVariableChanged(shadowGiftAssignment, "transportationToNextPenalty");
                    shadowGiftAssignment = shadowGiftAssignment.getNextGiftAssignment();
                    if (shadowGiftAssignment != null && transportationWeight != null) {
                        transportationWeight += shadowGiftAssignment.getGiftWeight();
                    }
                }
            }
        } else {
            Reindeer reindeer = (Reindeer) sourceStandstill;
            Long transportationWeight = reindeer.getTransportationWeight();
            Long transportationToNextPenalty =
                    calculateTransportationToNextPenalty(transportationWeight, sourceStandstill);
            if (!ObjectUtils.equals(sourceStandstill.getTransportationToNextPenalty(), transportationToNextPenalty)) {
                scoreDirector.beforeVariableChanged(sourceStandstill, "transportationToNextPenalty");
                sourceStandstill.setTransportationToNextPenalty(transportationToNextPenalty);
                scoreDirector.afterVariableChanged(sourceStandstill, "transportationToNextPenalty");
            }
        }
    }

    public Long calculateTransportationToNextPenalty(Long transportationWeight, Standstill standstill) {
        if (transportationWeight == null) {
            return null;
        }
        return ReindeerRoutingCostCalculator.multiplyWeightAndDistance(transportationWeight,
                standstill.getDistanceToNextGiftAssignmentOrReindeer());
    }

}
