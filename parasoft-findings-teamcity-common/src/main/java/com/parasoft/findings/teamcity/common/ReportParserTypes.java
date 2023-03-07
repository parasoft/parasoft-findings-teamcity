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
    private static final String SA_PMD_LABEL = "PMD"; //$NON-NLS-1$
    private static final String SA_PMDCPD_LABEL = "PMD Copy/Paste Detector (CPD)"; //$NON-NLS-1$

    private static final String SOATEST_XSL = "soatest-xunit.xsl"; //$NON-NLS-1$
    private static final String ANALYZERS_XSL = "xunit.xsl"; //$NON-NLS-1$
    private static final String SA_PMD_XSL = "sa-pmd.xsl"; //$NON-NLS-1$
    private static final String SA_PMDCPD_XSL = "sa-pmdcpd.xsl"; //$NON-NLS-1$

    public static final String JUNIT_PREFIX = "junit-"; //$NON-NLS-1$
    public static final String PMD_PREFIX = "pmd-"; //$NON-NLS-1$
    public static final String PMD_CPD_PREFIX = "pmdCpd-"; //$NON-NLS-1$

    private static final ReportParserDescriptor[] _parsers = new ReportParserDescriptor[] {
        new ReportParserDescriptor(ReportParserType.SOATEST, SOATEST_LABEL, SOATEST_XSL, JUNIT_PREFIX),
        new ReportParserDescriptor(ReportParserType.ANALYZERS, ANALYZERS_LABEL, ANALYZERS_XSL, JUNIT_PREFIX),
        new ReportParserDescriptor(ReportParserType.SA_PMD, SA_PMD_LABEL, SA_PMD_XSL, PMD_PREFIX),
        new ReportParserDescriptor(ReportParserType.SA_PMD_CPD, SA_PMDCPD_LABEL, SA_PMDCPD_XSL, PMD_CPD_PREFIX)
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

