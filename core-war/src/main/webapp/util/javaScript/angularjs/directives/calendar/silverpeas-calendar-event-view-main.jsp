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
<fmt:message var="timezoneLabel" key="calendar.label.timezone"/>
<fmt:message var="startDateLabel" key="GML.dateBegin"/>
<fmt:message var="endDateLabel" key="GML.dateEnd"/>
<fmt:message var="atTimeLabel" key="GML.at"/>
<fmt:message var="visibilityLabel" key="calendar.label.event.visibility"/>
<fmt:message var="priorityLabel" key="calendar.label.event.priority"/>

<fieldset class="skinFieldset">
  <legend>${mainInfoLabel}</legend>
  <div class="fields">
    <div class="field" ng-if="$ctrl.calendars">
      <span class="txtlibform">${calendarLabel}</span>
      <div class="champs">
        <span class="txtnav">{{$ctrl.ceo.event.calendar.title}}</span>
      </div>
    </div>
    <div class="field">
      <span class="txtlibform">${titleLabel}</span>
      <div class="champs">
        <span>{{$ctrl.ceo.event.title}}</span>
      </div>
    </div>
    <div class="field">
      <span class="txtlibform">${timezoneLabel}</span>
      <div class="champs">
        <span>{{$ctrl.zoneId}}</span>
      </div>
    </div>
    <div class="field">
      <span class="txtlibform">${startDateLabel}</span>
      <span class="champs">
        <span>{{$ctrl.ceo.startDate | displayAsDate}}</span>
        <span class="txtlibform">${atTimeLabel}</span>
        <span>{{$ctrl.ceo.startDate | displayAsTime}}</span>
      </span>
    </div>
    <div class="field">
      <span class="txtlibform">${endDateLabel}</span>
      <span class="champs">
        <span>{{$ctrl.ceo.endDate | displayAsDate}}</span>
        <span class="txtlibform">${atTimeLabel}</span>
        <span>{{$ctrl.ceo.endDate | displayAsTime}}</span>
      </span>
    </div>
    <div class="field">
      <span class="txtlibform">${locationLabel}</span>
      <div class="champs">
        <span>{{$ctrl.ceo.event.location}}</span>
      </div>
    </div>
    <div class="field">
      <span class="txtlibform">${descriptionLabel}</span>
      <div class="champs">
        <span ng-bind-html="$ctrl.ceo.event.description | noHTML | newlines"></span>
      </div>
    </div>
    <div class="field" ng-if="$ctrl.ceo.event.visibility">
      <span class="txtlibform">${visibilityLabel}</span>
      <div class="champs">
        <span>{{$ctrl.visibility.label}}</span>
      </div>
    </div>
    <div class="field" ng-if="$ctrl.ceo.event.priority">
      <span class="txtlibform">${priorityLabel}</span>
      <div class="champs">
        <span>{{$ctrl.priority.label}}</span>
      </div>
    </div>
  </div>
</fieldset>
