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

<fmt:message var="createCalendarLabel" key="calendar.menu.item.calendar.create"/>
<fmt:message var="modifyCalendarLabel" key="calendar.menu.item.calendar.modify"/>
<fmt:message var="deleteCalendarMessage" key="calendar.message.calendar.delete"><fmt:param>@name@</fmt:param></fmt:message>
<fmt:message var="titleLabel" key="GML.title"/>

<fmt:message key="calendar.menu.item.event.import" var="importEventLabel"/>
<fmt:message key="calendar.label.event.import.ical" var="icalFileImportLabel"/>
<fmt:message key="calendar.label.event.import.into" var="icalFileImportIntoLabel"/>

<c:set var="mandatoryMessage"><b>@name@</b> <fmt:message key='GML.MustBeFilled'/></c:set>
<c:set var="nbMaxMessage"><b>@name@</b> <fmt:message key='GML.data.error.message.string.limit'><fmt:param value="2000"/></fmt:message></c:set>

<div style="display: none">
  <view:loadScript src="/util/javaScript/checkForm.js"/>
  <span ng-init="$ctrl.messages.mandatory = '${silfn:escapeJs(mandatoryMessage)}'"></span>
  <span ng-init="$ctrl.messages.nbMax = '${silfn:escapeJs(nbMaxMessage)}'"></span>
  <span ng-init="$ctrl.messages.create= '${silfn:escapeJs(createCalendarLabel)}'"></span>
  <span ng-init="$ctrl.messages.update= '${silfn:escapeJs(modifyCalendarLabel)}'"></span>
  <span ng-init="$ctrl.messages.delete= '${silfn:escapeJs(deleteCalendarMessage)}'"></span>
  <span ng-init="$ctrl.labels.title = '${silfn:escapeJs(titleLabel)}'"></span>
</div>

<div class="silverpeas-calendar-management-save-popin" style="display: none" title="{{$ctrl.creating ? $ctrl.messages.create : $ctrl.messages.update}}">
  <p>
    <span class="txtlibform">{{$ctrl.labels.title}}</span>
    <input name="title" size="50" maxlength="2000" ng-model="$ctrl.calendar.title">&nbsp;<img border="0" src="${mandatoryIcons}" width="5" height="5"/>
  </p>
</div>

<div class="silverpeas-calendar-management-import-popin" style="display: none" title="${importEventLabel}">
  <p>
    <span class="champs">
      <span class="txtlibform">${icalFileImportLabel}</span>
      <silverpeas-file-upload api="$ctrl.fileUploadApi"
                              multiple="false"
                              info-inputs="false"
                              display-into-fieldset="false"
                              drag-and-drop-display-icon="false">
      </silverpeas-file-upload>
    </span>
    <div class="champs">
      <span class="txtlibform">${icalFileImportIntoLabel}</span>
      <select ng-model="$ctrl.importEventCalendar"
              ng-options="calendar as calendar.title for calendar in $ctrl.potentialCalendars | orderBy: 'createdDate' track by calendar.id"
              class="txtnav">
      </select>
    </div>
  </p>
</div>

<view:progressMessage/>
