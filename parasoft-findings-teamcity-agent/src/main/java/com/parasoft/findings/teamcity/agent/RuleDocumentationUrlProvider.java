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

import com.parasoft.xtest.common.dtp.IDtpServiceRegistry;
import com.parasoft.xtest.common.services.RawServiceContext;
import com.parasoft.xtest.configuration.dtp.XRestRulesClient;
import com.parasoft.xtest.configuration.rules.RuleDocumentationHelper;
import com.parasoft.xtest.services.api.IParasoftServiceContext;
import com.parasoft.xtest.services.api.ServiceUtil;
import jetbrains.buildServer.agent.AgentRunningBuild;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class RuleDocumentationUrlProvider {
    private static AgentRunningBuild _build;
    private Map<String, String> _ruleDocsUrls;
    private final TeamCityRuleDocumentationProvider _docProvider;

    public RuleDocumentationUrlProvider(AgentRunningBuild build, Properties settings) {
        _build = build;
        _ruleDocsUrls = new HashMap<>();
        _docProvider = new TeamCityRuleDocumentationProvider(settings);
    }

    public String getRuleDocUrl(String analyzer, String ruleId) {
        String key = analyzer+"/"+ruleId;
        if (_ruleDocsUrls.containsKey(key)) {
            return _ruleDocsUrls.get(key);
        } else {
            String ruleDocLocation = _docProvider.getRuleDocLocation(analyzer, ruleId);
            _ruleDocsUrls.put(key, ruleDocLocation);
            return ruleDocLocation;
        }
    }

    private static class TeamCityRuleDocumentationProvider {
        private final IParasoftServiceContext _context;

        private final XRestRulesClient _client;

        public TeamCityRuleDocumentationProvider(Properties settings) {
            _context = new RawServiceContext(settings);
            _client = getRuleClient(_context);
        }

        /**
         * @return url of rule docs or null
         */
        public String getRuleDocLocation(String analyzer, String ruleId) {
            RuleDocumentationHelper ruleDocHelper = new RuleDocumentationHelper(ruleId, analyzer, _client, _context);
            return ruleDocHelper.getNetworkRuleDocLocation();
        }

        private XRestRulesClient getRuleClient(IParasoftServiceContext context) {
            IDtpServiceRegistry registry = ServiceUtil.getService(IDtpServiceRegistry.class, context);
            if (registry == null) {
                return null;
            }

            XRestRulesClient rulesClient = XRestRulesClient.create(registry, context);
            if (rulesClient == null) {
                _build.getBuildLogger().error("Rules service client could not be created");
            }
            return rulesClient;
        }
    }
}
