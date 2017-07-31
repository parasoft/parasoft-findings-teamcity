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

import java.util.*;

import jetbrains.buildServer.*;
import jetbrains.buildServer.agent.*;

public class ParasoftFindingsBuildProcess implements BuildProcess, ParasoftFindingsProperties {
    private boolean _done = false;
    private BuildRunnerContext _context;

    public ParasoftFindingsBuildProcess(AgentRunningBuild build, BuildRunnerContext context) {
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
        } catch (Throwable t) {
            // TODO: handle
        } finally {
            _done = true;
        }
    }

    private void doWork() {
        Map<String, String> params = _context.getRunnerParameters();
        String p1 = params.get(PARAM_ONE);
    }

    public BuildFinishedStatus waitFor() throws RunBuildException {
        // TODO return appropriate status
        return BuildFinishedStatus.FINISHED_SUCCESS;
    }
}

