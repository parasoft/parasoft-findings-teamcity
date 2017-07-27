/*
 * (C) Copyright Parasoft Corporation 2017.  All rights reserved.
 * THIS IS UNPUBLISHED PROPRIETARY SOURCE CODE OF Parasoft
 * The copyright notice above does not evidence any
 * actual or intended publication of such source code.
 */

package com.parasoft.findings.teamcity.agent;

import com.parasoft.findings.teamcity.common.*;

import jetbrains.buildServer.*;
import jetbrains.buildServer.agent.*;

public class ParasoftFindingsAgent implements AgentBuildRunner,  AgentBuildRunnerInfo,
    ParasoftFindingsPluginType {

    public boolean canRun(BuildAgentConfiguration config) {
        return true;
    }

    public String getType() {
        return NL_PLUGIN_TYPE;
    }

    public BuildProcess createBuildProcess(AgentRunningBuild build, BuildRunnerContext context) throws RunBuildException {
        return new ParasoftFindingsBuildProcess(build, context);
    }

    public AgentBuildRunnerInfo getRunnerInfo() {
        return this;
    }
}

