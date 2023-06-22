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
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://www.silverpeas.com/tld/silverFunctions" prefix="silfn" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>
<c:set var="language" value="${sessionScope['SilverSessionController'].favoriteLanguage}"/>
<fmt:setLocale value="${language}"/>
<view:setBundle basename="org.silverpeas.map.multilang.mapBundle"/>

<fmt:message key="map.target.point.title" var="targetPointTitle"/>
<fmt:message key="map.target.point.title.readOnly" var="targetPointReadOnlyTitle"/>
<fmt:message key="map.target.point.none" var="noTargetMsg"/>

<!-- ########################################################################################### -->
<silverpeas-component-template name="target-point-button">
  <div class="silverpeas-form-pane">
    <div v-sp-init>
      {{addMessages({
      targetPointTitle : '${silfn:escapeJs(targetPointTitle)}',
      targetPointReadOnlyTitle : '${silfn:escapeJs(targetPointReadOnlyTitle)}'
    })}}
    </div>
  <silverpeas-button class="openMap" v-bind:title="title" v-on:click="open">
    {{ title }}
  </silverpeas-button>
  <silverpeas-map-target-point-popin v-on:api="popinApi = $event"
                                     v-bind:initial-map-location="initialMapLocation"
                                     v-bind:map-options="mapOptions"
                                     v-bind:adapter-class="adapterClass"
                                     v-bind:read-only="readOnly"
                                     v-on:map-lon-lat-target="$emit('map-lon-lat-target', $event)"></silverpeas-map-target-point-popin>
</silverpeas-component-template>

<!-- ########################################################################################### -->
<silverpeas-component-template name="target-point-popin">
  <div v-sp-init>
    {{addMessages({
    noTargetMsg : '${silfn:escapeJs(noTargetMsg)}'
  })}}
  </div>
  <silverpeas-popin v-on:api="popinApi = $event"
                    v-bind:title="'${silfn:escapeJs(targetPointTitle)}'"
                    v-bind:max-width="1000"
                    v-on:open="isOpen = true"
                    v-on:close="isOpen = false"
                    v-bind:type="readOnly ? 'information' : 'validation'">
    <div class="silverpeas-map-target-point-popin">
      <silverpeas-map-target-point-map v-if="isOpen"
                                       v-bind:initial-map-location="initialMapLocation"
                                       v-bind:map-options="mapOptions"
                                       v-bind:adapter-class="adapterClass"
                                       v-bind:read-only="readOnly"
                                       v-on:map-lon-lat-target="currentTarget = $event"></silverpeas-map-target-point-map>
    </div>
  </silverpeas-popin>
</silverpeas-component-template>

<!-- ########################################################################################### -->
<silverpeas-component-template name="target-point-map">
  <div class="silverpeas-map-target-point-map"></div>
</silverpeas-component-template>

<!-- ########################################################################################### -->
<silverpeas-component-template name="target-point">
  <div class="target-point">
    <div class="lat-lon" v-html="latLon"></div>
    <silverpeas-fade-transition>
      <div class="address" v-if="address" v-html="address"></div>
    </silverpeas-fade-transition>
  </div>
</silverpeas-component-template>