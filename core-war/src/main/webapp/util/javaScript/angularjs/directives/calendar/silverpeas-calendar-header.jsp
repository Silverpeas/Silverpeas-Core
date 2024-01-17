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

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://www.silverpeas.com/tld/silverFunctions" prefix="silfn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>

<c:set var="userLanguage" value="${sessionScope['SilverSessionController'].favoriteLanguage}"/>
<fmt:setLocale value="${userLanguage}"/>
<view:setBundle basename="org.silverpeas.calendar.multilang.calendarBundle"/>

<view:setConstant var="NEXT_EVENTS_VIEW_TYPE" constant="org.silverpeas.core.web.calendar.CalendarViewType.NEXT_EVENTS"/>
<view:setConstant var="DAILY_VIEW_TYPE" constant="org.silverpeas.core.web.calendar.CalendarViewType.DAILY"/>
<view:setConstant var="WEEKLY_VIEW_TYPE" constant="org.silverpeas.core.web.calendar.CalendarViewType.WEEKLY"/>
<view:setConstant var="MONTHLY_VIEW_TYPE" constant="org.silverpeas.core.web.calendar.CalendarViewType.MONTHLY"/>
<view:setConstant var="YEARLY_VIEW_TYPE" constant="org.silverpeas.core.web.calendar.CalendarViewType.YEARLY"/>

<fmt:message key="calendar.label.event.nextEvents" var="nextEventLabel"/>
<fmt:message key="GML.view.mode" var="viewModeLabel"/>
<fmt:message key="GML.allMP" var="allLabel"/>
<fmt:message key="GML.day" var="dayLabel"/>
<fmt:message key="GML.week" var="weekLabel"/>
<fmt:message key="GML.month" var="monthLabel"/>
<fmt:message key="GML.year" var="yearLabel"/>
<fmt:message key="calendar.message.event.occurrence.gotoPrevious" var="gotoPreviousOccurrenceLabel"/>
<fmt:message key="calendar.label.listViewType" var="listViewLabel"/>
<fmt:message key="calendar.label.calendarViewType" var="calendarViewLabel"/>

<div style="display: none">
  <span ng-init="$ctrl.viewTypes.nextEvents = '${NEXT_EVENTS_VIEW_TYPE}'"></span>
  <span ng-init="$ctrl.viewTypes.day = '${DAILY_VIEW_TYPE}'"></span>
  <span ng-init="$ctrl.viewTypes.week = '${WEEKLY_VIEW_TYPE}'"></span>
  <span ng-init="$ctrl.viewTypes.month = '${MONTHLY_VIEW_TYPE}'"></span>
  <span ng-init="$ctrl.viewTypes.year = '${YEARLY_VIEW_TYPE}'"></span>
  <span ng-init="$ctrl.labels.nextEvents = '${silfn:escapeJs(nextEventLabel)}'"></span>
  <span ng-init="$ctrl.labels.day = '${silfn:escapeJs(dayLabel)}'"></span>
  <span ng-init="$ctrl.labels.week = '${silfn:escapeJs(weekLabel)}'"></span>
  <span ng-init="$ctrl.labels.month = '${silfn:escapeJs(monthLabel)}'"></span>
  <span ng-init="$ctrl.labels.year = '${silfn:escapeJs(yearLabel)}'"></span>
</div>

<div class="silverpeas-calendar-header">
  <div class="sousNavBulle">
    <div class="top-part">
      <a ng-repeat="viewType in $ctrl.timeWindowViewContext.availableViewTypes"
         class="view-button" href="javascript:void(0)"
         ng-click="$ctrl.view({type:viewType, listViewMode:$ctrl.timeWindowViewContext.listViewMode})"
         ng-class="{'selected': $ctrl.isSelectedViewType(viewType)}">{{$ctrl.getViewTypeLabel(viewType)}}</a>
      <span class="time-cursor" ng-hide="$ctrl.isSelectedViewType($ctrl.viewTypes.nextEvents)">
        <span>-&#160;</span>
        <span> <a class="today-button" href="#" ng-click="$ctrl.timeWindow({type:'today'})" onfocus="this.blur()"><fmt:message key="GML.Today"/></a></span>
        <input type="text" class="reference-day" style="visibility: hidden"
               ng-model="$ctrl.timeWindowViewContext.formattedReferenceDay"
               ng-change="$ctrl.referenceDayChanged()">
        <a class="btn_navigation previous" href="#" ng-click="$ctrl.timeWindow({type:'previous'})" onfocus="this.blur()"><img border="0" alt="" src="<c:url value="/util/icons/arrow/arrowLeft.gif"/>"></a>
        <div class="period-label" ng-click="$ctrl.chooseReferenceDay()">
          <div class="inlineMessage goto-previous-occurrence" ng-if="$ctrl.timeWindowViewContext.backDay">
            <span ng-click="$ctrl.timeWindow({type : 'referenceDay', day : $ctrl.timeWindowViewContext.backDay});$event.stopPropagation();">${gotoPreviousOccurrenceLabel}</span>
          </div>
          <span>{{$ctrl.timeWindowViewContext.referencePeriodLabel}}</span>
        </div>
        <a class="btn_navigation next" href="#" ng-click="$ctrl.timeWindow({type:'next'})" onfocus="this.blur()"><img border="0" alt="" src="<c:url value="/util/icons/arrow/arrowRight.gif"/>"></a>
      </span>
      <span ng-if="$ctrl.isSelectedViewType($ctrl.viewTypes.nextEvents) && $ctrl.nextEventMonths.length">
        <span>-&#160;</span>
        <a ng-repeat="nextEventMonth in $ctrl.nextEventMonths"
           class="next-event-month-filter" href="javascript:void(0)"
           ng-click="nextEventMonth.selected = !nextEventMonth.selected"
           ng-class="{'selected': nextEventMonth.selected}">{{nextEventMonth.monthLabel}}</a>
      </span>
      <span ng-if="$ctrl.hasToDisplayViewMode()">
        <span>${viewModeLabel}</span>
        <a class="calendar-view-mode" href="javascript:void(0);" title="${calendarViewLabel}"
           ng-click="$ctrl.view({type:$ctrl.timeWindowViewContext.viewType, listViewMode:false})"
           ng-class="{'selected': !$ctrl.timeWindowViewContext.listViewMode}">&#160;</a>
        <a class="list-view-mode" href="javascript:void(0);" title="${listViewLabel}"
           ng-click="$ctrl.view({type:$ctrl.timeWindowViewContext.viewType, listViewMode:true})"
           ng-class="{'selected': $ctrl.timeWindowViewContext.listViewMode}">&#160;</a>
      </span>
    </div>
    <div ng-transclude></div>
    <div id="calendar-timezone">
      <span>{{$ctrl.timeWindowViewContext.zoneId}}</span>
    </div>
  </div>
</div>