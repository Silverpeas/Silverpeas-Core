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
<view:setBundle basename="org.silverpeas.contribution.multilang.contribution"/>

<c:url var="deleteIconUrl" value="/util/icons/delete.gif"/>

<fmt:message key="GML.emptyBasket" var="emptyBasketLabel"/>
<fmt:message key="GML.close" var="closeLabel"/>
<fmt:message key="GML.delete" var="deleteLabel"/>
<fmt:message key="GML.myBasket" var="buttonLabel"/>
<fmt:message key="contribution.basket.selector.title" var="popinTitle"/>
<fmt:message key="contribution.basket.selector.validation.noElementSelected" var="noElementSelectedMsg"/>

<!-- ########################################################################################### -->
<silverpeas-component-template name="basket-selection-main">
  <div class="silverpeas-basket-selection"
       v-show="displayed"
       v-on:click="api.toggleView()">
    <div v-sp-init>
      {{addMessages({
      buttonLabel : '${silfn:escapeJs(buttonLabel)}'
    })}}
    </div>
    <slot></slot>
    <silverpeas-attached-popin v-if="displayPopin"
                               v-bind:to-element="$el"
                               v-on:click.native.prevent.stop=""
                               v-bind:anchor="anchor"
                               fade-duration-type="long">
      <template v-slot:header>
        <silverpeas-link class="close" v-on:click.native.prevent.stop="api.toggleView()" title="${closeLabel}"></silverpeas-link>
      </template>
      <silverpeas-list v-if="basketElements" class="basket-element-list"
                       v-bind:items="basketElements"
                       v-bind:with-fade-transition="true">
        <silverpeas-list-item v-for="basketElement in basketElements" v-bind:key="basketElement.getId()">
          <basket-element v-bind:basket-element="basketElement"
                          v-on:click.native="goTo(basketElement)"
                          v-on:delete="deleteBasketElement"></basket-element>
        </silverpeas-list-item>
      </silverpeas-list>
    </silverpeas-attached-popin>
  </div>
</silverpeas-component-template>

<!-- ########################################################################################### -->
<silverpeas-component-template name="publication-basket-selector-main">
  <div>
    <div v-sp-init>
      {{addMessages({
      noElementSelectedMsg : '${silfn:escapeJs(noElementSelectedMsg)}'
    })}}
    </div>
    <silverpeas-popin v-on:api="popinApi = $event" title="${popinTitle}" minWidth="650">
      <div class="publication-basket-selector-container">
        <p v-if="!basketElements.length">${emptyBasketLabel}</p>
        <silverpeas-fade-transition-group duration-type="fast" class="basket-element-list">
          <li v-for="basketElement in basketElements" v-bind:key="basketElement.getId()">
            <basket-element v-bind:basket-element="basketElement"
                            v-on:select="selectBasketElement"
                            v-on:selectAndValidate="selectAndValidateBasketElement"
                            v-bind:read-only="true"
                            v-bind:class="{'selected' : (currentBasketElement && currentBasketElement.getId() === basketElement.getId())}"></basket-element>
          </li>
        </silverpeas-fade-transition-group>
      </div>
    </silverpeas-popin>
  </div>
</silverpeas-component-template>

<!-- ########################################################################################### -->
<silverpeas-component-template name="basket-element">
  <div class="basket-element"
       v-on:click="$emit('select', basketElement)"
       v-on:dblclick="$emit('selectAndValidate', basketElement)">
    <div class="image" v-if="basketElement.getImageSrc()">
      <img v-bind:src="basketElement.getImageSrc()" alt="">
    </div>
    <div class="editorial">
      <div class="title" v-html="title"></div>
      <div class="description" v-html="description"></div>
    </div>
    <silverpeas-fade-transition>
      <a href="javascript:void(0)"
         class="delete-button"
         v-if="displayDelete"
         v-on:click.prevent.stop="$emit('delete',basketElement)" title="${deleteLabel}">${deleteLabel}</a>
    </silverpeas-fade-transition>
  </div>
</silverpeas-component-template>