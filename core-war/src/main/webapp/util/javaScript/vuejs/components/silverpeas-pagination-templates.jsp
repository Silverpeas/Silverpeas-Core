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
<jsp:useBean id="language" type="java.lang.String"/>
<fmt:setLocale value="${language}"/>
<view:setBundle basename="org.silverpeas.util.viewGenerator.multilang.graphicElementFactoryBundle"/>

<!-- ########################################################################################### -->
<silverpeas-component-template name="pagination">
  <div class="silverpeas-pagination">
    <div v-if="!displayAtTop" ref="top"></div>
    <silverpeas-pagination-bar v-if="displayAtTop && displayPagination"
                               v-on:api="barApi = $event"
                               v-bind:page="page"
                               v-on:change="setCurrentPage"
                               v-on:page-size-change="setNbItemsPerPage"></silverpeas-pagination-bar>
    <slot v-bind:page="page"></slot>
    <silverpeas-pagination-bar v-if="!displayAtTop && displayPagination"
                               v-on:api="barApi = $event"
                               v-bind:page="page"
                               v-on:change="setCurrentPage"
                               v-on:page-size-change="setNbItemsPerPage"></silverpeas-pagination-bar>
  </div>
</silverpeas-component-template>

<fmt:message var="pageOnLabel" key="GEF.pagination.pageOn">
  <fmt:param>{{ page.numPage }}</fmt:param>
  <fmt:param>{{ page.nbPages }}</fmt:param>
</fmt:message>
<fmt:message var="gotoLabel" key="GEF.pagination.jumper"/>
<fmt:message var="gotoTitleLabel" key="GEF.pagination.jumptoPage"/>

<!-- ########################################################################################### -->
<silverpeas-component-template name="pagination-bar">
  <div class="pageNav silverpeas-pagination-bar">
    <div class="pageNavContent">
      <div class="itemIndex">
        <span>{{ page.firstIndex + 1 }}-{{ page.lastIndex + 1 }} / {{ page.nbAllItems }}</span>
      </div>
      <div v-if="displayPageOn" class="pageIndex">
        <span>${pageOnLabel}</span>
      </div>
      <silverpeas-smartpaginator v-bind:page="page"
                                 v-on:api="smartPaginationApi = $event"
                                 v-on:change="$emit('change', $event)"></silverpeas-smartpaginator>
      <div v-if="displayNumberPerPage" class="pageIndex numberPerPage">
        <a href="javascript:void(0)"
           v-for="pageItem in numberPerPageList"
           v-bind:class="{'selected': pageItem.nb === page.nbItemsPerPage}"
           v-bind:title="pageItem.title"
           v-on:click="$emit('page-size-change', pageItem.nb)">{{ pageItem.label }}</a>
      </div>
      <div v-if="displayGotoPage" class="pageJumper">
        <a href="javascript:void(0)" title="${gotoTitleLabel}"
           v-on:click="toggleGotoPage">${gotoLabel}&#160;</a>
        <input v-if="jumperEnabled" type="text" class="jumper" size="3" ref="pageJumper"
           v-on:focus="$refs.pageJumper.select()"
           v-on:blur="toggleGotoPage"
           v-on:keyup.enter="gotoPage">
      </div>
    </div>
  </div>
</silverpeas-component-template>

<!-- ########################################################################################### -->
<silverpeas-component-template name="smartpaginator">
  <div class="pageNav_silverpeas"></div>
</silverpeas-component-template>