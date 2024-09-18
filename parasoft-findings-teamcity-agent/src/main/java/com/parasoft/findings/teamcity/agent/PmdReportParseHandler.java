package com.parasoft.findings.teamcity.agent;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.ArrayList;
import java.util.List;

public class PmdReportParseHandler extends DefaultHandler {
    private String currentFileName;
    private StringBuilder currentTextOfViolationNode = new StringBuilder();
    private PmdViolation currentPmdViolation;

    private final List<PmdViolation> pmdViolations;

    public PmdReportParseHandler() {
        pmdViolations = new ArrayList<>();
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) {
        currentTextOfViolationNode.setLength(0);

        if ("file".equals(qName)) {
            currentFileName = attributes.getValue("name");
        }

        if ("violation".equals(qName)) {
            currentPmdViolation = new PmdViolation()
                .setFileName(currentFileName)
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
        currentTextOfViolationNode.append(ch, start, length);
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (qName.equals("violation")) {
            currentPmdViolation = currentPmdViolation.setMessage(currentTextOfViolationNode.toString().trim());
            pmdViolations.add(currentPmdViolation);
            currentPmdViolation = null;
        } else if (qName.equals("file")) {
            currentFileName = null;
        }
    }

    public List<PmdViolation> getPmdViolations() {
        return pmdViolations;
    }

    public class PmdViolation {
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
