/*
 * Copyright 2017 Parasoft Corporation
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

import com.parasoft.findings.teamcity.common.*;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.logging.*;

import javax.xml.transform.*;
import javax.xml.transform.stream.*;

import jetbrains.buildServer.*;
import jetbrains.buildServer.agent.*;
import jetbrains.buildServer.util.pathMatcher.*;

public class ParasoftFindingsBuildProcess implements BuildProcess, Callable<BuildFinishedStatus>, ParasoftFindingsProperties {
    private static final String PREFIX = "junit-"; //$NON-NLS-1$
    private static final String SOATEST_XSL = "soatest-xunit.xsl"; //$NON-NLS-1$
    private static final Logger LOG = Logger.getLogger
            (ParasoftFindingsBuildProcess.class.getName()); // logs into ./buildAgent/logs/wrapper.log

    private static final XsltErrorListener xsltErrorListener = new XsltErrorListener();
    private static final TransformerFactory tFactory = TransformerFactory.newInstance();

    private BuildRunnerContext _context;
    private AgentRunningBuild _build;
    private Future<BuildFinishedStatus> _futureStatus;

    public ParasoftFindingsBuildProcess(AgentRunningBuild build, BuildRunnerContext context) {
        _build = build;
        _context = context;
    }

    @Override
    public void interrupt() {
        _futureStatus.cancel(true);
    }

    @Override
    public boolean isInterrupted() {
        return _futureStatus.isCancelled() && this.isFinished();
    }

    @Override
    public boolean isFinished() {
        return _futureStatus.isDone();
    }

    @Override
    public void start() throws RunBuildException {
        try {
            _futureStatus = Executors.newSingleThreadExecutor().submit(this);
            LOG.info("Build step started.");
        } catch (RejectedExecutionException e) {
            LOG.severe("Failed to start build step.");
            throw new RunBuildException(e);
        }
    }

    @Override
    public BuildFinishedStatus waitFor() throws RunBuildException {
        try {
            BuildFinishedStatus status = _futureStatus.get();
            LOG.info("Build step finished.");
            return status;
        } catch (InterruptedException ie) {
            LOG.log(Level.INFO, "Build step was interrupted.", ie);
            throw new RunBuildException(ie);
        } catch (ExecutionException ee) {
            LOG.log(Level.INFO, "Error in build step.", ee);
            throw new RunBuildException(ee);
        } catch (CancellationException ce) {
            LOG.log(Level.INFO, "Build step was canceled.", ce);
            return BuildFinishedStatus.INTERRUPTED;
        }
    }

    @Override // Callable
    public BuildFinishedStatus call() throws Exception {
        BuildFinishedStatus status = BuildFinishedStatus.FINISHED_SUCCESS;
        try {
            doWork();
        } catch (final Throwable t) {
            LOG.log(Level.SEVERE, t.getMessage(), t);
            status = BuildFinishedStatus.FINISHED_FAILED;
        }
        return status;
    }

    private void doWork() {
        Map<String, String> params = _context.getRunnerParameters();
        String stReportsLocation = params.get(ST_REPORTS_SOURCE);
        LOG.info("Reading SOAtest reports from "+stReportsLocation);
        File checkoutDir = _build.getCheckoutDirectory();
        LOG.info("Writing transformed SOAtest reports to "+checkoutDir);

        List<File> reports = AntPatternFileCollector.scanDir(checkoutDir, new String[] {stReportsLocation}, null);
        if (reports.isEmpty()) {
            _build.getBuildLogger().message("No SOAtest XML reports found in "+
                    new File(checkoutDir, stReportsLocation).getAbsolutePath()+".");
        } else {
            for (File from : reports) {
                _build.getBuildLogger().message("Preparing to transform "+from.getAbsolutePath());
                String targetFileName = PREFIX + from.getName();
                File to = new File(checkoutDir, targetFileName);
                transform(from, to, SOATEST_XSL);
                _build.getBuildLogger().message("Wrote transformed report to "+to.getAbsolutePath());
            }
        }
    }

    private void transform(File from, File to, String xslFile) {
        try {
            StreamSource xml = new StreamSource(new FileInputStream(from));
            StreamSource xsl = new StreamSource(getClass().getResourceAsStream(xslFile));
            StreamResult target = new StreamResult(to);
            Transformer processor = tFactory.newTransformer(xsl);
            processor.setErrorListener(xsltErrorListener);
            processor.transform(xml, target);
        } catch (TransformerConfigurationException e) {
            LOG.log(Level.SEVERE, e.getMessage(), e);
        } catch (TransformerException e) {
            LOG.log(Level.SEVERE, e.getMessage(), e);
        } catch (IOException e) {
            LOG.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    private static class XsltErrorListener implements ErrorListener {
        @Override
        public void warning(TransformerException e) throws TransformerException {
            LOG.log(Level.WARNING, e.getMessage(), e);
        }

        @Override
        public void error(TransformerException e) throws TransformerException {
            LOG.log(Level.SEVERE, e.getMessage(), e);
        }

        @Override
        public void fatalError(TransformerException e) throws TransformerException {
            LOG.log(Level.SEVERE, e.getMessage(), e);
        }
    }
}
