<%--
  ~ Copyright (C) 2000 - 2019 Silverpeas
  ~
  ~ This program is free software: you can redistribute it and/or modify
  ~ it under the terms of the GNU Affero General Public License as
  ~ published by the Free Software Foundation, either version 3 of the
  ~ License, or (at your option) any later version.
  ~
  ~ As a special exception to the terms and conditions of version 3.0 of
  ~ the GPL, you may redistribute this Program in connection with Free/Libre
  ~ Open Source Software ("FLOSS") applications as described in Silverpeas's
  ~ FLOSS exception.  You should have received a copy of the text describing
  ~ the FLOSS exception, and it is also available here:
  ~ "https://www.silverpeas.org/legal/floss_exception.html"
  ~
  ~ This program is distributed in the hope that it will be useful,
  ~ but WITHOUT ANY WARRANTY; without even the implied warranty of
  ~ MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  ~ GNU Affero General Public License for more details.
  ~
  ~ You should have received a copy of the GNU Affero General Public License
  ~ along with this program.  If not, see <http://www.gnu.org/licenses/>.
  --%>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="org.silverpeas.core.util.URLUtil" %>
<%@ page import="org.silverpeas.core.viewer.model.ViewerSettings" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://www.silverpeas.com/tld/silverFunctions" prefix="silfn" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>

<c:set var="contentUrl" value="${requestScope.contentUrl}"/>
<c:set var="downloadEnabled" value="${requestScope.downloadEnabled}"/>
<c:set var="userLanguage" value="${requestScope.userLanguage}"/>
<c:set var="documentView" value="${requestScope.documentView}"/>
<jsp:useBean id="documentView" type="org.silverpeas.core.viewer.model.DocumentView"/>
<c:set var="displayViewerPath" value="${requestScope.displayViewerPath}"/>
<c:set var="displayLicenseKey" value="<%=ViewerSettings.getLicenceKey()%>"/>

<!DOCTYPE html>
<html>
<head>
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width, initial-scale=1, maximum-scale=1">
  <meta http-equiv="X-UA-Compatible" content="IE=edge">
  <style type="text/css">
    #viewer {
      width: ${param.width}px;
      height: ${param.height}px;
    }
  </style>
  <view:includePlugin name="minimalsilverpeas"/>
  <view:includePlugin name="fpviewer"/>
  <script type="text/javascript">
    whenSilverpeasReady(function() {
      window.webContext = '<%=URLUtil.getApplicationURL()%>';
      window.renderViewer({
        url : '${contentUrl}',
        nbPages : ${documentView.nbPages},
        documentSplit : ${documentView.documentSplit},
        searchDataComputed : ${documentView.areSearchDataComputed()},
        displayViewerPath : '${displayViewerPath}',
        displayLicenseKey : '${displayLicenseKey}',
        language : '${userLanguage}'
      });
    });
  </script>
</head>
<body tabindex="1">
<div id="viewer"></div>
</body>
</html>