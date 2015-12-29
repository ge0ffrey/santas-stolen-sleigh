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

    private static final double RADIAN_CONVERSION = Math.PI / 180.0;
    private static final int EARTH_R_IN_KM = 6371;
    private static final int TWICE_EARTH_R_IN_KM = 2 * EARTH_R_IN_KM;

    private double cartesianX;
    private double cartesianY;
    private double cartesianZ;

    public SphereLocation() {
        super();
    }

    public SphereLocation(long id, double latitude, double longitude) {
        super(id, latitude, longitude);
        updateCache();
    }

    public void updateCache() {
        final double latitudeInRads  = RADIAN_CONVERSION * latitude;
        final double longitudeInRads = RADIAN_CONVERSION * longitude;
        // Cartesian coordinates, normalized for a sphere of diameter 1.0
        this.cartesianX = 0.5 * Math.cos(latitudeInRads) * Math.sin(longitudeInRads);
        this.cartesianY = 0.5 * Math.cos(latitudeInRads) * Math.cos(longitudeInRads);
        this.cartesianZ = 0.5 * Math.sin(latitudeInRads);
    }

    @Override
    public double getDistanceTo(final Location o) {
        if (o == this) {
            return 0.0;
        }
        double r = Math.sqrt(getEuclideanDistanceSquaredTo(o));
        return TWICE_EARTH_R_IN_KM * Math.asin(r);
    }

    @Override
    public double getEuclideanDistanceSquaredTo(Location o) {
        if (o == this) {
            return 0.0;
        }
        SphereLocation other = (SphereLocation) o;
        double dX = cartesianX - other.cartesianX;
        double dY = cartesianY - other.cartesianY;
        double dZ = cartesianZ - other.cartesianZ;
        return dX * dX + dY * dY + dZ * dZ;
    }

}
