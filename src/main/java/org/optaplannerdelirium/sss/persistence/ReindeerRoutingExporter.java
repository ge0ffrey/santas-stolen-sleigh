/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
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

package org.optaplannerdelirium.sss.persistence;

import java.io.IOException;

import org.optaplanner.core.api.domain.solution.Solution;
import org.optaplanner.examples.common.persistence.AbstractTxtSolutionExporter;
import org.optaplannerdelirium.sss.domain.GiftAssignment;
import org.optaplannerdelirium.sss.domain.Reindeer;
import org.optaplannerdelirium.sss.domain.ReindeerRoutingSolution;

public class ReindeerRoutingExporter extends AbstractTxtSolutionExporter {

    public static final String OUTPUT_FILE_SUFFIX = "csv";

    public static void main(String[] args) {
        new ReindeerRoutingExporter().convertAll();
    }

    public ReindeerRoutingExporter() {
        super(new ReindeerRoutingDao());
    }

    @Override
    public String getOutputFileSuffix() {
        return OUTPUT_FILE_SUFFIX;
    }

    public TxtOutputBuilder createTxtOutputBuilder() {
        return new ReindeerRoutingOutputBuilder();
    }

    public static class ReindeerRoutingOutputBuilder extends TxtOutputBuilder {

        private ReindeerRoutingSolution solution;

        public void setSolution(Solution solution) {
            this.solution = (ReindeerRoutingSolution) solution;
        }

        public void writeSolution() throws IOException {
            bufferedWriter.write("GiftId,TripId" + "\n");
            for (Reindeer reindeer : solution.getReindeerList()) {
                for (GiftAssignment giftAssignment = reindeer.getNextGiftAssignment();
                        giftAssignment != null;
                        giftAssignment = giftAssignment.getNextGiftAssignment()) {
                    bufferedWriter.write(giftAssignment.getId() + "," + reindeer.getId() + "\n");
                }
            }
        }
    }

}
