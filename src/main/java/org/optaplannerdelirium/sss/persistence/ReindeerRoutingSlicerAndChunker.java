package org.optaplannerdelirium.sss.persistence;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.optaplanner.core.api.score.buildin.hardsoftlong.HardSoftLongScore;
import org.optaplanner.core.api.solver.SolverFactory;
import org.optaplanner.core.config.heuristic.selector.common.decorator.SelectionSorterOrder;
import org.optaplanner.core.impl.heuristic.selector.common.decorator.WeightFactorySelectionSorter;
import org.optaplanner.core.impl.score.director.InnerScoreDirector;
import org.optaplanner.core.impl.score.director.ScoreDirector;
import org.optaplanner.core.impl.score.director.ScoreDirectorFactory;
import org.optaplannerdelirium.sss.app.ReindeerRoutingApp;
import org.optaplannerdelirium.sss.domain.Gift;
import org.optaplannerdelirium.sss.domain.GiftAssignment;
import org.optaplannerdelirium.sss.domain.Reindeer;
import org.optaplannerdelirium.sss.domain.ReindeerRoutingSolution;
import org.optaplannerdelirium.sss.domain.Standstill;
import org.optaplannerdelirium.sss.domain.location.Location;
import org.optaplannerdelirium.sss.domain.solver.NorthPoleAngleGiftAssignmentDifficultyWeightFactory;
import org.optaplannerdelirium.sss.solver.score.ReindeerRoutingCostCalculator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReindeerRoutingSlicerAndChunker {

    protected final transient Logger logger = LoggerFactory.getLogger(getClass());

    public static void main(String[] args) {
        if (args.length != 1) {
            throw new IllegalArgumentException("Exactly 1 program argument required, instead of args ("
                    + Arrays.toString(args) + ").");
        }
        SliceOrChunkType type = SliceOrChunkType.valueOf(args[0].toUpperCase());
        new ReindeerRoutingSlicerAndChunker().execute(type);
    }

    public enum SliceOrChunkType {
        SLICE,
        UNSLICE,
        CHUNK,
        UNCHUNK;
    }

    private ReindeerRoutingImporter importer;
    private ReindeerRoutingExporter exporter;

    public ReindeerRoutingSlicerAndChunker() {
        importer = new ReindeerRoutingImporter();
        exporter = new ReindeerRoutingExporter();
    }

    public void execute(SliceOrChunkType type) {
        switch (type) {
            case SLICE:
                split(20, "slice");
                break;
            case UNSLICE:
                unsplit(20, "slice", ".bestScore.csv");
                break;
            case CHUNK:
                split(5, "chunk");
                break;
            case UNCHUNK:
                unsplit(5, "chunk", ".bestScore.csv");
                break;
            default:
                throw new IllegalArgumentException();
        }
    }

    private void split(int partitionCount, String sliceOrChunk) {
        File inputFile = new File(importer.getInputDir(), "gifts.csv");
        ReindeerRoutingSolution originalSolution = (ReindeerRoutingSolution) importer.readSolution(inputFile);
        ScoreDirector scoreDirector = SolverFactory.createFromXmlResource(ReindeerRoutingApp.SOLVER_CONFIG)
                .buildSolver().getScoreDirectorFactory().buildScoreDirector();

        ReindeerRoutingSolution cloneSolution = (ReindeerRoutingSolution) ((InnerScoreDirector) scoreDirector).cloneSolution(originalSolution);
        WeightFactorySelectionSorter<ReindeerRoutingSolution, GiftAssignment> sorter = new WeightFactorySelectionSorter<ReindeerRoutingSolution, GiftAssignment>(
                new NorthPoleAngleGiftAssignmentDifficultyWeightFactory(), SelectionSorterOrder.ASCENDING);
        sorter.sort(cloneSolution, cloneSolution.getGiftAssignmentList());

        int giftSize = cloneSolution.getGiftList().size();
        for (int i = 0; i < partitionCount; i++) {
            ReindeerRoutingSolution partitionSolution = new ReindeerRoutingSolution();
            partitionSolution.setId(cloneSolution.getId());
            int giftSubSize = giftSize / partitionCount;
            List<GiftAssignment> partitionGiftAssignmentList = cloneSolution.getGiftAssignmentList().subList(giftSubSize * i, giftSubSize * (i + 1));
            List<Gift> partitionGiftList = new ArrayList<Gift>(giftSubSize);
            for (GiftAssignment partitionGiftAssignment : partitionGiftAssignmentList) {
                partitionGiftList.add(partitionGiftAssignment.getGift());
            }
            partitionSolution.setGiftList(partitionGiftList);
            int reindeerSubSize = cloneSolution.getReindeerList().size() / partitionCount;
            partitionSolution.setReindeerList(cloneSolution.getReindeerList().subList(reindeerSubSize * i, reindeerSubSize * (i + 1)));
            partitionSolution.setGiftAssignmentList(partitionGiftAssignmentList);
            File outputFile = new File (importer.getInputDir(), sliceOrChunk + "s/gifts_" + sliceOrChunk + i + ".csv");
            writeSolutionButGiftsOnly(partitionSolution, outputFile);
        }
    }

    private void unsplit(int partitionCount, String sliceOrChunk, String suffix) {
        File originalFile = new File(importer.getInputDir(), "gifts.csv");
        ReindeerRoutingSolution originalSolution = (ReindeerRoutingSolution) importer.readSolution(originalFile);
        Map<Long, GiftAssignment> giftAssignmentMap = new HashMap<Long, GiftAssignment>(
                originalSolution.getGiftAssignmentList().size());
        for (GiftAssignment giftAssignment : originalSolution.getGiftAssignmentList()) {
            giftAssignmentMap.put(giftAssignment.getId(),giftAssignment);
        }
        Iterator<Reindeer> originalReindeerIt = originalSolution.getReindeerList().iterator();
        for (int i = 0; i < partitionCount; i++) {
            File inputFile = new File(importer.getInputDir(), sliceOrChunk + "s/gifts_" + sliceOrChunk + i + suffix);
            if (!inputFile.exists()) {
                throw new IllegalArgumentException("The inputFile (" + inputFile + ") does not exist.");
            }
            ReindeerRoutingSolution partitionSolution = (ReindeerRoutingSolution) importer.readSolution(inputFile);
            for (Reindeer partitionReindeer : partitionSolution.getReindeerList()) {
                Reindeer originalReindeer = originalReindeerIt.next();
                Standstill previousStandstill = originalReindeer;
                for (GiftAssignment partitionGiftAssignment = partitionReindeer.getNextGiftAssignment();
                        partitionGiftAssignment != null;
                        partitionGiftAssignment = partitionGiftAssignment.getNextGiftAssignment()) {
                    GiftAssignment originalGiftAssignment = giftAssignmentMap.get(partitionGiftAssignment.getId());
                    originalGiftAssignment.setPreviousStandstill(previousStandstill);
                    previousStandstill.setNextGiftAssignment(originalGiftAssignment);
                    originalGiftAssignment.setReindeer(originalReindeer);
                    originalGiftAssignment.setTransportationWeight(previousStandstill.getTransportationWeight() + originalGiftAssignment.getGiftWeight());
                    previousStandstill = originalGiftAssignment;
                }
            }
        }
        for (Reindeer reindeer : originalSolution.getReindeerList()) {
            reindeer.setTransportationToNextPenalty(
                    ReindeerRoutingCostCalculator.multiplyWeightAndDistance(reindeer.getTransportationWeight(),
                            reindeer.getDistanceToNextGiftAssignmentOrReindeer()));
        }
        for (GiftAssignment giftAssignment : originalSolution.getGiftAssignmentList()) {
            giftAssignment.setTransportationToNextPenalty(
                    ReindeerRoutingCostCalculator.multiplyWeightAndDistance(giftAssignment.getTransportationWeight(),
                            giftAssignment.getDistanceToNextGiftAssignmentOrReindeer()));
        }
        ScoreDirectorFactory scoreDirectorFactory = SolverFactory.createFromXmlResource(ReindeerRoutingApp.SOLVER_CONFIG).buildSolver().getScoreDirectorFactory();
        ScoreDirector scoreDirector = scoreDirectorFactory.buildScoreDirector();
        scoreDirector.setWorkingSolution(originalSolution);
        scoreDirector.calculateScore();
        HardSoftLongScore score = originalSolution.getScore();
        logger.info("Unsplit solution with score ({}).", score);
        String scoreString = !score.isFeasible() ? "infeasible" : Long.toString(score.getSoftScore());
        File outputFile = new File (importer.getInputDir(), "whole/gifts.score" + scoreString + ".csv");
        exporter.writeSolution(originalSolution, outputFile);
    }

    private void writeSolutionButGiftsOnly(ReindeerRoutingSolution solution, File outputFile) {
        BufferedWriter bufferedWriter = null;
        try {
            bufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFile), "UTF-8"));
            bufferedWriter.write("Gifts: " + solution.getGiftAssignmentList().size() + "\n");
            bufferedWriter.write("GiftId,Latitude,Longitude,Weight\n");
            for (GiftAssignment giftAssignment : solution.getGiftAssignmentList()) {
                Location location = giftAssignment.getLocation();
                bufferedWriter.write(giftAssignment.getId() + "," + location.getLatitude() + ","
                        + location.getLongitude() + "," + ReindeerRoutingCostCalculator.formatWeight(giftAssignment.getGiftWeight()) + "\n");
            }
        } catch (IOException e) {
            throw new IllegalArgumentException("Could not write the file (" + outputFile.getName() + ").", e);
        } finally {
            IOUtils.closeQuietly(bufferedWriter);
        }
    }

}
