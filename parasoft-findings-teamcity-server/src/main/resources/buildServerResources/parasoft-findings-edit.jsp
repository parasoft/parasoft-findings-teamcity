
<%@page contentType="text/html" pageEncoding="windows-1252"%>

<%@ page import="com.parasoft.findings.teamcity.common.ReportParserTypes" %>

<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>
<%@ taglib prefix="l" tagdir="/WEB-INF/tags/layout" %>
<%@ taglib prefix="forms" tagdir="/WEB-INF/tags/forms" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<jsp:useBean id="propertiesBean" scope="request" type="jetbrains.buildServer.controllers.BasePropertiesBean"/>
<jsp:useBean id="props" class="com.parasoft.findings.teamcity.server.ParasoftFindingsPropertiesBean" />

<%
    request.setAttribute("parsers", ReportParserTypes.getParsers());
%>

<l:settingsGroup title="Parasoft Configuration">
  <tr>
    <th><label for="${props.reportParserType}">Report parser type: </label></th>
    <td>
      <props:selectProperty id="${props.reportParserType}" name="${props.reportParserType}" enableFilter="true" className="longField">
        <c:forEach items="${parsers}" var="parser">
          <props:option value="${parser.id}">${parser.label}</props:option>
        </c:forEach>
      </props:selectProperty>
      <span class="smallNote">Select report type.</span>
    </td>
  </tr>
  <tr>
    <th><label for="${props.reportsLocation}">Report location pattern: <l:star/></label></th>
    <td>
      <props:textProperty id="${props.reportsLocation}" name="${props.reportsLocation}" className="longField"/>
      <span class="error" id="error_${props.reportsLocation}"></span>
      <span class="smallNote">Relative location in project check out directory.</span>
    </td>
  </tr>
</l:settingsGroup>
