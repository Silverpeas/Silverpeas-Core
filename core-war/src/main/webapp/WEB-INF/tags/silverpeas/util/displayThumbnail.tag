<%--
  Copyright (C) 2000 - 2013 Silverpeas

  This program is free software: you can redistribute it and/or modify
  it under the terms of the GNU Affero General Public License as
  published by the Free Software Foundation, either version 3 of the
  License, or (at your option) any later version.

  As a special exception to the terms and conditions of version 3.0 of
  the GPL, you may redistribute this Program in connection with Free/Libre
  Open Source Software ("FLOSS") applications as described in Silverpeas's
  FLOSS exception. You should have recieved a copy of the text describing
  the FLOSS exception, and it is also available here:
  "http://www.silverpeas.org/docs/core/legal/floss_exception.html"

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU Affero General Public License for more details.

  You should have received a copy of the GNU Affero General Public License
  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  --%>
<%@tag import="java.util.ArrayList"%>
<%@tag import="org.silverpeas.core.util.StringUtil"%>
<%@tag import="org.silverpeas.core.admin.service.OrganizationControllerProvider"%>
<%@tag import="org.silverpeas.core.admin.service.OrganizationController"%>
<%@tag import="org.silverpeas.core.admin.component.model.ComponentInstLight"%>
<%@tag import="java.util.List"%>
<%@ tag import="org.silverpeas.core.admin.user.model.UserDetail" %>
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

<%
List<ComponentInstLight> galleries = new ArrayList<>();
OrganizationController orgaController = OrganizationControllerProvider.getOrganisationController();
String[] compoIds = orgaController.getCompoId("gallery");
for (String compoId : compoIds) {
  if (StringUtil.getBooleanValue(orgaController.getComponentParameterValue("gallery" + compoId, "viewInWysiwyg"))) {
    ComponentInstLight gallery = orgaController.getComponentInstLight("gallery" + compoId);
    galleries.add(gallery);
  }
}
request.setAttribute("galleries", galleries);
%>

<%@ attribute name="thumbnail" required="true" type="org.silverpeas.core.io.media.image.thumbnail.model.ThumbnailDetail" description="The thumbnail to manage" %>
<%@ attribute name="mandatory" required="true" type="java.lang.Boolean" description="If thumbnail is mandatory" %>
<%@ attribute name="error" required="false" type="java.lang.String" description="Error" %>
<%@ attribute name="componentId" required="true" type="java.lang.String" description="Instance ID of object" %>
<%@ attribute name="objectId" required="true" type="java.lang.String" description="ID of object" %>
<%@ attribute name="objectType" required="true" type="java.lang.Integer" description="Type of object" %>
<%@ attribute name="backURL" required="true" type="java.lang.String" description="URL to go back after processing the request" %>
<%@ attribute name="width" required="false" type="java.lang.Integer" description="Width of thumbnail" %>
<%@ attribute name="height" required="false" type="java.lang.Integer" description="Height of thumbnail" %>
<style>
<!--
<c:if test="${thumbnail == null}">
#thumbnailPreviewAndActions {
display: none;
}
</c:if>
-->
</style>
<script type="text/javascript">
function updateThumbnail() {
	$("#thumbnailInputs").css("display", "block");
  }

  function cropThumbnail() {
	$("#thumbnailDialog").dialog("option", "title", "<fmt:message key="GML.thumbnail.update" bundle="${generalBundle}"/>");
	$("#thumbnailDialog").dialog("option", "width", 850);
	<c:url value="/Thumbnail/jsp/thumbnailManager.jsp" var="myURL"><c:param name="Action" value="Update"/>
	  <c:param name="modal" value="true"/>
	  <c:param name="ComponentId" value="${componentId}"/>
	  <c:param name="ObjectId" value="${objectId}"/>
	  <c:param name="ObjectType" value="${objectType}"/>
	  <c:param name="BackUrl" value="${backURL}"/>
	  <c:param name="ThumbnailWidth" value="${width}"/>
	  <c:param name="ThumbnailHeight" value="${height}"/>
	</c:url>
	$("#thumbnailDialog").load("${myURL}").dialog("open");
  }

  function deleteThumbnail() {
    $.ajax(webContext+"/services/thumbnail/${componentId}/${objectType}/${objectId}", {
		 type: "DELETE",
		 async : false,
		 cache : false,
		 success : function(data){
			$("#thumbnailPreviewAndActions").hide();
		 }
    });
  }

  function closeThumbnailDialog() {
	$("#thumbnailDialog").dialog("close");
  }

  function getExtension(filename) {
	var indexPoint = filename.lastIndexOf(".");
	// on verifie qu il existe une extension au nom du fichier
	if (indexPoint != -1) {
	  // le fichier contient une extension. On recupere l extension
	  var ext = filename.substring(indexPoint + 1);
	  return ext.toLowerCase();
	}
	return null;
  }

  function checkThumbnail(error) {
    <c:if test="${mandatory}">
      if ($('#thumbnailFile').val() == '' && $('#thumbnail').is('img') == false) {
        error.msg += " - '<fmt:message key="GML.thumbnail" bundle="${generalBundle}"/>' <fmt:message key="GML.MustBeFilled" bundle="${generalBundle}"/>\n";
        error.nb++;
      }
    </c:if>

    if ($('#thumbnailFile').length && $('#thumbnailFile').val() != '') {
      var logicalName = $('#thumbnailFile').val();
      var extension = getExtension(logicalName);
      if (extension == null || (extension != "gif" && extension != "jpeg" && extension != "jpg" && extension != "png")) {
        error.msg += " - '<fmt:message key="GML.thumbnail" bundle="${generalBundle}"/>' <fmt:message key="GML.thumbnail.badformat" bundle="${generalBundle}"/>\n";
        error.nb++;
      }
    }
    return false;
  }

  var galleryWindow = window;

  function openGallery(liste) {
    index = liste.selectedIndex;
    var componentId = liste.options[index].value;
	if (index != 0) {
      url = webContext+"/gallery/jsp/wysiwygBrowser.jsp?ComponentId="+componentId+"&Language=${_language}&FieldName=Thumbnail";
      windowName = "galleryWindow";
      larg = "820";
      haut = "600";
      windowParams = "directories=0,menubar=0,toolbar=0, alwaysRaised";
      if (!galleryWindow.closed && galleryWindow.name=="galleryWindow") {
        galleryWindow.close();
      }
      galleryWindow = SP_openWindow(url, windowName, larg, haut, windowParams);
    }
  }

  function choixImageInGalleryThumbnail(url) {
    $("#thumbnailPreviewAndActions").css("display", "block");
    $("#thumbnailActions").css("display", "none");
    var $thumbnail = $("#thumbnail");
    if ($thumbnail.length) {
      $thumbnail.attr("src", url);
    } else {
      // No thumbnail defined yet, insert element
      $("#thumbnailPreview").append($("<img>", {"id" : "thumbnail", "src" : url, "alt" : ''}));
    }
    $("#valueImageGallery").attr("value", url);
  }

  $(document).ready(function(){
    var dialogOpts = {
            modal: true,
            autoOpen: false,
            height: "auto"
    };
    $("#thumbnailDialog").dialog(dialogOpts);    //end dialog
  });
