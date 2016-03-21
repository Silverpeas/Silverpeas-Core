<%--

    Copyright (C) 2000 - 2013 Silverpeas

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    As a special exception to the terms and conditions of version 3.0 of
    the GPL, you may redistribute this Program in connection with Free/Libre
    Open Source Software ("FLOSS") applications as described in Silverpeas's
    FLOSS exception.  You should have received a copy of the text describing
    the FLOSS exception, and it is also available here:
    "http://www.silverpeas.org/docs/core/legal/floss_exception.html"

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

--%>

<%@page import="org.silverpeas.core.importexport.control.ImportExport"%>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>

<fmt:setLocale value="${sessionScope[sessionController].language}"/>
<view:setBundle bundle="${requestScope.resources.multilangBundle}"/>

<fmt:message var="browseBarExport" key="importExportPeas.Export"/>
<fmt:message var="closeButton" key="GML.cancel"/>
<fmt:message var="exportButton" key="GML.validate"/>

<html>
<head>
<title>ZIP export</title>
<view:looknfeel/>
<script type="text/javascript">
function exportData() {
	$.progressMessage();
	document.exportForm.submit();
}
</script>
</head>
<body>
<view:browseBar>
  <view:browseBarElt link="" label="${browseBarExport}"/>
</view:browseBar>
<view:window>
  <view:frame>
    <view:board>
    <div class="inlineMessage">
	<fmt:message key="importExportPeas.export.warning"/>
    </div>
    <form name="exportForm" action="/silverpeas/RimportExportPeas/jsp/ExportSavedItems" method="post">
      <fieldset>
	<legend><fmt:message key="importExportPeas.export.what"/></legend>
	<input type="radio" name="ExportMode" value="<%=ImportExport.EXPORT_FILESONLY %>" checked="checked"/> <fmt:message key="importExportPeas.export.mode.filesOnly"/><br/>
	<input type="radio" name="ExportMode" value="<%=ImportExport.EXPORT_PUBLICATIONSONLY %>"/> <fmt:message key="importExportPeas.export.mode.pubsOnly"/><br/>
	<input type="radio" name="ExportMode" value="<%=ImportExport.EXPORT_FULL %>"/> <fmt:message key="importExportPeas.export.mode.full"/>
      </fieldset>
    </form>
    </view:board>
    <br/>
    <center>
      <view:buttonPane>
	<view:button label="${exportButton}" action="javascript:exportData();"/>
	<view:button label="${closeButton}" action="javascript:window.close();"/>
      </view:buttonPane>
    </center>
  </view:frame>
</view:window>
<view:progressMessage/>
</body>
</html>