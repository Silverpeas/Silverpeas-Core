<%--
  ~ Copyright (C) 2000 - 2019 Silverpeas
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

<c:set var="deleteMessage"><fmt:message key='GML.confirmation.delete'><fmt:param value="{{$ctrl.occurrence.title}}"/></fmt:message></c:set>
<c:set var="deleteChoiceMessage"><fmt:message key='calendar.message.event.delete'><fmt:param value="{{$ctrl.occurrence.title}}"/></fmt:message></c:set>
<c:set var="updateLevelEventInfo"><fmt:message key='calendar.message.event.update.level.info'/></c:set>
<c:set var="updateChoiceMessage"><fmt:message key='calendar.message.event.update'><fmt:param value="{{$ctrl.occurrence.title}}"/></fmt:message></c:set>
<c:set var="calendarUpdateMessage"><fmt:message key='calendar.message.event.update.calendar.changed'><fmt:param value="{{$ctrl.occurrence.title}}"/></fmt:message></c:set>
<c:set var="attendeeAnswerChoiceMessage"><fmt:message key='calendar.message.event.attendee.participation.update'><fmt:param value="{{$ctrl.occurrence.title}}"/></fmt:message></c:set>
<c:set var="allOccurrenceMessage"><fmt:message key='calendar.message.event.recurrence.occurrence.all'/></c:set>
<c:set var="allOccurrencePartMessage"><fmt:message key='calendar.message.event.recurrence.occurrence.attendee.participation.all'/></c:set>
<c:set var="fromOccurrenceMessage"><fmt:message key='calendar.message.event.recurrence.occurrence.from'><fmt:param value="{{$ctrl.displayOriginalStartDate()}}"/></fmt:message></c:set>
<c:set var="uniqueOccurrenceMessage"><fmt:message key='calendar.message.event.recurrence.occurrence.unique'><fmt:param value="{{$ctrl.displayOriginalStartDate()}}"/></fmt:message></c:set>
<c:set var="deleteLevelEventInfo"><fmt:message key='calendar.message.event.delete.level.info'/></c:set>

<div class="silverpeas-calendar-event-management-update-popin" style="display: none">
  <p ng-if="$ctrl.updateMethodAtEventLevel && $ctrl.updateMethodType == 'ALL'" class="inlineMessage">${updateLevelEventInfo}</p>
  <span>${updateChoiceMessage}</span>
  <ul class="champs">
    <li ng-if="!$ctrl.updateMethodAtEventLevel || $ctrl.updateMethodType == 'UNIQUE'">
      <label>
        <input type="radio" name="updateMethodType" ng-model="$ctrl.updateMethodType" ng-value="'UNIQUE'">
        ${uniqueOccurrenceMessage}
      </label>
    </li>
    <li ng-if="!$ctrl.updateMethodAtEventLevel || $ctrl.updateMethodType == 'FROM'">
      <label>
        <input type="radio" name="updateMethodType" ng-model="$ctrl.updateMethodType" ng-value="'FROM'">
        ${fromOccurrenceMessage}
      </label>
    </li>
    <li ng-if="$ctrl.isFirstEventOccurrence()">
      <label>
        <input type="radio" name="updateMethodType" ng-model="$ctrl.updateMethodType" ng-value="'ALL'">
        ${allOccurrenceMessage}
      </label>
    </li>
  </ul>
</div>

<div class="silverpeas-calendar-event-management-attendee-answer-popin" style="display: none">
  <span>${attendeeAnswerChoiceMessage}</span>
  <ul class="champs">
    <li>
      <label>
        <input type="radio" name="answerMethodType" ng-model="$ctrl.answerMethodType" ng-value="'UNIQUE'">
        ${uniqueOccurrenceMessage}
      </label>
    </li>
    <li>
      <label>
        <input type="radio" name="answerMethodType" ng-model="$ctrl.answerMethodType" ng-value="'ALL'">
        ${allOccurrencePartMessage}
      </label>
    </li>
  </ul>
</div>

<div class="silverpeas-calendar-event-management-delete-popin" style="display: none">
  <p ng-if="$ctrl.isRecurrence() && $ctrl.isFirstEventOccurrence()" class="inlineMessage">${deleteLevelEventInfo}</p>
  <span ng-if="!$ctrl.isRecurrence()">${deleteMessage}</span>
  <span ng-if="$ctrl.isRecurrence()">${deleteChoiceMessage}</span>
  <ul class="champs" ng-if="$ctrl.isRecurrence()">
    <li ng-if="!$ctrl.isRecurrence() || !$ctrl.isFirstEventOccurrence()">
      <label>
        <input type="radio" name="deleteMethodType" ng-model="$ctrl.deleteMethodType" ng-value="'UNIQUE'">
        ${uniqueOccurrenceMessage}
      </label>
    </li>
    <li ng-if="!$ctrl.isRecurrence() || !$ctrl.isFirstEventOccurrence()">
      <label>
        <input type="radio" name="deleteMethodType" ng-model="$ctrl.deleteMethodType" ng-value="'FROM'">
        ${fromOccurrenceMessage}
      </label>
    </li>
    <li>
      <label>
        <input type="radio" name="deleteMethodType" ng-model="$ctrl.deleteMethodType" ng-value="'ALL'">
        ${allOccurrenceMessage}
      </label>
    </li>
  </ul>
</div>