package com.parasoft.findings.teamcity.agent;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.Stack;

public class ReportAnalysisHandler extends DefaultHandler {
    private boolean hasViolsExceptDupViol = false;
    private boolean hasDupViols = false;
    private boolean hasTestExecutions = false;
    private boolean isSOAtestReport = false;
    private final Stack<String> stack;

    public ReportAnalysisHandler() {
        stack = new Stack<>();
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) {
        stack.push(qName);

        if (stack.size() == 1 && !isSOAtestReport && "ResultsSession".equals(stack.get(0))) {
            isSOAtestReport = "SOAtest".equals(attributes.getValue("toolName"));
        }

        if (stack.size() == 4 && !(hasViolsExceptDupViol && hasDupViols)) {
            if ("ResultsSession".equals(stack.get(0)) &&
                "CodingStandards".equals(stack.get(1)) &&
                "StdViols".equals(stack.get(2))) {
                if ("DupViol".equals(stack.get(3))) {
                    hasDupViols = true;
                } else {
                    hasViolsExceptDupViol = true;
                }
            }
        }

        if (stack.size() == 2 && !hasTestExecutions) {
            if ("ResultsSession".equals(stack.get(0)) && "Exec".equals(stack.get(1))) {
                hasTestExecutions = true;
            }
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        stack.pop();
    }

    public boolean hasViolsExceptDupViol() {
        return hasViolsExceptDupViol;
    }

    public boolean hasDupViols() {
        return hasDupViols;
    }

    public boolean hasTestExecutions() {
        return hasTestExecutions;
    }

    public boolean isSOAtestReport() {
        return isSOAtestReport;
    }
}