</script>

<div class="fields">
	<div class="field" id="thumb">
		<div id="thumbnailPreviewAndActions">
			<div id="thumbnailPreview">
				<c:if test="${thumbnail != null}">
					<view:image src="${thumbnail.URL}" type="vignette.thumbnail" id="thumbnail" alt=""/>
				</c:if>
			</div>
			<div id="thumbnailActions">
				<c:if test="${thumbnail != null && thumbnail.cropable}">
					<a href="javascript:cropThumbnail()"><img src="<c:url value="/util/icons/arrow_in.png"/>" alt=""/> <fmt:message key="GML.thumbnail.crop" bundle="${generalBundle}"/></a>
				</c:if>
				<c:if test="${thumbnail != null && !mandatory}">
					<a href="javascript:deleteThumbnail()"><img src="<c:url value="/util/icons/cross.png"/>" alt="<fmt:message key="GML.thumbnail.delete" bundle="${generalBundle}"/>" title="<fmt:message key="GML.thumbnail.delete" bundle="${generalBundle}"/>"/> <fmt:message key="GML.thumbnail.delete" bundle="${generalBundle}"/></a>
				</c:if>
			</div>
		</div>

		<div id="thumbnailInputs">
			<img src="<c:url value="/util/icons/images.png"/>" alt="<fmt:message key="GML.thumbnail.update" bundle="${generalBundle}"/>" title="<fmt:message key="GML.thumbnail.update" bundle="${generalBundle}"/>"/> <input type="file" name="WAIMGVAR0" size="40" id="thumbnailFile"/>
			<c:if test="${not empty galleries}">
				<span class="txtsublibform"> <fmt:message key="GML.or" bundle="${generalBundle}"/> </span><input type="hidden" id="valueImageGallery" name="valueImageGallery"/>
				<select id="galleries" name="galleries" onchange="openGallery(this);this.selectedIndex=0;">
					<option selected><fmt:message key="GML.thumbnail.galleries" bundle="${generalBundle}"/></option>
				    <c:forEach items="${galleries}" var="gallery">
					<option value="${gallery.id}">${gallery.label}</option>
					</c:forEach>
		</select>
			</c:if>
		<c:if test="${mandatory}">
				<img src="<c:url value="/util/icons/mandatoryField.gif"/>" width="5" height="5" border="0" alt=""/>
		    </c:if>
		</div>
		<c:if test="${error != null}">
			<br/>
			<div style="font-style: italic;color:red;"><c:out value="${error}"/></div>
			<br/>
		</c:if>
	</div>
</div>

<div id="thumbnailDialog"></div>