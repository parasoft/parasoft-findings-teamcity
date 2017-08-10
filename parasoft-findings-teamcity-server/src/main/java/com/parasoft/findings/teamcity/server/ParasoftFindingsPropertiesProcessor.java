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

public class ParasoftFindingsPropertiesProcessor implements PropertiesProcessor, 
    ParasoftFindingsProperties {
    private static final String CANNOT_BE_EMPTY = "Please specify report location pattern."; //$NON-NLS-1$

    public Collection<InvalidProperty> process(Map<String, String> properties) {
        Collection<InvalidProperty> invalidProperties = new ArrayList<InvalidProperty>();
        String stReportsLocation = properties.get(REPORTS_LOCATION);
        if (stReportsLocation == null || stReportsLocation.trim().isEmpty()) {
            invalidProperties.add(new InvalidProperty(REPORTS_LOCATION, CANNOT_BE_EMPTY));
        }
        return invalidProperties;
    }
}
