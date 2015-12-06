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
import org.optaplannerdelirium.sss.domain.Standstill;

public class TransportationWeightUpdatingVariableListener implements VariableListener<GiftAssignment> {

    public void beforeEntityAdded(ScoreDirector scoreDirector, GiftAssignment giftAssignment) {
        // Do nothing
    }

    public void afterEntityAdded(ScoreDirector scoreDirector, GiftAssignment giftAssignment) {
        updateTransportationWeight(scoreDirector, giftAssignment);
    }

    public void beforeVariableChanged(ScoreDirector scoreDirector, GiftAssignment giftAssignment) {
        // Do nothing
    }

    public void afterVariableChanged(ScoreDirector scoreDirector, GiftAssignment giftAssignment) {
        updateTransportationWeight(scoreDirector, giftAssignment);
    }

    public void beforeEntityRemoved(ScoreDirector scoreDirector, GiftAssignment giftAssignment) {
        // Do nothing
    }

    public void afterEntityRemoved(ScoreDirector scoreDirector, GiftAssignment giftAssignment) {
        // Do nothing
    }

    protected void updateTransportationWeight(ScoreDirector scoreDirector, GiftAssignment sourceGiftAssignment) {
        Standstill previousStandstill = sourceGiftAssignment.getPreviousStandstill();
        GiftAssignment shadowGiftAssignment = sourceGiftAssignment;
        Long transportationWeight = previousStandstill == null ? null
                : previousStandstill.getTransportationWeight() + sourceGiftAssignment.getGiftWeight();
        while (shadowGiftAssignment != null && shadowGiftAssignment.getTransportationWeight() != transportationWeight) {
            scoreDirector.beforeVariableChanged(shadowGiftAssignment, "transportationWeight");
            shadowGiftAssignment.setTransportationWeight(transportationWeight);
            scoreDirector.afterVariableChanged(shadowGiftAssignment, "transportationWeight");
            shadowGiftAssignment = shadowGiftAssignment.getNextGiftAssignment();
            if (shadowGiftAssignment != null && transportationWeight != null) {
                transportationWeight += shadowGiftAssignment.getGiftWeight();
            }
        }
    }

}
