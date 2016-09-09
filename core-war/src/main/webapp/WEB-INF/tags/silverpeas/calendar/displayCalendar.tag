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

<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://www.silverpeas.com/tld/silverFunctions" prefix="silfn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>

<c:set var="userLanguage" value="${requestScope.resources.language}"/>
<fmt:setLocale value="${userLanguage}"/>
<view:setBundle basename="org.silverpeas.calendar.multilang.calendarBundle"/>

<%@ attribute name="domSelector" required="false"
              type="java.lang.String"
              description="The DOM selector that permits to identify the container" %>
<%@ attribute name="currentUser" required="true"
              type="org.silverpeas.core.admin.user.model.User"
              description="The current user" %>
<%@ attribute name="componentInstanceId" required="true"
              type="java.lang.String"
              description="The component instance identifier" %>
<%@ attribute name="componentUriBase" required="true"
              type="java.lang.String"
              description="The URI base of the component instance" %>
<%@ attribute name="timeWindowViewContext" required="true"
              type="org.silverpeas.core.web.calendar.CalendarTimeWindowViewContext"
              description="The component instance id associated to the drag and drop" %>

<view:setConstant var="WEEKLY_VIEW_TYPE" constant="org.silverpeas.core.web.calendar.CalendarViewType.WEEKLY"/>
<view:setConstant var="MONTHLY_VIEW_TYPE" constant="org.silverpeas.core.web.calendar.CalendarViewType.MONTHLY"/>

<c:if test="${domSelector == null}">
  <c:set var="domSelector" value="calendar-container"/>
</c:if>
<c:set var="highestUserRole" value="${silfn:getHighestRoleOfCurrentUserOn(componentInstanceId)}"/>

<view:tabs>
  <fmt:message key="GML.week" var="tmp"/>
  <c:set var="tmpAction">javascript:onClick=changeCalendarView('${WEEKLY_VIEW_TYPE}')</c:set>
  <view:tab label="${tmp}" action="${tmpAction}" selected="${timeWindowViewContext.viewType eq WEEKLY_VIEW_TYPE}"/>
  <fmt:message key="GML.month" var="tmp"/>
  <c:set var="tmpAction">javascript:onClick=changeCalendarView('${MONTHLY_VIEW_TYPE}')</c:set>
  <view:tab label="${tmp}" action="${tmpAction}" selected="${timeWindowViewContext.viewType eq MONTHLY_VIEW_TYPE}"/>
</view:tabs>

<div id="${domSelector}"></div>

<script type="text/javascript">
  function changeCalendarView(view) {
    silverpeasFormSubmit(
        sp.formConfig('${componentUriBase}calendars/context').withParam("view", view));
  }

  angular.module('silverpeas').value('context', {
    currentUserId : '${currentUser.id}',
    component : '${componentInstanceId}',
    componentUriBase : '${componentUriBase}',
    userRole: '${highestUserRole}'
  });
  initializeSilverpeasCalendar("#calendar-container");
</script>