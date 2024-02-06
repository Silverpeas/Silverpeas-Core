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
  ~ along with this program.  If not, see <http://www.gnu.org/licenses/>.
  --%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://www.silverpeas.com/tld/silverFunctions" prefix="silfn" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>
<c:set var="language" value="${sessionScope['SilverSessionController'].favoriteLanguage}"/>
<fmt:setLocale value="${language}"/>
<view:setBundle basename="org.silverpeas.a11y.multilang.a11yBundle"/>

<view:settings settings="org.silverpeas.a11y.settings.a11y"
               key="a11y.menu.slide.type"
               defaultValue="leftRight"
               var="slideType"/>

<fmt:message var="openMenuLabel" key="a11y.menu.open"/>
<fmt:message var="closeMenuLabel" key="a11y.menu.close"/>

<!-- ########################################################################################### -->
<silverpeas-component-template name="module">
  <silverpeas-a11y-menu v-bind:definitions="definitions"
                        v-bind:currents="currents"
                        v-on:select="$emit('select', $event)"
                        v-on:unselect="$emit('unselect', $event)"></silverpeas-a11y-menu>
</silverpeas-component-template>

<!-- ########################################################################################### -->
<silverpeas-component-template name="menu">
  <a href="javascript:void(0)" class="sp-a11y-open-menu" title="${openMenuLabel}"
     v-bind:style="{'display':(isClosed?'block':'none')}"
     v-on:click="open"><span>${openMenuLabel}</span></a>
  <silverpeas-slide-transition-group v-if="'${slideType}' !== 'none'" slide-type="${slideType}" v-on:leave="onLeave">
    <div v-if="isOpen">
      <silverpeas-a11y-menu-open v-bind:definitions="definitions"
                                 v-bind:currents="currents"
                                 v-on:close="close"
                                 v-on:select="$emit('select', $event)"
                                 v-on:unselect="$emit('unselect', $event)"></silverpeas-a11y-menu-open>
    </div>
  </silverpeas-slide-transition-group>
  <silverpeas-fade-transition-group v-else v-on:leave="onLeave">
    <div v-if="isOpen">
      <silverpeas-a11y-menu-open v-bind:definitions="definitions"
                                 v-bind:currents="currents"
                                 v-on:close="close"
                                 v-on:select="$emit('select', $event)"
                                 v-on:unselect="$emit('unselect', $event)"></silverpeas-a11y-menu-open>
    </div>
  </silverpeas-fade-transition-group>
</silverpeas-component-template>

<!-- ########################################################################################### -->
<silverpeas-component-template name="menu-open">
    <a href="javascript:void(0)" class="sp-a11y-close-menu" title="${closeMenuLabel}"
       v-on:click="$emit('close')"><span>${closeMenuLabel}</span></a>
    <ul class="sp-a11y-menu">
      <template v-for="definition in definitions"
                v-bind:key="definition.id">
        <silverpeas-a11y-definition-value v-for="value in definition.values"
                                          v-bind:key="value.id"
                                          v-bind:value="value"
                                          v-bind:definition="definition"
                                          v-bind:definitions="definitions"
                                          v-bind:currents="currents"
                                          v-on:select="$emit('select', $event)"
                                          v-on:unselect="$emit('unselect', $event)"></silverpeas-a11y-definition-value>
      </template>
    </ul>
</silverpeas-component-template>

<!-- ########################################################################################### -->
<silverpeas-component-template name="definition-value">
  <li v-bind:id="id" class="sp-a11y-menu-definition-value"
      v-bind:class="{'selected':selected}">
    <a href="javascript:void(0)"
       v-on:click="selectOrUnselect"
       v-html="value.label"></a>
  </li>
</silverpeas-component-template>