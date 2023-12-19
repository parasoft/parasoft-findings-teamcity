package com.parasoft.findings.teamcity.agent;

import jetbrains.buildServer.agent.*;
import jetbrains.buildServer.messages.BuildMessage1;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.io.File;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;

public class ParasoftFindingsBuildProcessTest {
    @InjectMocks
    ParasoftFindingsBuildProcess parasoftFindingsBuildProcess;
    @Mock
    AgentRunningBuild build;
    @Mock
    BuildRunnerContext context;
    @Mock
    BuildProgressLogger buildProgressLogger;

    Map<String, String> params = new HashMap<>();
    String reportDirPath = "src/test/resources/reports";
    File checkoutDir = new File("src/test/resources");

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        params.clear();
    }

    @Test
    public void test_transformStaticTestReport_withLocalSettings() throws Throwable {
        // Given
        params.put("settings.location", "settings/localSettings.properties");
        File localSettings = new File("src/test/resources/settings/localSettings.properties");

        // When
        testTransformStaticTestReport();

        // Then
        Mockito.verify(buildProgressLogger).message("File path for local settings is " + localSettings.getAbsolutePath());
    }

    @Test
    public void test_transformStaticTestReport_withEmptyLocalSettings() throws Throwable {
        // Given
        params.put("settings.location", "settings/emptyLocalSettings.properties");

        // When
        testTransformStaticTestReport();

        // Then
        Mockito.verify(buildProgressLogger, Mockito.atLeastOnce()).warning(Mockito.isA(String.class));
        Mockito.verify(buildProgressLogger).warning("No properties loaded");
    }

    private void testTransformStaticTestReport() throws Throwable {
        File pmdReport = new File(reportDirPath + "/pmd-cpptest_professional_report.xml");
        File pmdCpdReport = new File(reportDirPath + "/pmdCpd-cpptest_professional_report.xml");
        File testReport = new File(reportDirPath + "/cpptest_professional_report.xml");
        String pmdReportRelativePath = checkoutDir.toURI().relativize(pmdReport.toURI()).getPath();

        try {
            // Given
            setupMockedDataForTransformation("reports/cpptest_professional_report.xml");

            // When
            BuildFinishedStatus status = parasoftFindingsBuildProcess.call();
            String pmdReportSize = new DecimalFormat("0.00").format(pmdReport.length()/1024f);
            int invalidReportCount = parasoftFindingsBuildProcess.getInvalidReportCount();

            // Then
            Assertions.assertTrue(pmdReport.exists());
            Assertions.assertTrue(pmdCpdReport.exists());
            Assertions.assertEquals(status.name(), "FINISHED_SUCCESS");
            Assertions.assertEquals(invalidReportCount, 0);

            // Verify the functions are called at least once
            Mockito.verify(buildProgressLogger, Mockito.atLeastOnce()).message(Mockito.isA(String.class));
            Mockito.verify(buildProgressLogger, Mockito.atLeastOnce()).logMessage(Mockito.isA(BuildMessage1.class));

            // Verify the expected functions are called
            Mockito.verify(buildProgressLogger).message("Transforming " + testReport.getAbsolutePath() + " with Parasoft Code Inspection");
            Mockito.verify(buildProgressLogger).message("Generated report with transformation: " + pmdReport.getAbsolutePath());
            Mockito.verify(buildProgressLogger).message("Importing data from '"+ pmdReportRelativePath + "' ("+ pmdReportSize +" KB) with 'message service' processor");
            Mockito.verify(buildProgressLogger).message("Transforming " + testReport.getAbsolutePath() + " with Parasoft Duplicates");
            Mockito.verify(buildProgressLogger).message("Generated report with transformation: " + pmdCpdReport.getAbsolutePath());
        } finally {
            deleteIfExists(pmdReport);
            deleteIfExists(pmdCpdReport);
        }
    }

    @Test
    public void test_transformSOATestReport() throws Throwable {
        File junitReport = new File(reportDirPath + "/junit-SOAtest_report.xml");
        File pmdReport = new File( reportDirPath + "/pmd-SOAtest_report.xml");
        File testReport = new File(reportDirPath + "/SOAtest_report.xml");
        String pmdReportRelativePath = checkoutDir.toURI().relativize(pmdReport.toURI()).getPath();

        try {
            // Given
            setupMockedDataForTransformation("reports/SOAtest_report.xml");

            // When
            BuildFinishedStatus status = parasoftFindingsBuildProcess.call();
            String pmdReportSize = new DecimalFormat("0.00").format(pmdReport.length()/1024f);
            int invalidReportCount = parasoftFindingsBuildProcess.getInvalidReportCount();

            // Then
            Assertions.assertTrue(junitReport.exists());
            Assertions.assertTrue(pmdReport.exists());
            Assertions.assertEquals(status.name(), "FINISHED_SUCCESS");
            Assertions.assertEquals(invalidReportCount, 0);

            Mockito.verify(buildProgressLogger, Mockito.atLeastOnce()).message(Mockito.isA(String.class));
            Mockito.verify(buildProgressLogger, Mockito.atLeastOnce()).logMessage(Mockito.isA(BuildMessage1.class));

            Mockito.verify(buildProgressLogger).message("Transforming " + testReport.getAbsolutePath() + " with Parasoft Code Inspection");
            Mockito.verify(buildProgressLogger).message("Generated report with transformation: " + pmdReport.getAbsolutePath());
            Mockito.verify(buildProgressLogger).message("Importing data from '"+ pmdReportRelativePath + "' ("+ pmdReportSize +" KB) with 'message service' processor");
            Mockito.verify(buildProgressLogger).message("Transforming " + testReport.getAbsolutePath() + " with Parasoft SOAtest");
        } finally {
            deleteIfExists(pmdReport);
            deleteIfExists(junitReport);
        }
    }

    @Test
    public void test_transformUnitTestReport() throws Throwable {
        File junitReport = new File(reportDirPath + "/junit-jtest_report.xml");
        File testReport = new File(reportDirPath + "/jtest_report.xml");

        try {
            // Given
            setupMockedDataForTransformation("reports/jtest_report.xml");

            // When
            BuildFinishedStatus status = parasoftFindingsBuildProcess.call();
            int invalidReportCount = parasoftFindingsBuildProcess.getInvalidReportCount();

            // Then
            Assertions.assertTrue(junitReport.exists());
            Assertions.assertEquals(status.name(), "FINISHED_SUCCESS");
            Assertions.assertEquals(invalidReportCount, 0);

            Mockito.verify(buildProgressLogger, Mockito.atLeastOnce()).message(Mockito.isA(String.class));
            Mockito.verify(buildProgressLogger, Mockito.atLeastOnce()).logMessage(Mockito.isA(BuildMessage1.class));

            Mockito.verify(buildProgressLogger).message("Transforming " + testReport.getAbsolutePath() + " with Parasoft Analyzers");
            Mockito.verify(buildProgressLogger).message("Generated report with transformation: " + junitReport.getAbsolutePath());
        } finally {
            deleteIfExists(junitReport);
        }
    }

    @Test
    public void test_transform_reportNotFound() throws Throwable {
        // Given
        setupMockedDataForTransformation("report.xml");

        // When
        BuildFinishedStatus status = parasoftFindingsBuildProcess.call();

        // Then
        Assertions.assertEquals(status.name(), "FINISHED_SUCCESS");

        Mockito.verify(buildProgressLogger, Mockito.atLeastOnce()).error(Mockito.isA(String.class));
        Mockito.verify(buildProgressLogger).error("No reports found for pattern: report.xml");
    }

    @Test
    public void test_transformFail_invalidContent() throws Throwable {
        // Given
        File testReport = new File(reportDirPath + "/invalidReport.xml");
        File pmdReport = new File( reportDirPath + "/pmd-invalidReport.xml");
        File pmdCpdReport = new File(reportDirPath + "/pmdCpd-invalidReport.xml");
        File logDir = new File("buildAgent/logs");
        setupMockedDataForTransformation("reports/invalidReport.xml");

        ParasoftFindingsBuildProcess buildProcess = new ParasoftFindingsBuildProcess(build, context);
        BuildAgentConfiguration buildAgentConfiguration = Mockito.mock(BuildAgentConfiguration.class);
        doReturn(buildAgentConfiguration).when(build).getAgentConfiguration();
        doReturn(logDir).when(buildAgentConfiguration).getAgentLogsDirectory();

        // When
        BuildFinishedStatus status = buildProcess.call();
        int invalidReportCount = buildProcess.getInvalidReportCount();

        // Then
        Assertions.assertFalse(pmdReport.exists());
        Assertions.assertFalse(pmdCpdReport.exists());
        Assertions.assertEquals(status.name(), "FINISHED_FAILED");
        Assertions.assertEquals(invalidReportCount, 1);

        Mockito.verify(buildProgressLogger, Mockito.atLeastOnce()).error(Mockito.isA(String.class));
        Mockito.verify(buildProgressLogger, Mockito.atLeastOnce()).warning(Mockito.isA(String.class));
        Mockito.verify(buildProgressLogger, Mockito.atLeastOnce()).buildFailureDescription(Mockito.isA(String.class));

        Mockito.verify(buildProgressLogger).error("Unexpected report format: "+testReport.getAbsolutePath());
        Mockito.verify(buildProgressLogger).error("org.xml.sax.SAXParseException: The markup in the document following the root element must be well-formed.");
        Mockito.verify(buildProgressLogger).error("Please try recreating the report or see log for details: " + logDir + "\\wrapper.log");
        Mockito.verify(buildProgressLogger).warning("Skipping unrecognized report file: " + testReport.getAbsolutePath());
        Mockito.verify(buildProgressLogger).buildFailureDescription("Failed to parse XML report");
    }

    private void setupMockedDataForTransformation(String reportLocation) {
        params.put("reports.location", reportLocation);
        doReturn(params).when(context).getRunnerParameters();
        doReturn(buildProgressLogger).when(build).getBuildLogger();
        doReturn(checkoutDir).when(build).getCheckoutDirectory();
        doNothing().when(buildProgressLogger).message(Mockito.isA(String.class));
        doNothing().when(buildProgressLogger).logMessage(Mockito.isA(BuildMessage1.class));
        doNothing().when(buildProgressLogger).error(Mockito.isA(String.class));
        doNothing().when(buildProgressLogger).warning(Mockito.isA(String.class));
        doNothing().when(buildProgressLogger).buildFailureDescription(Mockito.isA(String.class));
    }

    private void deleteIfExists(File file) {
        if(file.exists()) {
            file.delete();
        }
    }
}