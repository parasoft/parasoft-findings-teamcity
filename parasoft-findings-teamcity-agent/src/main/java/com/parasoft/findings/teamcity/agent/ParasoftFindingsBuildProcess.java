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
import jetbrains.buildServer.messages.*;
import jetbrains.buildServer.util.pathMatcher.*;

public class ParasoftFindingsBuildProcess implements BuildProcess, Callable<BuildFinishedStatus>, ParasoftFindingsProperties {
    private static final String PREFIX = "junit-"; //$NON-NLS-1$
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
        String reportParserType = params.get(REPORT_PARSER_TYPE);
        ReportParserDescriptor rpd = ReportParserTypes.getDescriptor(reportParserType);

        if (rpd != null) {
            String reportsLocation = params.get(REPORTS_LOCATION);
            File checkoutDir = _build.getCheckoutDirectory();

            List<File> reports = AntPatternFileCollector.scanDir(checkoutDir, new String[] {reportsLocation}, null);
            if (reports.isEmpty()) {
                _build.getBuildLogger().error("No reports found for pattern: "+reportsLocation);
            } else {
                for (File from : reports) {
                    _build.getBuildLogger().message("Transforming "+from.getAbsolutePath()+" with "+rpd.getLabel());
                    String targetFileName = PREFIX + from.getName();
                    File to = new File(from.getParentFile(), targetFileName);
                    transform(from, to, rpd.getXSL(), checkoutDir);
                    _build.getBuildLogger().message("Wrote transformed report to "+to.getAbsolutePath());
                }
            }
        } else {
            _build.getBuildLogger().error("No parsers found for type "+reportParserType);
        }
    }

    private void transform(File from, File to, String xslFile, File checkoutDir) {
        try {
            StreamSource xml = new StreamSource(new FileInputStream(from));
            StreamSource xsl = new StreamSource(getClass().getResourceAsStream(xslFile));
            StreamResult target = new StreamResult(to);
            Transformer processor = tFactory.newTransformer(xsl);
            processor.setErrorListener(xsltErrorListener);
            processor.transform(xml, target);

            // Send a notification to TC that a JUnit report is ready to be consumed.
            // This allows running the plug-in build step without having to configure 
            // the XML Report Processing build feature in a TC project.
            String relativePath = checkoutDir.toURI().relativize(to.toURI()).getPath();
            _build.getBuildLogger().logMessage(DefaultMessagesInfo.createTextMessage
                    ("##teamcity[importData type='junit' path='"+relativePath+"']"));
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
