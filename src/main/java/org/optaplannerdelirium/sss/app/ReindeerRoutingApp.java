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
import org.optaplanner.examples.common.app.CommonApp;
import org.optaplanner.examples.common.persistence.AbstractSolutionImporter;
import org.optaplanner.examples.common.persistence.SolutionDao;
import org.optaplanner.examples.common.swingui.SolutionPanel;
import org.optaplannerdelirium.sss.domain.ReindeerRoutingSolution;
import org.optaplannerdelirium.sss.persistence.ReindeerRoutingDao;
import org.optaplannerdelirium.sss.persistence.ReindeerRoutingImporter;
import org.optaplannerdelirium.sss.swingui.ReindeerRoutingPanel;

public class ReindeerRoutingApp extends CommonApp<ReindeerRoutingSolution> {

    public static final String SOLVER_CONFIG
            = "org/optaplannerdelirium/sss/solver/reindeerRoutingSolverConfig.xml";

    public static void main(String[] args) {
        prepareSwingEnvironment();
        new ReindeerRoutingApp().init();
    }

    public ReindeerRoutingApp() {
        super("Reindeer routing",
                "Santa's stolen sleigh competition on kaggle.", SOLVER_CONFIG, null);
    }

    @Override
    protected SolutionPanel createSolutionPanel() {
        return new ReindeerRoutingPanel();
    }

    @Override
    protected SolutionDao createSolutionDao() {
        return new ReindeerRoutingDao();
    }

    @Override
    protected AbstractSolutionImporter[] createSolutionImporters() {
        return new AbstractSolutionImporter[]{
                new ReindeerRoutingImporter()
        };
    }

}
