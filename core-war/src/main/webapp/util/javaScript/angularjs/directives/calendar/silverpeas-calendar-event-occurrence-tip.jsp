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

<%@ page import="org.silverpeas.core.web.selection.BasketSelectionUI" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://www.silverpeas.com/tld/silverFunctions" prefix="silfn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>

<c:set var="userLanguage" value="${sessionScope['SilverSessionController'].favoriteLanguage}"/>
<jsp:useBean id="userLanguage" type="java.lang.String"/>
<fmt:setLocale value="${userLanguage}"/>
<view:setBundle basename="org.silverpeas.calendar.multilang.calendarBundle"/>
<c:set var="putIntoBasketSnippet" value='<%=BasketSelectionUI.getPutIntoBasketSelectionHtmlSnippet("@callback@", userLanguage)%>'/>

<fmt:message var="fromDateLabel" key='GML.date.from'/>
<fmt:message var="atLabel" key='GML.at'/>
<fmt:message var="toLabel" key='GML.to'/>

<c:if test="${not empty putIntoBasketSnippet}">
  ${putIntoBasketSnippet.replace('onclick', 'ng-click').replace('@callback@', '$ctrl.putIntoBasket();$event.stopPropagation()')}
</c:if>
<div class="occurrence-extra">
  <div class="occurrence-date">
    <div ng-if="$ctrl.occurrence.onAllDay">
      <span ng-if="$ctrl.onSameDay()">{{$ctrl.startDate() | displayAsDate}}</span>
      <span ng-if="!$ctrl.onSameDay()">${fromDateLabel} {{$ctrl.startDate() | displayAsDate}}</span>
      <span ng-if="!$ctrl.onSameDay()">${toLabel} {{$ctrl.endDate() | displayAsDate}}</span>
    </div>
    <div ng-if="!$ctrl.occurrence.onAllDay && $ctrl.onSameDay()">
      <span>{{$ctrl.startDate() | displayAsDate}} - {{$ctrl.startDate() | displayAsTime}} ${atLabel} {{$ctrl.endDate() | displayAsTime}}</span>
    </div>
    <div ng-if="!$ctrl.occurrence  .onAllDay && !$ctrl.onSameDay()">
      <span>${fromDateLabel} {{$ctrl.startDate() | displayAsDate}} ${atLabel} {{$ctrl.startDate() | displayAsTime}}</span>
      <span>${toLabel} {{$ctrl.endDate() | displayAsDate}} ${atLabel} {{$ctrl.endDate() | displayAsTime}}</span>
    </div>
  </div>
  <div class="occurrence-location" ng-if="$ctrl.occurrence.location">
    <span ng-bind-html="$ctrl.occurrence.location | noHTML | newlines"></span>
  </div>
  <div class="occurrence-external-link" ng-if="$ctrl.occurrence.externalUrl()">
    <a target="_blank" href="{{$ctrl.occurrence.externalUrl()}}">{{$ctrl.occurrence.externalUrl()}}</a>
  </div>
  <div class="occurrence-reminder" ng-class="{'reminder-set':$ctrl.reminder, 'no-reminder':!$ctrl.reminder}">
    <silverpeas-calendar-event-reminder occurrence="$ctrl.occurrence"
                                        reminder="$ctrl.reminder"
                                        on-created="$ctrl.onReminderChange($ctrl.occurrence)"
                                        on-updated="$ctrl.onReminderChange($ctrl.occurrence)"
                                        on-deleted="$ctrl.onReminderChange($ctrl.occurrence)"></silverpeas-calendar-event-reminder>
  </div>
</div>
<div>
  <span class="occurrence-description" ng-if="$ctrl.occurrence.calendarSync" ng-bind-html="$ctrl.occurrence.description | trustedHTML"></span>
  <span class="occurrence-description" ng-if="!$ctrl.occurrence.calendarSync" ng-bind-html="$ctrl.occurrence.description | noHTML | newlines"></span>
</div>
<div>
  <silverpeas-attendees ng-if="$ctrl.occurrence.attendees.length"
                        attendees="$ctrl.occurrence.attendees"
                        is-simple-mode="true"
                        on-attendee-answer="$ctrl.onAttendeeParticipationAnswer($ctrl.occurrence, attendee)">
  </silverpeas-attendees>
  <silverpeas-calendar-event-occurrence-tip-actions
      occurrence="$ctrl.occurrence"
      on-view="$ctrl.onView(occurrence)"
      on-modify="$ctrl.onModify(occurrence)"
      on-delete="$ctrl.onDelete(occurrence)"
      on-go-to-first-occurrence="$ctrl.onGoToFirstOccurrence(occurrence)">
  </silverpeas-calendar-event-occurrence-tip-actions>
</div>