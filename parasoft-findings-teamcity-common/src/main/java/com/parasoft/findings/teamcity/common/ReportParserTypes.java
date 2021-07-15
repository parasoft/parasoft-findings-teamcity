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

package com.parasoft.findings.teamcity.common;

import static com.parasoft.findings.teamcity.common.ReportParserDescriptor.ReportParserType;

public class ReportParserTypes {
    private static final String SOATEST_LABEL = "Parasoft SOAtest"; //$NON-NLS-1$
    private static final String ANALYZERS_LABEL = "Parasoft Analyzers"; //$NON-NLS-1$

    private static final String SOATEST_XSL = "soatest-xunit.xsl"; //$NON-NLS-1$
    private static final String ANALYZERS_XSL = "xunit.xsl"; //$NON-NLS-1$

    private static final ReportParserDescriptor[] _parsers = new ReportParserDescriptor[] { 
        new ReportParserDescriptor(ReportParserType.SOATEST, SOATEST_LABEL, SOATEST_XSL),
        new ReportParserDescriptor(ReportParserType.ANALYZERS, ANALYZERS_LABEL, ANALYZERS_XSL)
    };

    public static ReportParserDescriptor[] getParsers() {
        return _parsers;
    }

    public static ReportParserDescriptor getDefault() {
        return _parsers[0];
    }

    public static ReportParserDescriptor getDescriptor(String id) {
        for (ReportParserDescriptor p : _parsers) {
            if (p.getId().equals(id)) {
                return p;
            }
        }
        return null;
    }

    public static String getParserLabel(String id) {
        ReportParserDescriptor d = getDescriptor(id);
        return d != null ? d.getLabel() : null;
    }
}

