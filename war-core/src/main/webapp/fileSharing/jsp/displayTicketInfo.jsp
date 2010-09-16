<%--

    Copyright (C) 2000 - 2009 Silverpeas

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    As a special exception to the terms and conditions of version 3.0 of
    the GPL, you may redistribute this Program in connection with Free/Libre
    Open Source Software ("FLOSS") applications as described in Silverpeas's
    FLOSS exception.  You should have received a copy of the text describing
    the FLOSS exception, and it is also available here:
    "http://repository.silverpeas.com/legal/licensing"

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

--%>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%@ page isELIgnored="false"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>

<view:setBundle basename="com.silverpeas.external.filesharing.multilang.fileSharingBundle"/>
<view:setBundle basename="com.silverpeas.external.filesharing.settings.fileSharingIcons" var="icons" />
<html>
<head>
<script type="text/javascript" src="<c:url value="/util/javaScript/animation.js" />"></script>
<style type="text/css">
td { font-family: "Verdana", "Arial", sans-serif; font-size: 10px}
</style>
</head>

<c:set var="attachment" value="${requestScope.attAttachment}" />
<c:set var="document" value="${requestScope.attDocument}" />
<c:set var="documentVersion" value="${requestScope.attDocumentVersion}" />
<c:set var="key" value="${requestScope.Key}" />

<body>
<br/>
<center>
<c:if test="${attachment!=null}">
<table>
	<tr><td><fmt:message key="fileSharing.nameFile" /> :</td><td><c:out value="${attachment.logicalName}"/></td></tr>
	<tr><td><fmt:message key="fileSharing.sizeFile" /> :</td><td><c:out value="${attachment.attachmentFileSize}"/></td></tr>
	<tr><td><fmt:message key="fileSharing.downloadLink" /> :</td><td><a href="<c:url value="/LinkFile/Key/${requestScope.Key}/${attachment.logicalName}" />" ><fmt:message key="fileSharing.downloadLink" /></a></td></tr>
</table>
</c:if>
<c:if test="${document!=null}">
<table>
	<tr><td><fmt:message key="fileSharing.nameFile" /> :</td><td><c:out value="${document.name}"/> v<c:out value="${documentVersion.majorNumber}"/>.<c:out value="${documentVersion.minorNumber}"/> (<c:out value="${documentVersion.logicalName}"/>)</td></tr>
	<tr><td><fmt:message key="fileSharing.sizeFile" /> :</td><td><c:out value="${documentVersion.displaySize}"/></td></tr>
	<tr><td><fmt:message key="fileSharing.downloadLink" /> :</td><td><a href="<c:url value="/LinkFile/Key/${requestScope.Key}/${documentVersion.logicalName}" />" ><fmt:message key="fileSharing.downloadLink" /></a></td></tr>
</table>
</c:if>
</center>
</body>
</html>