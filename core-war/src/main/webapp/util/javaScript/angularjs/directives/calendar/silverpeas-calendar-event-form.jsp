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

<fmt:message var="validateLabel" key="GML.validate"/>
<fmt:message var="cancelLabel" key="GML.cancel"/>
<c:set var="mandatoryMessage"><b>@name@</b> <fmt:message key='GML.MustBeFilled'/></c:set>
<c:set var="mustBePositiveIntegerMessage"><b>@name@</b> <fmt:message key='GML.MustContainsPositiveNumber'/></c:set>
<c:set var="nbMaxMessage"><b>@name@</b> <fmt:message key='GML.data.error.message.string.limit'><fmt:param value="@length@"/></fmt:message></c:set>
<c:set var="correctDateMessage"><b>@name@</b> <fmt:message key='GML.MustContainsCorrectDate'/></c:set>
<c:set var="correctHourMessage"><b>@name@</b> <fmt:message key='GML.MustContainsCorrectHour'/></c:set>
<c:set var="correctPeriodMessage"><b>@end@</b> <fmt:message key='GML.MustContainsPostDateTo'/> <b>@start@</b></c:set>
<c:set var="updateLevelEventInfo"><fmt:message key='calendar.message.event.update.level.info'/></c:set>
<fmt:message var="goTofirstOccurrenceLabel" key="calendar.message.event.occurrence.gotoFirst"/>
<fmt:message var="gotoPreviousOccurrenceLabel" key="calendar.message.event.occurrence.gotoPrevious"/>

<div style="display: none">
  <span ng-init="$ctrl.api.messages.mandatory = '${silfn:escapeJs(mandatoryMessage)}'"></span>
  <span ng-init="$ctrl.api.messages.mustBePositiveInteger = '${silfn:escapeJs(mustBePositiveIntegerMessage)}'"></span>
  <span ng-init="$ctrl.api.messages.nbMax = '${silfn:escapeJs(nbMaxMessage)}'"></span>
  <span ng-init="$ctrl.api.messages.date.correct = '${silfn:escapeJs(correctDateMessage)}'"></span>
  <span ng-init="$ctrl.api.messages.time.correct = '${silfn:escapeJs(correctHourMessage)}'"></span>
  <span ng-init="$ctrl.api.messages.period.correct = '${silfn:escapeJs(correctPeriodMessage)}'"></span>
</div>

<p ng-if="!$ctrl.isFirstEventOccurrence()" class="inlineMessage">${updateLevelEventInfo}<br/>
  <span>
    <a href="#" class="first-occurrence-label"
       ng-click="$ctrl.goToFirstOccurrence()">${goTofirstOccurrenceLabel}</a>
  </span>
</p>
<p class="inlineMessage" ng-if="$ctrl.previousOccurrence">
  <span>
    <a href="#" ng-click="$ctrl.goToPreviousOccurrence()">${gotoPreviousOccurrenceLabel}</a>
  </span>
</p>

<div ng-if="$ctrl.data" ng-transclude></div>

<div class="legend">
  <img alt="mandatory" src="${mandatoryIcons}" width="5" height="5"/>&nbsp;
  <fmt:message key='GML.requiredField'/>
</div>

<p>
  <silverpeas-button-pane>
    <silverpeas-button ng-click="$ctrl.api.validate()" ng-hide="$ctrl.readOnly"> ${validateLabel} </silverpeas-button>
    <silverpeas-button ng-click="$ctrl.api.cancel()">${cancelLabel}</silverpeas-button>
  </silverpeas-button-pane>
</p>