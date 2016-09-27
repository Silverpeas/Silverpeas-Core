<%--
  ~ Copyright (C) 2000 - 2016 Silverpeas
  ~
  ~ This program is free software: you can redistribute it and/or modify
  ~ it under the terms of the GNU Affero General Public License as
  ~ published by the Free Software Foundation, either version 3 of the
  ~ License, or (at your option) any later version.
  ~
  ~ As a special exception to the terms and conditions of version 3.0 of
  ~ the GPL, you may redistribute this Program in connection with Free/Libre
  ~ Open Source Software ("FLOSS") applications as described in Silverpeas's
  ~ FLOSS exception. You should have received a copy of the text describing
  ~ the FLOSS exception, and it is also available here:
  ~ "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
  ~
  ~ This program is distributed in the hope that it will be useful,
  ~ but WITHOUT ANY WARRANTY; without even the implied warranty of
  ~ MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  ~ GNU Affero General Public License for more details.
  ~
  ~ You should have received a copy of the GNU Affero General Public License
  ~ along with this program.  If not, see <http://www.gnu.org/licenses/>.
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

<c:set var="event" value="${requestScope.event}"/>

<fmt:message var="back" key="GML.back"/>
<fmt:message var="modifyLabel" key="GML.modify"/>
<fmt:message key="GML.delete" var="deleteLabel"/>

<c:url var="backUri" value="${requestScope.navigationContext.previousNavigationStep.uri}"/>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" id="ng-app" ng-app="silverpeas.usercalendar">
<head>
  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
  <view:looknfeel/>
  <view:includePlugin name="calendar"/>
  <view:script src="/userCalendar/jsp/javaScript/angularjs/services/usercalendar.js"/>
  <view:script src="/userCalendar/jsp/javaScript/angularjs/usercalendar.js"/>
</head>
<body ng-controller="viewController">
<view:browseBar componentId="${componentId}" path="${requestScope.navigationContext}"/>
<view:operationPane>
  <c:if test="${highestUserRole.isGreaterThanOrEquals(adminRole)}">
    <silverpeas-calendar-event-management api="eventMng"
                                          on-occurrence-deleted="goToPage('${backUri}')">
    </silverpeas-calendar-event-management>
    <view:operation
        action="angularjs:editEventOccurrence(calendars, ceo)"
        altText="${modifyLabel}"/>
    <view:operation
        action="angularjs:eventMng.removeOccurrence(ceo)"
        altText="${deleteLabel}"/>
  </c:if>
</view:operationPane>
<view:window>
  <view:frame>
    <view:buttonPane>
      <view:button label="${back}" action="${backUri}"/>
    </view:buttonPane>
  </view:frame>
</view:window>
<view:progressMessage/>

<script type="text/javascript">
  userCalendar.value('context', {
    currentUserId : '${currentUserId}',
    currentUserLanguage : '${currentUserLanguage}',
    component : '${componentId}',
    componentUriBase : '${componentUriBase}',
    userRole : '${highestUserRole}'
  });
</script>
</body>
</html>