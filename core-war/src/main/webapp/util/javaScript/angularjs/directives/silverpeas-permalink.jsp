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
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.silverpeas.com/tld/silverFunctions" prefix="silfn" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>
<c:set var="language" value="${sessionScope['SilverSessionController'].favoriteLanguage}"/>
<fmt:setLocale value="${language}"/>
<view:setBundle basename="org.silverpeas.multilang.generalMultilang"/>

<fmt:message var="permalinkLabel" key="GML.permalink"/>
<fmt:message var="permalinkCopyLabel" key="GML.permalink.copy"/>
<fmt:message var="permalinkCopyOkMessage" key="GML.permalink.copy.ok"/>
<fmt:message var="permalinkHelp" key="GML.permalink.help"/>

<c:url var="linkIconUrl" value="/util/icons/link.gif"/>

<div style="display: none">
  <span ng-init="$ctrl.messages.copyOk = '${silfn:escapeJs(permalinkCopyOkMessage)}'"></span>
</div>

<script type="text/ng-template" id="###silverpeas.permalink.full">
  <div ng-include src="$ctrl.getFullTemplateUrl()"></div>
</script>

<script type="text/ng-template" id="###silverpeas.permalink.simple">
  <a ng-class="{'sp-permalink': !$ctrl.noHrefHook, 'sp-direct-permalink': $ctrl.noHrefHook}" href="{{$ctrl.link}}" title="${permalinkLabel}"><img src="${linkIconUrl}" alt="${permalinkLabel}" /></a>
  <input type="text" value="{{$ctrl.link}}" />
  <silverpeas-button title="${permalinkCopyLabel}" ng-click="$ctrl.copyLink()" class="copy-to-clipboard">${permalinkCopyLabel}</silverpeas-button>
</script>

<div class="permalink" ng-class="{'simple' : $ctrl.simple}" ng-include="$ctrl.getTemplate()"></div>
