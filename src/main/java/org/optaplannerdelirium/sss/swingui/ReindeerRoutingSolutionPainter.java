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

package org.optaplannerdelirium.sss.swingui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import org.optaplanner.examples.common.swingui.latitudelongitude.LatitudeLongitudeTranslator;
import org.optaplanner.swing.impl.TangoColorFactory;
import org.optaplannerdelirium.sss.domain.GiftAssignment;
import org.optaplannerdelirium.sss.domain.Reindeer;
import org.optaplannerdelirium.sss.domain.ReindeerRoutingSolution;
import org.optaplannerdelirium.sss.domain.location.Location;

public class ReindeerRoutingSolutionPainter {

    private static final int TEXT_SIZE = 12;
    private static final int TIME_WINDOW_DIAMETER = 26;
    private static final NumberFormat NUMBER_FORMAT = new DecimalFormat("#,##0.00");

    private BufferedImage canvas = null;
    private LatitudeLongitudeTranslator translator = null;
    private Long minimumTimeWindowTime = null;
    private Long maximumTimeWindowTime = null;

    public ReindeerRoutingSolutionPainter() {
    }

    public BufferedImage getCanvas() {
        return canvas;
    }

    public LatitudeLongitudeTranslator getTranslator() {
        return translator;
    }

    public void reset(ReindeerRoutingSolution solution, Dimension size, ImageObserver imageObserver) {
        translator = new LatitudeLongitudeTranslator();
        for (Reindeer reindeer : solution.getReindeerList()) {
            Location location = reindeer.getLocation();
            translator.addCoordinates(location.getLatitude(), location.getLongitude());
        }
        for (GiftAssignment giftAssignment : solution.getGiftAssignmentList()) {
            Location location = giftAssignment.getLocation();
            translator.addCoordinates(location.getLatitude(), location.getLongitude());
        }
        double width = size.getWidth();
        double height = size.getHeight();
        translator.prepareFor(width, height - 10 - TEXT_SIZE);

        Graphics2D g = createCanvas(width, height);
        g.setFont(g.getFont().deriveFont((float) TEXT_SIZE));
        g.setStroke(TangoColorFactory.NORMAL_STROKE);
        for (GiftAssignment giftAssignment : solution.getGiftAssignmentList()) {
            Location location = giftAssignment.getLocation();
            int x = translator.translateLongitudeToX(location.getLongitude());
            int y = translator.translateLatitudeToY(location.getLatitude());
            g.setColor(TangoColorFactory.ALUMINIUM_4);
            g.fillRect(x - 1, y - 1, 3, 3);
            String demandString = Long.toString(giftAssignment.getGiftWeight());
            g.drawString(demandString, x - (g.getFontMetrics().stringWidth(demandString) / 2), y - TEXT_SIZE / 2);
        }
        int colorIndex = 0;
        // TODO Too many nested for loops
        for (Reindeer reindeer : solution.getReindeerList()) {
            g.setColor(TangoColorFactory.ALUMINIUM_3);
            int x = translator.translateLongitudeToX(reindeer.getLocation().getLongitude());
            int y = translator.translateLatitudeToY(reindeer.getLocation().getLatitude());
            g.fillRect(x - 2, y - 2, 5, 5);
            g.setColor(TangoColorFactory.SEQUENCE_2[colorIndex]);
            GiftAssignment reindeerInfoGiftAssignment = null;
            long longestNonDepotDistance = -1L;
            long load = 0L;
            for (GiftAssignment giftAssignment : solution.getGiftAssignmentList()) {
                if (giftAssignment.getPreviousStandstill() != null && giftAssignment.getReindeer() == reindeer) {
                    load += giftAssignment.getGiftWeight();
                    Location previousLocation = giftAssignment.getPreviousStandstill().getLocation();
                    Location location = giftAssignment.getLocation();
                    translator.drawRoute(g, previousLocation.getLongitude(), previousLocation.getLatitude(),
                            location.getLongitude(), location.getLatitude(),
                            true, false);
                    // Determine where to draw the reindeer info
                    long distance = giftAssignment.getDistanceFromPreviousStandstill();
                    if (giftAssignment.getPreviousStandstill() instanceof GiftAssignment) {
                        if (longestNonDepotDistance < distance) {
                            longestNonDepotDistance = distance;
                            reindeerInfoGiftAssignment = giftAssignment;
                        }
                    } else if (reindeerInfoGiftAssignment == null) {
                        // If there is only 1 giftAssignment in this chain, draw it on a line to the Depot anyway
                        reindeerInfoGiftAssignment = giftAssignment;
                    }
                    // Line back to the reindeer depot
                    if (giftAssignment.getNextGiftAssignment() == null) {
                        Location reindeerLocation = reindeer.getLocation();
                        translator.drawRoute(g, location.getLongitude(), location.getLatitude(),
                                reindeerLocation.getLongitude(), reindeerLocation.getLatitude(),
                                true, true);
                    }
                }
            }
            // Draw reindeer info
            if (reindeerInfoGiftAssignment != null) {
                Location previousLocation = reindeerInfoGiftAssignment.getPreviousStandstill().getLocation();
                Location location = reindeerInfoGiftAssignment.getLocation();
                double longitude = (previousLocation.getLongitude() + location.getLongitude()) / 2.0;
                int infoX = translator.translateLongitudeToX(longitude);
                double latitude = (previousLocation.getLatitude() + location.getLatitude()) / 2.0;
                int infoY = translator.translateLatitudeToY(latitude);
                boolean ascending = (previousLocation.getLongitude() < location.getLongitude())
                        ^ (previousLocation.getLatitude() < location.getLatitude());

                g.drawString(Long.toString(load),
                        infoX + 1, (ascending ? infoY - 1 : infoY + TEXT_SIZE + 1));
            }
            colorIndex = (colorIndex + 1) % TangoColorFactory.SEQUENCE_2.length;
        }
    }

    public Graphics2D createCanvas(double width, double height) {
        int canvasWidth = (int) Math.ceil(width) + 1;
        int canvasHeight = (int) Math.ceil(height) + 1;
        canvas = new BufferedImage(canvasWidth, canvasHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = canvas.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, canvasWidth, canvasHeight);
        return g;
    }

}
