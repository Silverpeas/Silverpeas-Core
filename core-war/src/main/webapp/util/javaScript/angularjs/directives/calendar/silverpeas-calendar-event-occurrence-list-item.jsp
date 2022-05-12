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

<fmt:message var="gotoLabel" key='calendar.label.event.view'/>
<fmt:message var="fromLabel" key='GML.From'/>
<fmt:message var="atLabel" key='GML.at'/>
<fmt:message var="toLabel" key='GML.to'/>

<script type="text/ng-template" id="###silverpeas.calendar.event.occurrence-list-item.display-grouped-by-day">
  <div class="occurrence-name">
    <a href="javascript:void(0)" title="${gotoLabel}">{{$ctrl.occurrence.title}}</a>
    <span ng-if="$ctrl.hasTime()" class="start-hour">${atLabel} {{$ctrl.occurrence.startDate | displayAsTime}}</span>
  </div>
  <div class="occurrence-extra" ng-if="$ctrl.occurrence.location">
    <div class="occurrence-location">
      <div class="bloc"><span>{{$ctrl.occurrence.location}}</span></div>
    </div>
  </div>
</script>

<script type="text/ng-template" id="###silverpeas.calendar.event.occurrence-list-item.display-grouped-by-month">
  <h2 class="occurrence-name">{{$ctrl.occurrence.title}}</h2>
  <div class="occurrence-extra" ng-if="$ctrl.occurrence.location || !$ctrl.occurrence.onAllDay || $ctrl.occurrence.externalUrl()">
    <div class="occurrence-location" ng-if="$ctrl.occurrence.location">
      <div class="bloc"><span>{{$ctrl.occurrence.location}}</span></div>
    </div>
    <div class="occurrence-date" ng-if="!$ctrl.occurrence.onAllDay">
      <div class="bloc">
        <span>${fromLabel} {{$ctrl.occurrence.startDate | displayAsTime}}</span>
        <span>${toLabel} {{$ctrl.occurrence.endDate | displayAsDate}} ${atLabel} {{$ctrl.occurrence.endDate | displayAsTime}}</span>
      </div>
    </div>
    <div class="occurrence-external-link" ng-if="$ctrl.occurrence.externalUrl()">
      <div class="bloc" ng-click="$ctrl.performExternalLink();$event.stopPropagation()">
        <a target="_blank" href="javascript:void(0)"
           ng-click="$ctrl.performExternalLink();$event.stopPropagation()">{{$ctrl.occurrence.externalUrl()}}</a>
      </div>
    </div>
  </div>
  <div class="occurrence-description">
    <div ng-if="$ctrl.occurrence.calendarSync" ng-bind-html="$ctrl.occurrence.description | trustedHTML"></div>
    <div ng-if="!$ctrl.occurrence.calendarSync" ng-bind-html="$ctrl.occurrence.description | noHTML | newlines"></div>
    <br class="clearAll">
  </div>
</script>

<div class="fields" ng-include="$ctrl.groupByMonth ? '###silverpeas.calendar.event.occurrence-list-item.display-grouped-by-month': '###silverpeas.calendar.event.occurrence-list-item.display-grouped-by-day'"></div>
