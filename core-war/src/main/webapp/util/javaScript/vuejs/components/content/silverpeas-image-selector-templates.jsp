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
  ~ along with this program.  If not, see <https://www.gnu.org/licenses/>.
  --%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.silverpeas.com/tld/silverFunctions" prefix="silfn" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>
<c:set var="language" value="${sessionScope['SilverSessionController'].favoriteLanguage}"/>
<fmt:setLocale value="${language}"/>
<view:setBundle basename="org.silverpeas.util.attachment.multilang.attachment"/>

<c:url var="deleteIconUrl" value="/util/icons/delete.gif"/>

<fmt:message var="dragAndDropFilesLabel" key="GML.upload.dragAndDrop.files"/>
<fmt:message var="browseLabel" key="GML.upload.choose.browse"/>
<fmt:message key="GML.delete" var="deleteLabel"/>
<fmt:message key="GML.or" var="orLabel"/>
<fmt:message key="image.selector.title" var="popinTitle"/>
<fmt:message key="image.selector.noImage" var="noImageMessage"/>
<fmt:message key="image.selector.section.url.current" var="currentUrlSectionTitle"/>
<fmt:message key="image.selector.section.url.next" var="nextUrlSectionTitle"/>
<fmt:message key="image.selector.section.url.current.back" var="backToCurrentImage"/>
<fmt:message key="image.selector.section.select.image" var="selectImageTitle"/>
<fmt:message key="image.selector.section.mediaBanks" var="mediaBanksSectionTitle"/>

<c:set var="confirmDeleteMsg"><fmt:message key='image.selector.section.attachments.delete.confirm'/></c:set>

<!-- ########################################################################################### -->
<silverpeas-component-template name="main">
  <div>
    <div v-sp-init>
      {{addMessages({
      confirmDeleteMsg : '${silfn:escapeJs(confirmDeleteMsg)}'
    })}}
    </div>
    <silverpeas-popin v-on:api="popinApi = $event" title="${popinTitle}" v-bind:minWidth="650">
      <div class="image-selector-container">
        <div class="selected-image">
          <h2 class="section-title">{{isSelectedSrcSameAsInitial ? "${silfn:escapeJs(currentUrlSectionTitle)}" : "${silfn:escapeJs(nextUrlSectionTitle)}"}}</h2>
          <image-url v-on:api="imageUrlApi = $event"
                     v-on:change="selectedSrc = $event"
                     v-bind:currentUrl="selectedSrc"
                     v-bind:currentAttachment="selectedAttachment"></image-url>
          <a href="javascript:void(0)" class="back-to-previous-image"
             v-if="!isSelectedSrcSameAsInitial" v-on:click="backToCurrentImage">${backToCurrentImage}</a>
        </div>
        <div class="fileUpload-container image-attachments">
          <h3 class="section-title">${selectImageTitle}</h3>
          <silverpeas-fade-transition v-if="!imgAttachments.length" duration-type="fast">
            <span>${noImageMessage}</span>
          </silverpeas-fade-transition>
          <silverpeas-fade-transition v-if="imgAttachments.length" duration-type="fast">
            <ul class="list" v-if="imgAttachments.length">
              <silverpeas-fade-transition-group duration-type="fast">
                <image-attachment v-for="imgAttachment in imgAttachments" v-bind:key="imgAttachment.id"
                                  v-bind:attachment="imgAttachment"
                                  v-on:select-image="updateSelectedImageAttachment"
                                  v-on:delete-image-attachment="deleteImageAttachment"></image-attachment>
              </silverpeas-fade-transition-group>
            </ul>
          </silverpeas-fade-transition>
          <div class="actions dng add-zone">
            <div class="droparea" ref="ddContainer">
              <span>${dragAndDropFilesLabel}</span>
            </div>
            <div class="sp_button button upload-manually">
              <input class="dragAndDrop" type="file" v-on:change="uploadFilesManually" multiple ref="dragAndDropInput">
              <a v-on:click="$refs.dragAndDropInput.click()">${browseLabel}</a>
            </div>
          </div>
        </div>
        <div v-show="displayMediaBank" class="media-bank-container">
          <span class="section-title">${orLabel} </span>
          <span class="media-bank">${mediaBanksSectionTitle}</span>
          <media-bank v-on:loaded="displayMediaBank = $event.length > 0"></media-bank>
        </div>
      </div>
    </silverpeas-popin>
  </div>
</silverpeas-component-template>

<!-- ########################################################################################### -->
<silverpeas-component-template name="image-url">
  <div class="image-url">
    <div>
      <silverpeas-fade-transition duration-type="fast">
        <img class="thumbnail" v-if="previewUrl" v-bind:src="previewUrl" alt=""/>
      </silverpeas-fade-transition>
    </div>
    <input type="text" v-model="url"/>
  </div>
</silverpeas-component-template>

<!-- ########################################################################################### -->
<silverpeas-component-template name="media-bank">
  <div class="media-bank">
    <ul>
      <silverpeas-fade-transition-group>
        <li v-for="mediaApp in mediaApps" v-bind:key="mediaApp.id"
            v-bind:title="mediaApp.description"
            v-on:click.stop.prevent="openMediaFileManager(mediaApp)">
          <span>{{mediaApp.label}}</span>
        </li>
      </silverpeas-fade-transition-group>
    </ul>
  </div>
</silverpeas-component-template>

<!-- ########################################################################################### -->
<silverpeas-component-template name="image-attachment">
  <li class="image-attachment"
      v-bind:title="attachment.description"
      v-on:click="$emit('select-image', attachment)">
    <silverpeas-fade-transition duration-type="fast">
      <img class="thumbnail" v-if="previewUrl" v-bind:src="previewUrl" v-bind:title="title" alt=""/>
    </silverpeas-fade-transition>
    <a href="javascript:void(0)" v-on:click.stop.prevent="$emit('delete-image-attachment', attachment)" title="${deleteLabel}"><img src="${deleteIconUrl}" alt="${deleteLabel}"></a>
  </li>
</silverpeas-component-template>