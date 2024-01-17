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
<view:setBundle basename="org.silverpeas.multilang.generalMultilang"/>

<fmt:message key="GML.imageTool.choose" var="chooseLabel"/>
<fmt:message key="GML.imageTool.crop" var="cropLabel"/>
<fmt:message key="GML.imageTool.cropImage" var="cropImageLabel"/>
<fmt:message key="GML.imageTool.cropImage.select" var="cropImageSelectLabel"/>
<fmt:message key="GML.imageTool.cropImage.result" var="cropImageResultLabel"/>
<fmt:message key="GML.back" var="goBackLabel"/>
<fmt:message key="GML.delete" var="deleteLabel"/>
<fmt:message key="GML.imageTool.badformat" var="badFormatErrMsg">
  <fmt:param value="{0}"/>
  <fmt:param value="{1}"/>
</fmt:message>
<fmt:message key="GML.imageTool.expectedformat" var="expectedFormatMsg">
  <fmt:param value="{0}"/>
</fmt:message>
<fmt:message key="GML.or" var="orMsgPart"/>
<fmt:message key="GML.imageTool.imagebanks" var="imageBanksLabel"/>
<fmt:message key="GML.imageTool.imagebank" var="imageBankLabel"/>
<fmt:message key="GML.imageTool.imagebank.help" var="imageBankHelpLabel"/>

<!-- ########################################################################################### -->
<silverpeas-component-template name="image-file-input">
  <div class="image-file-input" v-if="ready">
    <div v-sp-init>
      {{addMessages({
      expectedFormatMsg : '${silfn:escapeJs(expectedFormatMsg)}',
      badFormatErrMsg : '${silfn:escapeJs(badFormatErrMsg)}',
      orMsgPart : '${silfn:escapeJs(orMsgPart)}',
      imageBanksLabel : '${silfn:escapeJs(imageBanksLabel)}',
      imageBankLabel : '${silfn:escapeJs(imageBankLabel)}',
      imageBankHelpLabel : '${silfn:escapeJs(imageBankHelpLabel)}'
    })}}
    </div>
    <div v-if="previewUrl" v-show="!cropCtx.previewData" class="image-preview" ref="preview">
      <img v-bind:src="previewUrl" alt=""/>
    </div>
    <div v-if="cropCtx.previewData" class="cropped-image-preview" ref="croppedPreview"
         v-bind:style="croppedPreviewWidthAndHeightStyle">
      <img v-bind:src="cropImageUrl" alt="" v-bind:style="cropCtx.previewData.previewCss"/>
    </div>
    <div class="image-actions">
      <a v-if="displayCropAction" href="javascript:void(0)" v-on:click="cropCtx.formEnabled = true">
        <img src="<c:url value="/util/icons/arrow_in.png"/>" alt=""/>
        ${cropLabel}
      </a>
      <a v-if="displayGoBackAction" href="javascript:void(0)" v-on:click="goBack">
        <img src="<c:url value="/util/icons/back.png"/>" alt="${goBackLabel}" title="${goBackLabel}"/>
        ${goBackLabel}
      </a>
      <a v-if="displayDelAction" href="javascript:void(0)" v-on:click="deleteImage">
        <img src="<c:url value="/util/icons/cross.png"/>" alt="${deleteLabel}" title="${deleteLabel}"/>
        ${deleteLabel}
      </a>
    </div>
    <div class="image-inputs">
      <div v-if="displayFileData">
        <span v-if="fileName" class="file-name">{{ fileName }}&#160;</span>
        <span v-if="humanReadableFileSize" class="file-size">({{ humanReadableFileSize }})&#160;</span>
      </div>
      <img src="<c:url value="/util/icons/images.png"/>" alt="${chooseLabel}" title="${chooseLabel}"/>
      <input ref="newImageFile" v-bind:accept="acceptedTypes" v-on:change="newImageFile"
             size="40" v-bind:name="name" type='file'
             v-bind:title="expectedFormatHelp" />
      <div v-if="imageBanks.length === 0" class="help" v-html="expectedFormatHelp"></div>
      <template v-else>
        <span class="txtsublibform"> ${orMsgPart} </span><input type="hidden" v-model="imageBankUrl" name="valueImageGallery"/>
        <select v-if="imageBanks.length > 1" ref="imageBankSelector" v-on:change="openImageBank">
          <option selected>${imageBanksLabel}</option>
          <option v-for="imageBank in imageBanks"
                  v-bind:value="imageBank.id" v-bind:key="imageBank.id">
            {{ imageBank.label }}
          </option>
        </select>
        <a v-else
           href="javascript:void(0)"  class="sp_button button-imageBank" title="${imageBankHelpLabel}"
           v-on:click="openImageBank">${imageBankLabel}</a>
      </template>
      <silverpeas-mandatory-indicator v-if="displayMandatory"></silverpeas-mandatory-indicator>
    </div>
    <%--   CROP POPIN   --%>
    <silverpeas-popin v-on:api="cropCtx.popin = $event"
                      v-bind:title="'${silfn:escapeJs(cropImageLabel)}'"
                      v-bind:dialog-class="'crop-image-popin'"
                      type="validation"
                      v-bind:minWidth="850">
      <silverpeas-form-pane v-if="displayCropAction"
                            v-on:api="cropCtx.pane = $event"
                            v-bind:mandatoryLegend="false"
                            v-bind:manualActions="true"
                            v-on:data-update="validateCrop">
        <silverpeas-crop-image-form
            v-if="cropCtx.formEnabled"
            v-on:initialized="cropImage"
            v-bind:full-image-url="cropImageUrl"
            v-bind:crop-data="currentCropData"></silverpeas-crop-image-form>
      </silverpeas-form-pane>
    </silverpeas-popin>
  </div>
</silverpeas-component-template>

<!-- ########################################################################################### -->
<silverpeas-component-template name="crop-image-form">
  <p class="txtlibform">${cropImageSelectLabel}</p>
  <div class="crop-image-form container">
    <img ref="cropbox" v-bind:src="fullImageUrl" alt="" />
    <div class="preview-container">
      <p class="txtlibform">${cropImageResultLabel}</p>
      <div class="preview" v-bind:style="{width:cropData.previewWidth + 'px', height:cropData.previewHeight+ 'px'}">
        <img ref="preview" v-bind:src="fullImageUrl" alt="" />
      </div>
    </div>
  </div>
</silverpeas-component-template>