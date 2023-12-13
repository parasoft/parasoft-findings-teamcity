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

import com.parasoft.findings.utils.common.util.StringUtil;
import jetbrains.buildServer.agent.AgentRunningBuild;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Objects;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LocalSettingsHelper {
    private static final String validUrlPattern = "(\\w+)://([\\w.]+)/(\\S*)";
    private static final Logger LOG = Logger.getLogger
            (LocalSettingsHelper.class.getName()); // logs into ./buildAgent/logs/wrapper.log
    public static String DTP_URL = "dtp.url";

    private AgentRunningBuild _build;

    public LocalSettingsHelper(AgentRunningBuild _build) {
        this._build = _build;
    }

    public Properties loadLocalSettings(File checkoutDir, String settingsPath) {
        if (StringUtil.isEmpty(settingsPath)) {
            return new Properties();
        }
        File localSettingsFile = new File(settingsPath);
        if (localSettingsFile.isAbsolute() && localSettingsFile.exists()) {
            return loadProperties(localSettingsFile);
        } else {
            return loadProperties(new File(checkoutDir, settingsPath));
        }
    }

    /**
     * The logic of this method is based on a limitation in TeamCity's URL validation pattern for link icon.<br>
     * Basically it only support domain name instead of host and port in the URL. The pattern used is /(\w+)://([\w.]+)/(\S*)/.
     * We can improve the support once this feature request is resolved:
     * <a href='https://youtrack.jetbrains.com/issue/TW-80190/Make-inspection-type-despection-icon-support-an-internal-link'>link</a><br>
    * */
    public boolean isDtpUrlValidForTeamCity(String dtpUrl) {
        if (Objects.isNull(dtpUrl)) {
            _build.getBuildLogger().warning("\"dtp.url\" property not found");
            return false;
        }
        _build.getBuildLogger().message("\"dtp.url\" property value: " + dtpUrl);
        dtpUrl = dtpUrl.endsWith("/") ? dtpUrl : dtpUrl + "/";
        Pattern p = Pattern.compile(validUrlPattern);
        Matcher m = p.matcher(dtpUrl);
        boolean isValid = m.matches();
        if (!isValid) {
            _build.getBuildLogger().warning("The value of \"dtp.url\" property is invalid for TeamCity. Please ensure that \"https://{domainName}\" is used instead of \"https://{hostName}:{port}\"");
        }
        return isValid;
    }

    private Properties loadProperties(File file) {
        _build.getBuildLogger().message("File path for local settings is " + file.getAbsolutePath());
        Properties props = new Properties();
        InputStream input = null;
        try {
            input = Files.newInputStream(file.toPath());
            props.load(input);
        } catch (IOException e) {
            _build.getBuildLogger().error("Local settings file does not exist");
            LOG.log(Level.SEVERE, e.getMessage(), e);
            return props;
        } finally {
            IOUtils.closeQuietly(input);
        }
        if (props.isEmpty()) {
            _build.getBuildLogger().warning("No properties loaded");
        }
        return props;
    }
}
