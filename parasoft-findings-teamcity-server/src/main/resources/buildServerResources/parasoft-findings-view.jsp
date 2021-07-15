
<%@page contentType="text/html" pageEncoding="windows-1252"%>

<%@ page import="java.util.Map" %>
<%@ page import="com.parasoft.findings.teamcity.common.ParasoftFindingsProperties" %>
<%@ page import="com.parasoft.findings.teamcity.server.ParasoftFindingsRunType" %>

<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>
<%@ taglib prefix="l" tagdir="/WEB-INF/tags/layout" %>
<%@ taglib prefix="forms" tagdir="/WEB-INF/tags/forms" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<jsp:useBean id="propertiesBean" scope="request" type="jetbrains.buildServer.controllers.BasePropertiesBean"/>
<jsp:useBean id="props" class="com.parasoft.findings.teamcity.server.ParasoftFindingsPropertiesBean" />

  <div class="parameter">
    <label for="${props.reportsLocation}">Report location pattern: <l:star /></label>
    <props:displayValue name="${props.reportsLocation}" emptyValue="not specified"/>
  </div>
