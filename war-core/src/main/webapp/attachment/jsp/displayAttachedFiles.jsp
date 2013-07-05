<%--

    Copyright (C) 2000 - 2012 Silverpeas

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
<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>
<%@ taglib uri="http://www.silverpeas.com/tld/silverFunctions" prefix="silfn" %>
<%@ taglib uri="http://www.silverpeas.com/tld/contextMenu" prefix="menu" %>
<%@ page errorPage="../../admin/jsp/errorpage.jsp"%>
<%@ page import="com.silverpeas.util.ForeignPK" %>
<%@ page import="org.silverpeas.attachment.AttachmentServiceFactory" %>
<%@ page import="org.silverpeas.attachment.model.DocumentType" %>
<%@ page import="org.silverpeas.attachment.model.SimpleDocument" %>
<%@ page import="com.stratelia.silverpeas.peasCore.ComponentContext" %>
<%@ page import="org.silverpeas.attachment.web.VersioningSessionController" %>

<%@ include file="checkAttachment.jsp"%>

<view:setBundle basename="org.silverpeas.util.attachment.multilang.attachment" />
<fmt:setLocale value="${sessionScope.SilverSessionController.favoriteLanguage}" />
<view:componentParam var="isComponentVersioned" componentId="${param.ComponentId}" parameter="versionControl" />
<c:set var="isVersionActive" value="${silfn:booleanValue(isComponentVersioned)}" />
<view:includePlugin name="qtip"/>
<view:includePlugin name="iframepost"/>
<view:includePlugin name="popup"/>
<view:includePlugin name="preview"/>
<c:choose>
  <c:when test="${isVersionActive}">
    <script type="text/javascript" src='<c:url value="/attachment/jsp/javaScript/versionedDragAndDrop.js" />' ></script>
<%
  MainSessionController mainSessionCtrl = (MainSessionController) session.getAttribute(MainSessionController.MAIN_SESSION_CONTROLLER_ATT);
  VersioningSessionController versioningSC = (VersioningSessionController) request.getAttribute(URLManager.CMP_VERSIONINGPEAS);
  if(versioningSC == null) {
      String componentId = request.getParameter("ComponentId");
      ComponentContext componentContext = mainSessionCtrl.createComponentContext(null, componentId);
      VersioningSessionController component = new VersioningSessionController(mainSessionCtrl, componentContext);
      session.setAttribute("Silverpeas_versioningPeas", component);
      versioningSC = component;
    }
  versioningSC.setProfile(request.getParameter("profile"));
  %>
  </c:when>
  <c:otherwise>
<script type="text/javascript" src='<c:url value="/attachment/jsp/javaScript/dragAndDrop.js" />' ></script>
  </c:otherwise>
</c:choose>
<script type="text/javascript" src='<c:url value="/util/javaScript/upload_applet.js" />' ></script>
<script type="text/javascript" src='<c:url value="/util/yui/yahoo-dom-event/yahoo-dom-event.js" /> '></script>
<script type="text/javascript" src='<c:url value="/util/yui/container/container_core-min.js" />' ></script>
<script type="text/javascript" src='<c:url value="/util/yui/animation/animation-min.js" />' ></script>
<script type="text/javascript" src='<c:url value="/util/yui/menu/menu-min.js" />' ></script>
<link rel="stylesheet" type="text/css" href='<c:url value="/util/yui/menu/assets/menu.css" />'/>

  <view:settings var="spinfireViewerEnable" settings="org.silverpeas.util.attachment.Attachment" defaultValue="${false}" key="SpinfireViewerEnable" />
  <view:setConstant var="spinfire" constant="com.silverpeas.util.MimeTypes.SPINFIRE_MIME_TYPE" />
  <view:setConstant var="mainSessionControllerAtt" constant="com.stratelia.silverpeas.peasCore.MainSessionController.MAIN_SESSION_CONTROLLER_ATT" />
  <c:set var="mainSessionController" value="${sessionScope[mainSessionControllerAtt]}" />
  <view:settings var="onlineEditingEnable" settings="org.silverpeas.util.attachment.Attachment" defaultValue="${false}" key="OnlineEditingEnable" />
  <view:settings var="dAndDropEnable" settings="org.silverpeas.util.attachment.Attachment" defaultValue="${false}" key="DragAndDropEnable" />
  <c:set var="webdavEditingEnable" value="${mainSessionController.webDAVEditingEnabled && onlineEditingEnable}" />
  <c:set var="dndDisabledLocally" value="${'false' eq fn:toLowerCase(param.dnd)}" scope="page"/>
  <c:set var="dragAndDropEnable" value="${mainSessionController.dragNDropEnabled && dAndDropEnable && not dndDisabledLocally}" />

  <c:set var="userProfile" value="${fn:toLowerCase(param.Profile)}" scope="page"/>
  <c:set var="contextualMenuEnabled" value="${'admin' eq userProfile || 'publisher' eq userProfile || 'writer' eq userProfile}" scope="page" />
  <view:componentParam var="xmlForm" componentId="${param.ComponentId}" parameter="XmlFormForFiles" />
  <view:componentParam var="useFileSharingParam" componentId="${param.ComponentId}" parameter="useFileSharing" />
  <c:set var="useFileSharing" value="${'yes' eq fn:toLowerCase(useFileSharingParam) && 'admin' eq userProfile }" />
  <c:choose>
    <c:when test="${contextualMenuEnabled}">
      <c:set var="iconStyle" scope="page">style="cursor:move"</c:set>
    </c:when>
    <c:otherwise>
      <c:set var="iconStyle" scope="page" value="${''}" />
    </c:otherwise>
  </c:choose>
  <c:choose>
    <c:when test="${param.AttachmentPosition != null}">
      <c:set var="attachmentPosition" scope="page" value="${param.AttachmentPosition}" />
    </c:when>
    <c:otherwise>
      <c:set var="attachmentPosition" scope="page" value="${'right'}" />
    </c:otherwise>
  </c:choose>
  <c:choose>
    <c:when test="${'right' eq attachmentPosition}">
      <c:set var="isAttachmentPositionRight" scope="page" value="${true}" />
      <c:set var="isAttachmentPositionBottom" scope="page" value="${false}" />
    </c:when>
    <c:otherwise>
      <c:set var="isAttachmentPositionRight" scope="page" value="${false}" />
      <c:choose>
        <c:when test="${'bottom' eq attachmentPosition}">
          <c:set var="isAttachmentPositionBottom" scope="page"  value="${true}" />
        </c:when>
        <c:otherwise>
          <c:set var="isAttachmentPositionBottom" scope="page" value="${false}" />
        </c:otherwise>
      </c:choose>
    </c:otherwise>
  </c:choose>
  <c:choose>
    <c:when test="${param.ShowTitle != null}">
      <c:set var="showTitle" scope="page" value="${silfn:booleanValue(param.ShowTitle)}" />
    </c:when>
    <c:otherwise>
      <c:set var="showTitle" scope="page" value="${true}" />
    </c:otherwise>
  </c:choose>
  <c:choose>
    <c:when test="${param.ShowFileSize != null}">
      <c:set var="showFileSize" scope="page" value="${silfn:booleanValue(param.ShowFileSize)}" />
    </c:when>
    <c:otherwise>
      <c:set var="showFileSize" scope="page" value="${true}" />
    </c:otherwise>
  </c:choose>
  <c:choose>
    <c:when test="${param.ShowInfo != null}">
      <c:set var="showInfo" scope="page" value="${silfn:booleanValue(param.ShowInfo)}" />
    </c:when>
    <c:otherwise>
      <c:set var="showInfo" scope="page" value="${true}" />
    </c:otherwise>
  </c:choose>
  <c:choose>
    <c:when test="${param.ShowIcon != null}">
      <c:set var="showIcon" scope="page" value="${silfn:booleanValue(param.ShowIcon)}" />
    </c:when>
    <c:otherwise>
      <c:set var="showIcon" scope="page" value="${true}" />
    </c:otherwise>
  </c:choose>
  <c:set var="fromAlias" value="${silfn:booleanValue(param.Alias)}" />
  <c:set var="useXMLForm" value="${silfn:isDefined(xmlForm)}" />
  <c:set var="indexIt" value="${silfn:booleanValue(param.IndexIt)}" />
  <c:set var="showMenuNotif" value="${silfn:booleanValue(param.ShowMenuNotif)}" />
  <c:set var="displayUniversalLinks"><%=URLManager.displayUniversalLinks()%></c:set>

  <c:set var="Silverpeas_Attachment_ObjectId" value="${param.Id}" scope="session" />
  <c:set var="Silverpeas_Attachment_ComponentId" value="${param.ComponentId}" scope="session" />
  <c:choose>
    <c:when test="${silfn:isDefined(param.context)}">
      <c:set var="Silverpeas_Attachment_Context" value="${param.Context}" scope="session" />
    </c:when>
    <c:otherwise>
      <c:set var="Silverpeas_Attachment_Context" value="attachment" scope="session" />
    </c:otherwise>
  </c:choose>
  <c:set var="Silverpeas_Attachment_Profile" value="${userProfile}" scope="session" />
  <c:set var="Silverpeas_Attachment_IndexIt" value="${indexIt}" />
  <c:choose>
    <c:when test="${! silfn:isDefined(param.Language)}">
      <c:set var="contentLanguage" value="${null}" />
    </c:when>
    <c:otherwise>
      <c:set var="contentLanguage" value="${param.Language}" />
    </c:otherwise>
  </c:choose>
  <c:set var="componentId" value="${param.ComponentId}" />
