/*
 * Copyright 2014 JBoss Inc
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

package org.optaplannerdelirium.sss.domain.location;

import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("CachingSphereLocation")
public class CachingSphereLocation extends SphereLocation {

    private double[] distanceToMap; // index is location.getId()

    public CachingSphereLocation() {
        super();
    }

    public CachingSphereLocation(long id, double latitude, double longitude) {
        super(id, latitude, longitude);
    }

    public void createDistanceToMap(List<CachingSphereLocation> locationList) {
        distanceToMap = new double[locationList.size()];
        for (CachingSphereLocation other : locationList) {
            distanceToMap[other.getId().intValue()] = getHaversineDistanceTo(other);
        }
    }

    @Override
    public double getDistanceTo(final Location o) {
        return distanceToMap[o.getId().intValue()];
    }

}
