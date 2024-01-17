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

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://www.silverpeas.com/tld/silverFunctions" prefix="silfn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>

<c:set var="userLanguage" value="${sessionScope['SilverSessionController'].favoriteLanguage}"/>
<fmt:setLocale value="${userLanguage}"/>
<view:setBundle basename="org.silverpeas.reminder.multilang.reminder"/>

<fmt:message var="reminderLabel" key="reminder.label"/>
<fmt:message var="addLabel" key="reminder.add"/>

<script type="text/ng-template" id="###silverpeas.contribution.reminder.DATETIME">
  <span>Not yet implemented</span>
</script>

<script type="text/ng-template" id="###silverpeas.contribution.reminder.DURATION">
  <label ng-if="$ctrl.mainLabel">{{$ctrl.mainLabel === 'defaultLabel' ? '${reminderLabel}' : $ctrl.mainLabel}}</label>
  <silverpeas-contribution-reminder-duration-item
      ng-if="$ctrl.reminder"
      reminder="$ctrl.reminder"
      possible-durations="$ctrl.possibleDurations"
      default-duration-index="$ctrl.defaultDurationIndex"
      autonomous="$ctrl.autonomous"
      on-updated="$ctrl.onUpdatedHook()"
      on-deleted="$ctrl.onDeletedHook()"></silverpeas-contribution-reminder-duration-item>
</script>

<silverpeas-contribution-reminder-management
    api="$ctrl.reminderApi"
    on-created="$ctrl.reminder = reminder;$ctrl.onCreated({reminder:reminder})"></silverpeas-contribution-reminder-management>
<div ng-if="$ctrl.shown">
  <a href="javascript:void(0)" class="add-action" ng-click="$ctrl.add()" ng-if="$ctrl.addLabel && !$ctrl.reminder">{{$ctrl.addLabel === 'defaultLabel' ? '${addLabel}' : $ctrl.addLabel}}</a>
  <div ng-if="$ctrl.mode == 'DATETIME' && $ctrl.reminder" ng-include="'###silverpeas.contribution.reminder.DATETIME'"></div>
  <div ng-if="$ctrl.mode == 'DURATION' && $ctrl.reminder" ng-include="'###silverpeas.contribution.reminder.DURATION'"></div>
</div>