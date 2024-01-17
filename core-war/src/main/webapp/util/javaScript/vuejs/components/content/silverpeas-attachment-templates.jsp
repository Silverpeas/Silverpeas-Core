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
<%@ page import="org.silverpeas.core.webapi.documenttemplate.DocumentTemplateWebManager" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.silverpeas.com/tld/silverFunctions" prefix="silfn" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>
<c:set var="language" value="${sessionScope['SilverSessionController'].favoriteLanguage}"/>
<fmt:setLocale value="${language}"/>
<view:setBundle basename="org.silverpeas.util.attachment.multilang.attachment"/>
<view:setBundle basename="org.silverpeas.documentTemplate.multilang.documentTemplate" var="docTplBundle"/>

<c:set var="isDocumentTemplateEnabled" value="<%=DocumentTemplateWebManager.get().existsDocumentTemplate()%>" />

<fmt:message key="attachment.dialog.add" var="addFilesTitle" />
<fmt:message key="GML.file" var="fileLabel"/>
<fmt:message key="documentTemplate.label" var="docTemplateLabel" bundle="${docTplBundle}"/>

<!-- ########################################################################################### -->
<silverpeas-component-template name="silverpeas-attachment-management">
  <div class="silverpeas-attachment-management">
    <silverpeas-add-files-popin
        v-on:api="addPopinApi = $event"
        v-bind:component-instance-id="context.componentInstanceId"
        v-bind:is-i18n-content="context.isI18nContent"
        v-bind:i18n-content-language="context.i18nContentLanguage">
      <silverpeas-attachment-form v-on:api="addFormApi = $event"
                                  v-bind:context="context"
                                  v-on:foreign-id-change="initAddToResourceFormData"></silverpeas-attachment-form>
    </silverpeas-add-files-popin>
  </div>
</silverpeas-component-template>

<!-- ########################################################################################### -->
<silverpeas-component-template name="silverpeas-attachment-form">
  <div class="silverpeas-attachment-form form-container">
    <silverpeas-hidden-input id="foreignId" name="foreignId" v-model="foreignId"></silverpeas-hidden-input>
    <silverpeas-hidden-input id="indexIt"  name="indexIt" v-model="indexIt"></silverpeas-hidden-input>
    <div>
      <silverpeas-label for="fileTitle" class="label-ui-dialog"><fmt:message key="Title"/></silverpeas-label>
      <div class="champ-ui-dialog">
        <silverpeas-text-input name="fileTitle" v-bind:size="60" id="fileTitle"
                               v-model="fileTitle"></silverpeas-text-input>
      </div>
    </div>
    <div>
      <silverpeas-label for="fileDescription" class="label-ui-dialog"><fmt:message key="GML.description" /></silverpeas-label>
      <div class="champ-ui-dialog">
        <silverpeas-multiline-text-input name="fileDescription" id="fileDescription"
                                         v-bind:rows="3" v-model="fileDescription"></silverpeas-multiline-text-input>
      </div>
    </div>
    <template v-if="context.isVersionActive">
      <div class="version-type">
        <silverpeas-label for="versionType-0" class="label-ui-dialog"><fmt:message key="attachment.version.label"/></silverpeas-label>
        <div class="champ-ui-dialog">
          <silverpeas-radio-input value="0" name="versionType" id="versionType-0"
                                  v-model="versionType"></silverpeas-radio-input>
          <silverpeas-label for="versionType-0" class="value"><fmt:message key="attachment.version_public.label"/></silverpeas-label>
          <silverpeas-radio-input value="1" name="versionType" id="versionType-1"
                                  v-model="versionType"></silverpeas-radio-input>
          <silverpeas-label for="versionType-1" class="value"><fmt:message key="attachment.version_wip.label"/></silverpeas-label>
        </div>
      </div>
      <div>
        <silverpeas-label for="commentMessage" class="label-ui-dialog"><fmt:message key="attachment.dialog.comment"/></silverpeas-label>
        <div class="champ-ui-dialog">
          <silverpeas-multiline-text-input name="commentMessage" id="commentMessage"
                                           v-bind:cols="60" v-bind:rows="3"
                                           v-model="commentMessage"></silverpeas-multiline-text-input>
        </div>
      </div>
    </template>
  </div>
</silverpeas-component-template>