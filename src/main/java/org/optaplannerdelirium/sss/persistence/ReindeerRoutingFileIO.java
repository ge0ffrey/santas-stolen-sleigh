/*
 * Copyright 2011 JBoss Inc
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

import java.io.File;

import org.optaplanner.core.api.domain.solution.Solution;
import org.optaplanner.persistence.common.api.domain.solution.SolutionFileIO;

public class ReindeerRoutingFileIO implements SolutionFileIO {

    public static final String FILE_EXTENSION = "csv";

    private ReindeerRoutingImporter importer = new ReindeerRoutingImporter();
    private ReindeerRoutingExporter exporter = new ReindeerRoutingExporter();

    public String getInputFileExtension() {
        return FILE_EXTENSION;
    }

    public String getOutputFileExtension() {
        return FILE_EXTENSION;
    }

    public Solution read(File inputSolutionFile) {
        return importer.readSolution(inputSolutionFile);
    }

    public void write(Solution solution, File outputSolutionFile) {
        exporter.writeSolution(solution, outputSolutionFile);
    }

}
