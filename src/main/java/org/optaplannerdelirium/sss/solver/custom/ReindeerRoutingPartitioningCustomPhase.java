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
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.optaplanner.core.api.domain.solution.Solution;
import org.optaplanner.core.api.score.Score;
import org.optaplanner.core.api.solver.Solver;
import org.optaplanner.core.api.solver.SolverFactory;
import org.optaplanner.core.config.heuristic.selector.common.decorator.SelectionSorterOrder;
import org.optaplanner.core.impl.heuristic.selector.common.decorator.WeightFactorySelectionSorter;
import org.optaplanner.core.impl.phase.custom.AbstractCustomPhaseCommand;
import org.optaplanner.core.impl.phase.custom.CustomPhaseCommand;
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

    private final String PARTITION_SOLVER_CONFIG_RESOURCE = "partitionSolverConfigResource";

    private SolverFactory<ReindeerRoutingSolution> solverFactory;

    public ReindeerRoutingPartitioningCustomPhase() {
    }

    @Override
    public void applyCustomProperties(Map<String, String> customPropertyMap) {
        String partitionSolverConfigResource = customPropertyMap.get(PARTITION_SOLVER_CONFIG_RESOURCE);
        if (partitionSolverConfigResource == null) {
            throw new IllegalArgumentException("A customProperty (" + PARTITION_SOLVER_CONFIG_RESOURCE
                    + ") is missing from the solver configuration.");
        }
        if (customPropertyMap.size() != 1) {
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
        int partitionListSize = giftSize >= 1000 ? 100 : 4;
        if (giftSize % partitionListSize != 0) {
            throw new IllegalStateException();
        }
        List<Future<ReindeerRoutingSolution>> futureList = new ArrayList<Future<ReindeerRoutingSolution>>(partitionListSize);
        for (int i = 0; i < partitionListSize; i++) {
            ReindeerRoutingSolution partitionSolution = new ReindeerRoutingSolution();
            partitionSolution.setId(cloneSolution.getId());
            int giftSubSize = giftSize / partitionListSize;
            List<GiftAssignment> partitionGiftAssignmentList = cloneSolution.getGiftAssignmentList().subList(giftSubSize * i, giftSubSize * (i + 1));
            List<Gift> partitionGiftList = new ArrayList<Gift>(giftSubSize);
            for (GiftAssignment partitionGiftAssignment : partitionGiftAssignmentList) {
                partitionGiftList.add(partitionGiftAssignment.getGift());
            }
            partitionSolution.setGiftList(partitionGiftList);
            int reindeerSubSize = cloneSolution.getReindeerList().size() / partitionListSize;
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
