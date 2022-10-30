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

<fmt:message key="GML.minimize" var="minimizeLabel"/>
<fmt:message key="GML.maximize" var="maximizeLabel"/>

<!-- ########################################################################################### -->
<silverpeas-component-template name="info-point-bloc-label-value">
  <div class="bloc" v-if="hideIfNotDefined && (isValueSlot || value)">
    <div class="row">
      <div v-if="isLabelSlot" class="cell" v-bind:class="labelClass">
        <slot name="label"></slot>
      </div>
      <div v-else class="cell" v-bind:class="labelClass" v-html="label"></div>
      <div v-if="isValueSlot" class="cell" v-bind:class="valueClass">
        <slot></slot>
      </div>
      <div v-else class="cell" v-bind:class="valueClass" v-html="value"></div>
    </div>
  </div>
</silverpeas-component-template>

<!-- ########################################################################################### -->
<silverpeas-component-template name="mapping-filters">
  <silverpeas-fade-transition duration-type="long">
    <div class="mapping-filters">
      <div v-sp-init>
        {{addMessages({
        minimizeLabel : '${silfn:escapeJs(minimizeLabel)}',
        maximizeLabel : '${silfn:escapeJs(maximizeLabel)}'
      })}}
      </div>
      <a class="min-max" href="javascript:void(0)" v-if="showMinMax"
         v-on:click="toggle"
         v-bind:title="toggleLabel">
        <img v-bind:src="toggleUrl" v-bind:alt="toggleLabel" v-bind:title="toggleLabel">
      </a>
      <silverpeas-map-form-mapping-category-filter
          v-for="category in categories" v-bind:key="category.id"
          v-bind:category="category"
          v-bind:showLabel="showLabels"></silverpeas-map-form-mapping-category-filter>
    </div>
  </silverpeas-fade-transition>
</silverpeas-component-template>

<!-- ########################################################################################### -->
<silverpeas-component-template name="mapping-category-filter">
  <div class="category"
       v-bind:class="cssClassList" v-bind:style="styles"
       v-bind:title="!showLabel ? category.label : ''" v-on:click="toggleVisibility">
    <span v-bind:name="category.id" v-html="showLabel ? category.label : ''"></span>
  </div>
</silverpeas-component-template>