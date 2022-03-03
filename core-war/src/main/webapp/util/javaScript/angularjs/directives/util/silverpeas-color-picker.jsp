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
  ~ along with this program.  If not, see <http://www.gnu.org/licenses/>.
  --%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>

<c:set var="userLanguage" value="${sessionScope['SilverSessionController'].favoriteLanguage}"/>
<fmt:setLocale value="${userLanguage}"/>
<view:setBundle basename="org.silverpeas.multilang.generalMultilang"/>

<fmt:message var="defaultColorLabel" key="GML.picker.color.default"/>

<div class="silverpeas-color-picker"
     ng-style="{'background-color':$ctrl.color,'border-color':$ctrl.color}"></div>
<div class="silverpeas-color-picker-set" style="display: none">
  <div class="silverpeas-color-picker"
       ng-repeat="color in $ctrl.potentialColors"
       ng-click="$ctrl.select(color)"
       ng-style="{'background-color':color}"
       ng-class="{'active' : ($ctrl.color === color)}"></div>
  <div class="silverpeas-color-picker"
       ng-click="$ctrl.select('none')"
       ng-style="{'background': 'transparent url(<c:url value="/util/icons/delete.gif"/>) 2px 2px no-repeat'}"
       ng-class="{'active' : ($ctrl.color === 'none')}" title="${defaultColorLabel}"></div>
</div>
