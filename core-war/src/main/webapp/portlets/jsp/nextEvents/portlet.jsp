<%--
  ~ Copyright (C) 2000 - 2022 Silverpeas
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
<%@page import="org.silverpeas.core.admin.component.model.PersonalComponent"%>
<%@page import="org.silverpeas.core.admin.component.model.PersonalComponentInstance"%>
<%@ page import="org.silverpeas.core.admin.user.model.User" %>
<%@ page import="org.silverpeas.web.usercalendar.UserCalendarSettings" %>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%@ include file="../portletImport.jsp"%>

<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<view:setConstant var="adminRole" constant="org.silverpeas.core.admin.user.model.SilverpeasRole.admin"/>
<c:set var="componentInstance" value="<%=PersonalComponentInstance.from(User.getCurrentRequester(), PersonalComponent.getByName(UserCalendarSettings.COMPONENT_NAME).orElse(null))%>"/>
<jsp:useBean id="componentInstance" type="org.silverpeas.core.admin.component.model.PersonalComponentInstance"/>
<c:url var="componentUriBase" value="<%=URLUtil.getComponentInstanceURL(componentInstance.getId())%>"/>
<c:set var="userZoneId" value="${componentInstance.user.userPreferences.zoneId}"/>

<c:set var="noEventLabel"><%=message.getString("NoEvents")%></c:set>

<c:set var="nbMaxItems" value="${requestScope.nbEvents}"/>

<portlet:defineObjects/>

<view:includePlugin name="calendar"/>
<view:script src="/portlets/jsp/nextEvents/javaScript/angularjs/services/nextevents.js"/>
<view:script src="/portlets/jsp/nextEvents/javaScript/angularjs/nextevents.js"/>

<div class="portlet" id="ng-app" ng-app="silverpeas.nextevents" ng-controller="mainController">
  <silverpeas-calendar-event-occurrence-list
      ng-if="occurrences"
      no-occurrence-label="${noEventLabel}"
      occurrences="occurrences"
      on-event-occurrence-click="gotToEventOccurrence(occurrence)">
  </silverpeas-calendar-event-occurrence-list>
  <script type="text/javascript">
    nextEvents.value('context', {
      currentUserId : '${componentInstance.user.id}',
      currentUserLanguage : '${componentInstance.user.userPreferences.language}',
      component : '${componentInstance.id}',
      componentUriBase : '${componentUriBase}',
      userRole: '${adminRole}',
      zoneId : '${userZoneId}',
      limit : '${nbMaxItems}'
    });
  </script>
</div>