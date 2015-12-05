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

package org.optaplannerdelirium.sss.swingui;

import java.awt.BorderLayout;
import javax.swing.JTabbedPane;

import org.optaplanner.core.api.domain.solution.Solution;
import org.optaplanner.examples.common.swingui.SolutionPanel;
import org.optaplannerdelirium.sss.domain.ReindeerRoutingSolution;

public class ReindeerRoutingPanel extends SolutionPanel {

    private ReindeerRoutingWorldPanel reindeerRoutingWorldPanel;

    public ReindeerRoutingPanel() {
        setLayout(new BorderLayout());
        JTabbedPane tabbedPane = new JTabbedPane();
        reindeerRoutingWorldPanel = new ReindeerRoutingWorldPanel(this);
        reindeerRoutingWorldPanel.setPreferredSize(PREFERRED_SCROLLABLE_VIEWPORT_SIZE);
        tabbedPane.add("World", reindeerRoutingWorldPanel);
        add(tabbedPane, BorderLayout.CENTER);
    }

    @Override
    public boolean isWrapInScrollPane() {
        return false;
    }

    @Override
    public boolean isRefreshScreenDuringSolving() {
        return true;
    }

    public ReindeerRoutingSolution getReindeerRoutingSolution() {
        return (ReindeerRoutingSolution) solutionBusiness.getSolution();
    }

    public void resetPanel(Solution solutionObject) {
        ReindeerRoutingSolution solution = (ReindeerRoutingSolution) solutionObject;
        reindeerRoutingWorldPanel.resetPanel(solution);
    }

    @Override
    public void updatePanel(Solution solutionObject) {
        ReindeerRoutingSolution solution = (ReindeerRoutingSolution) solutionObject;
        reindeerRoutingWorldPanel.updatePanel(solution);
    }
}
