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

/**
 * Alternative meter. Don't work that well.
 */
public class LongitudeGiftAssignmentNearbyDistanceMeter implements NearbyDistanceMeter<GiftAssignment, Standstill> {

    @Override
    public double getNearbyDistance(GiftAssignment origin, Standstill destination) {
        // TODO the world is a sphere:
        // TODO the left and right edge of the map connect
        // TODO longitude at the equator is longer than at the poles.
        return Math.abs(origin.getLocation().getLongitude() - destination.getLocation().getLongitude());
    }

}
