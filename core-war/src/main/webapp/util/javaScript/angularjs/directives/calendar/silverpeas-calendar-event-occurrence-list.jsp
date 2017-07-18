<%--
  ~ Copyright (C) 2000 - 2017 Silverpeas
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

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://www.silverpeas.com/tld/silverFunctions" prefix="silfn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>

<c:set var="userLanguage" value="${sessionScope['SilverSessionController'].favoriteLanguage}"/>
<fmt:setLocale value="${userLanguage}"/>
<view:setBundle basename="org.silverpeas.calendar.multilang.calendarBundle"/>

<fmt:message var="todayLabel" key='GML.Today'/>
<fmt:message var="tomorrowLabel" key='GML.Tomorrow'/>

<div style="display: none">
  <span ng-init="$ctrl.labels.today = '${silfn:escapeJs(todayLabel)}'"></span>
  <span ng-init="$ctrl.labels.tomorrow = '${silfn:escapeJs(tomorrowLabel)}'"></span>
</div>

<span ng-if="!$ctrl.occurrences.length">{{$ctrl.noOccurrenceLabel}}</span>
<ul ng-if="$ctrl.occurrences.length">
  <li ng-repeat="dayOccurrences in $ctrl.occurrencesGroupedByDay"
      ng-class="{'high-priority':dayOccurrences.containsAtLeastOneImportant}"
      class="day-events">
    <div class="short-date">
      <span class="day-in-month">{{$ctrl.getStartDayNumberInMonth(dayOccurrences.dayDate)}}</span>
      <span class="short-date-separator">/</span>
      <span class="month-number">{{$ctrl.getMonthNumber(dayOccurrences.dayDate)}}</span>
    </div>
    <div class="long-date">
      <span class="full-date">{{$ctrl.getDayDate(dayOccurrences.dayDate)}}</span>
    </div>
    <silverpeas-calendar-event-occurrence-list-item
        ng-repeat="occurrence in dayOccurrences"
        occurrence="occurrence"
        class="{{$ctrl.getOccurrenceClasses(occurrence)}}"
        on-click="$ctrl.onEventOccurrenceClick({occurrence:occurrence})">
    </silverpeas-calendar-event-occurrence-list-item>
  </li>
</ul>