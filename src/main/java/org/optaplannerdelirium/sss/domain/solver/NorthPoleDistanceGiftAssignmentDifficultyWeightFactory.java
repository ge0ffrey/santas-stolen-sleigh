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

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.optaplanner.core.impl.heuristic.selector.common.decorator.SelectionSorterWeightFactory;
import org.optaplannerdelirium.sss.domain.GiftAssignment;
import org.optaplannerdelirium.sss.domain.ReindeerRoutingSolution;
import org.optaplannerdelirium.sss.domain.location.Location;

/**
 * On large datasets, the constructed solution looks like pizza slices.
 */
public class NorthPoleDistanceGiftAssignmentDifficultyWeightFactory
        implements SelectionSorterWeightFactory<ReindeerRoutingSolution, GiftAssignment> {

    public Comparable createSorterWeight(ReindeerRoutingSolution reindeerRoutingSolution, GiftAssignment giftAssignment) {
        return new NorthPoleAngleGiftAssignmentDifficultyWeight(giftAssignment,
                giftAssignment.getLocation().getDistanceTo(Location.NORTH_POLE)
                        + Location.NORTH_POLE.getDistanceTo(giftAssignment.getLocation()),
                giftAssignment.getLocation().getAngle(Location.NORTH_POLE)
        );
    }

    public static class NorthPoleAngleGiftAssignmentDifficultyWeight
            implements Comparable<NorthPoleAngleGiftAssignmentDifficultyWeight> {

        private final GiftAssignment giftAssignment;
        private final double northPoleRoundTripDistance;
        private final double northPoleAngle;

        public NorthPoleAngleGiftAssignmentDifficultyWeight(GiftAssignment giftAssignment,
                double northPoleRoundTripDistance, double northPoleAngle) {
            this.giftAssignment = giftAssignment;
            this.northPoleRoundTripDistance = northPoleRoundTripDistance;
            this.northPoleAngle = northPoleAngle;
        }

        public int compareTo(NorthPoleAngleGiftAssignmentDifficultyWeight other) {
            return new CompareToBuilder()
                    .append(northPoleRoundTripDistance, other.northPoleRoundTripDistance) // Ascending (further from the north pole are more difficult)
                    .append(northPoleAngle, other.northPoleAngle)
                    .append(giftAssignment.getId(), other.giftAssignment.getId())
                    .toComparison();
        }

    }

}
