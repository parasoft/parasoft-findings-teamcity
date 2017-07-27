/*
 * (C) Copyright Parasoft Corporation 2017.  All rights reserved.
 * THIS IS UNPUBLISHED PROPRIETARY SOURCE CODE OF Parasoft
 * The copyright notice above does not evidence any
 * actual or intended publication of such source code.
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

