<%--

    Copyright (C) 2000 - 2013 Silverpeas

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    As a special exception to the terms and conditions of version 3.0 of
    the GPL, you may redistribute this Program in connection with Free/Libre
    Open Source Software ("FLOSS") applications as described in Silverpeas's
    FLOSS exception.  You should have received a copy of the text describing
    the FLOSS exception, and it is also available here:
    "http://www.silverpeas.org/docs/core/legal/floss_exception.html"

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

--%>

<%@page import="org.silverpeas.core.web.mvc.controller.ComponentContext"%>
<%@page import="org.silverpeas.core.contribution.attachment.AttachmentServiceProvider"%>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>
<%@ taglib uri="http://www.silverpeas.com/tld/silverFunctions" prefix="silfn" %>
<%@ taglib tagdir="/WEB-INF/tags/silverpeas/util" prefix="viewTags" %>

<%@ page import="org.silverpeas.core.contribution.attachment.model.DocumentType" %>
<%@ page import="org.silverpeas.core.contribution.attachment.model.SimpleDocument" %>
<%@ page import="org.silverpeas.web.attachment.VersioningSessionController" %>
<%@ page import="org.silverpeas.core.ForeignPK" %>
<%@ include file="checkAttachment.jsp"%>
<view:setConstant var="spinfire" constant="org.silverpeas.core.util.MimeTypes.SPINFIRE_MIME_TYPE" />
<c:set var="mainSessionController" value="<%=m_MainSessionCtrl%>" />
<view:settings var="onlineEditingEnable" settings="org.silverpeas.util.attachment.Attachment" defaultValue="${false}" key="OnlineEditingEnable" />
<view:settings var="dAndDropEnable" settings="org.silverpeas.util.attachment.Attachment" defaultValue="${false}" key="DragAndDropEnable" />
<c:set var="webdavEditingEnable" value="${mainSessionController.webDAVEditingEnabled && onlineEditingEnable}" />
<c:set var="dragAndDropEnable" value="${mainSessionController.dragNDropEnabled && dAndDropEnable}" />
<view:setBundle basename="org.silverpeas.util.attachment.multilang.attachment" />
<fmt:setLocale value="${sessionScope.SilverSessionController.favoriteLanguage}" />
<c:set var="id" value="${param.Id}" />
<c:set var="Silverpeas_Attachment_ObjectId" scope="session" value="${id}" />
<c:set var="componentId" value="${param.ComponentId}" />
<c:set var="Silverpeas_Attachment_ComponentId" scope="session" value="${componentId}" />
<c:set var="dNdVisible" value="${param.DNDVisible}" />
<c:set var="originWysiwyg" scope="page" value="${view:booleanValue(param.OriginWysiwyg)}" />
<view:componentParam var="isComponentVersioned" componentId="${param.ComponentId}" parameter="versionControl" />
<c:choose>
  <c:when test="${! view:isDefined(param.Context)}">
    <c:set var="context" value="${'attachment'}" />
  </c:when>
  <c:otherwise>
    <c:set var="context" value="${param.Context}" />
  </c:otherwise>
</c:choose>
<c:set var="Silverpeas_Attachment_Context" scope="session" value="${context}" />
<c:set var="url" value="${param.Url}" />
<c:choose>
  <c:when test="${! view:isDefined(param.Language)}">
    <c:set var="contentLanguage" value="${null}" />
  </c:when>
  <c:otherwise>
    <c:set var="contentLanguage" value="${param.Language}" />
  </c:otherwise>
</c:choose>
<c:set var="indexIt" value="${view:booleanValue(param.IndexIt)}" />
<c:set var="_isI18nHandled" value="${silfn:isI18n() && silfn:isDefined(contentLanguage)}" />
<%
  List<SimpleDocument> attachments = AttachmentServiceProvider.getAttachmentService().
          listDocumentsByForeignKeyAndType(new ForeignPK(request.getParameter("Id"), request.getParameter("ComponentId")),
          DocumentType.valueOf((String)session.getAttribute("Silverpeas_Attachment_Context")),
          (String) pageContext.getAttribute("contentLanguage"));
  pageContext.setAttribute("attachments", attachments);