<%
  List<SimpleDocument> attachments = AttachmentServiceFactory.getAttachmentService().
          listDocumentsByForeignKeyAndType(new ForeignPK(request.getParameter("Id"), request.getParameter("ComponentId")),
          DocumentType.valueOf((String)session.getAttribute("Silverpeas_Attachment_Context")),
          (String) pageContext.getAttribute("contentLanguage"));
  pageContext.setAttribute("attachments", attachments);
%>
<c:if test="${!empty pageScope.attachments  || (silfn:isDefined(userProfile) && ('user' != userProfile))}">
<div class="attachments bgDegradeGris">
  <div class="bgDegradeGris header"><h4 class="clean"><fmt:message key="GML.attachments" /></h4></div>
  <c:if test="${contextualMenuEnabled}">
  <div id="attachment-creation-actions"><a class="menubar-creation-actions-item" href="javascript:addAttachment('<c:out value="${sessionScope.Silverpeas_Attachment_ObjectId}" />');"><span><img alt="" src="<c:url value="/util/icons/create-action/add-file.png" />"/><fmt:message key="attachment.add"/></span></a></div>
  </c:if>
    <ul id="attachmentList">
        <c:forEach items="${pageScope.attachments}" var="varAttachment" >
        <c:choose>
          <c:when test="${varAttachment.versioned && !(varAttachment.public) && ('user' eq userProfile)}">
            <c:set var="currentAttachment" value="${varAttachment.lastPublicVersion}" />
          </c:when>
          <c:otherwise>
            <c:set var="currentAttachment" value="${varAttachment}" />
          </c:otherwise>
        </c:choose>
        <c:if test="${currentAttachment ne null}">
          <c:if test="${isAttachmentPositionRight}">
            <li id='attachment_<c:out value="${currentAttachment.oldSilverpeasId}"/>' class='attachmentListItem' <c:out value="${iconStyle}" escapeXml="false"/> >
          </c:if>
          <c:if test="${contextualMenuEnabled}">
            <menu:simpleDocument attachment="${currentAttachment}" contentLanguage="${contentLanguage}"
                                 showMenuNotif="${showMenuNotif}" useContextualMenu="${useContextualMenu}"
                                 useFileSharing="${useFileSharing}" useWebDAV="${webdavEditingEnable}" useXMLForm="${useXMLForm}" />
          </c:if>
          <span class="lineMain">
              <c:if test="${contextualMenuEnabled && !pageScope.useContextualMenu}">
                <img id='edit_<c:out value="${currentAttachment.oldSilverpeasId}"/>' src='<c:url value="/util/icons/arrow/menuAttachment.gif" />' class="moreActions"/>
              </c:if>
              <c:if test="${showIcon}">
                <img id='img_<c:out value="${currentAttachment.oldSilverpeasId}"/>' src='<c:out value="${currentAttachment.displayIcon}" />' class="icon" />
              </c:if>
              <c:choose>
                <c:when test="${fromAlias}">
                  <c:set var="attachmentUrl" value="${currentAttachment.aliasURL}" />
                </c:when>
                <c:otherwise>
                  <c:url var="attachmentUrl" value="${currentAttachment.attachmentURL}" />
                </c:otherwise>
              </c:choose>
              <c:choose>
                <c:when test="${! silfn:isDefined(currentAttachment.title) || ! showTitle}">
                  <c:set var="title" value="${currentAttachment.filename}" />
                </c:when>
                <c:otherwise>
                  <c:set var="title" value="${currentAttachment.title}" />
                </c:otherwise>
              </c:choose>
              <a id='url_<c:out value="${currentAttachment.oldSilverpeasId}"/>' href="${attachmentUrl}" target="_blank"><c:out value="${title}" /></a>
              <c:if test="${currentAttachment.versioned}">
                &nbsp;<span class="version-number" id="version_<c:out value="${currentAttachment.oldSilverpeasId}"/>">v<c:out value="${currentAttachment.majorVersion}"/>.<c:out value="${currentAttachment.minorVersion}"/></span>
              </c:if>
            </span>
            <span class="lineSize">
              <c:if test="${displayUniversalLinks}">
                <a href='<c:out value="${currentAttachment.universalURL}" escapeXml="false" />'><img src='<c:url value="/util/icons/link.gif"/>' border="0" alt="<fmt:message key="CopyLink"/>" title="<fmt:message key="CopyLink"/>" /></a>
                </c:if>
                <c:if test="${showFileSize}">
                  <c:out value="${view:humanReadableSize(currentAttachment.size)}" />
                </c:if>
                 - <view:formatDateTime value="${currentAttachment.updated}" />
              <c:if test="${silfn:isPreviewable(currentAttachment.attachmentPath)}">
                <img onclick="javascript:preview(this, '<c:out value="${currentAttachment.id}" />');" class="preview-file" src='<c:url value="/util/icons/preview.png"/>' alt="<fmt:message key="GML.preview"/>" title="<fmt:message key="GML.preview" />"/>
              </c:if>
              <c:if test="${silfn:isViewable(currentAttachment.attachmentPath)}">
                <img onclick="javascript:view(this, '<c:out value="${currentAttachment.id}" />');" class="view-file" src='<c:url value="/util/icons/view.png"/>' alt="<fmt:message key="GML.view"/>" title="<fmt:message key="GML.view" />"/>
              </c:if>
            </span>
              <c:if test="${silfn:isDefined(currentAttachment.title) && showTitle}">
                <span class="fileName"><c:out value="${currentAttachment.filename}" /></span>
            </c:if>
              <c:if test="${silfn:isDefined(currentAttachment.description) && showInfo}">
                <span class="description"><view:encodeHtmlParagraph string="${currentAttachment.description}" /></span>
            </c:if>
              <c:if test="${silfn:isDefined(currentAttachment.xmlFormId)}">
                <br/><a rel='<c:url value="/RformTemplate/jsp/View">
                        <c:param name="width" value="400"/>
                        <c:param name="ObjectId" value="${currentAttachment.id}"/>
                        <c:param name="ObjectLanguage" value="${contentLanguage}"/>
                        <c:param name="ComponentId" value="${componentId}"/>
                        <c:param name="ObjectType" value="${'Attachment'}"/>
                        <c:param name="XMLFormName" value="${currentAttachment.xmlFormId}"/>
                      </c:url>' href="#" title='<c:out value="${title}"/>' ><fmt:message key="attachment.xmlForm.View" /></a>
            </c:if>
            <view:componentParam var="hideAllVersionsLink" componentId="${param.ComponentId}" parameter="hideAllVersionsLink" />
            <c:set var="shouldHideAllVersionsLink" scope="page" value="${silfn:booleanValue(hideAllVersionsLink) && 'user' eq userProfile}" />
            <c:set var="shouldShowAllVersionLink" scope="page" value="${currentAttachment.versioned && ( ('user' eq userProfile && currentAttachment.public) || !('user' eq userProfile  || empty currentAttachment.history))}" />
            <c:if test="${shouldShowAllVersionLink && !shouldHideAllVersionsLink}" >
                <span class="linkAllVersions">
                  <img alt='<fmt:message key="allVersions" />' src='<c:url value="/util/icons/bullet_add_1.gif" />' /> <a href="javaScript:viewPublicVersions('<c:out value="${currentAttachment.id}" />')" /><fmt:message key="allVersions" /></a>
                </span>
            </c:if>
            <c:if test="${contextualMenuEnabled}">
              <c:choose>
                <c:when test="${currentAttachment.readOnly}">
                  <div class='workerInfo'  id='worker<c:out value="${currentAttachment.oldSilverpeasId}" />' style="visibility:visible"><fmt:message key="readOnly" /> <view:username zoom="false" userId="${currentAttachment.editedBy}" /> <fmt:message key="at" /> <view:formatDateTime value="${currentAttachment.reservation}" /></div>
                </c:when>
                <c:otherwise>
                  <div class='workerInfo'  id='worker<c:out value="${currentAttachment.oldSilverpeasId}" />' style="visibility:hidden"> </div>
                </c:otherwise>
              </c:choose>
            </c:if>
                  <c:if test="${spinfireViewerEnable && spinfire eq silfn:mimeType(currentAttachment.filename)}">
                    <div id="switchView" name="switchView" style="display: none">
              <a href="#" onClick="changeView3d('<c:out value="${currentAttachment.id}" />')"><img name="iconeView<c:out value="${currentAttachment.id}" />" valign="top" border="0" src="<c:url value="/util/icons/masque3D.gif" />"></a>
              </div><div id="<c:out value="${currentAttachment.id}" />" style="display: none">
                <object classid="CLSID:A31CCCB0-46A8-11D3-A726-005004B35102" width="300" height="200" id="XV" >
                  <param name="ModelName" value="<c:out value="${url}" escapeXml="false"/>">
                  <param name="BorderWidth" value="1">
                  <param name="ReferenceFrame" value="1">
                  <param name="ViewportActiveBorder" value="FALSE">
                  <param name="DisplayMessages" value="TRUE">
                  <param name="DisplayInfo" value="TRUE">
                  <param name="SpinX" value="0">
                  <param name="SpinY" value="0">
                  <param name="SpinZ" value="0">
                  <param name="AnimateTransitions" value="0">
                  <param name="ZoomFit" value="1">
                </object>
              </div>
              <br/>
            </c:if>
            <c:if test="${isAttachmentPositionRight}"></li></c:if>
          </c:if>
        </c:forEach>
    </ul>
    <c:if test="${contextualMenuEnabled && dragAndDropEnable}">
      <c:choose>
        <c:when test="${isVersionActive}">
        <div>
            <div class="dragNdrop">
              <a href="javascript:showDnD()" id="dNdActionLabel"><fmt:message key="GML.DragNDropExpand"/></a>
            </div>
            <div id="DragAndDrop" style="background-color: #CDCDCD; border: 1px solid #CDCDCD; padding: 0px" align="top"> </div>
            <div id="DragAndDropDraft" style="background-color: #CDCDCD; border: 1px solid #CDCDCD; padding: 0px" align="top"> </div>
          </div>
        </c:when>
        <c:otherwise>
          <view:settings var="maximumFileSize" settings="org.silverpeas.util.uploads.uploadSettings" key="MaximumFileSize" defaultValue="${10000000}" />
          <c:url var="dropUrl" value="/DragAndDrop/drop">
            <c:param name="UserId" value="${mainSessionController.userId}" />
            <c:param name="ComponentId" value="${componentId}" />
            <c:param name="PubId" value="${param.Id}" />
            <c:param name="IndexIt" value="${indexIt}" />
            <c:param name="Context" value="${param.Context}"/>
          </c:url>
          <div class="dragNdrop">
            <a href="javascript:showHideDragDrop('<%=URLManager.getServerURL(request)%><c:out value="${dropUrl}" />','<%=URLManager.getFullApplicationURL(request)%>/upload/explanationShort_<%=language%>.html','<fmt:message key="GML.applet.dnd.alt" />','<c:out value="${maximumFileSize}" />','<%=URLManager.getApplicationURL()%>','<fmt:message key="GML.DragNDropExpand" />','<fmt:message key="GML.DragNDropCollapse" />')" id="dNdActionLabel"><fmt:message key="GML.DragNDropExpand" /></a>
            <div id="DragAndDrop" style="background-color: #CDCDCD; border: 1px solid #CDCDCD; padding: 0px" align="top"> </div>
          </div>
      </c:otherwise>
    </c:choose>
  </c:if>
