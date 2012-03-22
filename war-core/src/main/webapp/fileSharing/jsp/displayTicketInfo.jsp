<%--

    Copyright (C) 2000 - 2012 Silverpeas

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    As a special exception to the terms and conditions of version 3.0 of
    the GPL, you may redistribute this Program in connection with Free/Libre
    Open Source Software ("FLOSS") applications as described in Silverpeas's
    FLOSS exception.  You should have received a copy of the text describing
    the FLOSS exception, and it is also available here:
    "http://www.silverpeas.org/legal/licensing"

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

<c:set var="attachment" value="${requestScope.attAttachment}" />
<c:set var="document" value="${requestScope.attDocument}" />
<c:set var="documentVersion" value="${requestScope.attDocumentVersion}" />
<c:set var="key" value="${requestScope.Key}" />
<c:set var="wallpaper" value="${requestScope.wallpaper}"/>
<c:set var="ticket" value="${requestScope.attTicket}"/>
<c:set var="endDate" value=""/>
<c:if test="${not ticket.continuous}">
  <c:set var="endDate"><fmt:message key="fileSharing.endDate"/>: <view:formatDate value="${ticket.endDate}"/></c:set>
</c:if>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
  <head>
    <script type="text/javascript" src="<c:url value="/util/javaScript/animation.js" />"></script>

    <link href="<c:url value='/util/styleSheets/silverpeas_light_style.css'/>" type="text/css" rel="stylesheet">
    <link href="<c:url value='/fileSharing/jsp/styleSheets/filesharing.css'/>" type="text/css" rel="stylesheet"/>

    <style type="text/css">
      <!--
      body {
        background-image: url('<c:out value="${wallpaper}" escapeXml="false"/>');;
      }
      -->
    </style>
  </head>


  <body id="fileSharingTicket">
    <c:if test="${attachment!=null}">
      <div class="tableBoard">

        <strong><c:out value="${ticket.creator.displayedName}"/></strong> <fmt:message key="fileSharing.shareFile"/><br/><br/>

        <img alt="image" src="<c:out value='${ticket.attachmentDetail.attachmentIcon}'/>" id="img_44"/>

        <a target="_blank" href="<c:url value="/LinkFile/Key/${requestScope.Key}/${attachment.logicalName}" />" ><strong><c:out value="${attachment.logicalName}"/> </strong></a><br/>
        <fmt:message key="fileSharing.sizeFile" /> : <c:out value="${attachment.attachmentFileSize}"/><br/>
        <c:out value="${endDate}"/><br/>
        <hr/>
        <i><fmt:message key="fileSharing.downloadFileHelp"/></i>
      </div>

      <div class="center">
        <span class="milieuBoutonV5">
          <a target="_blank" href="<c:url value="/LinkFile/Key/${requestScope.Key}/${attachment.logicalName}" />" ><fmt:message key="fileSharing.downloadLink"/></a>
        </span>
      </div>
    </c:if>
    <c:if test="${document!=null}">
      <div class="tableBoard">

        <strong><c:out value="${ticket.creator.displayedName}"/></strong> <fmt:message key="fileSharing.shareFile"/><br/><br/>

        <img alt="image" src="<c:out value='${documentVersion.documentIcon}'/>" id="img_44"/>

        <a target="_blank" href="<c:url value="/LinkFile/Key/${requestScope.Key}/${documentVersion.logicalName}" />"><strong><c:out value="${document.name}"/> v<c:out value="${documentVersion.majorNumber}"/>.<c:out value="${documentVersion.minorNumber}"/> (<c:out value="${documentVersion.logicalName}"/>)</strong></a><br/>
        <fmt:message key="fileSharing.sizeFile" /> : <strong><c:out value="${documentVersion.displaySize}"/></strong><br/>
        <c:out value="${endDate}"/><br/>
        <hr/>
        <i><fmt:message key="fileSharing.downloadFileHelp"/></i>
      </div>

      <div class="center">
        <span class="milieuBoutonV5">
          <a target="_blank" href="<c:url value="/LinkFile/Key/${requestScope.Key}/${documentVersion.logicalName}" />">
            <fmt:message key="fileSharing.downloadLink" />
          </a>
        </span>
      </div>
    </c:if>
  </body>
</html>