
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
</l:settingsGroup>
