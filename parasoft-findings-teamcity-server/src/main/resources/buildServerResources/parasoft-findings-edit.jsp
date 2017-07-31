
<%@page contentType="text/html" pageEncoding="windows-1252"%>

<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>
<%@ taglib prefix="l" tagdir="/WEB-INF/tags/layout" %>
<%@ taglib prefix="admin" tagdir="/WEB-INF/tags/admin" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="bs" tagdir="/WEB-INF/tags" %>

<jsp:useBean id="propertiesBean" scope="request" type="jetbrains.buildServer.controllers.BasePropertiesBean"/>
<jsp:useBean id="props" class="com.parasoft.findings.teamcity.server.ParasoftFindingsPropertiesBean" />

<l:settingsGroup title="Parasoft Configuration">
  <tr>
    <th><label for="${props.soatestReportsLocation}">SOAtest Reports Location: <l:star /></label></th>
    <td>
        <props:textProperty id="${props.soatestReportsLocation}" name="${props.soatestReportsLocation}" className="longField"/>
        <span class="smallNote">Relative location in project check out directory.</span>
    </td>
  </tr>
</l:settingsGroup>
