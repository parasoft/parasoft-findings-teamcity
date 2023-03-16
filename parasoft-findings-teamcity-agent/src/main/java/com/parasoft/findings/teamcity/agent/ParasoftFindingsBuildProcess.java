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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.*;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import javax.xml.transform.ErrorListener;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.*;

import com.parasoft.findings.teamcity.common.ParasoftFindingsProperties;
import com.parasoft.findings.teamcity.common.ReportParserDescriptor;
import com.parasoft.findings.teamcity.common.ReportParserDescriptor.ReportParserType;
import com.parasoft.findings.teamcity.common.ReportParserTypes;

import jetbrains.buildServer.RunBuildException;
import jetbrains.buildServer.agent.AgentRunningBuild;
import jetbrains.buildServer.agent.BuildFinishedStatus;
import jetbrains.buildServer.agent.BuildProcess;
import jetbrains.buildServer.agent.BuildRunnerContext;
import jetbrains.buildServer.messages.DefaultMessagesInfo;
import jetbrains.buildServer.util.pathMatcher.AntPatternFileCollector;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class ParasoftFindingsBuildProcess implements BuildProcess, Callable<BuildFinishedStatus>, ParasoftFindingsProperties {
    private static final String JUNIT_TESTSUITE_TAG_NAME = "testsuite";
    private static final String JUNIT_TESTSUITES_TAG_NAME = "testsuites";
    private static final String PMD_TAG_NAME = "pmd";
    private static final String PMD_CPD_TAG_NAME = "pmd-cpd";

    private static final Logger LOG = Logger.getLogger
            (ParasoftFindingsBuildProcess.class.getName()); // logs into ./buildAgent/logs/wrapper.log

    private static final XPathFactory xpathFactory = XPathFactory.newInstance();
    private static final TransformerFactory tFactory = TransformerFactory.newInstance();

    private BuildRunnerContext _context;
    private AgentRunningBuild _build;
    private Future<BuildFinishedStatus> _futureStatus;
    private XsltErrorListener _xsltErrorListener;
    private boolean _transformFailed = false;
    private int _invalidReportCount;

    public ParasoftFindingsBuildProcess(AgentRunningBuild build, BuildRunnerContext context) {
        _build = build;
        _context = context;
        _xsltErrorListener = new XsltErrorListener(this);
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
            if (_invalidReportCount > 0) {
                status = BuildFinishedStatus.FINISHED_FAILED;
                String description = _invalidReportCount > 1 ? "Failed to parse XML reports" :
                    "Failed to parse XML report";
                _build.getBuildLogger().buildFailureDescription(description);
            }
        } catch (final Throwable t) {
            LOG.log(Level.SEVERE, t.getMessage(), t);
            status = BuildFinishedStatus.FINISHED_FAILED;
        }
        return status;
    }

    private void doWork() {
        _invalidReportCount = 0;
        Map<String, String> params = _context.getRunnerParameters();
        String reportsLocation = params.get(REPORTS_LOCATION);
        File checkoutDir = _build.getCheckoutDirectory();

        List<File> reports = AntPatternFileCollector.scanDir(checkoutDir, new String[] {reportsLocation}, null);
        if (reports.isEmpty()) {
            _build.getBuildLogger().error("No reports found for pattern: "+reportsLocation);
        } else {
            for (File from : reports) {
                List<ReportParserDescriptor> rpds = getReportParserDescriptors(from);
                if (rpds.isEmpty()) {
                    _build.getBuildLogger().warning("Skipping unrecognized report file: " + from.getAbsolutePath());
                    continue;
                }
                for (ReportParserDescriptor rpd : rpds) {
                    _build.getBuildLogger().message("Transforming "+from.getAbsolutePath()+" with "+rpd.getLabel());
                    String targetFileName = rpd.getOutputFileNamePrefix() + from.getName();
                    File to = new File(from.getParentFile(), targetFileName);
                    transform(from, to, rpd.getXSL(), checkoutDir);
                }
            }
        }
    }

    private List<ReportParserDescriptor> getReportParserDescriptors(File from) {
        List<ReportParserDescriptor> descriptors = new ArrayList<ReportParserDescriptor>();
        try {
            Document document = getDocument(from);
            if (checkIfNodeExists(document, "/ResultsSession/CodingStandards/StdViols/*[not(name()='DupViol')]")) {
                descriptors.add(ReportParserTypes.getDescriptor(ReportParserType.SA_PMD.name()));
            }

            if (checkIfNodeExists(document, "/ResultsSession/CodingStandards/StdViols/DupViol")) {
                descriptors.add(ReportParserTypes.getDescriptor(ReportParserType.SA_PMD_CPD.name()));
            }

            if (checkIfNodeExists(document, "/ResultsSession/Exec")) {
                if (checkIfNodeExists(document, "/ResultsSession[contains(@toolName,'SOAtest')]")) {
                    descriptors.add(ReportParserTypes.getDescriptor(ReportParserType.SOATEST.name()));
                } else {
                    descriptors.add(ReportParserTypes.getDescriptor(ReportParserType.ANALYZERS.name()));
                }
            }
        } catch (Exception e) {
            reportUnexpectedFormat(from);
            LOG.log(Level.SEVERE, e.getMessage(), e);
        }
        return descriptors;
    }

    private Document getDocument(File file) throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        return builder.parse(file);
    }

    private NodeList getNodes(Document document, String xpathExpression) throws XPathExpressionException {
        XPath xpath = xpathFactory.newXPath();
        XPathExpression expr = xpath.compile(xpathExpression);
        return (NodeList) expr.evaluate(document, XPathConstants.NODESET);
    }

    private boolean checkIfNodeExists(Document document, String xpathExpression) throws XPathExpressionException {
        NodeList nodes = getNodes(document, xpathExpression);
        return nodes != null && nodes.getLength() > 0;
    }

    private void transform(File from, File to, String xslFile, File checkoutDir) {
        _transformFailed = false;
        try {
            StreamSource xml = new StreamSource(new FileInputStream(from));
            StreamSource xsl = new StreamSource(getClass().getResourceAsStream(xslFile));
            StreamResult target = new StreamResult(to);
            Transformer processor = tFactory.newTransformer(xsl);
            processor.setErrorListener(_xsltErrorListener);
            processor.transform(xml, target);

            String type = getContentType(to);
            if (type != null && !_transformFailed) {
                _build.getBuildLogger().message("Wrote transformed report to " + to.getAbsolutePath());
                String relativePath = checkoutDir.toURI().relativize(to.toURI()).getPath();
                if("pmd".equals(type)) {
                    // Use service message to send the inspection and inspectionType from generated pmd-xx.xml file into TC.
                    // We can not use message service to import generated pdm-xx.xml file directly
                    // since there is no attribute which can be recognized by TC to be as inspection type description.
                    // By default, the ruleset value in pdm-xx.xml is used to be as the description if we import it directly, but it is not expected.
                    parseAndSendViolationMessages(to);
                } else {
                    // Send a notification to TC that a JUnit or a PMD/CPD report is ready to be consumed.
                    // This allows running the plug-in build step without having to configure
                    // the XML Report Processing build feature in a TC project.
                    _build.getBuildLogger().logMessage(DefaultMessagesInfo.createTextMessage
                            ("##teamcity[importData type='"+type+"' path='"+relativePath + "']"));
                }
            } else {
                reportUnexpectedFormat(from);
            }
        } catch (TransformerException e) {
            reportUnexpectedFormat(from);
            LOG.log(Level.SEVERE, e.getMessage(), e);
        } catch (IOException e) {
            reportUnexpectedFormat(from);
            LOG.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    private void reportUnexpectedFormat(File from) {
        _invalidReportCount++;
        _build.getBuildLogger().error("Unexpected report format: "+from.getAbsolutePath()+ ". \nPlease try recreating the report. If this does not resolve the issue, please contact Parasoft support.");
    }

    private void parseAndSendViolationMessages(File pmdReport) {
        try {
            Map<String, CodeInspectionType> codeInspectionTypeMap = new HashMap<String, CodeInspectionType>();
            List<CodeInspectionInstance> codeInspectionInstances = new ArrayList<CodeInspectionInstance>();
            Document document = getDocument(pmdReport);

            // Handle file elements
            NodeList fileNodes = getNodes(document, "/pmd/file");
            for(int i = 0; i < fileNodes.getLength(); i++) {
                Node fileNode = fileNodes.item(i);
                if(Node.ELEMENT_NODE != fileNode.getNodeType()) {
                    continue;
                }
                // Handle violation elements
                NodeList violationNodes = fileNode.getChildNodes();
                for(int j = 0; j < violationNodes.getLength(); j++) {
                    Node violationNode = violationNodes.item(j);
                    if(Node.ELEMENT_NODE != violationNode.getNodeType()) {
                        continue;
                    }
                    NamedNodeMap fileAttributes = fileNode.getAttributes();
                    NamedNodeMap violationAttributes = violationNode.getAttributes();
                    Node violationContent = violationNode.getFirstChild();
                    String cit_rule = violationAttributes.getNamedItem("rule").getNodeValue();
                    String cit_category = violationAttributes.getNamedItem("ruleset").getNodeValue();
                    String cit_description = violationAttributes.getNamedItem("ruledescription").getNodeValue();
                    String ci_message = violationContent.getNodeValue();
                    String ci_line = violationAttributes.getNamedItem("beginline").getNodeValue();
                    String ci_fileLocation = fileAttributes.getNamedItem("name").getNodeValue();
                    String ci_severityNumber = violationAttributes.getNamedItem("priority").getNodeValue();

                    codeInspectionTypeMap.put(cit_rule, new CodeInspectionType(cit_rule, cit_description, cit_category));
                    codeInspectionInstances.add(new CodeInspectionInstance(cit_rule, ci_message, ci_fileLocation, ci_line, ci_severityNumber));
                }
            }

            for (CodeInspectionType type : codeInspectionTypeMap.values()) {
                _build.getBuildLogger().logMessage(DefaultMessagesInfo.createTextMessage
                        ("##teamcity[inspectionType id='"+type.ruleId+"' name='"+type.ruleId+"' description='<html><body>"+type.description+"</body></html>' category='"+type.category+"']"));
            }

            for (CodeInspectionInstance instance : codeInspectionInstances) {
                _build.getBuildLogger().logMessage(DefaultMessagesInfo.createTextMessage
                        ("##teamcity[inspection typeId='"+instance.typeId+"' message='"+instance.message+"' file='"+instance.file+"' line='"+instance.line+"' SEVERITY='"+instance.severity+"']"));
            }
        } catch (Exception e) {
            reportUnexpectedFormat(pmdReport);
            LOG.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    private String getContentType(File to) {
        if (!to.exists()) {
            return null;
        }
        final String ENCODING = "UTF-8";
        FileInputStream in = null;
        XMLEventReader eventReader = null;
        try {
            XMLInputFactory factory = XMLInputFactory.newInstance();
            in = new FileInputStream(to);
            eventReader = factory.createXMLEventReader(in, ENCODING);
            while (eventReader.hasNext()) {
                XMLEvent event = eventReader.nextEvent();
                switch (event.getEventType()) {
                case XMLStreamConstants.START_ELEMENT:
                    StartElement startElement = event.asStartElement();
                    String qName = startElement.getName().getLocalPart();
                    if (JUNIT_TESTSUITE_TAG_NAME.equals(qName) || JUNIT_TESTSUITES_TAG_NAME.equals(qName)) {
                        return "junit";
                    } else if (PMD_TAG_NAME.equals(qName)) {
                        return "pmd";
                    } else if (PMD_CPD_TAG_NAME.equals(qName))
                        return "pmdCpd";
                    }
            }
        } catch (FileNotFoundException e) {
            LOG.log(Level.SEVERE, e.getMessage(), e);
        } catch (XMLStreamException e) {
            LOG.log(Level.SEVERE, e.getMessage(), e);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (Throwable t) {
                    // ignore
                }
            }
            if (eventReader != null) {
                try {
                    eventReader.close();
                } catch (Throwable t) {
                    // ignore
                }
            }
        }
        return null;
    }

    protected void transformFailed() {
        _transformFailed = true;
    }

    private static class XsltErrorListener implements ErrorListener {
        private ParasoftFindingsBuildProcess _process;
        XsltErrorListener(ParasoftFindingsBuildProcess process) {
            _process = process;
        }

        @Override
        public void warning(TransformerException e) throws TransformerException {
            LOG.log(Level.WARNING, e.getMessage(), e);
        }

        @Override
        public void error(TransformerException e) throws TransformerException {
            LOG.log(Level.SEVERE, e.getMessage(), e);
            _process.transformFailed();
        }

        @Override
        public void fatalError(TransformerException e) throws TransformerException {
            LOG.log(Level.SEVERE, e.getMessage(), e);
            _process.transformFailed();
        }
    }

    private String escapeString(String str) {
        return str.replace("|", "||")
                .replace("'", "|'")
                .replace("[", "|[")
                .replace("]", "|]")
                .replace("\n", "|n");
    }

    private class CodeInspectionType {
        String ruleId;
        String description;
        String category;

        public CodeInspectionType(String ruleId, String description, String category) {
            this.ruleId = ruleId;
            this.description = escapeString(description);
            this.category = escapeString(category);
        }
    }

    private class CodeInspectionInstance {
        String typeId;
        String message;
        String file;
        String line;
        String severity;

        public CodeInspectionInstance(String typeId, String message, String file, String line, String severityNumber) {
            this.typeId = typeId;
            this.message = escapeString(message);
            this.file = file;
            this.line = line;
            this.severity = "1".equals(severityNumber) ? "ERROR" : "WARNING";
        }
    }
}
