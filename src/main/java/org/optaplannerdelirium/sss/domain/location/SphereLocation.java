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

@XStreamAlias("SphereLocation")
public class SphereLocation extends Location {

    private static final double EARTH_R_IN_KM = 6372.8;

    public SphereLocation() {
    }

    public SphereLocation(long id, double latitude, double longitude) {
        super(id, latitude, longitude);
    }

    @Override
    public double getDistanceTo(Location other) {
        double latitudeDiff = Math.toRadians(other.getLatitude() - latitude);
        double longitudeDiff = Math.toRadians(other.getLongitude() - longitude);
        double a = Math.pow(Math.sin(latitudeDiff / 2), 2)
                + Math.pow(Math.sin(longitudeDiff / 2), 2)
                * Math.cos(Math.toRadians(latitude)) * Math.cos(Math.toRadians(other.getLatitude()));
        double c = 2 * Math.asin(Math.sqrt(a));
        return EARTH_R_IN_KM * c;
    }

}
