<%--
  ~ Copyright (C) 2000 - 2022 Silverpeas
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

<fmt:message var="mainInfoLabel" key="GML.bloc.information.principals"/>
<fmt:message var="calendarLabel" key="calendar.label.event.calendar"/>
<fmt:message var="titleLabel" key="GML.title"/>
<fmt:message var="descriptionLabel" key="GML.description"/>
<fmt:message var="contentLabel" key="GML.content"/>
<fmt:message var="locationLabel" key="calendar.label.event.location"/>
<fmt:message var="externalUrlLabel" key="calendar.label.event.externalUrl"/>
<fmt:message var="onAllDayLabel" key="calendar.label.event.onallday"/>
<fmt:message var="timezoneLabel" key="calendar.label.timezone"/>
<fmt:message var="startDateLabel" key="GML.dateBegin"/>
<fmt:message var="endDateLabel" key="GML.dateEnd"/>
<fmt:message var="atTimeLabel" key="GML.at"/>
<fmt:message var="visibilityLabel" key="calendar.label.event.visibility"/>
<fmt:message var="priorityLabel" key="calendar.label.event.priority"/>
<fmt:message var="reminderLabel" key="GML.reminder"/>

<div style="display: none">
  <span ng-init="$ctrl.labels.mainInfo = '${silfn:escapeJs(mainInfoLabel)}'"></span>
  <span ng-init="$ctrl.labels.calendar = '${silfn:escapeJs(calendarLabel)}'"></span>
  <span ng-init="$ctrl.labels.title = '${silfn:escapeJs(titleLabel)}'"></span>
  <span ng-init="$ctrl.labels.description = '${silfn:escapeJs(descriptionLabel)}'"></span>
  <span ng-init="$ctrl.labels.content = '${silfn:escapeJs(contentLabel)}'"></span>
  <span ng-init="$ctrl.labels.location = '${silfn:escapeJs(locationLabel)}'"></span>
  <span ng-init="$ctrl.labels.externalUrl = '${silfn:escapeJs(externalUrlLabel)}'"></span>
  <span ng-init="$ctrl.labels.onAllDay = '${silfn:escapeJs(onAllDayLabel)}'"></span>
  <span ng-init="$ctrl.labels.timezone = '${silfn:escapeJs(timezoneLabel)}'"></span>
  <span ng-init="$ctrl.labels.startDate = '${silfn:escapeJs(startDateLabel)}'"></span>
  <span ng-init="$ctrl.labels.endDate = '${silfn:escapeJs(endDateLabel)}'"></span>
  <span ng-init="$ctrl.labels.atTime = '${silfn:escapeJs(atTimeLabel)}'"></span>
  <span ng-init="$ctrl.labels.visibility = '${silfn:escapeJs(visibilityLabel)}'"></span>
  <span ng-init="$ctrl.labels.priority = '${silfn:escapeJs(priorityLabel)}'"></span>
