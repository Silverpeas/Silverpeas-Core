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

<c:url var="mandatoryIcons" value="/util/icons/mandatoryField.gif"/>

<fmt:message var="mainInfoLabel" key="GML.bloc.information.principals"/>
<fmt:message var="calendarLabel" key="calendar.label.event.calendar"/>
<fmt:message var="titleLabel" key="GML.title"/>
<fmt:message var="descriptionLabel" key="GML.description"/>
<fmt:message var="locationLabel" key="calendar.label.event.location"/>
<fmt:message var="onAllDayLabel" key="calendar.label.event.onallday"/>
<fmt:message var="startDateLabel" key="GML.dateBegin"/>
<fmt:message var="endDateLabel" key="GML.dateEnd"/>
<fmt:message var="atTimeLabel" key="GML.at"/>
<fmt:message var="visibilityLabel" key="calendar.label.event.visibility"/>
<fmt:message var="priorityLabel" key="calendar.label.event.priority"/>

<div style="display: none">
  <span ng-init="$ctrl.labels.mainInfo = '${silfn:escapeJs(mainInfoLabel)}'"></span>
  <span ng-init="$ctrl.labels.calendar = '${silfn:escapeJs(calendarLabel)}'"></span>
  <span ng-init="$ctrl.labels.title = '${silfn:escapeJs(titleLabel)}'"></span>
  <span ng-init="$ctrl.labels.description = '${silfn:escapeJs(descriptionLabel)}'"></span>
  <span ng-init="$ctrl.labels.location = '${silfn:escapeJs(locationLabel)}'"></span>
  <span ng-init="$ctrl.labels.onAllDay = '${silfn:escapeJs(onAllDayLabel)}'"></span>
  <span ng-init="$ctrl.labels.startDate = '${silfn:escapeJs(startDateLabel)}'"></span>
  <span ng-init="$ctrl.labels.endDate = '${silfn:escapeJs(endDateLabel)}'"></span>
  <span ng-init="$ctrl.labels.atTime = '${silfn:escapeJs(atTimeLabel)}'"></span>
  <span ng-init="$ctrl.labels.visibility = '${silfn:escapeJs(visibilityLabel)}'"></span>
  <span ng-init="$ctrl.labels.priority = '${silfn:escapeJs(priorityLabel)}'"></span>
</div>

<fieldset class="skinFieldset">
  <legend>{{$ctrl.labels.mainInfo}}</legend>
  <div class="fields">
    <div class="field" ng-if="$ctrl.visibleCalendars">
      <label class="txtlibform" for="sp_cal_event_form_main_c">{{$ctrl.labels.calendar}}</label>
      <div class="champs">
        <span class="txtnav" ng-if="$ctrl.visibleCalendars.length == 1">{{$ctrl.data.event.calendar.title}}</span>
        <select ng-if="$ctrl.visibleCalendars.length > 1" ng-model="$ctrl.data.event.calendar"
                ng-options="calendar as calendar.title for calendar in $ctrl.visibleCalendars | orderBy: 'createdDate' track by calendar.id"
                id="sp_cal_event_form_main_c" class="txtnav">
        </select>
      </div>
    </div>
    <div class="field">
      <label class="txtlibform" for="sp_cal_event_form_main_title">{{$ctrl.labels.title}}</label>
      <div class="champs">
        <input id="sp_cal_event_form_main_title" name="title" size="50" maxlength="2000"
               ng-model="$ctrl.data.event.title">&nbsp;<img border="0" src="${mandatoryIcons}" width="5" height="5">
      </div>
    </div>
    <div class="field">
      <label class="txtlibform" for="sp_cal_event_form_main_ad">{{$ctrl.labels.onAllDay}}</label>
      <div class="champs">
        <input id="sp_cal_event_form_main_ad" name="allDay" type="checkbox"
               ng-model="$ctrl.data.event.onAllDay"/>
      </div>
    </div>
    <div class="field">
      <label class="txtlibform" for="sp_cal_event_form_main_sd">{{$ctrl.labels.startDate}}</label>
      <div class="champs">
        <silverpeas-date-picker date-id="sp_cal_event_form_main_sd"
                                name="startDate"
                                date="$ctrl.data.startDate"
                                status="$ctrl.data.startDateStatus"
                                mandatory="true">
        </silverpeas-date-picker>
        <silverpeas-time-picker ng-if="!$ctrl.data.event.onAllDay"
                                time-id="sp_cal_event_form_main_sdt"
                                name="startTime"
                                time="$ctrl.data.startDate"
                                status="$ctrl.data.startTimeStatus"
                                mandatory="true">
          <span class="txtlibform" for="sp_cal_event_form_main_sdt">{{$ctrl.labels.atTime}}</span>
        </silverpeas-time-picker>
      </div>
    </div>
    <div class="field">
      <label class="txtlibform" for="sp_cal_event_form_main_ed">{{$ctrl.labels.endDate}}</label>
      <div class="champs">
        <silverpeas-date-picker date-id="sp_cal_event_form_main_ed"
                                name="endDate"
                                date="$ctrl.data.endDate"
                                status="$ctrl.data.endDateStatus"
                                mandatory="true">
        </silverpeas-date-picker>
        <silverpeas-time-picker ng-if="!$ctrl.data.event.onAllDay"
                                time-id="sp_cal_event_form_main_edt"
                                name="endTime"µ
                                time="$ctrl.data.endDate"
                                status="$ctrl.data.endTimeStatus"
                                mandatory="true">
          <span class="txtlibform" for="sp_cal_event_form_main_edt">{{$ctrl.labels.atTime}}</span>
        </silverpeas-time-picker>
      </div>
    </div>
    <div class="field">
      <label class="txtlibform" for="sp_cal_event_form_main_loc">{{$ctrl.labels.location}}</label>
      <div class="champs">
        <input id="sp_cal_event_form_main_loc" name="title" size="50" maxlength="255"
               ng-model="$ctrl.data.event.location">
      </div>
    </div>
    <div class="field">
      <label class="txtlibform" for="sp_cal_event_form_main_desc">{{$ctrl.labels.description}}</label>
      <div class="champs">
        <textarea id="sp_cal_event_form_main_desc" name="description" rows="6" cols="50" maxlength="4000"
                  ng-model="$ctrl.data.event.description"></textarea>
      </div>
    </div>
    <div class="field" ng-if="$ctrl.data.event.visibility">
      <label class="txtlibform">{{$ctrl.labels.visibility}}</label>
      <div class="champs">
        <label ng-repeat="visibility in $ctrl.visibilities">
          <input type="radio" ng-model="$ctrl.data.event.visibility" ng-value="visibility.name">
          {{visibility.label}}
        </label>
      </div>
    </div>
    <div class="field" ng-if="$ctrl.data.event.priority">
      <label class="txtlibform">{{$ctrl.labels.priority}}</label>
      <div class="champs">
        <label ng-repeat="priority in $ctrl.priorities">
          <input type="radio" ng-model="$ctrl.data.event.priority" ng-value="priority.name">
          {{priority.label}}
        </label>
      </div>
    </div>
  </div>
</fieldset>
