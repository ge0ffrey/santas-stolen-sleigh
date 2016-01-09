/*
 * Copyright 2015 JBoss Inc
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
import java.util.Iterator;
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

public class BigGiftInitializer extends AbstractCustomPhaseCommand {

    public void changeWorkingSolution(ScoreDirector scoreDirector) {
        ReindeerRoutingSolution originalSolution = (ReindeerRoutingSolution) scoreDirector.getWorkingSolution();
        List<Reindeer> reindeerList = originalSolution.getReindeerList();
        Iterator<Reindeer> reindeerIterator = reindeerList.iterator();
        for (GiftAssignment giftAssignment : originalSolution.getGiftAssignmentList()) {
            if (giftAssignment.getGift().isBigGift() && giftAssignment.getPreviousStandstill() == null) {
                if (!reindeerIterator.hasNext()) {
                    throw new IllegalStateException("There are more big gifts than there are reindeer ("
                            + reindeerList.size() + ").");
                }
                Reindeer reindeer = reindeerIterator.next();
                scoreDirector.beforeVariableChanged(giftAssignment, "previousStandstill");
                giftAssignment.setPreviousStandstill(reindeer);
                scoreDirector.afterVariableChanged(giftAssignment, "previousStandstill");
            }
        }
        scoreDirector.triggerVariableListeners();
    }

}
