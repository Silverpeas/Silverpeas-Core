<%--
  Copyright (C) 2000 - 2022 Silverpeas

  This program is free software: you can redistribute it and/or modify
  it under the terms of the GNU Affero General Public License as
  published by the Free Software Foundation, either version 3 of the
  License, or (at your option) any later version.

  As a special exception to the terms and conditions of version 3.0 of
  the GPL, you may redistribute this Program in connection with Free/Libre
  Open Source Software ("FLOSS") applications as described in Silverpeas's
  FLOSS exception. You should have received a copy of the text describing
  the FLOSS exception, and it is also available here:
  "http://www.silverpeas.org/docs/core/legal/floss_exception.html"

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU Affero General Public License for more details.

  You should have received a copy of the GNU Affero General Public License
  along with this program.  If not, see <https://www.gnu.org/licenses/>.
  --%>
<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://www.silverpeas.com/tld/silverFunctions" prefix="silfn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>
<%@ taglib prefix="plugins" tagdir="/WEB-INF/tags/silverpeas/plugins" %>

<c:set var="_language" value="${requestScope.resources.language}"/>
<fmt:setLocale value="${_language}"/>
<view:setBundle basename="org.silverpeas.multilang.generalMultilang" var="generalBundle"/>

<%@ attribute name="thumbnail" required="true" type="org.silverpeas.core.contribution.model.Thumbnail" description="The thumbnail to manage" %>
<%@ attribute name="mandatory" required="true" type="java.lang.Boolean" description="If thumbnail is mandatory" %>
<%@ attribute name="width" required="true" type="java.lang.Integer" description="Width of thumbnail" %>
<%@ attribute name="height" required="true" type="java.lang.Integer" description="Height of thumbnail" %>

<view:includePlugin name="imagetool"/>
<script type="text/javascript">

  function checkThumbnail(error) {
    if (imageToolApp.manager.checkImageMustBeFilled()) {
      error.msg += " - '<fmt:message key="GML.thumbnail" bundle="${generalBundle}"/>' <fmt:message key="GML.MustBeFilled" bundle="${generalBundle}"/>\n";
      error.nb++;
    }
    return false;
  }

  whenSilverpeasReady(function(){
    window.imageToolApp = SpVue.createApp({
      data : function() {
        return {
          manager : undefined,
          markAsDeleted : false,
          imageModel : {}
        }
      },
      computed : {
        previewImageUrl : function() {
          if (!this.hasBeenDeleted) {
            return '${thumbnail.URL}';
          }
        },
        fullImageUrl : function() {
          if (!this.hasBeenDeleted) {
            return '${thumbnail.nonCroppedURL}';
          }
        },
        cropData : function() {
          const cropData = {
            previewWidth : ${width},
            previewHeight : ${height}
          };<c:if test="${thumbnail.cropped}">
          if (!this.hasBeenDeleted) {
            cropData.box = {
              offsetX : ${thumbnail.XStart},
              offsetY : ${thumbnail.YStart},
              width : ${thumbnail.XLength},
              height : ${thumbnail.YLength}
            }
          }</c:if>
          if (this.imageModel.cropData) {
            cropData.box = this.imageModel.cropData.box;
          }
          return cropData;
        },
        hasBeenDeleted : function() {
          this.markAsDeleted = this.markAsDeleted || this.imageModel.deleteOriginal;
          return this.markAsDeleted;
        }
      }
    }).mount('#thumb');
  });
</script>

<div class="fields">
  <div class="field" id="thumb">
    <silverpeas-image-file-input
        v-on:api="manager = $event"
        id="image-file-input"
        v-bind:crop-enabled="true"
        v-bind:image-banks-enabled="true"
        v-bind:mandatory="${mandatory}"
        v-bind:preview-image-url="previewImageUrl"
        v-bind:full-image-url="fullImageUrl"
        v-bind:crop-data="cropData"
        v-model="imageModel"></silverpeas-image-file-input>
    <template v-if="imageModel.cropData">
      <input type="hidden" name="ThumbnailWidth" v-model="imageModel.cropData.previewWidth"/>
      <input type="hidden" name="ThumbnailHeight" v-model="imageModel.cropData.previewHeight"/>
      <input type="hidden" name="XStart" v-model="imageModel.cropData.box.offsetX"/>
      <input type="hidden" name="YStart" v-model="imageModel.cropData.box.offsetY"/>
      <input type="hidden" name="XLength" v-model="imageModel.cropData.box.width"/>
      <input type="hidden" name="YLength" v-model="imageModel.cropData.box.height"/>
    </template>
    <input v-else-if="imageModel.deleteOriginal" type="hidden" name="ThumbnailDeletion" value="true"/>
  </div>
</div>