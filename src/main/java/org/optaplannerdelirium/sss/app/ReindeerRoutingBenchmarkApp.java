/*
 * Copyright 2012 Red Hat, Inc. and/or its affiliates.
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

import org.optaplanner.examples.common.app.CommonBenchmarkApp;

public class ReindeerRoutingBenchmarkApp extends CommonBenchmarkApp {

    public static void main(String[] args) {
        new ReindeerRoutingBenchmarkApp().buildAndBenchmark(args);
    }

    /**
     * To use another config that the default one, open the run configuration
     * and add program argument such as "template" or "ge0ffreyLaptop" (without the quotes).
     */
    public ReindeerRoutingBenchmarkApp() {
        super(
                new ArgOption("default",
                        "org/optaplannerdelirium/sss/benchmark/reindeerRoutingBenchmarkConfig.xml"),
                new ArgOption("ge0ffreyLaptop",
                        "org/optaplannerdelirium/sss/benchmark/ge0ffrey/ge0ffreyLaptopBenchmarkConfig.xml"),
                new ArgOption("ge0ffreyDesktop",
                        "org/optaplannerdelirium/sss/benchmark/ge0ffrey/ge0ffreyDesktopBenchmarkConfig.xml"),
                new ArgOption("ge0ffreyServer",
                        "org/optaplannerdelirium/sss/benchmark/ge0ffrey/ge0ffreyServerBenchmarkConfig.xml"),
                new ArgOption("template",
                        "org/optaplannerdelirium/sss/benchmark/reindeerRoutingBenchmarkConfigTemplate.xml.ftl", true)
        );
    }

}
