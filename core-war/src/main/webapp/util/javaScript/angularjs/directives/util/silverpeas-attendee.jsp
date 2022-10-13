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
<view:setBundle basename="org.silverpeas.multilang.generalMultilang"/>

<fmt:message var="removeLabel" key="GML.action.remove"/>
<fmt:message var="awaitingLabel" key="GML.attendee.participation.status.awaiting"/>
<fmt:message var="acceptedLabel" key="GML.attendee.participation.status.accepted"/>
<fmt:message var="declinedLabel" key="GML.attendee.participation.status.declined"/>
<fmt:message var="tentativeLabel" key="GML.attendee.participation.status.tentative"/>
<fmt:message var="requiredLabel" key="GML.attendee.presence.status.required"/>
<fmt:message var="optionalLabel" key="GML.attendee.presence.status.optional"/>
<fmt:message var="informativeLabel" key="GML.attendee.presence.status.informative"/>

<c:url value='/util/icons/' var="iconPrefixUrl"/>

<div style="display: none">
  <span ng-init="$ctrl.labels.awaiting = '${silfn:escapeJs(awaitingLabel)}'"></span>
  <span ng-init="$ctrl.labels.accepted = '${silfn:escapeJs(acceptedLabel)}'"></span>
  <span ng-init="$ctrl.labels.declined = '${silfn:escapeJs(declinedLabel)}'"></span>
  <span ng-init="$ctrl.labels.tentative = '${silfn:escapeJs(tentativeLabel)}'"></span>
  <span ng-init="$ctrl.labels.required = '${silfn:escapeJs(requiredLabel)}'"></span>
  <span ng-init="$ctrl.labels.optional = '${silfn:escapeJs(optionalLabel)}'"></span>
  <span ng-init="$ctrl.labels.informative = '${silfn:escapeJs(informativeLabel)}'"></span>
</div>

<span class="{{$ctrl.attendee.participationStatus}}" ng-class="{userToZoom:$ctrl.isUserZoom()}"
      rel="{{$ctrl.attendee.id}}">{{$ctrl.attendee.fullName}}</span>
<span class="participation" ng-if="$ctrl.isWriteMode || $ctrl.isUserZoom()">
  <span class="{{$ctrl.attendee.participationStatus}}" title="{{$ctrl.getParticipationStatusLabel()}}">{{$ctrl.getParticipationStatusLabel()}}</span>
  <span ng-if="$ctrl.attendee.participationStatus != 'AWAITING'">
    <img ng-init="p = $ctrl.getParticipationStatusDefinition($ctrl.attendee.participationStatus)"
         ng-src="${iconPrefixUrl}{{p.icon}}" alt="{{p.label}}">
  </span>
</span>
<span class="participation" ng-if="!$ctrl.isWriteMode && !$ctrl.isUserZoom()">
  <a href="javascript:void(0)"
     ng-repeat="participationStatus in $ctrl.participationStatuses"
     ng-if="participationStatus.name != 'AWAITING'"
     ng-click="$ctrl.attendee.participationStatus != participationStatus.name ? $ctrl.answer(participationStatus.name) : null"
     ng-class="{'not-answered':$ctrl.attendee.participationStatus != participationStatus.name, 'answered':$ctrl.attendee.participationStatus == participationStatus.name}"
     title="{{participationStatus.label}}">
    <img ng-src="${iconPrefixUrl}{{participationStatus.icon}}" alt="{{participationStatus.label}}">
  </a>
</span>
<span class="presence" ng-if="$ctrl.isWriteMode">
  <select ng-model="$ctrl.attendee.presenceStatus"
          ng-options="presenceStatus.name as presenceStatus.label for presenceStatus in $ctrl.presenceStatuses"></select>
</span>
<div class="operation" ng-if="$ctrl.isWriteMode">
<a href="#" title="${removeLabel}" ng-click="$ctrl.onRemove({attendee:$ctrl.attendee})">
  <img src="<c:url value='/util/icons/delete.gif'/>" title="${removeLabel}" alt="${removeLabel}">
</a>
</div>