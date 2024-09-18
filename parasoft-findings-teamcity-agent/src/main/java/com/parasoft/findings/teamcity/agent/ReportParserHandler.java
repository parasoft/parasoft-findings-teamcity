package com.parasoft.findings.teamcity.agent;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

import java.util.HashSet;
import java.util.Set;

public class ReportParserHandler extends DefaultHandler {
    private final Set<String> reportElements = new HashSet<>();
    private static final Set<String> REQUIRED_ELEMENTS = new HashSet<>();
    private boolean stdViolElementExists = false;
    private boolean dupViolElementExists = false;
    private boolean isSOAtestReport = false;

    public ReportParserHandler() {
        REQUIRED_ELEMENTS.add("ResultsSession");
        REQUIRED_ELEMENTS.add("CodingStandards");
        REQUIRED_ELEMENTS.add("StdViols");
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) {
        reportElements.add(qName);

        if ("ResultsSession".equals(qName)) {
            isSOAtestReport = "SOAtest".equals(attributes.getValue("toolName"));
        }

        if (reportElements.containsAll(REQUIRED_ELEMENTS)) {
            checkViolationElements(qName);
        }
    }

    private void checkViolationElements(String qName) {
        if ("StdViol".equals(qName) || "MetViol".equals(qName) || "FlowViol".equals(qName)) {
            stdViolElementExists = true;
        } else if ("DupViol".equals(qName)) {
            dupViolElementExists = true;
        }
    }

    public boolean hasStdViolElement() {
        return stdViolElementExists;
    }

    public boolean hasDupViolElement() {
        return dupViolElementExists;
    }

    public boolean hasExecElement() {
        return reportElements.contains("Exec");
    }

    public boolean isSOAtestReport() {
        return isSOAtestReport;
    }
}
