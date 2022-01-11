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

<view:setConstant var="highPriority" constant="org.silverpeas.core.calendar.Priority.HIGH"/>
<view:setConstant var="privatePriority" constant="org.silverpeas.core.calendar.VisibilityLevel.PRIVATE"/>

<c:url var="mandatoryIcons" value="/util/icons/mandatoryField.gif"/>

<fmt:message var="fromDateLabel" key='GML.date.from'/>
<fmt:message var="atLabel" key='GML.at'/>
<fmt:message var="toLabel" key='GML.to'/>

<h2 class="occurrence-name">
  <span>{{$ctrl.ceo.title}}</span>
  <span class="important" ng-if="$ctrl.priority.name == '${highPriority}'"></span>
  <span class="private" ng-if="$ctrl.visibility.name == '${privatePriority}'"></span>
</h2>
<div>
  <span class="txtnav">{{$ctrl.ceo.calendar.title}}</span>
  <span class="calendar-timezone">{{$ctrl.zoneId}}</span>
</div>
<div class="occurrence-extra">
  <div class="occurrence-location" ng-if="$ctrl.ceo.location">
    <div class="bloc"><span>{{$ctrl.ceo.location}}</span></div>
  </div>
  <div class="occurrence-date">
    <div class="bloc" ng-if="$ctrl.ceo.onAllDay">
      <span ng-if="$ctrl.onSameDay()">{{$ctrl.startDate() | displayAsDate}}</span>
      <span ng-if="!$ctrl.onSameDay()">${fromDateLabel} {{$ctrl.startDate() | displayAsDate}}</span>
      <span ng-if="!$ctrl.onSameDay()">${toLabel} {{$ctrl.endDate() | displayAsDate}}</span>
    </div>
    <div class="bloc" ng-if="!$ctrl.ceo.onAllDay && $ctrl.onSameDay()">
      <span>{{$ctrl.startDate() | displayAsDate}} - {{$ctrl.startDate() | displayAsTime}} ${atLabel} {{$ctrl.endDate() | displayAsTime}}</span>
    </div>
    <div class="bloc" ng-if="!$ctrl.ceo.onAllDay && !$ctrl.onSameDay()">
      <span>${fromDateLabel} {{$ctrl.startDate() | displayAsDate}} ${atLabel} {{$ctrl.startDate() | displayAsTime}}</span>
      <span>${toLabel} {{$ctrl.endDate() | displayAsDate}} ${atLabel} {{$ctrl.endDate() | displayAsTime}}</span>
    </div>
  </div>
  <div class="occurrence-external-link" ng-if="$ctrl.ceo.externalUrl()">
    <div class="bloc">
      <a target="_blank" href="{{$ctrl.ceo.externalUrl()}}">{{$ctrl.ceo.externalUrl()}}</a>
    </div>
  </div>
</div>
<div class="occurrence-description" ng-if="$ctrl.ceo.description">
  <p ng-bind-html="$ctrl.ceo.description | noHTML | newlines"></p>
</div>
<div class="occurrence-content rich-content" ng-if="$ctrl.ceo.content">
  <p ng-bind-html="$ctrl.ceo.content | trustedHTML"></p>
</div>
