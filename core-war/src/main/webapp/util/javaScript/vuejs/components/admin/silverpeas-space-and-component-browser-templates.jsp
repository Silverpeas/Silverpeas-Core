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
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.silverpeas.com/tld/silverFunctions" prefix="silfn" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>
<c:set var="language" value="${sessionScope['SilverSessionController'].favoriteLanguage}"/>
<fmt:setLocale value="${language}"/>
<view:setBundle basename="org.silverpeas.multilang.generalMultilang"/>

<fmt:message var="rootLabel" key="GML.home"/>
<fmt:message var="spacesLabel" key="GML.spaces"/>
<fmt:message var="gotoSpaceLabel" key="GML.space.goto"/>
<fmt:message var="selectSpaceLabel" key="GML.space.select"/>
<fmt:message var="componentsLabel" key="GML.components"/>
<fmt:message var="gotoComponentLabel" key="GML.component.goto"/>
<fmt:message var="selectComponentLabel" key="GML.component.select"/>

<!-- ########################################################################################### -->
<silverpeas-component-template name="space-and-component-browser">
  <div class="space-and-component-browser">
    <private-space-and-component-breadcrumb
        v-bind:space-selectable="spaceSelectable"
        v-bind:space-content-enabled="spaceContentEnabled"
        v-bind:component-selectable="componentSelectable"
        v-bind:component-content-enabled="componentContentEnabled"
        v-bind:current-space="currentSpace"
        v-bind:current-component="currentComponent"
        v-on:enter-root="loadRoot"
        v-on:enter-space="loadSpace">
      <slot name="extend-browser-breadcrumb"></slot>
    </private-space-and-component-breadcrumb>
    <div class="browser">
      <private-space-and-component-spaces
          v-if="currentSpaces"
          v-bind:space-selectable="spaceSelectable"
          v-bind:space-content-enabled="spaceContentEnabled"
          v-bind:component-selectable="componentSelectable"
          v-bind:component-content-enabled="componentContentEnabled"
          v-bind:spaces="currentSpaces"
          v-bind:space-filter="spaceFilter"
          v-bind:is-browser-extended="isBrowserExtensionSlot()"
          v-on:enter-space="loadSpace"
          v-on:select-space="selectSpace"></private-space-and-component-spaces>
      <private-space-and-component-space-components
          v-if="displayComponents"
          v-bind:space-selectable="spaceSelectable"
          v-bind:space-content-enabled="spaceContentEnabled"
          v-bind:component-selectable="componentSelectable"
          v-bind:component-content-enabled="componentContentEnabled"
          v-bind:components="currentComponents"
          v-bind:component-filter="componentFilter"
          v-on:enter-component="loadComponent"
          v-on:select-component="selectComponent"></private-space-and-component-space-components>
      <template v-if="currentSpaces">
        <slot name="extend-browser"></slot>
        <slot name="extend-browser-selection"></slot>
      </template>
    </div>
  </div>
</silverpeas-component-template>

<!-- ########################################################################################### -->
<silverpeas-component-template name="breadcrumb">
  <div v-sp-init>
    {{addMessages({
    gotoSpaceLabel : '${silfn:escapeJs(gotoSpaceLabel)}',
    gotoComponentLabel : '${silfn:escapeJs(gotoComponentLabel)}'
  })}}
  </div>
  <div class="breadcrumb">
    <a href="javascript:void(0)" title="${rootLabel}" class="root"
       v-on:click="$emit('enter-root')"></a>
    <template v-if="path">
      <template v-for="item in path" v-bind:key="item.id">
        <span class="separator"></span>
        <a href="javascript:void(0)"
           v-html="item.label"
           v-bind:title="gotoTitle(item)"
           v-bind:class="getCssClass(item)"
           v-bind:style="getCssStyle(item)"
           v-on:click="onItem(item)"></a>
      </template>
    </template>
    <slot></slot>
  </div>
</silverpeas-component-template>

<!-- ########################################################################################### -->
<silverpeas-component-template name="spaces">
  <div class="spaces">
    <h4>${spacesLabel}</h4>
    <ul>
      <private-space-and-component-spaces-space
          v-for="space in spaces" v-bind:key="space.id"
          v-bind:space-selectable="spaceSelectable"
          v-bind:space-content-enabled="spaceContentEnabled"
          v-bind:component-selectable="componentSelectable"
          v-bind:component-content-enabled="componentContentEnabled"
          v-bind:space-filter="spaceFilter"
          v-bind:space="space"
          v-bind:is-browser-extended="isBrowserExtended"
          v-on:enter-root="$emit('enter-root')"
          v-on:enter-space="$emit('enter-space', $event)"
          v-on:select-space="$emit('select-space', $event)"></private-space-and-component-spaces-space>
    </ul>
  </div>
</silverpeas-component-template>

<!-- ########################################################################################### -->
<silverpeas-component-template name="spaces-space">
  <div v-sp-init>
    {{addMessages({
    gotoSpaceLabel : '${silfn:escapeJs(gotoSpaceLabel)}',
    selectSpaceLabel : '${silfn:escapeJs(selectSpaceLabel)}'
  })}}
  </div>
  <li v-bind:id="domId()"
      v-bind:class="{'selectable':isSpaceSelectable(),'walkable':isGotoEnabled()}"
      v-bind:title="gotoTitle()"
      v-on:click="onSpaceNavigation">
    <a v-if="isGotoEnabled()"
       href="javascript:void(0)" class="goto"
       v-bind:class="{'children':space.hasChildren}"
       v-bind:title="gotoTitle()"
       v-on:click="onSpaceNavigation">
      <span v-html="space.label"></span>
    </a>
    <span v-else
          class="label"
          v-bind:title="space.label"
          v-html="space.label"></span>
    <a v-if="isSpaceSelectable()"
       href="javascript:void(0)" class="select"
       v-bind:title="selectTitle()"
       v-on:click.native.prevent.stop="onSpaceSelect">
      <span v-html="space.label"></span>
    </a>
  </li>
</silverpeas-component-template>

<!-- ########################################################################################### -->
<silverpeas-component-template name="components">
  <div v-sp-init>
    {{addMessages({
    gotoComponentLabel : '${silfn:escapeJs(gotoComponentLabel)}',
    selectComponentLabel : '${silfn:escapeJs(selectComponentLabel)}'
  })}}
  </div>
  <div class="components">
    <h4>${componentsLabel}</h4>
    <ul>
      <li v-for="component in components" v-bind:key="component.id"
          v-bind:id="domId(component)"
          v-bind:class="{'selectable':isComponentSelectable(),'walkable':isComponentContentEnabled()}"
          v-bind:style="{'--browser-component-icon':'url(' + componentIconUrl(component) + ')'}"
          v-bind:title="gotoTitle(component)">
        <a v-if="isComponentContentEnabled()"
           href="javascript:void(0)" class="goto"
           v-bind:title="gotoTitle(component)"
           v-on:click="onComponentNavigation(component)">
          <span v-html="component.label"></span>
        </a>
        <span v-else
              class="label"
              v-bind:title="component.label"
              v-html="component.label"></span>
        <a v-if="isComponentSelectable()"
           href="javascript:void(0)"
           v-bind:class="{'select':isComponentSelectable(component)}"
           v-bind:title="selectTitle(component)"
           v-on:click.native.prevent.stop="onComponentSelect(component)">
          <span v-html="component.label"></span>
        </a>
      </li>
    </ul>
  </div>
</silverpeas-component-template>