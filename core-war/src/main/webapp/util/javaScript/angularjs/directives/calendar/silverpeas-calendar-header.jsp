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

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://www.silverpeas.com/tld/silverFunctions" prefix="silfn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>

<c:set var="userLanguage" value="${sessionScope['SilverSessionController'].favoriteLanguage}"/>
<fmt:setLocale value="${userLanguage}"/>
<view:setBundle basename="org.silverpeas.calendar.multilang.calendarBundle"/>

<view:setConstant var="DAILY_VIEW_TYPE" constant="org.silverpeas.core.web.calendar.CalendarViewType.DAILY"/>
<view:setConstant var="WEEKLY_VIEW_TYPE" constant="org.silverpeas.core.web.calendar.CalendarViewType.WEEKLY"/>
<view:setConstant var="MONTHLY_VIEW_TYPE" constant="org.silverpeas.core.web.calendar.CalendarViewType.MONTHLY"/>

<fmt:message key="GML.day" var="dayLabel"/>
<fmt:message key="GML.week" var="weekLabel"/>
<fmt:message key="GML.month" var="monthLabel"/>

<div class="silverpeas-calendar-header">
  <silverpeas-tabs>
    <silverpeas-tabs-item label="${dayLabel}" on-click="$ctrl.view({type:'${DAILY_VIEW_TYPE}'})"
                          selected="$ctrl.timeWindowViewContext.viewType == '${DAILY_VIEW_TYPE}'"></silverpeas-tabs-item>
    <silverpeas-tabs-item label="${weekLabel}" on-click="$ctrl.view({type:'${WEEKLY_VIEW_TYPE}'})"
                          selected="$ctrl.timeWindowViewContext.viewType == '${WEEKLY_VIEW_TYPE}'"></silverpeas-tabs-item>
    <silverpeas-tabs-item label="${monthLabel}" on-click="$ctrl.view({type:'${MONTHLY_VIEW_TYPE}'})"
                          selected="$ctrl.timeWindowViewContext.viewType == '${MONTHLY_VIEW_TYPE}'"></silverpeas-tabs-item>
  </silverpeas-tabs>
  <div class="sousNavBulle">
    <div id="navigation">
      <div id="currentScope">
        <span id="today"> <a href="#" ng-click="$ctrl.timeWindow({type:'today'})" onfocus="this.blur()"><fmt:message key="GML.Today"/></a></span>
        <a href="#" ng-click="$ctrl.timeWindow({type:'previous'})" onfocus="this.blur()"><img align="top" border="0" alt="" src="<c:url value="/util/icons/arrow/arrowLeft.gif"/>"></a>
        <a href="#" ng-click="$ctrl.timeWindow({type:'next'})" onfocus="this.blur()"><img align="top" border="0" alt="" src="<c:url value="/util/icons/arrow/arrowRight.gif"/>"></a>
        <span class="txtnav">{{$ctrl.timeWindowViewContext.referencePeriodLabel}}</span>
      </div>
    </div>
  </div>
</div>