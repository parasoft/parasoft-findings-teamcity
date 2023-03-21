
<%@page contentType="text/html" pageEncoding="windows-1252"%>

<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>
<%@ taglib prefix="l" tagdir="/WEB-INF/tags/layout" %>
<%@ taglib prefix="forms" tagdir="/WEB-INF/tags/forms" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<jsp:useBean id="propertiesBean" scope="request" type="jetbrains.buildServer.controllers.BasePropertiesBean"/>
<jsp:useBean id="props" class="com.parasoft.findings.teamcity.server.ParasoftFindingsPropertiesBean" />

<l:settingsGroup title="Parasoft Configuration">
  <tr>
    <th><label for="${props.reportsLocation}">Report location pattern: <l:star/></label></th>
    <td>
      <props:textProperty id="${props.reportsLocation}" name="${props.reportsLocation}" className="longField"/>
      <span class="error" id="error_${props.reportsLocation}"></span>
      <span class="smallNote">Relative location in project check out directory.</span>
    </td>
  </tr>
  <tr>
    <th><label for="${props.settingsLocation}">Settings: </label></th>
    <td>
      <props:textProperty id="${props.settingsLocation}" name="${props.settingsLocation}" className="longField"/>
      <span class="error" id="error_${props.settingsLocation}"></span>
      <span class="smallNote">Absolute or checkout directory relative path to the settings file.</span>
      <span class="smallNote">In order to get access to rules documentation:</span>
      <span class="smallNote">&nbsp;&nbsp;&nbsp;&nbsp;- use dtp.url property to specify DTP server address.</span>
    </td>
  </tr>
</l:settingsGroup>
