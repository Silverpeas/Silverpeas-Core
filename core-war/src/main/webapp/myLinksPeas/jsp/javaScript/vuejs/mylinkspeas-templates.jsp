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

<c:set var="currentUserLanguage" value="${sessionScope['SilverSessionController'].favoriteLanguage}"/>
<fmt:setLocale value="${currentUserLanguage}"/>
<view:setBundle basename="org.silverpeas.mylinks.multilang.myLinksBundle"/>
<view:setBundle basename="org.silverpeas.mylinks.settings.myLinksIcons" var="icons"/>
<c:set var="lookHelper" value="${sessionScope['Silverpeas_LookHelper']}"/>
<view:setBundle bundle="${lookHelper.localizedBundle}" var="lookBundle"/>

<fmt:message var="labelBookmarksMore" key="look.home.bookmarks.more" bundle="${lookBundle}"/>

<!-- ########################################################################################### -->
<silverpeas-component-template name="widget">
  <div class="mylinkspeas-widget" v-bind:class="css">
    <mylinkspeas-accordion v-if="linksByCategory"
                           v-bind:linksByCategory="linksByCategory"></mylinkspeas-accordion>
    <ul class="accordion user-favorit-list" v-if="singleCategoryLinks">
      <mylinkspeas-accordion-link v-for="(link, index) in singleCategoryLinks" v-bind:key="link.linkId"
                                  v-bind:link="link" v-bind:index="index">
      </mylinkspeas-accordion-link>
    </ul>
    <a v-if="singleCategoryLinks && moreLinks" v-on:click="toggleBookmarks"
       title="${labelBookmarksMore}" href="javascript:void(0)" class="link-more"><span>${labelBookmarksMore}</span></a>
  </div>
</silverpeas-component-template>

<!-- ########################################################################################### -->
<silverpeas-component-template name="accordion">
  <ul class="accordion user-favorit-list">
    <mylinkspeas-accordion-category-links v-for="(category, index) in linksByCategory" v-bind:key="category.catId"
                                          v-bind:category="category" v-bind:index="index"
                                          v-bind:links="linksByCategory[category.catIdAsString]">
    </mylinkspeas-accordion-category-links>
  </ul>
</silverpeas-component-template>

<!-- ########################################################################################### -->
<silverpeas-component-template name="accordion-category-links">
  <li class="category-links" v-bind:class="css">
    <silverpeas-fade-transition duration-type="fast">
      <div class="title" v-on:click="doOpen">
        <a href="javascript:void(0)" v-bind:title="category.description">{{category.name}}</a>
      </div>
    </silverpeas-fade-transition>
    <silverpeas-fade-transition duration-type="fast">
      <div class="links" v-if="open">
        <ul>
          <mylinkspeas-accordion-link v-for="(link, index) in links" v-bind:key="link.linkId"
                                      v-bind:link="link" v-bind:index="index">
          </mylinkspeas-accordion-link>
        </ul>
      </div>
    </silverpeas-fade-transition>
  </li>
</silverpeas-component-template>

<!-- ########################################################################################### -->
<silverpeas-component-template name="accordion-link">
  <li class="link" v-bind:class="css">
    <a v-bind:class="linkCss" v-bind:href="url" v-bind:target="target" v-bind:title="link.description">{{link.name}}</a>
  </li>
</silverpeas-component-template>