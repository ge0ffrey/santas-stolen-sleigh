/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
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

package org.optaplannerdelirium.sss.solver.custom;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.optaplanner.core.api.solver.Solver;
import org.optaplanner.core.api.solver.SolverFactory;
import org.optaplanner.core.config.heuristic.selector.common.decorator.SelectionSorterOrder;
import org.optaplanner.core.impl.heuristic.selector.common.decorator.WeightFactorySelectionSorter;
import org.optaplanner.core.impl.phase.custom.AbstractCustomPhaseCommand;
import org.optaplanner.core.impl.score.director.InnerScoreDirector;
import org.optaplanner.core.impl.score.director.ScoreDirector;
import org.optaplannerdelirium.sss.domain.Gift;
import org.optaplannerdelirium.sss.domain.GiftAssignment;
import org.optaplannerdelirium.sss.domain.Reindeer;
import org.optaplannerdelirium.sss.domain.ReindeerRoutingSolution;
import org.optaplannerdelirium.sss.domain.Standstill;
import org.optaplannerdelirium.sss.domain.solver.NorthPoleAngleGiftAssignmentDifficultyWeightFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReindeerRoutingPartitioningCustomPhase  extends AbstractCustomPhaseCommand {

    private final String PARTITION_SOLVER_CONFIG_RESOURCE_PROPERTY = "partitionSolverConfigResource";
    private final String PARTITION_COUNT_PROPERTY = "partitionCount";

    protected final transient Logger logger = LoggerFactory.getLogger(getClass());

    private SolverFactory<ReindeerRoutingSolution> solverFactory;
    private int partitionCount;

    public ReindeerRoutingPartitioningCustomPhase() {
    }

    @Override
    public void applyCustomProperties(Map<String, String> customPropertyMap) {
        String partitionSolverConfigResource = customPropertyMap.get(PARTITION_SOLVER_CONFIG_RESOURCE_PROPERTY);
        if (partitionSolverConfigResource == null) {
            throw new IllegalArgumentException("A customProperty (" + PARTITION_SOLVER_CONFIG_RESOURCE_PROPERTY
                    + ") is missing from the solver configuration.");
        }
        String partitionCountString = customPropertyMap.get(PARTITION_COUNT_PROPERTY);
        if (partitionCountString == null) {
            throw new IllegalArgumentException("A customProperty (" + PARTITION_COUNT_PROPERTY
                    + ") is missing from the solver configuration.");
        }
        try {
            partitionCount = Integer.parseInt(partitionCountString);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("The customProperty (" + PARTITION_COUNT_PROPERTY
                    + ")'s value (" + partitionCount + ") is not a valid int.", e);
        }
        if (customPropertyMap.size() != 2) {
            throw new IllegalArgumentException("The customPropertyMap's size (" + customPropertyMap.size()
                    + ") is not 1.");
        }
        solverFactory = SolverFactory.createFromXmlResource(partitionSolverConfigResource);
    }

    public void changeWorkingSolution(ScoreDirector scoreDirector) {
        int availableProcessorCount = Runtime.getRuntime().availableProcessors();
        ExecutorService executorService = Executors.newFixedThreadPool(Math.max(availableProcessorCount - 2, 1));
        ReindeerRoutingSolution originalSolution = (ReindeerRoutingSolution) scoreDirector.getWorkingSolution();

        ReindeerRoutingSolution cloneSolution = (ReindeerRoutingSolution) ((InnerScoreDirector) scoreDirector).cloneSolution(originalSolution);
        WeightFactorySelectionSorter<ReindeerRoutingSolution, GiftAssignment> sorter = new WeightFactorySelectionSorter<ReindeerRoutingSolution, GiftAssignment>(
                new NorthPoleAngleGiftAssignmentDifficultyWeightFactory(), SelectionSorterOrder.ASCENDING);
        sorter.sort(cloneSolution, cloneSolution.getGiftAssignmentList());
        int giftSize = cloneSolution.getGiftList().size();
        if (giftSize < 1000) {
            logger.warn("Partition count automatically lowered from " + partitionCount + " to 4.");
            partitionCount = 4;
        }
        if (giftSize % partitionCount != 0) {
            throw new IllegalStateException();
        }
        List<Future<ReindeerRoutingSolution>> futureList = new ArrayList<Future<ReindeerRoutingSolution>>(partitionCount);
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
            ReindeerRoutingPartitionRunner runner = new ReindeerRoutingPartitionRunner(partitionSolution);
            Future<ReindeerRoutingSolution> future = executorService.submit(runner);
            futureList.add(future);
        }
        List<Reindeer> reindeerList = originalSolution.getReindeerList();
        List<GiftAssignment> giftAssignmentList = originalSolution.getGiftAssignmentList();
        Map<Long, Standstill> standstillMap = new HashMap<Long, Standstill>(reindeerList.size() + giftAssignmentList.size());
        for (Reindeer reindeer : reindeerList) {
            standstillMap.put(-reindeer.getId(), reindeer);
        }
        for (GiftAssignment giftAssignment : giftAssignmentList) {
            standstillMap.put(giftAssignment.getId(), giftAssignment);
        }
        for (Future<ReindeerRoutingSolution> future : futureList) {
            ReindeerRoutingSolution partitionSolution;
            try {
                partitionSolution = future.get();
            } catch (InterruptedException e) {
                throw new IllegalStateException(e);
            } catch (ExecutionException e) {
                throw new IllegalStateException(e);
            }
            for (GiftAssignment partitionGiftAssignment : partitionSolution.getGiftAssignmentList()) {
                GiftAssignment originalGiftAssignment = (GiftAssignment) standstillMap.get(partitionGiftAssignment.getId());
                Standstill previousStandstill = partitionGiftAssignment.getPreviousStandstill();
                Standstill originalPreviousStandstill = standstillMap.get(previousStandstill instanceof GiftAssignment
                        ? ((GiftAssignment) previousStandstill).getId() : - ((Reindeer) previousStandstill).getId());
                scoreDirector.beforeVariableChanged(originalGiftAssignment, "previousStandstill");
                originalGiftAssignment.setPreviousStandstill(originalPreviousStandstill);
                scoreDirector.afterVariableChanged(originalGiftAssignment, "previousStandstill");
            }
        }
        scoreDirector.triggerVariableListeners();
    }

    public class ReindeerRoutingPartitionRunner implements Callable<ReindeerRoutingSolution> {

        private ReindeerRoutingSolution solution;

        public ReindeerRoutingPartitionRunner(ReindeerRoutingSolution solution) {
            this.solution = solution;
        }

        @Override
        public ReindeerRoutingSolution call() {
            Solver<ReindeerRoutingSolution> solver = solverFactory.buildSolver();
            return solver.solve(solution);
        }

    }

}
