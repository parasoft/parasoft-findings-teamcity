/*
 * (C) Copyright Parasoft Corporation 2017.  All rights reserved.
 * THIS IS UNPUBLISHED PROPRIETARY SOURCE CODE OF Parasoft
 * The copyright notice above does not evidence any
 * actual or intended publication of such source code.
 */

package com.parasoft.findings.teamcity.server;

import com.parasoft.findings.teamcity.common.*;

import java.util.*;

import jetbrains.buildServer.serverSide.*;

public class ParasoftFindingsRunType extends RunType implements ParasoftFindingsPluginType {
    private static final String NL_EDIT_JSP = "parasoft-findings-edit.jsp";
    private static final String NL_VIEW_JSP = "parasoft-findings-view.jsp";

    // TODO: localize
    // TODO: replace with actual strings
    private static final String STR_DISPLAY_NAME = "Parasoft Findings PlugIn";
    private static final String STR_DESCRIPTION = "Parasoft Findings PlugIn description";

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
        // TODO: fill in defaults
        Map<String, String> defaults = new HashMap<String, String>();
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

