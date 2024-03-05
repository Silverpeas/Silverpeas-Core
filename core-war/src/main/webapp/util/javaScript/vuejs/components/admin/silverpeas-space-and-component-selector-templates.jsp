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
<view:setBundle basename="org.silverpeas.admin.multilang.admin"/>

<fmt:message var="selectSpaceLabel" key="admin.selector.space"/>
<fmt:message var="selectSpacesLabel" key="admin.selector.spaces"/>
<fmt:message var="selectComponentLabel" key="admin.selector.component"/>
<fmt:message var="selectComponentsLabel" key="admin.selector.components"/>
<fmt:message var="selectSpaceAndComponentLabel" key="admin.selector.space.andComponent"/>
<fmt:message var="selectSpaceAndComponentsLabel" key="admin.selector.space.andComponents"/>
<fmt:message var="selectSpacesAndComponentLabel" key="admin.selector.spaces.andComponent"/>
<fmt:message var="selectSpacesAndComponentsLabel" key="admin.selector.spaces.andComponents"/>
<fmt:message var="componentsLabel" key="GML.components"/>
<fmt:message var="gotoSpaceLabel" key="GML.space.goto"/>
<fmt:message var="gotoComponentLabel" key="GML.component.goto"/>
<fmt:message var="removeLabel" key="GML.action.remove"/>
<fmt:message var="selectionLabel" key="GML.selection"/>
<fmt:message var="spacesLabel" key="GML.spaces"/>

<!-- ########################################################################################### -->
<silverpeas-component-template name="space-and-component-selector-popin">
  <div v-sp-init>
    {{addMessages({
    selectSpaceLabel : '${silfn:escapeJs(selectSpaceLabel)}',
    selectSpacesLabel : '${silfn:escapeJs(selectSpacesLabel)}',
    selectComponentLabel : '${silfn:escapeJs(selectComponentLabel)}',
    selectComponentsLabel : '${silfn:escapeJs(selectComponentsLabel)}',
    selectSpaceAndComponentLabel : '${silfn:escapeJs(selectSpaceAndComponentLabel)}',
    selectSpaceAndComponentsLabel : '${silfn:escapeJs(selectSpaceAndComponentsLabel)}',
    selectSpacesAndComponentLabel : '${silfn:escapeJs(selectSpacesAndComponentLabel)}',
    selectSpacesAndComponentsLabel : '${silfn:escapeJs(selectSpacesAndComponentsLabel)}'
  })}}
  </div>
  <a v-if="selectLink"
     href="javascript:void(0)" class="select-button"
     v-bind:title="selectLinkLabel"
     v-on:click="openSelector"
     v-html="selectLinkLabel"></a>
  <silverpeas-popin v-on:api="popinApi = $event"
                    v-bind:type="type"
                    v-bind:title="selectPopinTitle"
                    v-bind:dialog-class="dialogClass"
                    v-bind:min-width="minWidth"
                    v-bind:max-width="maxWidth"
                    v-bind:open-promise="open-promise"
                    v-on:open="onOpen"
                    v-on:close="onClose">
    <silverpeas-space-and-component-selector v-if="selectedSpaces && selectedComponents"
                                             v-bind:space-selectable="spaceSelectable"
                                             v-bind:space-selection="selectedSpaces"
                                             v-bind:component-selectable="componentSelectable"
                                             v-bind:component-selection="selectedComponents"
                                             v-bind:space-content-enabled="spaceContentEnabled"
                                             v-bind:component-content-enabled="componentContentEnabled"
                                             v-bind:space-filter="spaceFilter"
                                             v-bind:component-filter="componentFilter"
                                             v-bind:admin-access="adminAccess"
                                             v-on:enter-root="$emit('enter-root')"
                                             v-on:enter-space="$emit('enter-space', $event)"
                                             v-on:select-space="$emit('select-space', $event)"
                                             v-on:unselect-space="$emit('unselect-space', $event)"
                                             v-on:space-selection="onSpaceSelection"
                                             v-on:enter-component="$emit('enter-component', $event)"
                                             v-on:select-component="$emit('select-component', $event)"
                                             v-on:unselect-component="$emit('unselect-component', $event)"
                                             v-on:component-selection="onComponentSelection">
      <template v-if="!!$slots['extend-browser-breadcrumb']" v-slot:extend-browser-breadcrumb>
        <slot name="extend-browser-breadcrumb"></slot>
      </template>
      <template v-if="!!$slots['extend-browser']" v-slot:extend-browser>
        <slot name="extend-browser"></slot>
      </template>
      <template v-if="!!$slots['extend-browser-selection']" v-slot:extend-browser-selection>
        <slot name="extend-browser-selection"></slot>
      </template>
    </silverpeas-space-and-component-selector>
  </silverpeas-popin>
</silverpeas-component-template>

