package com.parasoft.findings.teamcity.agent;

import jetbrains.buildServer.RunBuildException;
import jetbrains.buildServer.agent.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static com.parasoft.findings.teamcity.common.ParasoftFindingsPluginType.PLUGIN_TYPE;

public class ParasoftFindingsAgentTest {

    private final ParasoftFindingsAgent parasoftFindingsAgent = new ParasoftFindingsAgent();

    @Test
    public void test_canRun() {
        BuildAgentConfiguration buildAgentConfiguration = Mockito.mock(BuildAgentConfiguration.class);

        boolean testResult = parasoftFindingsAgent.canRun(buildAgentConfiguration);

        Assertions.assertTrue(testResult);
    }

    @Test
    public void test_getType() {
        String testResult = parasoftFindingsAgent.getType();
        Assertions.assertEquals(PLUGIN_TYPE, testResult);
    }

    @Test
    public void test_createBuildProcess() throws RunBuildException {
        AgentRunningBuild build = Mockito.mock(AgentRunningBuild.class);
        BuildRunnerContext context = Mockito.mock(BuildRunnerContext.class);

        ParasoftFindingsBuildProcess testResult = (ParasoftFindingsBuildProcess) parasoftFindingsAgent.createBuildProcess(build, context);

        Assertions.assertNotNull(testResult);
    }

    @Test
    public void test_getRunningInfo() {
        AgentBuildRunnerInfo testResult = parasoftFindingsAgent.getRunnerInfo();
        Assertions.assertEquals(parasoftFindingsAgent, testResult);
    }
}
