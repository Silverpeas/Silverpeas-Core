<%--
  ~ Copyright (C) 2000 - 2024 Silverpeas
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
  ~ along with this program.  If not, see <https://www.gnu.org/licenses/>.
  --%>

<%@ page import="org.silverpeas.kernel.bundle.ResourceLocator" %>

<%@ include file="check.jsp" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://www.silverpeas.com/tld/silverFunctions" prefix="silfn" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>

<c:set var="currentUserLanguage" value="${sessionScope['SilverSessionController'].favoriteLanguage}"/>
<fmt:setLocale value="${currentUserLanguage}"/>
<view:setBundle bundle="${requestScope.resources.multilangBundle}"/>
<view:setBundle bundle="${requestScope.resources.iconsBundle}" var="icons"/>
<view:setBundle basename="org.silverpeas.calendar.multilang.calendarBundle" var="calendarBundle"/>
<c:url var="componentUriBase" value="${requestScope.componentUriBase}"/>

<c:set var="currentUser"            value="${requestScope.currentUser}"/>
<c:set var="currentUserId"          value="${currentUser.id}"/>
<c:set var="componentId"            value="${requestScope.browseContext[3]}"/>
<c:set var="highestUserRole"        value="${requestScope.highestUserRole}"/>
<c:set var="timeWindowViewContext"  value="${requestScope.timeWindowViewContext}"/>
<jsp:useBean id="timeWindowViewContext" type="org.silverpeas.web.usercalendar.UserCalendarTimeWindowViewContext"/>
<c:set var="userMainCalendar"  value="${requestScope.userMainCalendar}"/>
<jsp:useBean id="userMainCalendar" type="org.silverpeas.core.webapi.calendar.CalendarEntity"/>
<c:set var="nextEventViewLimit" value='<%=ResourceLocator.getSettingBundle("org.silverpeas.calendar.settings.calendar").getInteger("calendar.nextEvents.limit")%>'/>

<view:setConstant var="adminRole" constant="org.silverpeas.core.admin.user.model.SilverpeasRole.ADMIN"/>

<fmt:message key="usercalendar.name" var="userCalendarLabel"/>
<fmt:message key="GML.print" var="printLabel" bundle="${calendarBundle}"/>
<fmt:message key="calendar.menu.item.calendar.create" var="createCalendarLabel" bundle="${calendarBundle}"/>
<fmt:message key="calendar.menu.item.calendar.synchronized.create" var="createSynchronizedCalendarLabel" bundle="${calendarBundle}"/>
<fmt:message key="calendar.menu.item.event.add" var="addEventLabel" bundle="${calendarBundle}"/>
<fmt:message key="usercalendar.menu.item.exportyourevents" var="exportYourEventLabel"/>
<fmt:message key="calendar.menu.item.event.import" var="importEventLabel" bundle="${calendarBundle}"/>
<fmt:message key="usercalendar.menu.item.viewuserparticipation" var="viewUserParticipationEventLabel"/>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" id="ng-app" ng-app="silverpeas.usercalendar">
<head>
  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
  <view:looknfeel/>
  <view:includePlugin name="calendar"/>
  <view:script src="/userCalendar/jsp/javaScript/angularjs/services/usercalendar.js"/>
  <view:script src="/userCalendar/jsp/javaScript/angularjs/usercalendar.js"/>
</head>
<body ng-controller="calendarController">
<view:operationPane>
  <c:if test="${highestUserRole.isGreaterThanOrEquals(adminRole)}">
    <view:operation action="javascript:print()" altText="${printLabel}"/>
    <view:operationSeparator />
    <silverpeas-calendar-management api="calMng"
                                    on-created="userCalendar.addCalendar(calendar)"
                                    on-imported-events="userCalendar.refetchCalendars()"></silverpeas-calendar-management>
    <fmt:message key="userCalendar.icons.addEvent" var="opIcon" bundle="${icons}"/>
    <c:url var="opIcon" value="${opIcon}"/>
    <view:operationOfCreation action="angularjs:newEvent()"
                              altText="${addEventLabel}" icon="${opIcon}"/>
    <fmt:message key="userCalendar.icons.addCalendar" var="opIcon" bundle="${icons}"/>
    <c:url var="opIcon" value="${opIcon}"/>
    <view:operationOfCreation action="angularjs:calMng.add()"
                              altText="${createCalendarLabel}" icon="${opIcon}"/>
    <fmt:message key="userCalendar.icons.addSynchronizedCalendar" var="opIcon" bundle="${icons}"/>
    <c:url var="opIcon" value="${opIcon}"/>
    <view:operationOfCreation action="angularjs:calMng.add(true)"
                              altText="${createSynchronizedCalendarLabel}" icon="${opIcon}"/>
    <view:operationSeparator />
    <view:operation action="${userMainCalendar.getURI()}/export/ical"
                              altText="${exportYourEventLabel}"/>
    <view:operation action="angularjs:calMng.importICalEvents(userCalendar.getCalendars())"
                    altText="${importEventLabel}"/>
    <view:operationSeparator />
    <view:operation action="angularjs:selectUsersForViewingTheirEvents()"
                              altText="${viewUserParticipationEventLabel}"/>
  </c:if>
</view:operationPane>
<input type="text" ng-disabled="true" style="display: none;" class="user-ids"
       id="userParticipation-userIds"
       name="userParticipationUserPanelCurrentUserIds"
       ng-model="participationIds"
       ng-list>
<view:window>
  <view:frame>
    <view:areaOfOperationOfCreation/>
    <silverpeas-calendar api="userCalendar"
                         participation-user-ids="participationIds"
                         on-day-click="newEvent(startMoment)"
                         on-event-occurrence-view="viewEventOccurrence(occurrence)"
                         on-event-occurrence-modify="editEventOccurrence(occurrence)">
    </silverpeas-calendar>
  </view:frame>
</view:window>

<script type="text/javascript">
  userCalendar.value('context', {
    currentUserId : '${currentUserId}',
    currentUserLanguage : '${currentUserLanguage}',
    component : '${componentId}',
    componentUriBase : '${componentUriBase}',
    userRole: '${highestUserRole}',
    zoneId : '${timeWindowViewContext.zoneId.toString()}',
    limit : ${nextEventViewLimit}
  });
</script>
</body>
</html>