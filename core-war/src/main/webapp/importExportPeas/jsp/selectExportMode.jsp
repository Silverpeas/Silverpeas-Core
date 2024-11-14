<%--

    Copyright (C) 2000 - 2024 Silverpeas

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    As a special exception to the terms and conditions of version 3.0 of
    the GPL, you may redistribute this Program in connection with Free/Libre
    Open Source Software ("FLOSS") applications as described in Silverpeas's
    FLOSS exception.  You should have received a copy of the text describing
    the FLOSS exception, and it is also available here:
    "https://www.silverpeas.org/legal/floss_exception.html"

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.

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

<view:sp-page>
  <view:sp-head-part title="Export">
  <script type="text/javascript">
    function exportData() {
      $.progressMessage();
      document.exportForm.submit();
    }
  </script>
  </view:sp-head-part>
  <view:sp-body-part>
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
              <br/><br/>
              <legend><fmt:message key="importExportPeas.export.options"/></legend>
              <input type="checkbox" name="UseIdForFolders" value="true"/> <fmt:message key="importExportPeas.export.useIdForFolders"/><br/>
            </fieldset>
          </form>
        </view:board>
        <br/>
          <view:buttonPane verticalPosition="center">
          <view:button label="${exportButton}" action="javascript:exportData();"/>
          <view:button label="${closeButton}" action="javascript:window.close();"/>
          </view:buttonPane>
      </view:frame>
    </view:window>
    <view:progressMessage/>
  </view:sp-body-part>
</view:sp-page>