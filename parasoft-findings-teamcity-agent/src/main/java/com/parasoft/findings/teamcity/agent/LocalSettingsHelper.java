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

import com.parasoft.xtest.common.text.UString;
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

    private AgentRunningBuild _build;

    public LocalSettingsHelper(AgentRunningBuild _build) {
        this._build = _build;
    }

    public Properties loadLocalSettings(File checkoutDir, String settingsPath) {
        if (UString.isEmpty(settingsPath)) {
            return new Properties();
        }
        File localSettingsFile = new File(settingsPath);
        if (!localSettingsFile.exists()) {
            localSettingsFile = new File(checkoutDir, settingsPath);
        }
        return loadProperties(localSettingsFile);
    }

    /**
     * Check the value is a valid URL for TeamCity. <br>
     * The value will be used to be as prefix of a link which points to the doc on DTP server.<br>
     * But TeamCity has a limitation for the link,
     * it needs the value to be like https://{domainName}/{anyString} instead of https://{hostName}:{port}/{anyString}"<br>
     * Here is the <a href='https://youtrack.jetbrains.com/issue/TW-80190/Make-inspection-type-despection-icon-support-an-internal-link'>feature request for TeamCity</a><br>
     * e.g. <br>
     *   https://dtp.parasoft.com/anyString is valid.<br>
     *   https://locahohost:8443/anyString is invalid.
    * */
    public boolean isDtpUrlValidForTeamCity(String dtpUrl) {
        if (Objects.isNull(dtpUrl)) {
            _build.getBuildLogger().warning("dtp.url property not found");
            return false;
        }
        _build.getBuildLogger().message("dtp.url property value: " + dtpUrl);
        dtpUrl = dtpUrl.endsWith("/") ? dtpUrl : dtpUrl + "/";
        Pattern p = Pattern.compile(validUrlPattern);
        Matcher m = p.matcher(dtpUrl);
        boolean isValid = m.matches();
        if (!isValid) {
            _build.getBuildLogger().warning("dtp.url property value is invalid for TeamCity. Try to use https://{domainName} instead of https://{hostName}:{port}");
        }
        return isValid;
    }

    private Properties loadProperties(File file) {
        _build.getBuildLogger().message("Path to local settings is " + file.getAbsolutePath());
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
