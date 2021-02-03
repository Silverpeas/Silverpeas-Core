<%--
  ~ Copyright (C) 2000 - 2021 Silverpeas
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

<c:url var="mandatoryIcons" value="/util/icons/mandatoryField.gif"/>

<fmt:message var="recurrenceLabel" key="calendar.label.event.recurrence"/>
<fmt:message var="periodicityLabel" key="calendar.label.event.recurrence.periodicity"/>
<fmt:message var="frequencyLabel" key="calendar.label.event.recurrence.frenquency"/>
<fmt:message var="endLabel" key="GML.end"/>
<fmt:message var="neverLabel" key="GML.never"/>
<fmt:message var="afterLabel" key="GML.after"/>
<fmt:message var="theLabel" key="GML.date.the"/>
<fmt:message var="countLabel" key="calendar.label.event.recurrence.count"/>
<fmt:message var="monthRuleLabel" key="calendar.label.event.recurrence.month.rule"/>
<fmt:message var="monthRuleDayOfMonthLabel" key="calendar.label.event.recurrence.month.rule.dayofmonth"><fmt:param value="{{$ctrl.getDefaultMonthDayNumber()}}"/></fmt:message>

<div class="bgDegradeGris">
  <div class="bgDegradeGris header">
    <h4 class="clean">${recurrenceLabel}</h4>
  </div>
  <div class="fields">
    <div class="field">
      <span class="txtlibform label">${periodicityLabel}</span>
      <div class="champs">
        <span>{{$ctrl.recurrence.label}}</span>
      </div>
    </div>
    <div class="field" ng-if="$ctrl.isRecurrence()">
      <span class="txtlibform label">${frequencyLabel}</span>
      <div class="champs">
        <span>{{$ctrl.ceo.recurrence.frequency.interval}}</span>
        <span>{{$ctrl.recurrence.shortLabel}}</span>
      </div>
    </div>
    <div class="field" ng-if="$ctrl.isWeekRecurrence()">
      <label class="txtlibform label">${theLabel}</label>
      <div class="champs">
        <span ng-repeat="dayOfWeek in $ctrl.daysOfWeek"><span ng-if="!$first">, </span>{{dayOfWeek.label}}</span>
      </div>
    </div>
    <div class="field" ng-if="$ctrl.isMonthRecurrence()">
      <label class="txtlibform label">${monthRuleLabel}</label>
      <div class="champs">
        <span ng-switch="$ctrl.month.rule">
          <span ng-switch-when="DAYOFMONTH">${monthRuleDayOfMonthLabel}</span>
          <span ng-switch-default>{{$ctrl.month.nth.label}} {{$ctrl.month.dayOfWeek.label}}</span>
        </span>
      </div>
    </div>
    <div class="field" ng-if="$ctrl.isRecurrence()">
      <label class="txtlibform label">${endLabel}</label>
      <div class="champs">
        <span ng-switch="$ctrl.endType">
          <span ng-switch-when="THE">
            <span>${theLabel} </span><span>{{$ctrl.ceo.recurrence.endDate | displayAsDate}}</span>
          </span>
          <span ng-switch-when="AFTER">
            <span>${afterLabel} </span>
            <span>{{$ctrl.ceo.recurrence.count}}</span>
            <span> ${fn:toLowerCase(countLabel)}</span>
          </span>
          <span ng-switch-default>${neverLabel}</span>
        </span>
      </div>
    </div>
  </div>
</div>
