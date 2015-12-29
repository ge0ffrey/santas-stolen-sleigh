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

package org.optaplannerdelirium.sss.solver.nearby;

import org.optaplanner.core.impl.heuristic.selector.common.nearby.NearbyDistanceMeter;
import org.optaplannerdelirium.sss.domain.GiftAssignment;
import org.optaplannerdelirium.sss.domain.Standstill;

public class EuclideanGiftAssignmentNearbyDistanceMeter implements NearbyDistanceMeter<GiftAssignment, Standstill> {

    /**
     * By returning the square of the euclidean distance,
     * the same nearby ordering is exactly the same as the real haversine distance
     * of {@link GiftAssignmentNearbyDistanceMeter},
     * but it's much faster.
     * @param origin never null
     * @param destination never null
     * @return the square of the euclidean distance (not the haversine distance)
     */
    @Override
    public double getNearbyDistance(GiftAssignment origin, Standstill destination) {
        return origin.getLocation().getEuclideanDistanceSquaredTo(destination.getLocation());
    }

}
