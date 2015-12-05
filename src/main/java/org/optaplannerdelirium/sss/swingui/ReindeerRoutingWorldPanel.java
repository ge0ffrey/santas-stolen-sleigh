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

import java.awt.Graphics;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;

import org.optaplannerdelirium.sss.domain.ReindeerRoutingSolution;

public class ReindeerRoutingWorldPanel extends JPanel {

    private final ReindeerRoutingPanel reindeerRoutingPanel;

    private ReindeerRoutingSolutionPainter solutionPainter = new ReindeerRoutingSolutionPainter();

    public ReindeerRoutingWorldPanel(ReindeerRoutingPanel reindeerRoutingPanel) {
        this.reindeerRoutingPanel = reindeerRoutingPanel;
        solutionPainter = new ReindeerRoutingSolutionPainter();
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                // TODO Not thread-safe during solving
                ReindeerRoutingSolution solution = ReindeerRoutingWorldPanel.this.reindeerRoutingPanel.getReindeerRoutingSolution();
                if (solution != null) {
                    resetPanel(solution);
                }
            }
        });
    }

    public void resetPanel(ReindeerRoutingSolution solution) {
        solutionPainter.reset(solution, getSize(), this);
        repaint();
    }

    public void updatePanel(ReindeerRoutingSolution solution) {
        resetPanel(solution);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        BufferedImage canvas = solutionPainter.getCanvas();
        if (canvas != null) {
            g.drawImage(canvas, 0, 0, this);
        }
    }

}
