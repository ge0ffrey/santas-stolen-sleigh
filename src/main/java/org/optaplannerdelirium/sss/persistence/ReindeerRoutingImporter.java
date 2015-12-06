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

package org.optaplannerdelirium.sss.persistence;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.optaplanner.core.api.domain.solution.Solution;
import org.optaplanner.examples.common.persistence.AbstractTxtSolutionImporter;
import org.optaplannerdelirium.sss.domain.Gift;
import org.optaplannerdelirium.sss.domain.GiftAssignment;
import org.optaplannerdelirium.sss.domain.Reindeer;
import org.optaplannerdelirium.sss.domain.ReindeerRoutingSolution;
import org.optaplannerdelirium.sss.domain.location.Location;
import org.optaplannerdelirium.sss.domain.location.SphereLocation;

public class ReindeerRoutingImporter extends AbstractTxtSolutionImporter {

    public ReindeerRoutingImporter() {
        super(new ReindeerRoutingDao());
    }

    public ReindeerRoutingImporter(boolean withoutDao) {
        super(withoutDao);
    }

    @Override
    public String getInputFileSuffix() {
        return ReindeerRoutingFileIO.FILE_EXTENSION;
    }

    public TxtInputBuilder createTxtInputBuilder() {
        return new ReindeerRoutingInputBuilder();
    }

    public static class ReindeerRoutingInputBuilder extends TxtInputBuilder {

        private static final BigDecimal WEIGHT_MULTIPLIER = new BigDecimal(Gift.WEIGHT_MULTIPLIER);
        private ReindeerRoutingSolution solution;

        private int giftListSize;

        public Solution readSolution() throws IOException {
            solution = new ReindeerRoutingSolution();
            solution.setId(0L);
            readGiftList();
            createReindeerList();
            createGiftAssignmentList();

            logger.info("ReindeerRoutingSolution {} has {} reindeers and {} gift.",
                    getInputId(),
                    solution.getReindeerList().size(),
                    solution.getGiftAssignmentList().size());
            return solution;
        }

        private void readGiftList() throws IOException {
            giftListSize = readIntegerValue("Gifts *:");
            readConstantLine("GiftId,Latitude,Longitude,Weight");
            List<Gift> giftList = new ArrayList<Gift>(giftListSize);
            for (int i = 0; i < giftListSize; i++) {
                String line = bufferedReader.readLine();
                String[] lineTokens = splitBy(line, "\\,", ",", 4, true, false);
                Gift gift = new Gift();
                gift.setId(Long.parseLong(lineTokens[0]));
                Location location = new SphereLocation();
                location.setId(gift.getId());
                location.setLatitude(Double.parseDouble(lineTokens[1]));
                location.setLongitude(Double.parseDouble(lineTokens[2]));
                gift.setLocation(location);
                gift.setWeight(new BigDecimal(lineTokens[3]).multiply(WEIGHT_MULTIPLIER).longValue());
                giftList.add(gift);
            }
            solution.setGiftList(giftList);
        }

        private void createReindeerList() {
            int reindeerListSize = 1000;
            List<Reindeer> reindeerList = new ArrayList<Reindeer>(reindeerListSize);
            long id = 0;
            for (int i = 0; i < reindeerListSize; i++) {
                Reindeer reindeer = new Reindeer();
                reindeer.setId(id);
                id++;
                reindeer.setStartingLocation(Location.NORTH_POLE);
                reindeerList.add(reindeer);
            }
            solution.setReindeerList(reindeerList);
        }

        private void createGiftAssignmentList() {
            List<Gift> giftList = solution.getGiftList();
            List<GiftAssignment> giftAssignmentList = new ArrayList<GiftAssignment>(giftList.size());
            for (Gift gift : giftList) {
                GiftAssignment giftAssignment = new GiftAssignment();
                giftAssignment.setId(gift.getId());
                giftAssignment.setGift(gift);
                giftAssignmentList.add(giftAssignment);
            }
            solution.setGiftAssignmentList(giftAssignmentList);
        }
        
    }

}
