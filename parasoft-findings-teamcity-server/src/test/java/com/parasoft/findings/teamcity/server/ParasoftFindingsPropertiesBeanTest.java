package com.parasoft.findings.teamcity.server;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static com.parasoft.findings.teamcity.common.ParasoftFindingsProperties.REPORTS_LOCATION;
import static com.parasoft.findings.teamcity.common.ParasoftFindingsProperties.SETTINGS_LOCATION;

public class ParasoftFindingsPropertiesBeanTest {

    private final ParasoftFindingsPropertiesBean parasoftFindingsPropertiesBean = new ParasoftFindingsPropertiesBean();

    @Test
    public void test_getReportsLocation() {
        String testResult = parasoftFindingsPropertiesBean.getReportsLocation();
        Assertions.assertEquals(REPORTS_LOCATION, testResult);
    }

    @Test
    public void test_getSettingsLocation() {
        String testResult = parasoftFindingsPropertiesBean.getSettingsLocation();
        Assertions.assertEquals(SETTINGS_LOCATION, testResult);
    }
}
