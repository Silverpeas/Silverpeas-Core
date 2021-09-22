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
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.silverpeas.com/tld/silverFunctions" prefix="silfn" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>

<fmt:setLocale value="${sessionScope['SilverSessionController'].favoriteLanguage}"/>
<view:setBundle basename="org.silverpeas.jobStartPagePeas.multilang.jobStartPagePeasBundle"/>
<view:setBundle basename="org.silverpeas.jobStartPagePeas.settings.jobStartPagePeasIcons" var="icons"/>
<c:set var="lookHelper" value="${sessionScope['Silverpeas_LookHelper']}"/>
<view:setBundle bundle="${lookHelper.localizedBundle}" var="lookBundle"/>

<fmt:message var="pxUrl" key="JSPP.px" bundle="${icons}"/>
<c:url var="pxUrl" value="${pxUrl}"/>
<fmt:message var="homeSpaceIconUrl" key="JSPP.homeSpaceIcon" bundle="${icons}"/>
<c:url var="homeSpaceIconUrl" value="${homeSpaceIconUrl}"/>
<fmt:message var="chooseLabel" key="JSPP.Choose"/>
<fmt:message var="backToMainSpaceLabel" key="JSPP.BackToMainSpacePage"/>
<c:url var="iconsPrefix" value="/util/icons"/>

<!-- ########################################################################################### -->
<silverpeas-component-template name="root">
  <div class="admin-navigation">
    <admin-navigation-space-selector
        v-if="selectorSpaces"
        v-on:space-select="loadSelectedSpace"
        v-bind:spaces="selectorSpaces"
        v-bind:currentSpace="selectorCurrentSpace"></admin-navigation-space-selector>
    <admin-navigation-tree-level
        v-if="tree"
        v-on:space-select="loadSelectedSpace"
        v-on:application-select="loadSelectedApplication"
        v-bind:tree="tree"></admin-navigation-tree-level>
  </div>
</silverpeas-component-template>

<!-- ########################################################################################### -->
<silverpeas-component-template name="space-selector">
  <div class="intfdcolor51">
    <div class="treeview_selectSpace">
      <input name="privateSubDomain" type="hidden"/>
      <img src="${pxUrl}" height="20" width="0" alt=""/>
      <span class="selectNS">
        <select size=1 v-model="selectedSpaceId" v-on:change="selected">
          <option value="none">${chooseLabel}</option>
          <option value="none">--------------------</option>
          <option v-for="space in spaces" v-bind:value="space.id">
            {{space.label}}
          </option>
        </select>
      </span>
      <a href="javascript:void(0)" v-on:click="selected">
        <img id="space-icon" src="${homeSpaceIconUrl}" alt="${backToMainSpaceLabel}" title="${backToMainSpaceLabel}"/>
      </a>
    </div>
  </div>
</silverpeas-component-template>

<!-- ########################################################################################### -->
<silverpeas-component-template name="tree-level">
  <div class="intfdcolor51">
    <div class="treeview_contentSpace">
      <div v-if="spacePath.length" class="treeview_path">
        <div v-for="(pathItem, depth) in spacePath" v-bind:key="pathItem.id" v-on:click="$emit('space-select', pathItem)">
          <a href="javascript:void(0)">
            <template v-for="index in depth"><span class="space"></span></template>
            <img src="${iconsPrefix}/treeview/trait-virage.gif" alt=""><span>{{pathItem.label}}</span>
          </a>
        </div>
      </div>
      <admin-navigation-tree-space
          v-for="space in spaces" v-bind:key="space.id"
          v-bind:space="space" v-bind:level="spacePath.length"
          v-on:space-select="$emit('space-select', $event)"></admin-navigation-tree-space>
      <admin-navigation-tree-application
          v-for="application in applications" v-bind:key="application.id"
          v-bind:application="application" v-bind:level="spacePath.length"
          v-on:application-select="$emit('application-select', $event)"></admin-navigation-tree-application>
    </div>
  </div>
</silverpeas-component-template>

<!-- ########################################################################################### -->
<silverpeas-component-template name="tree-space">
  <div id="domId" v-bind:name="space.id" v-on:click="$emit('space-select', space)">
    <a href="javascript:void(0)">
      <img src="${iconsPrefix}/treeview/plus.gif" alt="">{{space.label}}
    </a>
  </div>
</silverpeas-component-template>

<!-- ########################################################################################### -->
<silverpeas-component-template name="tree-application">
  <div v-on:click="$emit('application-select', application)">
    <a href="javascript:void(0)">
      <img v-bind:src="iconUrl" class="component-icon" alt="">{{application.label}}
    </a>
  </div>
</silverpeas-component-template>