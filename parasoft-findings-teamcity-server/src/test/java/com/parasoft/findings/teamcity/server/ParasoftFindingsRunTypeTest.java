package com.parasoft.findings.teamcity.server;

import jetbrains.buildServer.serverSide.PropertiesProcessor;
import jetbrains.buildServer.serverSide.RunTypeRegistry;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Map;

import static com.parasoft.findings.teamcity.common.ParasoftFindingsPluginType.PLUGIN_TYPE;
import static com.parasoft.findings.teamcity.common.ParasoftFindingsProperties.REPORTS_LOCATION;

public class ParasoftFindingsRunTypeTest {

    private final ParasoftFindingsRunType parasoftFindingsRunType = new ParasoftFindingsRunType(Mockito.mock(RunTypeRegistry.class), null);

    @Test
    public void test_getDescription() {
        String testResult = parasoftFindingsRunType.getDescription();
        Assertions.assertEquals("Parasoft products reporting.", testResult);
    }

    @Test
    public void test_getDisplayName() {
        String testResult = parasoftFindingsRunType.getDisplayName();
        Assertions.assertEquals("Parasoft Findings", testResult);
    }

    @Test
    public void test_getType() {
        String testResult = parasoftFindingsRunType.getType();
        Assertions.assertEquals(PLUGIN_TYPE, testResult);
    }

    @Test
    public void test_getDefaultRunnerProperties() {
        Map<String, String> testResults = parasoftFindingsRunType.getDefaultRunnerProperties();

        Assertions.assertNotNull(testResults);
        Assertions.assertEquals(1, testResults.size());
        Assertions.assertEquals("**/rep*.xml", testResults.get(REPORTS_LOCATION));
    }

    @Test
    public void test_getEditRunnerParamsJspFilePath() {
        String testResult = parasoftFindingsRunType.getEditRunnerParamsJspFilePath();
        Assertions.assertEquals("parasoft-findings-edit.jsp", testResult);
    }

    @Test
    public void test_getRunnerPropertiesProcessor() {
        PropertiesProcessor testResult = parasoftFindingsRunType.getRunnerPropertiesProcessor();
        Assertions.assertNotNull(testResult);
    }

    @Test
    public void test_getViewRunnerParamsJspFilePath() {
        String testResult = parasoftFindingsRunType.getViewRunnerParamsJspFilePath();
        Assertions.assertEquals("parasoft-findings-view.jsp", testResult);
    }
}
