<%--

    Copyright (C) 2000 - 2019 Silverpeas

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
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

--%>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>

<%@ page import="org.silverpeas.core.util.DateUtil" %>
<%@ page import="org.silverpeas.core.util.file.FileRepositoryManager" %>

<c:set var="exportReport" value="${requestScope.ExportReport}"/>
<jsp:useBean id="exportReport" type="org.silverpeas.core.importexport.report.ExportReport"/>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
  <title>ZIP export</title>
  <view:looknfeel/>
</head>
<body>
<fmt:setLocale value="${sessionScope[sessionController].language}"/>
<view:setBundle bundle="${requestScope.resources.multilangBundle}"/>
<fmt:message var="browseBarExport" key="importExportPeas.Export"/>
<fmt:message var="closeButton" key="GML.close"/>
<view:browseBar>
  <view:browseBarElt link="" label="${browseBarExport}"/>
</view:browseBar>
<view:window>
  <view:frame>
    <view:board>
      <c:choose>
        <c:when test="${exportReport.error != null}">
          <c:forEach var="element" items="${exportReport.error.stackTrace}" >
            <c:out value="${element}"/> <br/>
          </c:forEach>
        </c:when>
        <c:otherwise>
          <table>
            <tr>
              <td class="txtlibform"><fmt:message key="importExportPeas.File"/> :</td>
              <td><a href="${exportReport.zipFilePath}">${exportReport.zipFileName}</a>
              <a href="${exportReport.zipFilePath}"><img src="<%=FileRepositoryManager.getFileIcon("zip")%>" border="0" align="absmiddle" alt="${exportReport.zipFileName}"/></a></td>
            </tr>
            <tr>
              <td class="txtlibform"><fmt:message key="importExportPeas.FileSize"/> :</td>
              <td><%=FileRepositoryManager.formatFileSize(exportReport.getZipFileSize())%></td>
            </tr>
            <tr>
              <td class="txtlibform"><fmt:message key="importExportPeas.ExportDuration"/> :</td>
              <td><%=DateUtil.formatDuration(exportReport.getDuration())%></td>
            </tr>
          </table>
        </c:otherwise>
      </c:choose>
    </view:board>
    <view:buttonPane>
      <view:button label="${closeButton}" action="javaScript:window.close();"/>
    </view:buttonPane>
  </view:frame>
</view:window>
</body>
</html>