</div>
</c:if>
<div id="attachmentModalDialog" style="display: none"> </div>
<c:if test="${spinfireViewerEnable}">
  <script type="text/javascript">
  if (navigator.appName=='Microsoft Internet Explorer') {
    for (var i = 0; i < document.getElementsByName("switchView").length; i++) {
      document.getElementsByName("switchView")[i].style.display = '';
    }
  }

  function changeView3d(objectId) {
    if (document.getElementById(objectId).style.display == 'none') {
      document.getElementById(objectId).style.display = '';
      eval("iconeView" + objectId).src = '<c:url value="/util/icons/visible3D.gif" />';
    } else {
      document.getElementById(objectId).style.display = 'none';
      eval("iconeView" + objectId).src = '<c:url value="/util/icons/masque3D.gif" />';
    }
  }
  </script>
</c:if>

<script type="text/javascript">
  <c:url var="allVersionsUrl" value="/RVersioningPeas/jsp/ViewAllVersions">
    <c:param name="ComponentId" value="${componentId}" />
    <c:param name="fromAlias" value="${silfn:booleanValue(param.Alias)}"/>
    <c:param name="Language" value="${contentLanguage}"/>
  </c:url>
  var publicVersionsWindow = window;
  function viewPublicVersions(docId) {
      var url = '${allVersionsUrl}&DocId=' + docId;
      var windowName = "publicVersionsWindow";
      var windowParams = "directories=0,menubar=0,toolbar=0,scrollbars=1,alwaysRaised";
      if (!publicVersionsWindow.closed && publicVersionsWindow.name == "publicVersionsWindow") {
        publicVersionsWindow.close();
      }
      publicVersionsWindow = SP_openWindow(url,windowName, "800", "475",
      "directories=0,menubar=0,toolbar=0,scrollbars=1,alwaysRaised");
    }

  <view:settings var="maximumFileSize" settings="org.silverpeas.util.uploads.uploadSettings" key="MaximumFileSize" defaultValue="${10000000}" />
  <c:url var="publicURL" value="/VersioningDragAndDrop/jsp/Drop">
    <c:param name="UserId" value="${mainSessionController.userId}" />
    <c:param name="ComponentId" value="${componentId}" />
    <c:param name="Id" value="${param.Id}" />
    <c:param name="IndexIt" value="${indexIt}" />
    <c:param name="Type" value="0"/>
  </c:url>
  <c:url var="workURL" value="/VersioningDragAndDrop/jsp/Drop">
    <c:param name="UserId" value="${mainSessionController.userId}" />
    <c:param name="ComponentId" value="${componentId}" />
    <c:param name="Id" value="${param.Id}" />
    <c:param name="IndexIt" value="${indexIt}" />
    <c:param name="Type" value="1"/>
  </c:url>
  function showDnD() {
    showHideDragDrop('${publicURL}', '<%=URLManager.getFullApplicationURL(request)%>/upload/VersioningPublic_<%=language%>.html',
      '${workURL}', '<%=URLManager.getFullApplicationURL(request)%>/upload/VersioningWork_<%=language%>.html',
      '<fmt:message key="GML.applet.dnd.alt" />', '${maximumFileSize}', '<%=URLManager.getApplicationURL()%>',
      '<fmt:message key="GML.DragNDropExpand" />', '<fmt:message key="GML.DragNDropCollapse" />');
  }

  String.prototype.format = function () {
    var args = arguments;
    return this.replace(/\{(\d+)\}/g, function (m, n) { return args[n]; });
  };


  // Create the tooltips only on document load
  $(document).ready(function() {
    // Use the each() method to gain access to each elements attributes
    $('a[rel]').each(function() {
      $(this).qtip({
        content : {
          // Set the text to an image HTML string with the correct src URL to the loading image you want to use
          text : '<img class="throbber" src="<c:url value="/util/icons/inProgress.gif" />" alt="Loading..." />',
          ajax: {
            url : $(this).attr('rel') // Use the rel attribute of each element for the url to load
          },
          title : {
            text : '<fmt:message key="attachment.xmlForm.ToolTip"/> \"' + $(this).attr('title') + "\"", // Give the tooltip a title using each elements text
            button : '<fmt:message key="GML.close" />' // Show a close link in the title
          }
        },
        position : {
          adjust : {
            method : "flip flip"
          },
          at : "left center",
          my : "right center",
          viewport : $(window) // Keep the tooltip on-screen at all times
        },
        show : {
          solo : true,
          event : "click"
        },
        hide : {
          event : "unfocus"
        },
        style : {
          tip : true, // Apply a speech bubble tip to the tooltip at the designated tooltip corner
          width : 570,
          classes : "qtip-shadow qtip-light"
        }
      })
    });

    // function to transform insecable string into secable one
    $(".lineMain a").html(function() {
      var newLibelle = "";
      var maxLength = 38;
      var chainesInsecables = $(this).text().split(" ");
      for (i = 0; i < chainesInsecables.length; i++) {
        var chainesSecables = " ";
        while (chainesInsecables[i].length > maxLength) {
          chainesSecables = chainesSecables + chainesInsecables[i].substring(0, maxLength) + '<br/>';
          chainesInsecables[i] = chainesInsecables[i].substring(maxLength);
        }
        chainesInsecables[i] = chainesSecables + chainesInsecables[i];
        newLibelle = newLibelle + chainesInsecables[i];
      }
      $(this).html(newLibelle);
    });
  });

  <c:if test="${contextualMenuEnabled}" >
    var pageMustBeReloadingAfterSorting = false;

    function checkout(id, oldId, webdav, edit, download) {
      if (id.length > 0) {
        pageMustBeReloadingAfterSorting = true;
        $.post('<c:url value="/Attachment" />', {Id:id, FileLanguage:'<c:out value="${contentLanguage}" />', Action:'Checkout'}, function(data) {
          if (data == 'ok') {
            var oMenu = eval("oMenu" + oldId);
            oMenu.getItem(3).cfg.setProperty("disabled", false); // checkin
            oMenu.getItem(0).cfg.setProperty("disabled", true); // checkout
            oMenu.getItem(1).cfg.setProperty("disabled", true);	// checkout and download
            if (!webdav) {
              oMenu.getItem(2).cfg.setProperty("disabled", true);  // edit online
            }
            //disable delete
            oMenu.getItem(3, 1).cfg.setProperty("disabled", true); // delete
            oMenu.getItem(2, 1).cfg.setProperty("disabled", true); // switch
            var $worker = $('#worker' + oldId);
            $worker.html("<fmt:message key="readOnly"/> <%=m_MainSessionCtrl.getCurrentUserDetail().getDisplayedName()%> <fmt:message key="at"/> <%=DateUtil.getOutputDate(new Date(), language)%>");
            $worker.css({'visibility':'visible'});
            if (edit) {
              var url = "<%=URLManager.getFullApplicationURL(request)%>/attachment/jsp/launch.jsp?documentUrl=" + eval("webDav".concat(oldId));
              window.open(url, '_self');
            } else if (download) {
              var url = $('#url_' + oldId).attr('href');
              window.open(url);
            }
          } else {
            alert('<fmt:message key="attachment.dialog.checkout.nok"/>');
            window.location.href = window.location.href;
          }
        }, 'text');
        pageMustBeReloadingAfterSorting = true;
      }
    }

    function checkoutAndDownload(id, oldId, webdav) {
      checkout(id, oldId, webdav, false, true);
    }

    function checkoutAndEdit(id, oldId) {
      checkout(id, oldId, true, true, false);
    }

    function switchState(id, isVersioned) {
      <fmt:message key="attachment.switch.warning.simple" var="warningSimple"/>
      <fmt:message key="attachment.switch.warning.versioned" var="warningVersioned"/>
      <fmt:message key="attachment.switchState.toVersioned" var="warningTitleSimple"/>
      <fmt:message key="attachment.switchState.toSimple" var="warningTitleVersioned"/>
      if(isVersioned) {
        $("#dialog-attachment-switch").dialog( "option" , 'title' , '<c:out value="${silfn:escapeJs(warningTitleVersioned)}" />' );
        $("#attachment-switch-warning-message").empty().append('<c:out value="${silfn:escapeJs(warningSimple)}" />');
        $("#attachment-switch-versioned").hide();
        $("#attachment-switch-simple").show();
      } else {
        $("#dialog-attachment-switch").dialog( "option" , 'title' , '<c:out value="${silfn:escapeJs(warningTitleSimple)}" />' );
        $("#attachment-switch-warning-message").empty().append('<c:out value="${silfn:escapeJs(warningVersioned)}" />');
        $("#attachment-switch-simple").hide();
        $("#attachment-switch-versioned").show();
      }
      $("#dialog-attachment-switch").data("id", id).dialog("open");
      pageMustBeReloadingAfterSorting = true;
    }

    function checkin(id, oldId, webdav, forceRelease, isVersioned) {
      if(isVersioned === true) {
        $("#simple_fields_attachment-checkin").hide();
        $("#versioned_fields_attachment-checkin").show();
      }else {
        $("#versioned_fields_attachment-checkin").hide();
        $("#simple_fields_attachment-checkin").show();
      }
      $("#dialog-attachment-checkin").data("attachmentId", id).data("oldId", oldId).data("webdav", webdav).data("forceRelease", forceRelease).dialog("open");
      pageMustBeReloadingAfterSorting = true;
    }

    function menuCheckin(id) {
      var oMenu = eval("oMenu" + id);
      oMenu.getItem(3).cfg.setProperty("disabled", true);
      oMenu.getItem(0).cfg.setProperty("disabled", false);
      oMenu.getItem(1).cfg.setProperty("disabled", false);
      oMenu.getItem(2).cfg.setProperty("disabled", false);
      oMenu.getItem(3, 1).cfg.setProperty("disabled", false)
      oMenu.getItem(2, 1).cfg.setProperty("disabled", false);
      var $worker = $('#worker' + id);
      $worker.html("");
      $worker.css({'visibility':'hidden'});
      if (pageMustBeReloadingAfterSorting) {
        reloadIncludingPage();
      }
    }

    function notifyAttachment(attachmentId) {
      alertUsersAttachment(attachmentId); //dans publication.jsp
    }

    function addAttachment(foreignId) {
      $("#add-attachment-form #foreignId").val(foreignId);
      $("#dialog-attachment-add").dialog("open");
    }

    function deleteAttachment(id, filename) {
      $("#attachment-delete-warning-message").html('<fmt:message key="attachment.suppressionConfirmation" /> <b>' + filename + '</b> ?');
      $("#dialog-attachment-delete").data("id", id).data("filename", filename).dialog("open");
    }

    function deleteContent(id, lang) {
      var deleteUrl = '<c:url value="/services/documents/${sessionScope.Silverpeas_Attachment_ComponentId}/document/"/>' + id + '/content/' + lang;
      $.ajax({
        url: deleteUrl,
        type: "DELETE",
        cache: false,
        success: function(data) {}
      });
    }

    function removeAttachment(attachmentId) {
      var sLanguages = "";
      var boxItems = document.removeForm.languagesToDelete;
      if (boxItems != null){
        //at least one checkbox exists
        var nbBox = boxItems.length;
        if ((nbBox == null) && (boxItems.checked)) {
          sLanguages += boxItems.value + ",";
        } else {
          for (var i = 0; i < boxItems.length; i++) {
            if (boxItems[i].checked){
              sLanguages += boxItems[i].value + ",";
            }
          }
        }
      }

      $.post('<c:url value="/Attachment" />', { id:attachmentId, Action:'Delete', languagesToDelete:sLanguages}, function(data){
        data = data.replace(/^\s+/g, '').replace(/\s+$/g, '');
        if (data == "attachmentRemoved") {
          $('#attachment_' + attachmentId).remove();
        } else {
          if (data == "translationsRemoved") {
            reloadIncludingPage();
          }
        }
        closeMessage();
      }, 'text');
      pageMustBeReloadingAfterSorting = true;
    }

    function reloadIncludingPage() {
      <c:choose>
        <c:when test="${! silfn:isDefined(param.CallbackUrl)}">document.location.reload();</c:when>
        <c:otherwise>document.location.href = "<c:url value="${param.CallbackUrl}" />";</c:otherwise>
      </c:choose>
    }

    function updateAttachment(attachmentId, lang) {
      loadAttachment(attachmentId, lang);
      $("#dialog-attachment-update").data('attachmentId', attachmentId).dialog("open");
    }

    <c:if test="${useXMLForm}">
      function EditXmlForm(id, lang) {
        var url = '<c:url value="/RformTemplate/jsp/Edit"><c:param name="IndexIt" value="${indexIt}" /><c:param name="ComponentId" value="${param.ComponentId}" /><c:param name="type" value="Attachment" /><c:param name="ObjectType" value="Attachment" /><c:param name="XMLFormName" value="${xmlForm}" /></c:url>&ReloadOpener=true&ObjectLanguage=' + lang + '&ObjectId=' + id;
        SP_openWindow(url, "test", "600", "400", "scrollbars=yes, resizable, alwaysRaised");
      }
    </c:if>

    function closeMessage() {
      $("#attachmentModalDialog").dialog("close");
    }

    function displayWarning(attachmentId) {
      var url = '<c:url value="/attachment/jsp/warning_locked.jsp" />' + '?id=' + attachmentId;
      $("#attachmentModalDialog").dialog("open").load(url);
    }

    $(document).ready(function() {
      $("#fileLang").on("change", function (event) {
        $("#fileLang option:selected").each(function () {
          loadAttachment($("#attachmentId").val(), $(this).val());
        });
    });

    $('#update-attachment-form').iframePostForm ({
      json : true,
      post : function () {
      },
      complete : function (response) {
        reloadIncludingPage();
        $(this).dialog("close");
      }
    });

    $('#add-attachment-form').iframePostForm ({
      json : true,
      post : function () {
      },
      complete : function (response) {
        reloadIncludingPage();
        $(this).dialog("close");
      }
    });

    $('#checkin-attachment-form').iframePostForm ({
      json : true,
      post : function () {},
      complete : function (response) {
        if (response.status) {
          menuCheckin(response.id);
        } else {
          displayWarning(response.attachmentId);
        }
        reloadIncludingPage();
      }
    });

    $("#dialog-attachment-delete").dialog({
      autoOpen: false,
      open:function() {
        var filename = $(this).data("filename");
        $("#button-delete-all").hide();
      <c:if test="${silfn:isI18n() && not isVersionActive && not view:booleanValue(param.notI18n)}">
        var translationsUrl = '<c:url value="/services/documents/${sessionScope.Silverpeas_Attachment_ComponentId}/document/"/>' + $(this).data("id") + '/translations';
        $.ajax({
          url: translationsUrl,
          type: "GET",
          async: false,
          cache: false,
          success: function(data) {
            if (data.length > 1) {
              $("#attachment-delete-warning-message").html('<fmt:message key="attachment.suppressionWhichTranslations" />');
              for(var i = 0 ; i < data.length; i++) {
                $("#delete-language-" + data[i].lang).show();
              }
              $("#attachment-delete-select-lang").show();
              $("#button-delete-all").show();
            } else {
              $("#attachment-delete-select-lang").hide();
              $("#attachment-delete-warning-message").html('<fmt:message key="attachment.suppressionConfirmation" /> <b>' + filename + '</b> ?');
              $("#button-delete-content").hide();
              $("#button-delete-all").show();
            }
          },
          error: function(jqXHR, textStatus, errorThrown) {
            alert(jqXHR.responseText + ' : ' + textStatus + ' :' + errorThrown);
          }
        });</c:if>
      },
      title: '<fmt:message key="attachment.dialog.delete" />',
      height: 'auto',
      width: 400,
      modal: true,
      buttons: {
        '<fmt:message key="GML.delete"/>': {
          id: "button-delete-content",
          text: "<fmt:message key="GML.delete"/>",
          click: function() {
            var attachmentId = $(this).data("id");
            <c:choose>
              <c:when test="${silfn:isI18n() && not isVersionActive && not view:booleanValue(param.notI18n)}">
                $("input[name='languagesToDelete']").filter(':checked').each(function() {
                  var deleteUrl = '<c:url value="/services/documents/${sessionScope.Silverpeas_Attachment_ComponentId}/document/"/>' + attachmentId + '/content/' + this.value;
                  $.ajax({
                    url: deleteUrl,
                    type: "DELETE",
                    cache: false,
                    async: false,
                    success: function(data) {},
                    error: function(jqXHR, textStatus, errorThrown) {
                      alert(jqXHR.responseText + ' : ' + textStatus + ' :' + errorThrown);
                    }
                  });
                });
                reloadIncludingPage();
                $("#dialog-attachment-delete").dialog("close");
              </c:when>
              <c:otherwise>
                  var deleteUrl = '<c:url value="/services/documents/${sessionScope.Silverpeas_Attachment_ComponentId}/document/"/>' + attachmentId;
                  $.ajax({
                    url: deleteUrl,
                    type: "DELETE",
                    cache: false,
                    async: false,
                    success: function(data) {
                      reloadIncludingPage();
                      $(this).dialog("close");
                    },
                    error: function(jqXHR, textStatus, errorThrown) {
                      alert(jqXHR.responseText + ' : ' + textStatus + ' :' + errorThrown);
                    }
                  });
                </c:otherwise>
              </c:choose>
           }
        },
        '<fmt:message key="attachment.dialog.button.deleteAll"/>': {
          id: "button-delete-all",
          text: "<fmt:message key="attachment.dialog.button.deleteAll"/>",
          click: function() {
            var attachmentId = $(this).data("id");
            var deleteUrl = '<c:url value="/services/documents/${sessionScope.Silverpeas_Attachment_ComponentId}/document/"/>' + attachmentId;
                $.ajax({
                  url: deleteUrl,
                  type: "DELETE",
                  cache: false,
                  async: false,
                  success: function(data) {
                    reloadIncludingPage();
                    $(this).dialog("close");
                  }
                });
          }
        },
        '<fmt:message key="GML.cancel"/>': function() {
          $(this).dialog("close");
        }
      },
      close: function() {
      }
    });

      $("#dialog-attachment-add").dialog({
        autoOpen: false,
        title: "<fmt:message key="attachment.dialog.add" />",
        height: 'auto',
        width: 550,
        modal: true,
        buttons: {
          '<fmt:message key="GML.ok"/>': function() {
              var filename =  $.trim( $("#file_create").val().split('\\').pop());
              if( filename === '') {
                return false;
              }
              var submitUrl = '<c:url value="/services/documents/${sessionScope.Silverpeas_Attachment_ComponentId}/document/create"/>';
              submitUrl = submitUrl + '/' +encodeURI(filename);
              $.progressMessage();
              if ("FormData" in window) {
                var formData = new FormData($("#add-attachment-form")[0]);
                $.ajax(submitUrl, {
	                processData: false,
	                contentType: false,
	                type: 'POST',
	                dataType: "json",
	                data: formData,
	                success:function(data) {
	                  reloadIncludingPage();
	                  $(this).dialog("close");
	                }
              	});
              } else {
                $('#add-attachment-form').attr('action', submitUrl);
                $('#add-attachment-form').submit();
              }},
           '<fmt:message key="GML.cancel"/>': function() {
              $(this).dialog("close");
           }
          }, close: function() {
          }
        });

      $("#dialog-attachment-update").dialog({
        autoOpen: false,
        title: "<fmt:message key="attachment.dialog.update" />",
        height: 'auto',
        width: 550,
        modal: true,
        buttons: {
          '<fmt:message key="GML.ok"/>': function() {
              var submitUrl = '<c:url value="/services/documents/${sessionScope.Silverpeas_Attachment_ComponentId}/document/"/>' + $(this).data('attachmentId');
              var filename =  $.trim( $("#file_upload").val().split('\\').pop());
              if( filename !== '') {
                submitUrl = submitUrl + '/' +encodeURI(filename);
              } else {
                submitUrl = submitUrl + '/no_file';
              }
              $.progressMessage();
              if ("FormData" in window) {
                var formData = new FormData($("#update-attachment-form")[0]);
                $.ajax(submitUrl, {
                processData: false,
                contentType: false,
                type: 'POST',
                dataType: "json",
                data: formData,
                success:function(data) {
                  reloadIncludingPage();
                  $(this).dialog("close");
                }
              });
            } else {
              $('#update-attachment-form').attr('action', submitUrl);
              $('#update-attachment-form').submit();
            } },
          <c:if test="${silfn:isI18n() && not isVersionActive && not view:booleanValue(param.notI18n)}">
          '<fmt:message key="attachment.dialog.delete.lang"/>': function() {
          if(confirm('<fmt:message key="attachment.suppressionConfirmation" />')) {
            $.ajax({
                url: '<c:url value="/services/documents/${sessionScope.Silverpeas_Attachment_ComponentId}/document/"/>' + $(this).data('attachmentId') + '/content/' + $("#fileLang").val(),
                type: "DELETE",
                contentType: "application/json",
                dataType: "json",
                class: "deleteLang",
                cache: false,
                success: function(data) {
                  reloadIncludingPage();
                  $(this).dialog("close");
                }
              });
              $(this).dialog("close");
              } },
          </c:if>
            '<fmt:message key="GML.cancel"/>': function() {
              $(this).dialog("close");
            }
          },
          close: function() {
          }
        });

        $("#attachmentList").sortable({opacity: 0.4, axis: 'y', cursor: 'move', placeholder: 'ui-state-highlight', forcePlaceholderSize: true});
        $("#attachmentModalDialog").dialog({
          autoOpen: false,
          modal: true,
          title: '<fmt:message key="attachment.dialog.delete" />' ,
          height: 'auto',
          width: 550
        });

        function submitCheckin(submitUrl) {
            $.ajax(submitUrl, {
              type: 'POST',
              dataType: "json",
              data: $("#checkin-attachment-form").serialize(),
              success:function(result) {
                reloadIncludingPage();
                $(this).dialog("close");
              },
              error: function(jqXHR, textStatus, errorThrown) {
                alert(jqXHR.responseText + ' : ' + textStatus + ' :' + errorThrown);
              }
            });
        }

        $("#dialog-attachment-switch").dialog({
        autoOpen: false,
        title: "<fmt:message key="attachment.dialog.switch"/>",
        height: 'auto',
        width: 550,
        modal: true,
        buttons: {
          '<fmt:message key="GML.ok"/>': function() {
            var submitUrl = '<c:url value="/services/documents/${sessionScope.Silverpeas_Attachment_ComponentId}/document/"/>' + $("#dialog-attachment-switch").data('id') + '/switchState';
            $.ajax(submitUrl, {
              type: 'PUT',
              dataType: "json",
              data: $("#attachment-switch-form").serialize(),
              success:function(result) {
                reloadIncludingPage();
                $(this).dialog("close");
              },
              error: function(jqXHR, textStatus, errorThrown) {
                alert(jqXHR.responseText + ' : ' + textStatus + ' :' + errorThrown);
              }
            });
          },
          '<fmt:message key="GML.cancel"/>': function() {
            $(this).dialog("close");
          }
        }
      });

        $("#dialog-attachment-checkin").dialog({
        autoOpen: false,
        title: "<fmt:message key="attachment.dialog.checkin"/>",
        height: 'auto',
        width: 550,
        modal: true,
        buttons: {
          '<fmt:message key="GML.ok"/>': function() {
            $('#force').val($("#dialog-attachment-checkin").data('forceRelease'));
            $('#webdav').val($("#dialog-attachment-checkin").data('webdav'));
            $('#checkin_oldId').val($("#dialog-attachment-checkin").data('oldId'));
            var submitUrl = '<c:url value="/services/documents/${sessionScope.Silverpeas_Attachment_ComponentId}/document/"/>' + $(this).data('attachmentId') + '/unlock';
            submitCheckin(submitUrl);
          },
          '<fmt:message key="attachment.revert"/>': function() {
              $('#force').val('true');
              $('#webdav').val('false');
              var submitUrl = '<c:url value="/services/documents/${sessionScope.Silverpeas_Attachment_ComponentId}/document/"/>' + $(this).data('attachmentId') + '/unlock';
              submitCheckin(submitUrl);
            },
            '<fmt:message key="GML.cancel"/>': function() {
              clearCheckin();
              $(this).dialog("close");
            }
          },
          close: function() {
            clearCheckin();
          }
        });


      $('#attachmentList').bind('sortupdate', function(event, ui) {
        var reg = new RegExp("attachment", "g");
        var data = $('#attachmentList').sortable('serialize');
        data += "#";
        var tableau = data.split(reg);
        var param = "";
        for (var i = 0; i < tableau.length; i++) {
          if (i != 0) {
            param += ",";
          }
          param += tableau[i].substring(3, tableau[i].length - 1);
        }
        sortAttachments(param);
      });

      });


      function sortAttachments(orderedList) {
        $.post('<c:url value="/Attachment" />', { orderedList:orderedList, Action:'Sort'}, function(data){
          data = data.replace(/^\s+/g, '').replace(/\s+$/g, '');
          if (data == "error") {
            alert("Une erreur s'est produite !");
          }
        }, 'text');
        if (pageMustBeReloadingAfterSorting) {
          reloadIncludingPage();
        }
      }

      function uploadCompleted(s) {
        reloadIncludingPage();
      }

      function ShareAttachment(id) {
        var url = '<c:url value="/RfileSharing/jsp/NewTicket"><c:param name="componentId" value="${param.ComponentId}" /><c:param name="type" value="Attachment" /></c:url>&objectId=' + id;
        SP_openWindow(url, "NewTicket", "700", "300", "scrollbars=no, resizable, alwaysRaised");
      }
  </c:if>

  function displayAttachment(attachment) {
    $('#fileName').text(attachment.fileName);
    $('#fileTitle').val(attachment.title);
    $('#fileDescription').val(attachment.description);
    if(attachment.versioned === 'true') {
       $('#fileName_label').text('<fmt:message key="attachment.version.actual" />');
       $('#file_upload_label').text('<fmt:message key="attachment.version.new" />');
       $('#versioned_fields_attachment-update').show();
    } else {
      $('#versioned_fields_attachment-update').hide();
      $('#fileName_label').text('<fmt:message key="GML.file"/>');
      $('#file_upload_label').text('<fmt:message key="fichierJoint" />');
    }
  }

  function clearAttachment() {
    $('#fileName').html('');
    $('#fileTitle').val('');
    $('#fileDescription').val('');
    $('#versioned_fields_attachment-update').hide();
  }

  function clearCheckin() {
    $('#checkin_oldId').val('');
    $('#force').val('false');
    $('#webdav').val('false');
    $('#comment').val('false');
  }

  function loadAttachment(id, lang) {
    var translationsUrl = '<c:url value="/services/documents/${sessionScope.Silverpeas_Attachment_ComponentId}/document/"/>' + id + '/translations';
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

  function preview(target, attachmentId) {
    $(target).preview("previewAttachment", {
      componentInstanceId: '<c:out value="${sessionScope.Silverpeas_Attachment_ComponentId}" />',
      attachmentId: attachmentId
    });
    return false;
  }

  function view(target, attachmentId) {
    $(target).view("viewAttachment", {
      componentInstanceId: "<c:out value="${sessionScope.Silverpeas_Attachment_ComponentId}" />",
      attachmentId: attachmentId
    });
    return false;
  }
</script>

<div id="dialog-attachment-update" style="display:none">
  <form name="update-attachment-form" id="update-attachment-form" method="post" enctype="multipart/form-data;charset=utf-8" accept-charset="UTF-8" target="iframe-post-form">
    <input type="hidden" name="IdAttachment" id="attachmentId"/>
        <c:if test="${silfn:isI18n() && not view:booleanValue(param.notI18n) }">
          <label for="langCreate" class="label-ui-dialog"><fmt:message key="GML.language"/></label>
          <span class="champ-ui-dialog"><view:langSelect elementName="fileLang" elementId="fileLang" langCode="${contentLanguage}" includeLabel="false" /></span>
        </c:if>
        <label id="fileName_label" for="fileName" class="label-ui-dialog"><fmt:message key="GML.file" /></label>
        <span id="fileName" class="champ-ui-dialog"></span>

        <label id="file_upload_label" for="file_upload" class="label-ui-dialog"><fmt:message key="fichierJoint" /></label>
        <span class="champ-ui-dialog"><input type="file" name="file_upload" size="50" id="file_upload" /></span>

        <label for="fileTitle" class="label-ui-dialog"><fmt:message key="Title"/></label>
        <span class="champ-ui-dialog"><input type="text" name="fileTitle" size="60" id="fileTitle" /></span>

        <label for="fileDescription" class="label-ui-dialog"><fmt:message key="GML.description" /></label>
        <span class="champ-ui-dialog"><textarea name="fileDescription" cols="60" rows="3" id="fileDescription"></textarea></span>

        <div id="versioned_fields_attachment-update" style="display:none">
          <label for="versionType" class="label-ui-dialog"><fmt:message key="attachment.version.label"/></label>
          <span class="champ-ui-dialog"><input value="0" type="radio" name="versionType" id="versionType" checked="checked"/><fmt:message key="attachment.version_public.label"/>
          <input value="1" type="radio" name="versionType" id="versionType"/><fmt:message key="attachment.version_wip.label"/></span>

          <label for="commentMessage" class="label-ui-dialog"><fmt:message key="attachment.dialog.comment"/></label>
          <span class="champ-ui-dialog"><textarea name="commentMessage" cols="60" rows="3" id="commentMessage"></textarea></span>
        </div>
    <input type="submit" value="Submit" style="display:none" />
  </form>
</div>

<div id="dialog-attachment-add" style="display:none">
  <form name="add-attachment-form" id="add-attachment-form" method="post" enctype="multipart/form-data;charset=utf-8" accept-charset="UTF-8" target="iframe-post-form">
    <input type="hidden" name="foreignId" id="foreignId" value="<c:out value="${sessionScope.Silverpeas_Attachment_ObjectId}" />" />
    <input type="hidden" name="indexIt" id="indexIt" value="<c:out value="${indexIt}" />" />
    <c:if test="${silfn:isI18n() && not isVersionActive && not view:booleanValue(param.notI18n)}">
      <label for="langCreate" class="label-ui-dialog"><fmt:message key="GML.language"/></label>
      <span class="champ-ui-dialog"><view:langSelect elementName="fileLang" elementId="langCreate" langCode="${contentLanguage}" includeLabel="false"/></span>
    </c:if>
    <label for="file_create" class="label-ui-dialog"><fmt:message key="fichierJoint"/></label>
    <span class="champ-ui-dialog"><input type="file" name="file_upload" size="50" id="file_create" /></span>
    <label for="fileTitleCreate" class="label-ui-dialog"><fmt:message key="Title"/></label>
    <span class="champ-ui-dialog"><input type="text" name="fileTitle" size="60" id="fileTitleCreate" /></span>
    <label for="fileDescriptionCreate" class="label-ui-dialog"><fmt:message key="GML.description" /></label>
    <span class="champ-ui-dialog"><textarea name="fileDescription" rows="3" id="fileDescriptionCreate"></textarea></span>
    <c:if test="${isVersionActive}">
      <label for="versionType" class="label-ui-dialog"><fmt:message key="attachment.version.label"/></label>
      <span class="champ-ui-dialog"><input value="0" type="radio" name="versionType" id="typeVersionPublic" checked="checked"/><fmt:message key="attachment.version_public.label"/>
      <input value="1" type="radio" name="versionType" id="typeVersionPrivate"/><fmt:message key="attachment.version_wip.label"/></span>
      <label for="commentMessage" class="label-ui-dialog"><fmt:message key="attachment.dialog.comment"/></label>
      <span class="champ-ui-dialog"><textarea name="commentMessage" cols="60" rows="3" id="commentMessage"></textarea></span>
    </c:if>
    <input type="submit" value="Submit" style="display:none" />
  </form>
</div>

<div id="dialog-attachment-delete" style="display:none">
  <span id="attachment-delete-warning-message"><fmt:message key="attachment.suppressionConfirmation" /></span>
    <c:if test="${silfn:isI18n() && not view:booleanValue(param.notI18n)}">
      <div id="attachment-delete-select-lang" style="display:none">
        <div id="languages">
          <c:forEach items="<%=com.silverpeas.util.i18n.I18NHelper.getAllSupportedLanguages()%>" var="supportedLanguage">
            <span id='delete-language-<c:out value="${supportedLanguage}"/>' style="display:none"><input type="checkbox" id='<c:out value="${supportedLanguage}"/>ToDelete' name="languagesToDelete" value='<c:out value="${supportedLanguage}"/>'/><c:out value="${silfn:i18nLanguageLabel(supportedLanguage, sessionScope.SilverSessionController.favoriteLanguage)}"/></span>
          </c:forEach>
        </div>
      </div>
    </c:if>
</div>

  <div id="dialog-attachment-switch" style="display:none">
    <p id="attachment-switch-warning-message">Prout</p>
    <form name="attachment-switch-form" id="attachment-switch-form" method="put" accept-charset="UTF-8">
      <div id="attachment-switch-simple" style="display:none">
        <label for="switch-version-major" class="label-ui-dialog"><fmt:message key="attachment.switch.version.major" /></label>
        <span id="attachment-switch-major" class="champ-ui-dialog"><input value="lastMajor" type="radio" name="switch-version" id="switch-version-major" checked="checked"/></span>
        <label for="switch-version-last" class="label-ui-dialog"><fmt:message key="attachment.switch.version.last" /></label>
        <span id="attachment-switch-last" class="champ-ui-dialog"><input value="last" type="radio" name="switch-version" id="switch-version-last"/></span>
      </div>
      <div id="attachment-switch-versioned" style="display:none">
       <label for="switch-version-comment" class="label-ui-dialog"><fmt:message key="attachment.dialog.comment" /></label>
      <span class="champ-ui-dialog"><textarea name="switch-version-comment" cols="60" rows="3" id="switch-version-comment"></textarea></span>
      </div>
      <input type="submit" value="Submit" style="display:none" />
    </form>
  </div>

 <div id="dialog-attachment-checkin" style="display:none">
  <form name="checkin-attachment-form" id="checkin-attachment-form" method="post" accept-charset="UTF-8" target="iframe-post-form">
    <input type="hidden" name="checkin_oldId" id="checkin_oldId" value="-1" />
    <input type="hidden" name="force" id="force" value="false" />
    <input type="hidden" name="webdav" id="webdav" value="false" />
    <div id="versioned_fields_attachment-checkin" style="display:none">
      <label for="private" class="label-ui-dialog"><fmt:message key="attachment.version.label"/></label>
      <span class="champ-ui-dialog"><input value="false" type="radio" name="private" id="private" checked="checked"/><fmt:message key="attachment.version_public.label"/>
        <input value="true" type="radio" name="private" id="private"/><fmt:message key="attachment.version_wip.label"/></span>

      <label for="comment" class="label-ui-dialog"><fmt:message key="attachment.dialog.comment" /></label>
      <span class="champ-ui-dialog"><textarea name="comment" cols="60" rows="3" id="comment"></textarea></span>
    </div>
    <div id="simple_fields_attachment-checkin" style="display:none"><fmt:message key="confirm.checkin.message" /></div>
    <input type="submit" value="Submit" style="display:none" />
  </form>
</div>

<view:progressMessage/>
