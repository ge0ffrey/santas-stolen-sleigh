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

package org.optaplannerdelirium.sss.app;

import org.optaplanner.core.api.solver.Solver;
import org.optaplanner.core.api.solver.SolverFactory;
import org.optaplannerdelirium.sss.domain.ReindeerRoutingSolution;
import org.optaplannerdelirium.sss.persistence.ReindeerRoutingExporter;
import org.optaplannerdelirium.sss.persistence.ReindeerRoutingImporter;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ReindeerRoutingHelloWorld {

    public static final String SOLVER_CONFIG
            = "org/optaplannerdelirium/sss/solver/reindeerRoutingSolverConfig.xml";

    private static ExecutorService E = Executors.newFixedThreadPool(1);

    public static void main(String[] args) throws IOException {
        System.setProperty("logback.level.org.optaplanner", "info"); // don't waste time logging too much

        // Build the Solver
        // TODO cli arg
        SolverFactory<ReindeerRoutingSolution> solverFactory = SolverFactory.createFromXmlResource(SOLVER_CONFIG);
        final Solver<ReindeerRoutingSolution> solver = solverFactory.buildSolver();

        // Load the problem
        final ReindeerRoutingSolution unsolvedReindeerRoutingSolution = (ReindeerRoutingSolution) (new ReindeerRoutingImporter().readSolution(new File("data/sss/import/gifts.csv")));

        // Solve the problem in the background
        System.out.println("OptaPlanner is working hard on your problem. Press any key to stop.");
        Runnable solving = new Runnable() {
            @Override
            public void run() {
                solver.solve(unsolvedReindeerRoutingSolution);
            }
        };
        E.submit(solving);

        // wait for the user to terminate
        System.in.read();
        System.out.println("OptaPlanner will now terminate.");

        // terminate on keypress and write the solution
        new ReindeerRoutingExporter().writeSolution(solver.getBestSolution(), new File("data/sss/solved/solution.csv")); // TODO cli arg
        System.out.println("Solution safely stored.");
        solver.terminateEarly();
        E.shutdownNow();
    }

}
