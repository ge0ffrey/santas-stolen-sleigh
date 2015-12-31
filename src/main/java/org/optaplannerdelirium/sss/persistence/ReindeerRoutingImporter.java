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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.optaplanner.core.api.domain.solution.Solution;
import org.optaplanner.examples.common.persistence.AbstractTxtSolutionImporter;
import org.optaplannerdelirium.sss.domain.Gift;
import org.optaplannerdelirium.sss.domain.GiftAssignment;
import org.optaplannerdelirium.sss.domain.Reindeer;
import org.optaplannerdelirium.sss.domain.ReindeerRoutingSolution;
import org.optaplannerdelirium.sss.domain.Standstill;
import org.optaplannerdelirium.sss.domain.location.Location;
import org.optaplannerdelirium.sss.domain.location.SphereLocation;
import org.optaplannerdelirium.sss.solver.score.ReindeerRoutingCostCalculator;

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

        private ReindeerRoutingSolution solution;
        private boolean initialized;
        private BufferedReader initializedBufferedReader;

        private int giftListSize;

        public Solution readSolution() throws IOException {
            solution = new ReindeerRoutingSolution();
            solution.setId(0L);
            bufferedReader.mark(1024);
            initialized = bufferedReader.readLine().trim().equals("GiftId,TripId");
            bufferedReader.reset();
            if (initialized) {
                initializedBufferedReader = bufferedReader;
                String solutionFileName = inputFile.getName();
                if (!solutionFileName.matches("gifts[^\\.]*\\..+\\.csv")) {
                    throw new IllegalArgumentException("Cannot deduce problem file from solution file ("
                            + solutionFileName + ") because the solution file does not follow"
                            + " the pattern <problemFile>.<solutionInfo>.csv");
                }
                File unsolvedInputFile = new File(inputFile.getParentFile(),
                        solutionFileName.replaceAll("(gifts[^\\.]*)\\..+\\.csv", "$1.csv"));
                bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(unsolvedInputFile), "UTF-8"));
            } else {
                initializedBufferedReader = null;
            }
            readGiftList();
            createReindeerList();
            createGiftAssignmentList();
            if (initialized) {
                bufferedReader = initializedBufferedReader;
                readGiftAssignmentList();
            }

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
                SphereLocation location = new SphereLocation();
                location.setId(gift.getId());
                if (location.getId().equals(Location.NORTH_POLE.getId())) {
                    throw new IllegalStateException("The location (" + location + ") uses an id (" + location.getId()
                            + ") which is already reserved for the north pole location (" + Location.NORTH_POLE + ").");
                }
                location.setLatitude(Double.parseDouble(lineTokens[1]));
                location.setLongitude(Double.parseDouble(lineTokens[2]));
                location.updateCache();
                gift.setLocation(location);
                gift.setWeight(ReindeerRoutingCostCalculator.parseWeight(lineTokens[3]));
                giftList.add(gift);
            }
            solution.setGiftList(giftList);
        }

        private void createReindeerList() {
            int reindeerListSize = giftListSize / 10;
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

        private void readGiftAssignmentList() throws IOException {
            readConstantLine("GiftId,TripId");
            Map<Long, Standstill> standstillMap = new HashMap<Long, Standstill>(giftListSize * 11 / 10);
            for (Reindeer reindeer : solution.getReindeerList()) {
                standstillMap.put(- reindeer.getId(), reindeer);
            }
            for (GiftAssignment giftAssignment : solution.getGiftAssignmentList()) {
                standstillMap.put(giftAssignment.getId(), giftAssignment);
            }
            List<GiftAssignment> reversingGiftAssignmentList = new ArrayList<GiftAssignment>(giftListSize);
            for (int i = 0; i < giftListSize; i++) {
                String line = bufferedReader.readLine();
                String[] lineTokens = splitBy(line, "\\,", ",", 2, true, false);
                GiftAssignment giftAssignment = (GiftAssignment) standstillMap.get(Long.parseLong(lineTokens[0]));
                if (giftAssignment == null) {
                    throw new IllegalStateException("No standstill with id (" + lineTokens[0] + ").");
                }
                Reindeer reindeer = (Reindeer) standstillMap.get(-Long.parseLong(lineTokens[1]));
                if (reindeer == null) {
                    throw new IllegalStateException("No standstill with id (" + lineTokens[1] + ").");
                }
                giftAssignment.setReindeer(reindeer);
                reversingGiftAssignmentList.add(giftAssignment);
            }
            Collections.reverse(reversingGiftAssignmentList);
            Reindeer previousReindeer = null;
            GiftAssignment previousGiftAssignment = null;
            for (GiftAssignment giftAssignment : reversingGiftAssignmentList) {
                Reindeer reindeer = giftAssignment.getReindeer();
                Standstill previousStandstill = (reindeer != previousReindeer) ? reindeer : previousGiftAssignment;
                giftAssignment.setPreviousStandstill(previousStandstill);
                previousStandstill.setNextGiftAssignment(giftAssignment);
                giftAssignment.setTransportationWeight(previousStandstill.getTransportationWeight() + giftAssignment.getGiftWeight());
                previousGiftAssignment = giftAssignment;
                previousReindeer = reindeer;
            }
            for (Reindeer reindeer : solution.getReindeerList()) {
                reindeer.setTransportationToNextPenalty(
                        ReindeerRoutingCostCalculator.multiplyWeightAndDistance(reindeer.getTransportationWeight(),
                                reindeer.getDistanceToNextGiftAssignmentOrReindeer()));
            }
            for (GiftAssignment giftAssignment : solution.getGiftAssignmentList()) {
                giftAssignment.setTransportationToNextPenalty(
                        ReindeerRoutingCostCalculator.multiplyWeightAndDistance(giftAssignment.getTransportationWeight(),
                                giftAssignment.getDistanceToNextGiftAssignmentOrReindeer()));
            }
        }
        
    }

}
