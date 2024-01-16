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
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page isELIgnored="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>

<view:sp-page>
  <fmt:setLocale value="${requestScope.userLanguage}"/>
  <view:setBundle basename="org.silverpeas.sharing.multilang.fileSharingBundle"/>
  <view:setBundle basename="org.silverpeas.sharing.settings.fileSharingIcons" var="icons"/>
  <c:set var="key" value="${requestScope.Key}"/>
  <c:set var="wallpaper" value="${requestScope.wallpaper}"/>
  <c:set var="ticket" value="${requestScope.attTicket}"/>
  <c:set var="endDate" value=""/>
  <c:if test="${not ticket.continuous}">
    <c:set var="endDate"><fmt:message key="sharing.endDate"/>:
      <view:formatDate value="${ticket.endDate}"/></c:set>
  </c:if>
  <view:sp-head-part noLookAndFeel="true">
    <link href="<c:url value='/util/styleSheets/silverpeas-main.css'/>" type="text/css" rel="stylesheet"/>
    <link href="<c:url value='/sharing/jsp/styleSheets/sharing.css'/>" type="text/css" rel="stylesheet"/>
  </view:sp-head-part>
  <view:sp-body-part id="fileSharingTicket">
    <div class="tableBoard">
      <strong><view:username userId="${ticket.creatorId}" zoom="false"/></strong>
      <fmt:message key="sharing.shareFile"/><br/><br/>
      <img alt="image" src="<c:out value='${requestScope.fileIcon}'/>" id="img_44"/>
      <a target="_blank" href="<c:url value="/LinkFile/Key/${requestScope.Key}/${ticket.resource.name}" />"><strong><c:out value="${ticket.resource.name}"/> </strong></a><br/>
      <fmt:message key="sharing.sizeFile"/> : <c:out value="${requestScope.fileSize}"/><br/>
      <c:out value="${endDate}"/><br/>
      <hr/>
      <em><fmt:message key="sharing.downloadFileHelp"/></em>
    </div>
    <div class="sp_buttonPane">
      <a class="sp_button" target="_blank" href="<c:url value="/LinkFile/Key/${requestScope.Key}/${ticket.resource.name}" />"><fmt:message key="sharing.downloadLink"/></a>
    </div>
  </view:sp-body-part>
</view:sp-page>