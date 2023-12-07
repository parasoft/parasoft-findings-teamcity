/*
 * Copyright 2023 Parasoft Corporation
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

import com.parasoft.findings.utils.doc.RuleDocumentationProvider;
import com.parasoft.findings.utils.doc.RuleDocumentationProvider.ClientStatus;
import jetbrains.buildServer.agent.AgentRunningBuild;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class RuleDocumentationUrlProvider {
    private static AgentRunningBuild _build;
    private Map<String, String> _ruleDocsUrls;
    private final RuleDocumentationProvider _docProvider;

    public RuleDocumentationUrlProvider(AgentRunningBuild build, Properties settings ) {
        _build = build;
        _ruleDocsUrls = new HashMap<>();
        _docProvider = new RuleDocumentationProvider(settings);
        if (_docProvider.getDtpDocServiceStatus() == ClientStatus.NOT_AVAILABLE) {
            _build.getBuildLogger().error(ClientStatus.NOT_AVAILABLE.toString() + ": " + settings.getProperty(LocalSettingsHelper.DTP_URL));
        } else if (_docProvider.getDtpDocServiceStatus() == ClientStatus.NOT_SUPPORTED_VERSION) {
            _build.getBuildLogger().error(ClientStatus.NOT_SUPPORTED_VERSION.toString());
        }
    }

    public String getRuleDocUrl(String analyzer, String ruleId) {
        if (_docProvider.getDtpDocServiceStatus() != ClientStatus.AVAILABLE) {
            return null;
        }
        String key = analyzer+"/"+ruleId;
        if (_ruleDocsUrls.containsKey(key)) {
            return _ruleDocsUrls.get(key);
        } else {
            String ruleDocLocation = _docProvider.getRuleDocLocation(analyzer, ruleId);
            _ruleDocsUrls.put(key, ruleDocLocation);
            return ruleDocLocation;
        }
    }
}
