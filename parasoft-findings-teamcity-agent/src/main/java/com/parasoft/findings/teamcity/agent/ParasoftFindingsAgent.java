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

import jetbrains.buildServer.*;
import jetbrains.buildServer.agent.*;

public class ParasoftFindingsAgent implements AgentBuildRunner,  AgentBuildRunnerInfo,
    ParasoftFindingsPluginType {

    public boolean canRun(BuildAgentConfiguration config) {
        return true;
    }

    public String getType() {
        return PLUGIN_TYPE;
    }

    public BuildProcess createBuildProcess(AgentRunningBuild build, BuildRunnerContext context) throws RunBuildException {
        return new ParasoftFindingsBuildProcess(build, context);
    }

    public AgentBuildRunnerInfo getRunnerInfo() {
        return this;
    }
}

