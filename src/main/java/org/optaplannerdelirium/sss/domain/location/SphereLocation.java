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

import com.thoughtworks.xstream.annotations.XStreamAlias;

import java.util.HashMap;
import java.util.Map;

@XStreamAlias("SphereLocation")
public class SphereLocation extends Location {

    private static final double EARTH_R_IN_KM = 6371;

    private static double getDistanceToUncached(Location first, Location other) {
        double latitudeDiff = Math.toRadians(other.getLatitude() - first.getLatitude());
        double longitudeDiff = Math.toRadians(other.getLongitude() - first.getLongitude());
        double a = Math.pow(Math.sin(latitudeDiff / 2), 2)
                + Math.pow(Math.sin(longitudeDiff / 2), 2)
                * Math.cos(Math.toRadians(first.getLatitude())) * Math.cos(Math.toRadians(other.getLatitude()));
        double c = 2 * Math.asin(Math.sqrt(a));
        return EARTH_R_IN_KM * c;
    }

    private static Map<Long, Map<Long, Double>> locationCache = new HashMap<Long, Map<Long, Double>>();

    public SphereLocation() {
    }

    public SphereLocation(long id, double latitude, double longitude) {
        super(id, latitude, longitude);
    }

    @Override
    public double getDistanceTo(Location other) {
        if (this == other) {
            return 0; // no need to cache this
        }
        // this should guarantee that no pair will be in the map twice
        final long firstId = Math.min(this.getId(), other.getId());
        final long secondId = Math.max(this.getId(), other.getId());
        synchronized (locationCache) {
            Map<Long, Double> cache = locationCache.get(firstId);
            if (cache == null) { // create mapping for the first key
                cache = new HashMap<Long, Double>();
                locationCache.put(firstId, cache);
            } else {
                final Double result = cache.get(secondId);
                if (result != null) { // the distance is cached already
                    return result;
                }
            }
            // cache the distance
            final double result = getDistanceToUncached(this, other);
            cache.put(secondId, result);
            return result;
        }
    }

}
