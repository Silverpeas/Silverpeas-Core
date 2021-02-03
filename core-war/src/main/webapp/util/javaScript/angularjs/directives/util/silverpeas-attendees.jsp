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
<view:setBundle basename="org.silverpeas.multilang.generalMultilang"/>

<fmt:message var="selectActionLabel" key="GML.action.select"/>

<script type="text/ng-template" id="###silverpeas.user.attendees.content">
  <div class="buttonPanel">
    <a href="#" class="explorePanel" ng-click="$ctrl.api.select()" ng-if="$ctrl.isWriteMode">
      <span>${selectActionLabel}</span>
    </a>
  </div>
  <ul class="access-list">
    <li ng-repeat="userAttendee in $ctrl.userAttendees | orderBy:'fullName' track by userAttendee.id">
      <silverpeas-attendee attendee="userAttendee"
                           on-remove="$ctrl.removeUserAttendee(attendee)"
                           is-write-mode="$ctrl.isWriteMode"
                           is-view-mode="$ctrl.isViewMode"
                           on-answer="$ctrl.onAttendeeAnswer({attendee:attendee})">
      </silverpeas-attendee>
    </li>
  </ul>
  <input type="text" ng-disabled="true" style="display: none;" class="user-ids"
         id="{{$ctrl.userPanelPrefix}}-userIds"
         name="{{$ctrl.userPanelPrefix}}UserPanelCurrentUserIds"
         ng-model="$ctrl.userPanelIds"
         ng-list>
</script>

<script type="text/ng-template" id="###silverpeas.user.attendees.simple.content">
  <ul class="simple-list" ng-cloak>
    <li ng-repeat="userAttendee in $ctrl.userAttendees | orderBy:'fullName' track by userAttendee.id">
      <silverpeas-attendee attendee="userAttendee"
                           is-simple-mode="$ctrl.isSimpleMode"
                           on-answer="$ctrl.onAttendeeAnswer({attendee:attendee})">
      </silverpeas-attendee>
    </li>
  </ul>
</script>

<fieldset ng-if="$ctrl.label" class="skinFieldset" ng-class="{empty:!userAttendee.length}">
  <legend>{{$ctrl.label}}</legend>
  <div class="fields" ng-include="$ctrl.getTemplate()"></div>
</fieldset>
<div ng-if="!$ctrl.label" class="fields" ng-include="$ctrl.getTemplate()"></div>