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
<fmt:message var="monthRuleDayOfWeekLabel" key="calendar.label.event.recurrence.month.rule.dayofweek"/>

<div style="display: none">
  <span ng-init="$ctrl.labels.recurrence = '${silfn:escapeJs(recurrenceLabel)}'"></span>
  <span ng-init="$ctrl.labels.periodicity = '${silfn:escapeJs(periodicityLabel)}'"></span>
  <span ng-init="$ctrl.labels.frequency = '${silfn:escapeJs(frequencyLabel)}'"></span>
  <span ng-init="$ctrl.labels.end = '${silfn:escapeJs(endLabel)}'"></span>
  <span ng-init="$ctrl.labels.never = '${silfn:escapeJs(neverLabel)}'"></span>
  <span ng-init="$ctrl.labels.after = '${silfn:escapeJs(afterLabel)}'"></span>
  <span ng-init="$ctrl.labels.the = '${silfn:escapeJs(theLabel)}'"></span>
  <span ng-init="$ctrl.labels.count = '${silfn:escapeJs(countLabel)}'"></span>
  <span ng-init="$ctrl.labels.monthRule = '${silfn:escapeJs(monthRuleLabel)}'"></span>
</div>
<fieldset class="skinFieldset">
  <legend>{{$ctrl.labels.recurrence}}</legend>
  <div class="fields">
    <div class="field">
      <label class="txtlibform" for="sp_cal_event_form_rec_p">{{$ctrl.labels.periodicity}}</label>
      <div class="champs">
        <select ng-model="$ctrl.recurrenceType"
                ng-options="recurrence.name as recurrence.label for recurrence in $ctrl.recurrences"
                id="sp_cal_event_form_rec_p">
        </select>
      </div>
    </div>
    <div class="field" ng-if="$ctrl.isRecurrence()">
      <label class="txtlibform" for="sp_cal_event_form_rec_p_f">{{$ctrl.labels.frequency}}</label>
      <div class="champs">
        <input id="sp_cal_event_form_rec_p_f" name="interval" size="3" maxlength="3"
               ng-model="$ctrl.data.recurrence.frequency.interval">&nbsp;<img border="0" src="${mandatoryIcons}" width="5" height="5">
        <span> {{$ctrl.recurrenceType.shortLabel}}</span>
      </div>
    </div>
    <div class="field" ng-if="$ctrl.isWeekRecurrence()">
      <label class="txtlibform">{{$ctrl.labels.the}}</label>
      <div class="champs">
        <label ng-repeat="dayOfWeek in $ctrl.weekDaysOfWeek">
          <input type="checkbox" ng-model="dayOfWeek.checked">
          {{dayOfWeek.label}}
        </label>
      </div>
    </div>
    <div class="field" ng-if="$ctrl.isMonthRecurrence()">
      <label class="txtlibform">{{$ctrl.labels.monthRule}}</label>
      <div class="champs">
        <div class="cell">
          <label>
            <input type="radio" name="monthRule" ng-model="$ctrl.month.rule" ng-value="'DAYOFMONTH'">
            ${monthRuleDayOfMonthLabel}
          </label>
        </div>
        <div class="cell">
          <div>
            <label>
              <input type="radio" name="monthRule" ng-model="$ctrl.month.rule" ng-value="'DAYOFWEEK'">
              ${monthRuleDayOfWeekLabel}
            </label>
          </div>
          <div class="month-rule-area" ng-if="$ctrl.month.rule == 'DAYOFWEEK'">
            <select ng-model="$ctrl.month.nth"
                    ng-options="nthDayOfWeek.nth as nthDayOfWeek.label for nthDayOfWeek in $ctrl.nthDaysOfWeek">
            </select>
            <select ng-model="$ctrl.month.dayOfWeek"
                    ng-options="dayOfWeek.name as dayOfWeek.label for dayOfWeek in $ctrl.daysOfWeek">
            </select>
          </div>
        </div>
      </div>
    </div>
    <div class="field" ng-if="$ctrl.isRecurrence()">
      <label class="txtlibform">{{$ctrl.labels.end}}</label>
      <div class="champs">
        <label>
          <input type="radio" name="end" ng-model="$ctrl.endType" ng-value="'NEVER'">
          {{$ctrl.labels.never}}
        </label>
        <label>
          <input type="radio" name="end" ng-model="$ctrl.endType" ng-value="'AFTER'">
          <span>{{$ctrl.labels.after}}&#160;</span><input id="sp_cal_event_form_rec_p_e" name="count" size="3" maxlength="3"
                                        ng-model="$ctrl.data.recurrence.count" ng-disabled="$ctrl.endType != 'AFTER'">
          <span>&#160;{{$ctrl.labels.count.toLowerCase()}}</span>
          <span ng-if="$ctrl.endType == 'AFTER'">&nbsp;<img border="0" src="${mandatoryIcons}" width="5" height="5"></span>
        </label>
        <label>
          <input type="radio" name="end" ng-model="$ctrl.endType" ng-value="'THE'">
          <span>{{$ctrl.labels.the}}&#160;
            <silverpeas-date-picker name="recurrenceEndDate"
                                    zone-id="$ctrl.zoneId"
                                    date="$ctrl.data.recurrence.endDate"
                                    status="$ctrl.data.recurrence.endDateStatus"
                                    is-disabled="$ctrl.endType != 'THE'"
                                    mandatory="$ctrl.endType == 'THE'">
            </silverpeas-date-picker>
          </span>
        </label>
      </div>
    </div>
  </div>
</fieldset>
