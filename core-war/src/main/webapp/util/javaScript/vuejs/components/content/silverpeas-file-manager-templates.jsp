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
<fmt:message key="GML.language" var="languageLabel"/>
<fmt:message key="GML.file" var="fileLabel"/>
<fmt:message key="documentTemplate.label" var="docTemplateLabel" bundle="${docTplBundle}"/>

<!-- ########################################################################################### -->
<silverpeas-component-template name="silverpeas-add-files-popin">
  <div class="silverpeas-add-files">
    <div v-sp-init>
      {{addMessages({
      title : '${silfn:escapeJs(addFilesTitle)}'
    })}}
    </div>
    <silverpeas-popin v-on:api="setPopinApi"
                      v-bind:title="title"
                      type="validation">
      <silverpeas-form-pane v-on:api="setFormPaneApi"
                            v-bind:manual-actions="true"
                            v-bind:mandatory-legend="true">
        <silverpeas-add-files-form v-on:api="setFormApi"
                                   v-bind:component-instance-id="componentInstanceId"
                                   v-bind:multiple="false"
                                   v-bind:is-document-template-enabled="${isDocumentTemplateEnabled}"
                                   v-bind:is-i18n-content="isI18nContent"
                                   v-bind:i18n-content-language="i18nContentLanguage"></silverpeas-add-files-form>
        <slot></slot>
      </silverpeas-form-pane>
    </silverpeas-popin>
  </div>
</silverpeas-component-template>

<!-- ########################################################################################### -->
<silverpeas-component-template name="silverpeas-add-files-form">
  <div class="silverpeas-add-files-form form-container">
    <div class="creation-choice" v-if="isDocumentTemplateEnabled">
      <div>
        <silverpeas-radio-input name="creation_choice" id="file_upload_choice"
                                value="upload" v-model="choice"></silverpeas-radio-input>
        <silverpeas-label for="file_upload_choice"><fmt:message key="attachment.newDocument"/></silverpeas-label>
      </div>
      <div>
        <silverpeas-radio-input name="creation_choice" id="from_template_choice"
                                value="template" v-model="choice"/>
        <silverpeas-label for="from_template_choice"><fmt:message key="attachment.fromDocumentTemplate"/></silverpeas-label>
      </div>
    </div>
    <div v-if="isI18nContent">
      <silverpeas-label for="i18nContentLanguage" class="label-ui-dialog">${languageLabel}</silverpeas-label>
      <div class="champ-ui-dialog">
        <silverpeas-select-language id="contentLanguage" name="contentLanguage"
                                    v-model="contentLanguage"></silverpeas-select-language>
      </div>
    </div>
    <div v-if="isDocumentTemplateEnabled" v-show="!displayUploadPart">
      <silverpeas-label for="file_create" class="label-ui-dialog"
                        v-bind:mandatory="!displayUploadPart">${docTemplateLabel}</silverpeas-label>
      <div class="champ-ui-dialog">
        <silverpeas-document-template-input class="document-template-input"
                                            id="file_create"
                                            v-on:api="setDocumentTemplateApi"></silverpeas-document-template-input>
      </div>
    </div>
    <div v-if="isDocumentTemplateEnabled" v-show="!displayUploadPart">
      <fmt:message key="attachment.file.name.help" var="tmpLabel"/>
      <div class="document-template-filename">
        <silverpeas-label for="file_create_name" class="label-ui-dialog"
                          v-bind:mandatory="!displayUploadPart"><fmt:message key="attachment.file.name"/></silverpeas-label>
        <div class="champ-ui-dialog">
            <silverpeas-text-input name="fileName"
                                   v-bind:size="60" id="file_create_name"
                                   v-model="fileName"></silverpeas-text-input>
            <span class="help">${tmpLabel}</span>
        </div>
      </div>
    </div>
    <div v-show="displayUploadPart">
      <silverpeas-label for="file_upload_part" class="label-ui-dialog"
                        v-bind:mandatory="displayUploadPart">${fileLabel}</silverpeas-label>
      <div class="champ-ui-dialog">
        <silverpeas-file-upload v-on:api="setFileUploadApi"
                                id="file_upload_part"
                                v-bind:component-instance-id="componentInstanceId"
                                v-bind:multiple="multiple"
                                v-bind:info-inputs="false"
                                v-bind:display-into-fieldset="false"
                                v-bind:drag-and-drop-display-icon="false"></silverpeas-file-upload>
      </div>
    </div>
  </div>
</silverpeas-component-template>