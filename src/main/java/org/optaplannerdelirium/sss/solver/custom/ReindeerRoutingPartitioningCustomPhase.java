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
import org.optaplanner.core.impl.phase.custom.CustomPhaseCommand;
import org.optaplanner.core.impl.score.director.InnerScoreDirector;
import org.optaplanner.core.impl.score.director.ScoreDirector;
import org.optaplanner.examples.vehiclerouting.domain.Customer;
import org.optaplanner.examples.vehiclerouting.domain.Standstill;
import org.optaplanner.examples.vehiclerouting.domain.Vehicle;
import org.optaplanner.examples.vehiclerouting.domain.VehicleRoutingSolution;
import org.optaplannerdelirium.sss.domain.Gift;
import org.optaplannerdelirium.sss.domain.GiftAssignment;
import org.optaplannerdelirium.sss.domain.ReindeerRoutingSolution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReindeerRoutingPartitioningCustomPhase implements CustomPhaseCommand {

    private SolverFactory<ReindeerRoutingSolution> solverFactory;
    private ExecutorService executorService;

    public void changeWorkingSolution(ScoreDirector scoreDirector) {
        solverFactory = SolverFactory.createFromXmlResource("org/optaplannerdelirium/sss/solver/partitionReindeerRoutingSolverConfig.xml");
        executorService = Executors.newFixedThreadPool(3);
        ReindeerRoutingSolution originalSolution = (ReindeerRoutingSolution) scoreDirector.getWorkingSolution();

        ReindeerRoutingSolution cloneSolution = (ReindeerRoutingSolution) ((InnerScoreDirector) scoreDirector).cloneSolution(originalSolution);
        int giftSize = cloneSolution.getGiftList().size();
        int partitionListSize = giftSize >= 1000 ? 20 : 4;
        if (giftSize % partitionListSize != 0) {
            throw new IllegalStateException();
        }
        List<Future<ReindeerRoutingSolution>> futureList = new ArrayList<Future<ReindeerRoutingSolution>>(partitionListSize);
        for (int i = 0; i < partitionListSize; i++) {
            ReindeerRoutingSolution partitionSolution = new ReindeerRoutingSolution();
            partitionSolution.setId(cloneSolution.getId());
            int giftSubSize = giftSize / partitionListSize;
            partitionSolution.setGiftList(cloneSolution.getGiftList().subList(giftSubSize * i, giftSubSize * (i + 1)));
            int reindeerSubSize = cloneSolution.getReindeerList().size() / partitionListSize;
            partitionSolution.setReindeerList(cloneSolution.getReindeerList().subList(reindeerSubSize * i, reindeerSubSize * (i + 1)));
            partitionSolution.setGiftAssignmentList(cloneSolution.getGiftAssignmentList().subList(giftSubSize * i, giftSubSize * (i + 1)));
            ReindeerRoutingPartitionRunner runner = new ReindeerRoutingPartitionRunner(partitionSolution);
            Future<ReindeerRoutingSolution> future = executorService.submit(runner);
            futureList.add(future);
        }
        List<GiftAssignment> giftAssignmentList = originalSolution.getGiftAssignmentList();
        Map<Long, GiftAssignment> giftAssignmentMap = new HashMap<Long, GiftAssignment>(giftAssignmentList.size());
        for (GiftAssignment giftAssignment : giftAssignmentList) {
            giftAssignmentMap.put(giftAssignment.getId(), giftAssignment);
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
                GiftAssignment originalGiftAssignment = giftAssignmentMap.get(partitionGiftAssignment.getId());
                scoreDirector.beforeVariableChanged(originalGiftAssignment, "previousStandstill");
                originalGiftAssignment.setPreviousStandstill(partitionGiftAssignment.getPreviousStandstill());
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
