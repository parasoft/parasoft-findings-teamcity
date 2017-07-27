/*
 * (C) Copyright Parasoft Corporation 2017.  All rights reserved.
 * THIS IS UNPUBLISHED PROPRIETARY SOURCE CODE OF Parasoft
 * The copyright notice above does not evidence any
 * actual or intended publication of such source code.
 */

package com.parasoft.findings.teamcity.server;

import java.util.*;

import jetbrains.buildServer.serverSide.*;

public class ParasoftFindingsPropertiesProcessor implements PropertiesProcessor {
    public Collection<InvalidProperty> process(Map<String, String> properties) {
        return Collections.emptySet();
    }
}