%>
<c:url var="noColorPix" value="/util/icons/colorPix/1px.gif" />
<c:url var="ArrayPnoColorPix" value="/util/icons/colorPix/15px.gif" />

<c:choose>
  <c:when test="${_isI18nHandled}">
    <c:set var="winAddHeight" value="270" />
    <c:set var="winHeight" value="240" />
  </c:when>
  <c:otherwise>
    <c:set var="winAddHeight" value="240" />
    <c:set var="winHeight" value="220" />
  </c:otherwise>
</c:choose>
<view:includePlugin name="qtip"/>
<view:includePlugin name="iframeajaxtransport"/>
<view:includePlugin name="popup"/>
<c:if test="${view:booleanValue(isComponentVersioned)}">
  <%
  MainSessionController mainSessionCtrl = (MainSessionController) session.getAttribute(MainSessionController.MAIN_SESSION_CONTROLLER_ATT);
  VersioningSessionController versioningSC = (VersioningSessionController) request.getAttribute(URLUtil.CMP_VERSIONINGPEAS);
  if(versioningSC == null) {
      String componentId = request.getParameter("ComponentId");
      ComponentContext componentContext = mainSessionCtrl.createComponentContext(null, componentId);
      VersioningSessionController component = new VersioningSessionController(mainSessionCtrl, componentContext);
      session.setAttribute("Silverpeas_versioningPeas", component);
      versioningSC = component;
    }
  versioningSC.setProfile(request.getParameter("profile"));
  %>
