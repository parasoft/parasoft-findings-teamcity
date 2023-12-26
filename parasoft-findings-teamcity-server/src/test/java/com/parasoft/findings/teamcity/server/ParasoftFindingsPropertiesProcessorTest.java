package com.parasoft.findings.teamcity.server;

import jetbrains.buildServer.serverSide.InvalidProperty;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static com.parasoft.findings.teamcity.common.ParasoftFindingsProperties.REPORTS_LOCATION;

public class ParasoftFindingsPropertiesProcessorTest {

    private final ParasoftFindingsPropertiesProcessor parasoftFindingsPropertiesProcessor = new ParasoftFindingsPropertiesProcessor();

    @Test
    public void test_process_normal() {
        Map<String, String> properties = new HashMap<>();
        properties.put(REPORTS_LOCATION, "**/rep*.xml");

        Collection<InvalidProperty> testResults = parasoftFindingsPropertiesProcessor.process(properties);
        Assertions.assertEquals(0, testResults.size());
    }

    @Test
    public void test_process_emptyReportLocation() {
        Collection<InvalidProperty> testResults = parasoftFindingsPropertiesProcessor.process(new HashMap<>());
        InvalidProperty expectedInvalidProperty = new InvalidProperty(REPORTS_LOCATION, "Please specify report location pattern.");

        Assertions.assertEquals(1, testResults.size());
        Assertions.assertTrue(testResults.contains(expectedInvalidProperty));
    }
}
