/*
 * Copyright 2012 JBoss Inc
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
import com.thoughtworks.xstream.annotations.XStreamInclude;
import org.optaplanner.examples.common.domain.AbstractPersistable;

@XStreamAlias("Location")
@XStreamInclude({
        SphereLocation.class
})
public abstract class Location extends AbstractPersistable {

    public static final Location NORTH_POLE = new SphereLocation(-1L, 90.0, 0.0);
    private static double TEMP = Math.PI / 180;

    protected double latitude;
    protected double longitude;
    protected double cartesianX, cartesianY, cartesianZ;

    public Location() {
    }

    public Location(long id, double latitude, double longitude) {
        super(id);
        this.latitude = latitude;
        this.longitude = longitude;
        final double latitudeInRads  = TEMP * latitude;
        final double longitudeInRads = TEMP * longitude;

        // Cartesian coordinates, normalized for a sphere of diameter 1.0
        this.cartesianX = 0.5 * Math.cos(latitudeInRads) * Math.sin(longitudeInRads);
        this.cartesianY = 0.5 * Math.cos(latitudeInRads) * Math.cos(longitudeInRads);
        this.cartesianZ = 0.5 * Math.sin(latitudeInRads);
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    // ************************************************************************
    // Complex methods
    // ************************************************************************

    public abstract double getDistanceTo(Location location);

    /**
     * The angle relative to the direction EAST.
     * @param location never null
     * @return in Cartesian coordinates
     */
    public double getAngle(Location location) {
        // Euclidean distance (Pythagorean theorem) - not correct when the surface is a sphere
        double latitudeDifference = location.latitude - latitude;
        double longitudeDifference = location.longitude - longitude;
        return Math.atan2(latitudeDifference, longitudeDifference);
    }

}
