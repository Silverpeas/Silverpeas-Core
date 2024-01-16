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

<%@ include file="check.jsp" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://www.silverpeas.com/tld/silverFunctions" prefix="silfn" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>

<c:set var="currentUserLanguage" value="${requestScope.resources.language}"/>
<fmt:setLocale value="${currentUserLanguage}"/>
<view:setBundle bundle="${requestScope.resources.multilangBundle}"/>
<view:setBundle bundle="${requestScope.resources.iconsBundle}" var="icons"/>
<view:setBundle basename="org.silverpeas.calendar.multilang.calendarBundle" var="calendarBundle"/>
<c:url var="componentUriBase" value="${requestScope.componentUriBase}"/>

<fmt:message var="modifyMenuLabel" key="GML.modify"/>

<view:setConstant var="adminRole" constant="org.silverpeas.core.admin.user.model.SilverpeasRole.admin"/>
<c:set var="highestUserRole"        value="${requestScope.highestUserRole}"/>

<c:set var="currentUser"            value="${requestScope.currentUser}"/>
<c:set var="currentUserId"          value="${currentUser.id}"/>
<c:set var="componentId"            value="${requestScope.browseContext[3]}"/>
<c:set var="timeWindowViewContext"  value="${requestScope.timeWindowViewContext}"/>
<jsp:useBean id="timeWindowViewContext" type="org.silverpeas.web.usercalendar.UserCalendarTimeWindowViewContext"/>

<c:set var="occurrenceStartDate" value="${requestScope.occurrenceStartDate}"/>
<c:set var="occurrenceUri" value=""/>
<c:set var="occurrence" value="${requestScope.occurrence}"/>
<c:set var="occurrenceEditable" value="${highestUserRole.isGreaterThanOrEquals(adminRole)}"/>
<c:if test="${occurrence != null}">
  <jsp:useBean id="occurrence" type="org.silverpeas.core.webapi.calendar.CalendarEventOccurrenceEntity"/>
  <c:set var="occurrenceUri" value="${occurrence.occurrenceUri}"/>
  <c:set var="occurrenceEditable" value="${occurrence.canBeModified()}"/>
</c:if>

<c:if test="${!occurrenceEditable}">
  <c:redirect url="/Error403.jsp"/>
</c:if>

<c:url var="backUri" value="${requestScope.navigationContext.previousNavigationStep.uri}"/>

<c:choose>
  <c:when test="${empty occurrenceUri}">
    <fmt:message var="browseBarPathLabel" key="calendar.menu.item.event.add" bundle="${calendarBundle}"/>
  </c:when>
  <c:otherwise>
    <c:set var="browseBarPathLabel">${modifyMenuLabel}</c:set>
  </c:otherwise>
</c:choose>

<fmt:message key="GML.delete" var="deleteLabel"/>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" id="ng-app" ng-app="silverpeas.usercalendar">
<head>
  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
  <view:looknfeel withFieldsetStyle="true"/>
  <view:includePlugin name="calendar"/>
  <view:script src="/userCalendar/jsp/javaScript/angularjs/services/usercalendar.js"/>
  <view:script src="/userCalendar/jsp/javaScript/angularjs/usercalendar.js"/>
</head>
<body ng-controller="editController">
<view:browseBar componentId="${componentId}" path="${requestScope.navigationContext}">
  <view:browseBarElt link="#" label="${browseBarPathLabel}"/>
</view:browseBar>
<view:operationPane>
  <silverpeas-calendar-event-management api="eventMng"
                                        on-created="goToPage('${backUri}')"
                                        on-occurrence-updated="goToPage('${backUri}')"
                                        on-occurrence-deleted="goToPage('${backUri}')">
  </silverpeas-calendar-event-management>
  <c:if test="${not empty occurrenceUri}">
    <view:operation action="angularjs:eventMng.removeOccurrence(ceo)" altText="${deleteLabel}"/>
  </c:if>
</view:operationPane>
<view:window>
  <view:frame>
    <silverpeas-calendar-event-form ng-if="ceo"
                                    api="userCalendarEventApi"
                                    calendar-event-occurrence="ceo"
                                    data="data"
                                    on-add-validated="eventMng.add(event)"
                                    on-modify-occurrence-validated="eventMng.modifyOccurrence(occurrence)"
                                    on-cancel="goToPage('${backUri}')">
      <silverpeas-calendar-event-form-main
          form-validation-priority="0"
          calendars="calendars"
          calendar-event-api="userCalendarEventApi"
          data="data"
          default-visibility="defaultVisibility"
          default-priority="defaultPriority">
      </silverpeas-calendar-event-form-main>
      <silverpeas-calendar-event-form-recurrence
          form-validation-priority="1"
          calendar-event-api="userCalendarEventApi"
          data="data">
      </silverpeas-calendar-event-form-recurrence>
      <silverpeas-calendar-event-form-attendees
          form-validation-priority="2"
          calendar-event-api="userCalendarEventApi"
          data="data">
      </silverpeas-calendar-event-form-attendees>
      <silverpeas-calendar-event-form-attachments
          ng-if="!data.id"
          form-validation-priority="3"
          calendar-event-api="userCalendarEventApi"
          data="data">
      </silverpeas-calendar-event-form-attachments>
    </silverpeas-calendar-event-form>
  </view:frame>
</view:window>
<view:progressMessage/>

<script type="text/javascript">
  userCalendar.value('context', {
    currentUserId : '${currentUserId}',
    currentUserLanguage : '${currentUserLanguage}',
    component : '${componentId}',
    componentUriBase : '${componentUriBase}',
    userRole : '${highestUserRole}',
    zoneId : '${timeWindowViewContext.zoneId.toString()}',
    occurrenceUri : '${occurrenceUri}',
    occurrenceStartDate : '${occurrenceStartDate}'
  });
</script>
</body>
</html>