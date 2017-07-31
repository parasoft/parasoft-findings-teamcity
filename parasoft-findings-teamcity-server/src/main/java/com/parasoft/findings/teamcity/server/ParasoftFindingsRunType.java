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

package com.parasoft.findings.teamcity.server;

import com.parasoft.findings.teamcity.common.*;

import java.util.*;

import jetbrains.buildServer.serverSide.*;
import jetbrains.buildServer.web.openapi.*;

public class ParasoftFindingsRunType extends RunType implements ParasoftFindingsPluginType,
    ParasoftFindingsProperties {
    private static final String NL_EDIT_JSP = "parasoft-findings-edit.jsp";
    private static final String NL_VIEW_JSP = "parasoft-findings-view.jsp";
    private static final String NL_DEFAULT_ST_REPORTS_LOCATION = "/SOAtestReports";

    // TODO: localize
    // TODO: replace with actual strings
    private static final String STR_DISPLAY_NAME = "Parasoft Findings";
    private static final String STR_DESCRIPTION = "Parasoft SOAtest reporting.";

    public ParasoftFindingsRunType(RunTypeRegistry runTypeRegistry, PluginDescriptor pluginDescriptor) {
        runTypeRegistry.registerRunType((RunType)this);
    }
    @Override
    public String getDescription() {
        return STR_DESCRIPTION;
    }

    @Override
    public String getDisplayName() {
        return STR_DISPLAY_NAME;
    }

    @Override
    public String getType() {
        return NL_PLUGIN_TYPE;
    }

    @Override
    public Map<String, String> getDefaultRunnerProperties() {
        Map<String, String> defaults = new HashMap<String, String>();
        defaults.put(ST_REPORTS_SOURCE, NL_DEFAULT_ST_REPORTS_LOCATION);
        return defaults;
    }

    @Override
    public String getEditRunnerParamsJspFilePath() {
        return NL_EDIT_JSP;
    }

    @Override
    public PropertiesProcessor getRunnerPropertiesProcessor() {
        return new ParasoftFindingsPropertiesProcessor();
    }

    @Override
    public String getViewRunnerParamsJspFilePath() {
        return NL_VIEW_JSP;
    }
}

