/*
 * Copyright 2017 Parasoft Corporation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.parasoft.findings.teamcity.agent;

import com.parasoft.findings.teamcity.common.*;

import java.io.*;
import java.util.*;
import java.util.logging.*;

import jetbrains.buildServer.*;
import jetbrains.buildServer.agent.*;
import jetbrains.buildServer.util.pathMatcher.*;

public class ParasoftFindingsBuildProcess implements BuildProcess, ParasoftFindingsProperties {
    private static final Logger LOG;

    static {
        // logs into ./buildAgent/logs/wrapper.log
        LOG = Logger.getLogger(ParasoftFindingsAgent.class.getName());
    }

    private boolean _done = false;
    private BuildRunnerContext _context;
    private AgentRunningBuild _build;

    public ParasoftFindingsBuildProcess(AgentRunningBuild build, BuildRunnerContext context) {
        _build = build;
        _context = context;
    }

    public void interrupt() {
    }

    public boolean isFinished() {
        return _done;
    }

    public boolean isInterrupted() {
        return false;
    }

    public void start() throws RunBuildException {
        _done = false;
        try {
            doWork();
        } catch (final Throwable t) {
            LOG.log(Level.SEVERE, t.getMessage(), t);
        } finally {
            _done = true;
        }
    }

    private void doWork() {
        Map<String, String> params = _context.getRunnerParameters();
        String stReportsLocation = params.get(ST_REPORTS_SOURCE);
        LOG.info("Reading SOAtest reports from "+stReportsLocation); // relative to checkout dir
        File checkoutDir = _build.getCheckoutDirectory();
        LOG.info("Writing transformed SOAtest reports to "+checkoutDir);
        List<File> reports = AntPatternFileCollector.scanDir(checkoutDir, new String[] {stReportsLocation}, null);
    }

    public BuildFinishedStatus waitFor() throws RunBuildException {
        // TODO return appropriate status
        return BuildFinishedStatus.FINISHED_SUCCESS;
    }
}

