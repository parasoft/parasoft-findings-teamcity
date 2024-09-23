package com.parasoft.findings.teamcity.agent;

import jetbrains.buildServer.agent.AgentRunningBuild;
import jetbrains.buildServer.messages.DefaultMessagesInfo;
import org.springframework.util.StringUtils;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.*;

public class PmdReportParseHandler extends DefaultHandler {

    private final AgentRunningBuild _build;
    private final RuleDocumentationUrlProvider _ruleDocumentationUrlProvider;
    private final Set<String> _inspectionTypeIds;

    private String _currentFileName;
    private final StringBuilder _currentTextOfViolationNode;
    private PmdViolation _currentPmdViolation;

    public PmdReportParseHandler(AgentRunningBuild build, RuleDocumentationUrlProvider _ruleDocumentationUrlProvider) {
        this._build = build;
        this._ruleDocumentationUrlProvider = _ruleDocumentationUrlProvider;
        this._currentTextOfViolationNode = new StringBuilder();
        this._inspectionTypeIds = new HashSet<>();
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) {
        _currentTextOfViolationNode.setLength(0);

        if ("file".equals(qName)) {
            _currentFileName = attributes.getValue("name");
        }

        if ("violation".equals(qName)) {
            _currentPmdViolation = new PmdViolation()
                .setFileName(_currentFileName)
                .setRule(attributes.getValue("rule"))
                .setruleSet(attributes.getValue("ruleset"))
                .setruleAnalyzer(attributes.getValue("ruleanalyzer"))
                .setType(attributes.getValue("type"))
                .setcategoryId(attributes.getValue("categoryid"))
                .setruleDescription(attributes.getValue("ruledescription"))
                .setbeginLine(attributes.getValue("beginline"))
                .setPriority(attributes.getValue("priority"));

        }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        _currentTextOfViolationNode.append(ch, start, length);
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (qName.equals("violation")) {
            _currentPmdViolation = _currentPmdViolation.setMessage(_currentTextOfViolationNode.toString().trim());
            uploadInspectionAndType(_currentPmdViolation);
            _currentPmdViolation = null;
        } else if (qName.equals("file")) {
            _currentFileName = null;
        }
    }

    private void uploadInspectionAndType(PmdViolation pmdViolation) {
        String cit_rule = pmdViolation.getRule();
        String cit_category = pmdViolation.getruleSet();
        String ruleAnalyzerId = pmdViolation.getruleAnalyzer();

        String cit_descriptionOrUrl = null;
        if (_ruleDocumentationUrlProvider != null) {
            if (StringUtils.isEmpty(ruleAnalyzerId)) {
                String violationType = pmdViolation.getType();
                String categoryId = pmdViolation.getcategoryId();
                ruleAnalyzerId = mapToAnalyzer(violationType, categoryId);
            }
            cit_descriptionOrUrl = _ruleDocumentationUrlProvider.getRuleDocUrl(ruleAnalyzerId, cit_rule);
        }
        if (cit_descriptionOrUrl == null) {
            cit_descriptionOrUrl = "<html><body>"+escapeString(pmdViolation.getruleDescription())+"</body></html>";
        }
        String ci_message = pmdViolation.getMessage();
        String ci_line = pmdViolation.getbeginLine();
        String ci_fileLocation = pmdViolation.getFileName();
        String ci_severityNumber = pmdViolation.getPriority();

        if (!_inspectionTypeIds.contains(cit_rule)) {
            _inspectionTypeIds.add(cit_rule);
            _build.getBuildLogger().logMessage(DefaultMessagesInfo.createTextMessage
                    ("##teamcity[inspectionType id='"+cit_rule+"' name='"+cit_rule+"' description='"+cit_descriptionOrUrl+"' category='"+escapeString(cit_category)+"']"));
        }
        _build.getBuildLogger().logMessage(DefaultMessagesInfo.createTextMessage
                ("##teamcity[inspection typeId='"+cit_rule+"' message='"+escapeString(ci_message)+"' file='"+ci_fileLocation+"' line='"+ci_line+"' SEVERITY='"+convertSeverity(ci_severityNumber)+"']"));
    }

    private String mapToAnalyzer(String violationType, String categoryId) {
        switch (violationType){
            case "DupViol":
                return "com.parasoft.xtest.cpp.analyzer.static.dupcode";
            case "FlowViol":
                return "com.parasoft.xtest.cpp.analyzer.static.flow";
            case "MetViol":
                return "com.parasoft.xtest.cpp.analyzer.static.metrics";
            default:
                if ("GLOBAL".equals(categoryId)) {
                    return "com.parasoft.xtest.cpp.analyzer.static.global";
                }
                return "com.parasoft.xtest.cpp.analyzer.static.pattern";
        }
    }

    private String convertSeverity(String severityNumber) {
        if (Objects.equals(severityNumber, "1")) {
            return "ERROR";
        } else {
            return "WARNING";
        }
    }

    private String escapeString(String str) {
        return str.replace("|", "||")
                .replace("'", "|'")
                .replace("[", "|[")
                .replace("]", "|]")
                .replace("\n", "|n")
                .replace("\r", "|r");
    }

    private static class PmdViolation {
        private String fileName;
        private String rule;
        private String ruleSet;
        private String ruleAnalyzer;
        private String type;
        private String categoryId;
        private String ruleDescription;
        private String beginLine;
        private String priority;
        private String message;

        public PmdViolation setFileName(String fileName) {
            this.fileName = fileName;
            return this;
        }

        public PmdViolation setRule(String rule) {
            this.rule = rule;
            return this;
        }

        public PmdViolation setruleSet(String ruleSet) {
            this.ruleSet = ruleSet;
            return this;
        }

        public PmdViolation setruleAnalyzer(String ruleAnalyzer) {
            this.ruleAnalyzer = ruleAnalyzer;
            return this;
        }

        public PmdViolation setType(String type) {
            this.type = type;
            return this;
        }

        public PmdViolation setcategoryId(String categoryId) {
            this.categoryId = categoryId;
            return this;
        }

        public PmdViolation setruleDescription(String ruleDescription) {
            this.ruleDescription = ruleDescription;
            return this;
        }

        public PmdViolation setbeginLine(String beginLine) {
            this.beginLine = beginLine;
            return this;
        }

        public PmdViolation setPriority(String priority) {
            this.priority = priority;
            return this;
        }

        public PmdViolation setMessage(String message) {
            this.message = message;
            return this;
        }

        public String getFileName() {
            return fileName;
        }

        public String getRule() {
            return rule;
        }

        public String getruleSet() {
            return ruleSet;
        }

        public String getruleAnalyzer() {
            return ruleAnalyzer;
        }

        public String getType() {
            return type;
        }

        public String getcategoryId() {
            return categoryId;
        }

        public String getruleDescription() {
            return ruleDescription;
        }

        public String getbeginLine() {
            return beginLine;
        }

        public String getPriority() {
            return priority;
        }

        public String getMessage() {
            return message;
        }
    }
}
