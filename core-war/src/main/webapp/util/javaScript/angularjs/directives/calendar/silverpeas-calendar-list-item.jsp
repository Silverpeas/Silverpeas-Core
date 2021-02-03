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

<fmt:message var="showLabel" key="calendar.action.show"/>
<fmt:message var="hideLabel" key="calendar.action.hide"/>
<fmt:message var="removeLabel" key="calendar.action.remove"/>
<fmt:message var="infoLabel" key="calendar.action.info"/>
<fmt:message var="synchronizeLabel" key="calendar.action.synchronize"/>
<fmt:message var="exportLabel" key="GML.export"/>
<fmt:message var="modifyLabel" key="GML.modify"/>
<fmt:message var="deleteLabel" key="GML.delete"/>
<fmt:message var="actionLabelManageSubscriptions" key="GML.manageSubscriptions"/>

<div class="silverpeas-calendar-list-item" ng-class="{'unselected':$ctrl.calendar.notVisible}">
  <silverpeas-color-picker color="$ctrl.calendar.color"
                           potential-colors="$ctrl.calendarPotentialColors"
                           on-select="$ctrl.onCalendarColorSelect({calendar:$ctrl.calendar,color:color})"
                           ng-if="$ctrl.calendar.color"></silverpeas-color-picker>
  <div class="item-detail">
  <div class="item-title">
     <div ng-if="$ctrl.calendar.canBeModified">
       <a href="#" ng-click="$ctrl.modify({calendar: $ctrl.calendar})">{{$ctrl.calendar.title}}</a>
     </div>
     <div>
        <a href="#" ng-click="$ctrl.view({calendar:$ctrl.calendar})">{{$ctrl.calendar.title}}</a>
      </div>
    </div>
    <a class="check-visibility" href="#" ng-click="$ctrl.onCalendarVisibilityToggle({calendar:$ctrl.calendar})" title="{{$ctrl.calendar.notVisible ? '${silfn:escapeJs(showLabel)}' : '${silfn:escapeJs(hideLabel)}'}}">{{$ctrl.calendar.notVisible ? '${silfn:escapeJs(showLabel)}' : '${silfn:escapeJs(hideLabel)}'}}</a>
    <a class="show-menu"></a>
    <a class="remove-calendar" href="#" ng-click="$ctrl.remove({calendar: $ctrl.calendar})" ng-if="$ctrl.calendar.canBeRemoved" title="${removeLabel}">${removeLabel}</a>
    <div class="silverpeas-calendar-list-item-menu" style="display: none">
      <ul>
        <li ng-if="!$ctrl.calendar.canBeRemoved">
          <a href="#" ng-click="$ctrl.view({calendar:$ctrl.calendar})">${infoLabel}</a>
        </li>
        <li ng-if="(!$ctrl.calendar.userPersonal || $ctrl.calendar.canBeDeleted) && !$ctrl.calendar.canBeRemoved">
          <a href="{{$ctrl.calendar.uri}}/export/ical">${exportLabel}</a>
        </li>
        <li ng-if="$ctrl.calendar.isSynchronized">
          <a href="#" ng-click="$ctrl.synchronize({calendar: $ctrl.calendar})">${synchronizeLabel}</a>
        </li>
        <li ng-if="$ctrl.calendar.canBeModified">
          <a href="#" ng-click="$ctrl.modify({calendar: $ctrl.calendar})">${modifyLabel}</a>
        </li>
        <li ng-if="$ctrl.calendar.canBeDeleted">
          <a href="#" ng-click="$ctrl.delete({calendar: $ctrl.calendar})">${deleteLabel}</a>
        </li>
        <li ng-if="$ctrl.spSubManager" class="separator"></li>
        <li ng-if="$ctrl.spSubManager && $ctrl.calendar.isCurrentUserAdmin()">
          <a ng-href="calendars/{{$ctrl.calendar.id}}/subscriptions/manage">${actionLabelManageSubscriptions}</a>
        </li>
        <li ng-if="$ctrl.spSubManager">
          <a href="#" ng-click="$ctrl.spSubManager.switchUserSubscription()"><span id="subscriptionMenuLabel{{$ctrl.calendar.id}}"></span></a>
        </li>
      </ul>
    </div>
  </div>
</div>