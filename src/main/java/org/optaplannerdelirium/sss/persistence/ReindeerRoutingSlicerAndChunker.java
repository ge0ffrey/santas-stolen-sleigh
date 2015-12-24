package org.optaplannerdelirium.sss.persistence;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.optaplanner.core.api.solver.SolverFactory;
import org.optaplanner.core.config.heuristic.selector.common.decorator.SelectionSorterOrder;
import org.optaplanner.core.impl.heuristic.selector.common.decorator.WeightFactorySelectionSorter;
import org.optaplanner.core.impl.score.director.InnerScoreDirector;
import org.optaplanner.core.impl.score.director.ScoreDirector;
import org.optaplannerdelirium.sss.app.ReindeerRoutingApp;
import org.optaplannerdelirium.sss.domain.Gift;
import org.optaplannerdelirium.sss.domain.GiftAssignment;
import org.optaplannerdelirium.sss.domain.ReindeerRoutingSolution;
import org.optaplannerdelirium.sss.domain.location.Location;
import org.optaplannerdelirium.sss.domain.solver.NorthPoleAngleGiftAssignmentDifficultyWeightFactory;
import org.optaplannerdelirium.sss.solver.score.ReindeerRoutingCostCalculator;

public class ReindeerRoutingSlicerAndChunker {

    public static void main(String[] args) {
        new ReindeerRoutingSlicerAndChunker().sliceAndChunk();
    }

    private ReindeerRoutingImporter importer;

    public ReindeerRoutingSlicerAndChunker() {
        importer = new ReindeerRoutingImporter();
    }

    public void sliceAndChunk() {
        sliceOrChunk(20, "slice");
        sliceOrChunk(5, "chunk");
    }

    private void sliceOrChunk( int partitionCount, String sliceOrChunk) {
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
            writeSolution(partitionSolution, outputFile);
        }
    }

    private void writeSolution(ReindeerRoutingSolution solution, File outputFile) {
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