</c:if>
<script type="text/javascript" src='<c:url value="/util/yui/yahoo-dom-event/yahoo-dom-event.js" /> '></script>
<script type="text/javascript" src='<c:url value="/util/yui/container/container_core-min.js" />' ></script>
<script type="text/javascript" src='<c:url value="/util/yui/animation/animation-min.js" />' ></script>
<script type="text/javascript" src='<c:url value="/util/yui/menu/menu-min.js" />' ></script>
<link rel="stylesheet" type="text/css" href='<c:url value="/util/yui/menu/assets/menu.css" />'/>
<script type="text/javascript" language='Javascript'>

  function moveAttachmentUp(id) {
    $.ajax({
      url: '<c:url value="/services/documents/${sessionScope.Silverpeas_Attachment_ComponentId}/document/"/>' + id + '/moveUp',
      type: "PUT",
      contentType: "application/json",
      dataType: "json",
      cache: false,
      success: function(data) {
        reloadPage();
      }
    });
  }

  function moveAttachmentDown(id) {
    $.ajax({
      url: '<c:url value="/services/documents/${sessionScope.Silverpeas_Attachment_ComponentId}/document/"/>' + id + '/moveDown',
      type: "PUT",
      contentType: "application/json",
      dataType: "json",
      cache: false,
      success: function(data) {
        reloadPage();
      }
    });
  }

  function uploadCompleted() {
    reloadPage();
  }

  function displayAttachment(attachment) {
    $('#fileName').text(attachment.fileName);
    $('#fileTitle').val(attachment.title);
    $('#fileDescription').val(attachment.description);
  }

  function clearAttachment() {
    $('#fileName').html('');
    $('#fileTitle').val('');
    $('#fileDescription').val('');
  }

  function addAttachment() {
    $("#dialog-attachment-add").dialog("open");
  }
  function deleteAttachment(id, filename) {
    $("#attachment-delete-warning-message").html('<fmt:message key="attachment.suppressionConfirmation" /> <b>' + filename + '</b> ?' );
    $("#dialog-attachment-delete").data("id", id).dialog("open");
  }

  function updateAttachment(attachmentId, lang) {
    loadAttachment(attachmentId, lang);
    $("#dialog-attachment-update").data('attachmentId', attachmentId).dialog("open");
  }

  function switchDownloadAllowedForReaders(attachmentId, allowed) {
    $.progressMessage();
    $.ajax({
      url : '<c:url value="/services/documents/${sessionScope.Silverpeas_Attachment_ComponentId}/document/"/>' +
          attachmentId + '/switchDownloadAllowedForReaders',
      type : "POST",
      cache : false,
      contentType : "application/json",
      dataType : "json",
      data : {"allowed" : (allowed) ? allowed : false},
      success : function(data) {
        reloadPage();
      }
    });
  }

  function loadAttachment(id, lang) {
    translationsUrl = '<c:url value="/services/documents/${sessionScope.Silverpeas_Attachment_ComponentId}/document/"/>' + id + '/translations';
    $.ajax({
      url: translationsUrl,
      type: "GET",
      contentType: "application/json",
      dataType: "json",
      cache: false,
      success: function(data) {
        $('#attachmentId').val(id);
        clearAttachment();
        $.each(data, function(index, attachment) {
          if (attachment.lang == lang) {
            displayAttachment(attachment);
            return false;
          }
          return true;
        });
      }
    });
  }
  $(document).ready(function() {
      $("#fileLang").on("change", function (event) {
        $("#fileLang option:selected").each(function () {
          alert($(this).val());
          loadAttachment($("#attachmentId").val(), $(this).val());
        });
    });

    var iframeSendComplete = function() {
      reloadPage();
      $(this).dialog("close");
    };

    $('#update-attachment-form').iframeAjaxFormSubmit ({
      complete : iframeSendComplete
    });

    $('#add-attachment-form').iframeAjaxFormSubmit ({
      complete : iframeSendComplete
    });

    $("#dialog-attachment-delete").dialog({
      autoOpen: false,
      title: '<fmt:message key="attachment.dialog.delete" />',
      height: 'auto',
      width: 400,
      modal: true,
      buttons: {
        '<fmt:message key="GML.delete"/>': function() {
          deleteUrl = '<c:url value="/services/documents/${sessionScope.Silverpeas_Attachment_ComponentId}/document/"/>' + $(this).data("id");
            $.ajax({
              url: deleteUrl,
              type: "DELETE",
              cache: false,
              success: function(data) {
                reloadPage();
                $(this).dialog("close");
              }
            });
          },
          '<fmt:message key="GML.cancel"/>': function() {
            $(this).dialog("close");
          }
        },
        close: function() {
        }
      });

    jQuery(document).ajaxError(function(event, jqXHR, settings, errorThrown) {
      $.closeProgressMessage();
    });

    $("#dialog-attachment-add").dialog({
      autoOpen : false,
      title : "<fmt:message key="attachment.dialog.add" />",
      height : 'auto',
      width : 550,
      modal : true,
      buttons : {
        '<fmt:message key="GML.ok"/>' : function() {
          var filename = $.trim($("#file_create").val().split('\\').pop());
          if (filename === '') {
            return false;
          }
          var submitUrl = '<c:url value="/services/documents/${sessionScope.Silverpeas_Attachment_ComponentId}/document/create"/>';
          submitUrl = submitUrl + '/' + encodeURIComponent(filename);
          $.progressMessage();
          if ("FormData" in window) {
            var formData = new FormData($("#add-attachment-form")[0]);
            $.ajax(submitUrl, {
              processData : false,
              contentType : false,
              type : 'POST',
              dataType : "json",
              data : formData,
              success : function(data) {
                reloadPage();
              }
            });
          } else {
            $('#add-attachment-form').attr('action', submitUrl);
            $('#add-attachment-form').submit();
          }
        }, '<fmt:message key="GML.cancel"/>' : function() {
          $(this).dialog("close");
        }
      },
      close : function() {
      }
    });

    $("#dialog-attachment-update").dialog({
      autoOpen : false,
      title : "<fmt:message key="attachment.dialog.update" />",
      height : 'auto',
      width : 550,
      modal : true,
      buttons : {
        '<fmt:message key="GML.ok"/>' : function() {
          var submitUrl = '<c:url value="/services/documents/${sessionScope.Silverpeas_Attachment_ComponentId}/document/"/>' +
              $(this).data('attachmentId');
          var filename = $.trim($("#file_upload").val().split('\\').pop());
          if (filename !== '') {
            submitUrl = submitUrl + '/' + encodeURIComponent(filename);
          } else {
            submitUrl = submitUrl + '/no_file';
          }
          $.progressMessage();
          if ("FormData" in window) {
            var formData = new FormData($("#update-attachment-form")[0]);
            $.ajax(submitUrl, {
              processData : false,
              contentType : false,
              type : 'POST',
              dataType : "json",
              data : formData,
              success : function(data) {
                reloadPage();
              }
            });
          } else {
            $('#update-attachment-form').attr('action', submitUrl);
            $('#update-attachment-form').submit();
          }
        },
        '<fmt:message key="GML.delete"/>' : function() {
          $.ajax({
            url : '<c:url value="/services/documents/${sessionScope.Silverpeas_Attachment_ComponentId}/document/"/>' +
                $(this).data('attachmentId'),
            type : "DELETE",
            contentType : "application/json",
            dataType : "json",
            cache : false,
            success : function(data) {
              reloadPage();
              $(this).dialog("close");
            }
          });
          $(this).dialog("close");
        },
        '<fmt:message key="GML.cancel"/>' : function() {
          $(this).dialog("close");
        }
      },
      close : function() {
      }
    });
    $("#attachmentModalDialog").dialog({
      autoOpen : false,
      modal : true,
      title : '<fmt:message key="attachment.dialog.delete" />',
      height : 'auto',
      width : 400
    });
  });

  function reloadPage() {
    location.reload();
  }

  function selectFile(fileUrl) {
    var funcNum = getUrlParam('CKEditorFuncNum');
    window.opener.CKEDITOR.tools.callFunction(funcNum, fileUrl);
    window.close() ;
  }

  function getUrlParam(paramName) {
    var reParam = new RegExp('(?:[\?&]|&amp;)' + paramName + '=([^&]+)', 'i') ;
    var match = window.location.search.match(reParam) ;
    return (match && match.length > 1) ? match[1] : '' ;
  }
