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
<view:setBundle basename="org.silverpeas.calendar.multilang.calendarBundle"/>

<c:url var="mandatoryIcons" value="/util/icons/mandatoryField.gif"/>

<fmt:message var="seeMoreLabel" key="GML.seeMore"/>
<fmt:message var="modifyLabel" key="GML.modify"/>
<fmt:message var="deleteLabel" key="GML.delete"/>
<fmt:message var="goTofirstOccurrenceLabel" key="calendar.message.event.occurrence.gotoFirst"/>

<p>
  <silverpeas-button-pane>
    <silverpeas-button ng-if="$ctrl.occurrence.canBeAccessed" ng-click="$ctrl.onView({occurrence:$ctrl.occurrence})">${seeMoreLabel}</silverpeas-button>
    <silverpeas-button ng-if="$ctrl.occurrence.canBeModified" ng-click="$ctrl.onModify({occurrence:$ctrl.occurrence})">${modifyLabel}</silverpeas-button>
    <silverpeas-button ng-if="$ctrl.occurrence.canBeDeleted" ng-click="$ctrl.onDelete({occurrence:$ctrl.occurrence})">${deleteLabel}</silverpeas-button>
  </silverpeas-button-pane>
  <div class="goto-first-occurrence">
    <a href="#" class="first-occurrence-label"
       ng-if="$ctrl.occurrence.recurrence && !$ctrl.occurrence.firstEventOccurrence"
       ng-click="$ctrl.onGoToFirstOccurrence({occurrence:$ctrl.occurrence})">${goTofirstOccurrenceLabel}</a>
  </div>
</p>