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
  ~ along with this program.  If not, see <https://www.gnu.org/licenses/>.
  --%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.silverpeas.com/tld/silverFunctions" prefix="silfn" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>
<c:set var="language" value="${sessionScope['SilverSessionController'].favoriteLanguage}"/>
<fmt:setLocale value="${language}"/>
<view:setBundle basename="org.silverpeas.documentTemplate.multilang.documentTemplate"/>

<fmt:message key="documentTemplate.input.select.link" var="selectLinkLabel"/>
<fmt:message key="GML.preview.file" var="previewLabel"/>
<fmt:message key="GML.view.file" var="viewLabel"/>
<fmt:message key="documentTemplate.input.select" var="selectLabel"/>
<fmt:message key="documentTemplate.input.unselect" var="unselectLabel"/>
<fmt:message key="documentTemplate.input.change" var="changeLabel"/>

<!-- ########################################################################################### -->
<silverpeas-component-template name="input">
  <div class="document-template-input">
    <a v-if="!selectedId"
       href="javascript:void(0)"
       v-on:click="openSelection"
       class="sp_button">${selectLinkLabel}</a>
    <document-template v-if="selectedDocumentTemplate"
                       v-bind:document-template="selectedDocumentTemplate"
                       v-bind:unselect="true"
                       v-on:select="openSelection"
                       v-on:unselect="clear"
                       selectLabel="${changeLabel}"></document-template>
    <input type="hidden" v-bind:name="inputName" v-model="selectedId">
    <silverpeas-popin v-on:api="selectPopinApi = $event"
                      title="${selectLinkLabel}"
                      type="basic">
      <silverpeas-list v-if="documentTemplates" class="document-template-list"
                       v-bind:items="documentTemplates"
                       v-bind:with-fade-transition="true">
        <silverpeas-list-item v-for="documentTemplate in documentTemplates" v-bind:key="documentTemplate.getId()">
          <document-template v-bind:document-template="documentTemplate"
                             v-on:select="selectDocumentTemplate"
                             selectLabel="${selectLabel}"></document-template>
        </silverpeas-list-item>
      </silverpeas-list>
    </silverpeas-popin>
  </div>
</silverpeas-component-template>

<!-- ########################################################################################### -->
<silverpeas-component-template name="document-template">
  <div class="document-template"
       v-on:click="$emit('select', documentTemplate)">
    <div class="editorial">
      <div class="title" v-html="title"></div>
      <div v-if="unselect && description" class="description" v-html="description"></div>
    </div>
    <div class="preview"
         v-if="documentTemplate.getPreviewUrl()"
         v-bind:title="selectLabel">
      <img v-bind:src="documentTemplate.getPreviewUrl()" alt="">
      <div v-if="!unselect && description" class="description" v-html="description"></div>
    </div>
    <div class="actions">
      <a class="preview-button preview-file"
         href="javascript:void(0)"
         v-on:click.stop.prevent="viewDocumentTemplate('preview')" title="${previewLabel}"></a>
      <a class="view-button view-file"
         href="javascript:void(0)"
         v-on:click.stop.prevent="viewDocumentTemplate('view')" title="${viewLabel}"></a>
      <a v-if="unselect"
         class="delete-button view-file"
         href="javascript:void(0)"
         v-on:click.stop.prevent="$emit('unselect', documentTemplate)" title="${unselectLabel}"></a>
    </div>
  </div>
</silverpeas-component-template>