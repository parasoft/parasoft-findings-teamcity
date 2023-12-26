package com.parasoft.findings.teamcity.common;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static com.parasoft.findings.teamcity.common.ReportParserTypes.JUNIT_PREFIX;

public class ReportParserTypesTest {

    @Test
    public void test_getParsers() {
        ReportParserDescriptor[] testResults = ReportParserTypes.getParsers();

        Assertions.assertEquals(4, testResults.length);
        Assertions.assertEquals("SOATEST", testResults[0].getId());
        Assertions.assertEquals("ANALYZERS", testResults[1].getId());
        Assertions.assertEquals("SA_PMD", testResults[2].getId());
        Assertions.assertEquals("SA_PMD_CPD", testResults[3].getId());
    }

    @Test
    public void test_getDefault() {
        ReportParserDescriptor testResult = ReportParserTypes.getDefault();

        Assertions.assertEquals("SOATEST", testResult.getId());
        Assertions.assertEquals("Parasoft SOAtest", testResult.getLabel());
        Assertions.assertEquals("soatest-xunit.xsl", testResult.getXSL());
        Assertions.assertEquals(JUNIT_PREFIX, testResult.getOutputFileNamePrefix());
    }

    @Test
    public void test_getParserLabel_normal() {
        String testResult = ReportParserTypes.getParserLabel("SOATEST");

        Assertions.assertNotNull(testResult);
        Assertions.assertEquals("Parasoft SOAtest", testResult);
    }

    @Test
    public void test_getParserLabel_labelNotFound() {
        String testResult = ReportParserTypes.getParserLabel("labelForTest");
        Assertions.assertNull(testResult);
    }
}
