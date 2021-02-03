<%--

    Copyright (C) 2000 - 2021 Silverpeas

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
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%@ include file="../portletImport.jsp"%>

<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://www.silverpeas.com/tld/silverFunctions" prefix="silfn" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>

<portlet:defineObjects/>
<portlet:actionURL var="actionURL"/>

<c:set var="_userLanguage" value="<%=language%>" scope="request"/>
<jsp:useBean id="_userLanguage" type="java.lang.String" scope="request"/>
<fmt:setLocale value="${_userLanguage}"/>
<view:setBundle basename="org.silverpeas.notificationserver.channel.silvermail.multilang.silvermail"/>
<view:setBundle basename="org.silverpeas.notificationserver.channel.silvermail.settings.silvermailIcons" var="icons"/>

<fmt:message var="dateLabel" key="date"/>
<fmt:message var="sourceLabel" key="source"/>
<fmt:message var="fromLabel" key="from"/>
<fmt:message var="urlLabel" key="url"/>
<fmt:message var="subjectLabel" key="subject"/>

<fmt:message var="linkIconUrl" key="silvermail.link" bundle="${icons}"/>

<view:includePlugin name="userNotification"/>
<script type="text/javascript">

  var _reloadList = function() {
    var ajaxConfig = sp.ajaxConfig('${actionURL}');
    return sp.load('#silvermail-portlet-list', ajaxConfig, true);
  };

  window.USERNOTIFICATION_PROMISE.then(function() {
    spUserNotification.addEventListener('userNotificationRead', _reloadList,
        "SILVERMAIL_portlet_UserNotificationRead");
    spUserNotification.addEventListener('userNotificationDeleted', _reloadList,
        "SILVERMAIL_portlet_UserNotificationDeleted");
    spUserNotification.addEventListener('userNotificationReceived', _reloadList,
        "SILVERMAIL_portlet_UserNotificationReceived");
    spUserNotification.addEventListener('userNotificationCleared', _reloadList,
        "SILVERMAIL_portlet_UserNotificationCleared");
  });
</script>
<div id="silvermail-portlet-list">
  <view:arrayPane var="userNotificationPortlet" routingAddress="${actionURL}">
    <view:arrayColumn title="${dateLabel}" sortable="true"/>
    <view:arrayColumn title="${urlLabel}" sortable="false"/>
    <view:arrayColumn title="${subjectLabel}" sortable="true"/>
    <view:arrayColumn title="${fromLabel}" sortable="true"/>
    <view:arrayColumn title="${sourceLabel}" sortable="true"/>
    <view:arrayLines var="userNotification" items="${requestScope.Messages}">
      <c:set var="unreadClasses" value="${userNotification.readen eq 0 ? 'unread-user-notification-inbox' : ''}"/>
      <view:arrayLine classes="ArrayCell ${unreadClasses}">
        <c:set var="viewUrl" value="javascript:onClick=spUserNotification.view(${userNotification.id})"/>
        <view:arrayCellText compareOn="${userNotification.id}">
          ${silfn:formatDate(userNotification.date, _userLanguage)}
        </view:arrayCellText>
        <view:arrayCellText>
          <c:if test="${not empty userNotification.url}">
            <a href="${userNotification.url}" class="sp-permalink" target="_top"><img src="<c:url value="${linkIconUrl}"/>" alt="" border="0"/></a>
          </c:if>
        </view:arrayCellText>
        <view:arrayCellText compareOn="${fn:toLowerCase(userNotification.subject)}">
          <a href="${viewUrl}">${silfn:escapeHtml(userNotification.subject)}</a>
        </view:arrayCellText>
        <view:arrayCellText compareOn="${fn:toLowerCase(userNotification.senderName)}">
          <a href="${viewUrl}">${silfn:escapeHtml(userNotification.senderName)}</a>
        </view:arrayCellText>
        <view:arrayCellText compareOn="${fn:toLowerCase(userNotification.source)}">
          ${silfn:escapeHtml(userNotification.source)}
        </view:arrayCellText>
      </view:arrayLine>
    </view:arrayLines>
  </view:arrayPane>
  <script type="text/javascript">
    whenSilverpeasReady(function() {
      sp.arrayPane.ajaxControls('#silvermail-portlet-list');
    });
  </script>
</div>