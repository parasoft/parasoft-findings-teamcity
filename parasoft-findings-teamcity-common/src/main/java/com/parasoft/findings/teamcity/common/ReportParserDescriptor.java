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

public class ReportParserDescriptor {
    public enum ReportParserType { SOATEST, ANALYZERS, SA_PMD, SA_PMD_CPD };

    private ReportParserType _type;
    private String _label;
    private String _xsl;
    private String _outputFileNamePrefix;

    public ReportParserDescriptor(ReportParserType type, String label, String xsl, String outputFileNamePrefix) {
        _type = type;
        _label = label;
        _xsl = xsl;
        _outputFileNamePrefix = outputFileNamePrefix;
    }

    public String getId() {
        return _type.name();
    }

    public String getLabel() {
        return _label;
    }

    public String getXSL() {
        return _xsl;
    }

    public String getOutputFileNamePrefix() {
        return _outputFileNamePrefix;
    }
}