</script>
<div style="text-align: center;">
  <view:board classes="attachmentDragAndDrop${id}">
  <table border="0" width="100%">
    <tr>
      <td><!--formulaire de gestion des fichiers joints -->
        <table border="0" cellspacing="3" cellpadding="0" width="100%">
            <tr>
              <td colspan="8" align="center" class="intfdcolor" height="1"><img src='<c:out value="${noColorPix}" />' alt=""/></td>
            </tr>
            <tr>
              <td align="center"><b><fmt:message key="type" /></b></td>
              <td align="left"><b><fmt:message key="GML.file" /></b></td>
              <td align="left"><b><fmt:message key="Title" /></b></td>
              <td align="left"><b><fmt:message key="GML.description" /></b></td>
              <td align="left"><b><fmt:message key="GML.size" /></b></td>
              <td align="left"><b><fmt:message key="uploadDate" /></b></td>
              <td align="center"><b><fmt:message key="GML.operations" /></b></td>
            </tr>
            <tr>
              <td colspan="8" align="center" class="intfdcolor" height="1"><img src='${noColorPix}' alt="" /></td>
            </tr>
            <c:url var="infoIcon" value="/util/icons/info.gif" />
            <c:url var="updateIcon" value="/util/icons/update.gif" />
            <fmt:message var="updateIconMsg" key="GML.modify" />
            <c:url var="deleteIcon" value="/util/icons/delete.gif" />
            <fmt:message var="deleteIconMsg" key="GML.delete" />
            <c:url var="moveUpIcon" value="/util/icons/arrow/arrowUp.gif" />
            <fmt:message var="moveUpIconMsg" key="Up" />
            <c:url var="moveDownIcon" value="/util/icons/arrow/arrowDown.gif" />
            <fmt:message var="moveDownIconMsg" key="Down" />

            <c:forEach items="${pageScope.attachments}" var="varAttachment" varStatus="attachmentIterStatus">

              <%-- Download variable handling --%>
              <c:set var="canUserDownloadFile" value="${true}"/>
              <c:set var="forbiddenDownloadClass" value=""/>
              <fmt:message key="GML.download.forbidden.readers" var="forbiddenDownloadHelp"/>
              <c:if test="${!varAttachment.downloadAllowedForReaders}">
                <c:set var="canUserDownloadFile"
                       value="${varAttachment.isDownloadAllowedForRolesFrom(mainSessionController.currentUserDetail)}"/>
                <c:if test="${!canUserDownloadFile}">
                  <c:set var="forbiddenDownloadClass" value="forbidden-download"/>
                  <fmt:message key="GML.download.forbidden" var="forbiddenDownloadHelp"/>
                </c:if>
              </c:if>

              <c:url var="currentAttachmentUrl" value="${varAttachment.attachmentURL}" />
              <tr id='attachment_${varAttachment.oldSilverpeasId}'>
                <td class="odd ${forbiddenDownloadClass}" align="center">
                  <c:choose>
                    <c:when test="${canUserDownloadFile}">
                      <a id="other" href='<c:out value="${currentAttachmentUrl}" />' target="_blank"><img src='<c:out value="${varAttachment.displayIcon}" />' border="0" alt=""/></a>
                    </c:when>
                    <c:otherwise>
                      <img src='<c:out value="${varAttachment.displayIcon}" />' border="0" alt=""/>
                    </c:otherwise>
                  </c:choose>
                </td>
                <td class="odd ${forbiddenDownloadClass}" align="left">
                  <c:choose>
                    <c:when test="${originWysiwyg}">
                      <a href="javascript:selectFile('<c:out value="${silfn:escapeJs(currentAttachmentUrl)}" />');"><c:out value="${varAttachment.filename}" /></a>
                    </c:when>
                    <c:otherwise>
                      <c:choose>
                        <c:when test="${canUserDownloadFile}">
                          <a href='<c:out value="${currentAttachmentUrl}" />' target="_blank"><c:out value="${varAttachment.filename}" /></a>
                        </c:when>
                        <c:otherwise>
                          <c:out value="${varAttachment.filename}" />
                        </c:otherwise>
                      </c:choose>
                    </c:otherwise>
                  </c:choose>
                </td>
                <td class="odd ${forbiddenDownloadClass}" align="left">
                  <c:choose>
                    <c:when test="${view:isDefined(varAttachment.title)}">
                      <c:out value="${varAttachment.title}" />
                    </c:when>
                    <c:otherwise>&nbsp;</c:otherwise>
                  </c:choose>
                </td>
                <td class="odd ${forbiddenDownloadClass}" align="center">
                  <view:icon altText="${varAttachment.description}" iconName="${infoIcon}" />
                </td>
                <td class="odd" align="left"><c:out value="${view:humanReadableSize(varAttachment.size)}" /></td>
                <td class="odd" align="left"><view:formatDate language="${sessionScope.SilverSessionController.favoriteLanguage}" value="${varAttachment.created}"/></td>
                <td class="odd" align="right">
                  <view:icons>
                    <view:icon iconName="${updateIcon}" altText="${updateIconMsg}" action="javascript:updateAttachment(\'${varAttachment.id}\', \'${varAttachment.language}\');"/>
                    <view:icon iconName="${deleteIcon}" altText="${deleteIconMsg}" action="javascript:deleteAttachment(\'${varAttachment.id}\',\'${silfn:escapeJs(varAttachment.filename)}\');"/>
                    <c:choose>
                      <c:when test="${! attachmentIterStatus.last}">
                        <view:icon iconName="${moveDownIcon}" altText="${moveDownIconMsg}" action="javascript:moveAttachmentDown(\'${varAttachment.id}\');"/>
                      </c:when>
                      <c:otherwise>
                        <view:icon iconName="${ArrayPnoColorPix}" altText="" action=""/>
                      </c:otherwise>
                    </c:choose>
                   <c:choose>
                      <c:when test="${! attachmentIterStatus.first}">
                        <view:icon iconName="${moveUpIcon}" altText="${moveUpIconMsg}" action="javascript:moveAttachmentUp(\'${varAttachment.id}\');"/>
                      </c:when>
                      <c:otherwise>
                        <view:icon iconName="${ArrayPnoColorPix}" altText="" action=""/>
                      </c:otherwise>
                    </c:choose>
                  </view:icons>
                </td>
              </tr>
            </c:forEach>
          <tr>
            <td colspan="8" align="center" class="intfdcolor" height="1"><img src='<c:out value="${noColorPix}" />' alt="" /></td>
          </tr>
        </table>
      </td>
    </tr>
  </table>
