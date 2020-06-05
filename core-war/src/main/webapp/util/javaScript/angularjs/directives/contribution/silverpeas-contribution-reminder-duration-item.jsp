<%--
  ~ Copyright (C) 2000 - 2020 Silverpeas
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
<view:setBundle basename="org.silverpeas.reminder.multilang.reminder"/>

<fmt:message var="deleteLabel" key="GML.delete"/>

<silverpeas-contribution-reminder-management api="$ctrl.reminderApi"
                                             on-updated="$ctrl.reminder = reminder;$ctrl.onUpdated({reminder:reminder})"
                                             on-deleted="$ctrl.reminder = undefined;$ctrl.onDeleted()"></silverpeas-contribution-reminder-management>

<select ng-model="$ctrl.selectedDurationAndUnit"  ng-if="$ctrl.reminder.canBeModified"
        ng-change="$ctrl.modify()"
        ng-options="duration.ui_id as duration.label for duration in $ctrl.possibleDurations">
</select>
<span ng-if="!$ctrl.reminder.canBeModified">{{$ctrl.getReminderLabel()}}</span>
<div class="actions">
  <a href="javascript:void(0)" ng-if="$ctrl.reminder.canBeDeleted" ng-click="$ctrl.remove()" title="${deleteLabel}">
    <img src="<c:url value='/util/icons/delete.gif'/>" alt="${deleteLabel}">
  </a>
</div>