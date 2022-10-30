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
<view:setBundle basename="org.silverpeas.multilang.generalMultilang"/>

<!-- ########################################################################################### -->
<silverpeas-component-template name="marker">
  <div class="map-marker" v-bind:class="cssClassList" v-bind:style="styles" v-on:click="toggleWindow">
    <a href="javascript:void(0)" v-html="marker.getLabel()"></a>
    <div class="pointer"></div>
  </div>
</silverpeas-component-template>

<!-- ########################################################################################### -->
<silverpeas-component-template name="tip-marker">
  <div class="map-tip-marker">
    <a class="map-tip-marker-button" href="javascript:void(0)" v-on:click="toggleWindow"></a>
    <div class="map-tip-marker-content" v-on:click="setCurrentClass"><slot></slot></div>
  </div>
</silverpeas-component-template>

<!-- ########################################################################################### -->
<silverpeas-component-template name="mapping-layers">
  <silverpeas-fade-transition duration-type="long" v-if="currentLayerName">
    <div class="mapping-layers">
      <silverpeas-map-mapping-layer
          v-for="layer in layers" v-bind:key="layer.id"
          v-bind:layer="layer"
          v-on:layer-change="select"></silverpeas-map-mapping-layer>
    </div>
  </silverpeas-fade-transition>
</silverpeas-component-template>

<!-- ########################################################################################### -->
<silverpeas-component-template name="mapping-layer">
  <div class="layer" v-bind:class="cssClassList">
    <span v-bind:name="layer.id" v-html="layer.label" v-on:click="$emit('layer-change', layer.id)"></span>
  </div>
</silverpeas-component-template>