<!-- ########################################################################################### -->
<silverpeas-component-template name="space-and-component-selector">
  <div class="space-and-component-selector">
    <silverpeas-space-and-component-browser v-on:api="browserApi = $event"
                                            v-bind:space-selectable="isAtLeastOneSpaceSelectable()"
                                            v-bind:component-selectable="isAtLeastOneComponentSelectable()"
                                            v-bind:space-content-enabled="spaceContentEnabled"
                                            v-bind:component-content-enabled="componentContentEnabled"
                                            v-bind:space-filter="spaceFilter"
                                            v-bind:component-filter="componentFilter"
                                            v-bind:admin-access="adminAccess"
                                            v-on:enter-root="enterRoot"
                                            v-on:enter-space="enterSpace($event, true)"
                                            v-on:select-space="selectSpace"
                                            v-on:enter-component="enterComponent($event, true)"
                                            v-on:select-component="selectComponent">
      <template v-if="!!$slots['extend-browser-breadcrumb']" v-slot:extend-browser-breadcrumb>
        <slot name="extend-browser-breadcrumb"></slot>
      </template>
      <template v-if="!!$slots['extend-browser']" v-slot:extend-browser>
        <slot name="extend-browser"></slot>
      </template>
      <template v-slot:extend-browser-selection>
        <div class="selected">
          <h4>${selectionLabel}</h4>
          <private-space-and-component-selector-selected-spaces
              v-if="isAtLeastOneSpaceSelectable()"
              v-bind:display-title="isAtLeastOneComponentSelectable() || isBrowserSelectionExtensionSlot()"
              v-bind:space-selection="selectedSpaces"
              v-on:enter-space="enterSpace"
              v-on:unselect-space="unselectSpace"></private-space-and-component-selector-selected-spaces>
          <private-space-and-component-selector-selected-components
              v-if="isAtLeastOneComponentSelectable()"
              v-bind:display-title="isAtLeastOneSpaceSelectable() || isBrowserSelectionExtensionSlot()"
              v-bind:component-selection="selectedComponents"
              v-on:enter-component="enterComponent"
              v-on:unselect-component="unselectComponent"></private-space-and-component-selector-selected-components>
          <slot name="extend-browser-selection"></slot>
        </div>
      </template>
    </silverpeas-space-and-component-browser>
  </div>
</silverpeas-component-template>

<!-- ########################################################################################### -->
<silverpeas-component-template name="selected-spaces">
  <div class="spaces">
    <h4 v-if="displayTitle">${spacesLabel}</h4>
    <ul>
      <private-space-and-component-selector-selected-spaces-space
          v-for="space in spaceSelection" v-bind:key="space.id"
          v-bind:space="space"
          v-on:enter-space="$emit('enter-space', $event)"
          v-on:unselect-space="$emit('unselect-space', $event)"></private-space-and-component-selector-selected-spaces-space>
    </ul>
  </div>
</silverpeas-component-template>

<!-- ########################################################################################### -->
<silverpeas-component-template name="selected-spaces-space">
  <div v-sp-init>
    {{addMessages({
    gotoSpaceLabel : '${silfn:escapeJs(gotoSpaceLabel)}'
  })}}
  </div>
    <li v-on:click="$emit('enter-space', space)"
        v-bind:title="gotoTitle">
      <a href="javascript:void(0)">
        <span v-html="space.label"></span>
      </a>
      <a href="javascript:void(0)" class="unselect"
         v-bind:title="'${silfn:escapeJs(removeLabel)} ' + space.label"
         v-on:click.native.prevent.stop="$emit('unselect-space', space)">
        <span v-html="'${silfn:escapeJs(removeLabel)} ' + space.label"></span>
      </a>
    </li>
</silverpeas-component-template>

<!-- ########################################################################################### -->
<silverpeas-component-template name="selected-components">
  <div class="components">
    <h4 v-if="displayTitle">${componentsLabel}</h4>
    <ul>
      <private-component-and-component-selector-selected-components-component
          v-for="component in componentSelection" v-bind:key="component.id"
          v-bind:component="component"
          v-on:enter-component="$emit('enter-component', $event)"
          v-on:unselect-component="$emit('unselect-component', $event)"
          v-bind:style="{'--selected-component-icon':'url(' + componentIconUrl(component) + ')'}"></private-component-and-component-selector-selected-components-component>
    </ul>
  </div>
</silverpeas-component-template>

<!-- ########################################################################################### -->
<silverpeas-component-template name="selected-components-component">
  <div v-sp-init>
    {{addMessages({
    gotoComponentLabel : '${silfn:escapeJs(gotoComponentLabel)}'
  })}}
  </div>
  <li v-on:click="$emit('enter-component', component)"
      v-bind:title="gotoTitle">
    <a href="javascript:void(0)">
      <span v-html="component.label"></span>
    </a>
    <a href="javascript:void(0)" class="unselect"
       v-bind:title="'${silfn:escapeJs(removeLabel)} ' + component.label"
       v-on:click.native.prevent.stop="$emit('unselect-component', component)"></a>
  </li>
</silverpeas-component-template>