</view:board>
<br />
  <fmt:message key="GML.add" var="addLabel" />
  <view:buttonPane>
    <view:button action="javascript:addAttachment()" label="${addLabel}" />
  </view:buttonPane>
</div>


<div id="dialog-attachment-update" style="display:none">
  <form name="update-attachment-form" id="update-attachment-form" method="post" enctype="multipart/form-data;charset=utf-8" accept-charset="UTF-8">
    <label for="fileName"><fmt:message key="GML.file" /></label><br/>
    <span id="fileName"></span><br/>
    <input type="hidden" name="IdAttachment" id="attachmentId"/><br/>
    <label for="file_upload"><fmt:message key="fichierJoint"/></label><br/>
    <input type="file" name="file_upload" size="60" id="file_upload" multiple/><br/>
    <view:langSelect elementName="fileLang" elementId="fileLang" langCode="fr" includeLabel="true" /><br/>
    <label for="fileTitle"><fmt:message key="Title"/></label><br/>
    <input type="text" name="fileTitle" size="60" id="fileTitle" /><br/>
    <label for="fileDescription"><fmt:message key="GML.description" /></label><br/>
    <textarea name="fileDescription" cols="60" rows="3" id="fileDescription"></textarea><br/>
    <input type="submit" value="Submit" style="display:none" />
  </form>
