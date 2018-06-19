<%--
  ~ Copyright (C) 2000 - 2018 Silverpeas
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

<div class="permalink" v-bind:class="{'simple' : simple}">
  <div v-init>
    {{addMessages({
    copyOk : '${silfn:escapeJs(permalinkCopyOkMessage)}'
    })}}
  </div>
  <div v-if="isFull" ref="fullContainer"></div>
  <div v-else>
    <a v-bind:class="{'sp-permalink': !noHrefHook, 'sp-direct-permalink': noHrefHook}" v-bind:href="link" title="${permalinkLabel}">
      <img src="${linkIconUrl}" alt="${permalinkLabel}" />
    </a>
    <input ref="linkInput" type="text" v-bind:value="link" />
    <silverpeas-button title="${permalinkCopyLabel}" v-on:click.native="copyLink()" class="copy-to-clipboard">${permalinkCopyLabel}</silverpeas-button>
  </div>
</div>