</div>
<fieldset class="skinFieldset">
  <legend>{{$ctrl.labels.mainInfo}}</legend>
  <div class="fields">
    <div class="field" ng-if="$ctrl.potentialCalendars">
      <label class="txtlibform" for="sp_cal_event_form_main_c">{{$ctrl.labels.calendar}}</label>
      <div class="champs">
        <span class="txtnav" ng-if="$ctrl.potentialCalendars.length == 1 || !$ctrl.isFirstEventOccurrence()">{{$ctrl.data.calendar.title}}</span>
        <select ng-if="$ctrl.potentialCalendars.length > 1 && $ctrl.isFirstEventOccurrence()" ng-model="$ctrl.data.calendar"
                ng-options="calendar as calendar.title for calendar in $ctrl.potentialCalendars | sortedCalendars track by calendar.id"
                id="sp_cal_event_form_main_c" class="txtnav">
        </select>
      </div>
    </div>
    <div class="field">
      <label class="txtlibform" for="sp_cal_event_form_main_title">{{$ctrl.labels.title}}</label>
      <div class="champs">
        <input id="sp_cal_event_form_main_title" name="title" size="50" maxlength="255"
               ng-model="$ctrl.data.title">&nbsp;<img border="0" src="${mandatoryIcons}" width="5" height="5">
      </div>
    </div>
    <div class="field">
      <label class="txtlibform" for="sp_cal_event_form_main_ad">{{$ctrl.labels.onAllDay}}</label>
      <div class="champs">
        <input id="sp_cal_event_form_main_ad" name="allDay" type="checkbox"
               ng-model="$ctrl.data.onAllDay"/>
      </div>
    </div>
    <div class="field">
      <label class="txtlibform" for="sp_cal_event_form_main_tz">{{$ctrl.labels.timezone}}</label>
      <div class="champs" id="sp_cal_event_form_main_tz">
        <span>{{$ctrl.zoneId}}</span>
      </div>
    </div>
    <div class="field">
      <label class="txtlibform" for="sp_cal_event_form_main_sd">{{$ctrl.labels.startDate}}</label>
      <div class="champs">
        <silverpeas-date-picker date-id="sp_cal_event_form_main_sd"
                                name="startDate"
                                zone-id="$ctrl.zoneId"
                                date="$ctrl.data.startDate"
                                status="$ctrl.data.startDateStatus"
                                mandatory="true">
        </silverpeas-date-picker>
        <silverpeas-time-picker ng-if="!$ctrl.data.onAllDay"
                                time-id="sp_cal_event_form_main_sdt"
                                name="startTime"
                                zone-id="$ctrl.zoneId"
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
                                zone-id="$ctrl.zoneId"
                                date="$ctrl.data.endDate"
                                status="$ctrl.data.endDateStatus"
                                mandatory="true">
        </silverpeas-date-picker>
        <silverpeas-time-picker ng-if="!$ctrl.data.onAllDay"
                                time-id="sp_cal_event_form_main_edt"
                                name="endTime"
                                zone-id="$ctrl.zoneId"
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
               ng-model="$ctrl.data.location">
      </div>
    </div>
    <div class="field">
      <label class="txtlibform" for="sp_cal_event_form_main_extUrl">{{$ctrl.labels.externalUrl}}</label>
      <div class="champs">
        <input id="sp_cal_event_form_main_extUrl" name="title" size="50" maxlength="255"
               ng-model="$ctrl.data.externalUrl"
               ng-model-options="{getterSetter: true}">
      </div>
    </div>
    <div class="field">
      <label class="txtlibform" for="sp_cal_event_form_main_desc">{{$ctrl.labels.description}}</label>
      <div class="champs">
        <textarea id="sp_cal_event_form_main_desc" name="description" rows="5" cols="10" maxlength="2000"
                  ng-model="$ctrl.data.description"></textarea>
      </div>
    </div>
    <div class="field" ng-if="$ctrl.wysiwygEditorConfig">
      <label class="txtlibform" for="sp_cal_event_form_main_content">{{$ctrl.labels.content}}</label>
      <div class="champs">
        <ng-ckeditor id="sp_cal_event_form_main_content"
                     ng-model="$ctrl.data.content"
                     ng-config="$ctrl.wysiwygEditorConfig"
                     backup-manager-options="{componentInstanceId:$ctrl.data.componentInstanceId(),resourceType:$ctrl.data.occurrenceType,resourceId:($ctrl.data.occurrenceId?$ctrl.data.eventId:null)}"></ng-ckeditor>
      </div>
    </div>
    <div class="field" ng-if="$ctrl.data.visibility">
      <label class="txtlibform">{{$ctrl.labels.visibility}}</label>
      <div class="champs">
        <label ng-repeat="visibility in $ctrl.visibilities">
          <input type="radio" ng-model="$ctrl.data.visibility" ng-value="visibility.name">
          {{visibility.label}}
        </label>
      </div>
    </div>
    <div class="field" ng-if="$ctrl.data.priority">
      <label class="txtlibform">{{$ctrl.labels.priority}}</label>
      <div class="champs">
        <label ng-repeat="priority in $ctrl.priorities">
          <input type="radio" ng-model="$ctrl.data.priority" ng-value="priority.name">
          {{priority.label}}
        </label>
      </div>
    </div>
    <div class="field" ng-if="!$ctrl.data.occurrenceId" ng-show="$ctrl.__reminderShown">
      <label class="txtlibform">${reminderLabel}</label>
      <div class="champs">
        <silverpeas-contribution-reminder mode="DURATION"
                                          api="$ctrl.reminderApi"
                                          reminder="$ctrl.data.reminder"
                                          contribution-id="$ctrl.eventContributionId"
                                          contribution-property="'NEXT_START_DATE_TIME'"
                                          process-name="'CalendarEventUserNotification'"
                                          main-label=""
                                          shown="$ctrl.__reminderShown"
                                          autonomous="false">
        </silverpeas-contribution-reminder>
      </div>
    </div>
  </div>
</fieldset>