</div>
<div id="dialog-attachment-add" style="display:none">
  <form name="add-attachment-form" id="add-attachment-form" method="post" enctype="multipart/form-data;charset=utf-8" accept-charset="UTF-8">
    <input type="hidden" name="foreignId" id="foreignId" value="<c:out value="${sessionScope.Silverpeas_Attachment_ObjectId}" />" />
    <input type="hidden" name="indexIt" id="indexIt" value="<c:out value="${indexIt}" />" />
    <input type="hidden" name="context" id="context" value="<c:out value="${context}" />" />
    <label for="file_create"><fmt:message key="fichierJoint"/></label><br/>
    <input type="file" name="file_upload" size="60" id="file_create" multiple/><br/>
    <view:langSelect elementName="fileLang" elementId="langCreate" langCode="fr" includeLabel="true" /><br/>
    <label for="fileTitleCreate"><fmt:message key="Title"/></label><br/>
    <input type="text" name="fileTitle" size="60" id="fileTitleCreate" /><br/>
    <label for="fileDescriptionCreate"><fmt:message key="GML.description" /></label><br/>
    <textarea name="fileDescription" cols="60" rows="3" id="fileDescriptionCreate"></textarea><br/>
    <input type="submit" value="Submit" style="display:none" />
  </form>
</div>

<div id="dialog-attachment-delete" style="display:none">
  <span id="attachment-delete-warning-message"><fmt:message key="attachment.suppressionConfirmation" /></span>
</div>

<view:progressMessage/>

<c:if test="${dragAndDropEnable}">
  <viewTags:attachmentDragAndDrop domSelector=".attachmentDragAndDrop${id}"
                                  componentInstanceId="${componentId}"
                                  resourceId="${id}"
                                  contentLanguage="${contentLanguage}"
                                  hasToBeIndexed="${indexIt}"
                                  documentType="${context}"
                                  helpCoverClass="droparea-cover-help-attachment-edit"/>
</c:if>
