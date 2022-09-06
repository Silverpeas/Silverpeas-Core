<%--

    Copyright (C) 2000 - 2022 Silverpeas

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    As a special exception to the terms and conditions of version 3.0 of
    the GPL, you may redistribute this Program in connection with Free/Libre
    Open Source Software ("FLOSS") applications as described in Silverpeas's
    FLOSS exception.  You should have received a copy of the text describing
    the FLOSS exception, and it is also available here:
    "https://www.silverpeas.org/legal/floss_exception.html"

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
<%@ taglib tagdir="/WEB-INF/tags/silverpeas/util" prefix="viewTags" %>
<%@ page errorPage="../../admin/jsp/errorpage.jsp"%>
<%@ page import="org.silverpeas.core.ResourceReference" %>
<%@ page import="org.silverpeas.core.admin.user.model.SilverpeasRole" %>
<%@ page import="org.silverpeas.core.contribution.attachment.AttachmentServiceProvider" %>
<%@ page import="org.silverpeas.core.contribution.attachment.model.DocumentType" %>
<%@ page import="org.silverpeas.core.contribution.attachment.model.SimpleDocument" %>
<%@ page import="org.silverpeas.core.i18n.I18NHelper" %>
<%@ page import="org.silverpeas.core.web.mvc.controller.ComponentContext" %>
<%@ page import="org.silverpeas.web.attachment.VersioningSessionController" %>
<%@ page import="java.util.Objects" %>
<%@ page import="java.util.stream.Collectors" %>

<%@ include file="checkAttachment.jsp"%>

<fmt:setLocale value="${requestScope.resources.language}"/>
<view:setBundle basename="org.silverpeas.util.attachment.multilang.attachment" />
<c:url var="mandatoryFieldUrl" value="/util/icons/mandatoryField.gif"/>
<view:componentParam var="publicationAlwaysVisiblePramValue" componentId="${param.ComponentId}" parameter="publicationAlwaysVisible" />
<view:componentParam var="isComponentVersioned" componentId="${param.ComponentId}" parameter="versionControl" />
<c:set var="isPublicationAlwaysVisible" value="${silfn:booleanValue(publicationAlwaysVisiblePramValue)}" />
<c:set var="isVersionActive" value="${not isPublicationAlwaysVisible and silfn:booleanValue(isComponentVersioned)}" />
<view:includePlugin name="qtip"/>
<view:includePlugin name="iframeajaxtransport"/>
<view:includePlugin name="popup"/>
<view:includePlugin name="preview"/>
<c:if test="${isVersionActive}">
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
<view:script src="/util/yui/yahoo-dom-event/yahoo-dom-event.js"/>
<view:script src="/util/yui/container/container_core-min.js"/>
<view:script src="/util/yui/animation/animation-min.js"/>
<view:script src="/util/yui/menu/menu-min.js"/>
<view:script src="/util/javaScript/jquery/jquery.cookie.js"/>
<view:link href="/util/yui/menu/assets/menu.css"/>

  <view:settings var="spinfireViewerEnable" settings="org.silverpeas.util.attachment.Attachment" defaultValue="${false}" key="SpinfireViewerEnable" />
  <view:setConstant var="spinfire" constant="org.silverpeas.core.util.MimeTypes.SPINFIRE_MIME_TYPE" />
  <c:set var="mainSessionController" value="<%=m_MainSessionCtrl%>" />
  <view:settings var="onlineEditingEnable" settings="org.silverpeas.util.attachment.Attachment" defaultValue="${false}" key="OnlineEditingEnable" />
  <view:settings var="dAndDropEnable" settings="org.silverpeas.util.attachment.Attachment" defaultValue="${false}" key="DragAndDropEnable" />
  <view:settings var="onlineEditingWithCustomProtocolAlert" settings="org.silverpeas.util.attachment.Attachment" defaultValue="${true}" key="attachment.onlineEditing.customProtocol.alert" />
  <c:set var="webdavEditingEnable" value="${mainSessionController.webDAVEditingEnabled && onlineEditingEnable}" />
  <c:set var="dndDisabledLocally" value="${silfn:isDefined(param.dnd) and not silfn:booleanValue(param.dnd)}" scope="page"/>
  <c:set var="dragAndDropEnable" value="${mainSessionController.dragNDropEnabled && dAndDropEnable && not dndDisabledLocally}" />

  <c:set var="handledSubscriptionType" value="${param.HandledSubscriptionType}"/>
  <c:set var="handledSubscriptionResourceId" value="${param.HandledSubscriptionResourceId}"/>
  <c:set var="handledSubscriptionLocationId" value="${param.HandledSubscriptionLocationId}"/>
  <c:set var="isHandledSubscriptionConfirmation"
         value="${not empty handledSubscriptionType and not empty handledSubscriptionResourceId}"/>
  <c:set var="isHandledModificationContext"
         value="${silfn:booleanValue(param.HandledContributionModificationContext)}"/>

  <view:componentParam var="commentActivated" componentId="${param.ComponentId}" parameter="tabComments"/>
  <c:if test="${not silfn:booleanValue(commentActivated)}">
    <view:componentParam var="commentActivated" componentId="${param.ComponentId}" parameter="comments"/>
  </c:if>

  <c:set var="fromAlias" value="${silfn:isDefined(param.Alias)}" />
  <jsp:useBean id="fromAlias" type="java.lang.Boolean"/>
  <c:set var="userProfile" value="${fn:toLowerCase(param.Profile)}" scope="page"/>
  <c:if test="${empty userProfile or fromAlias}">
    <c:set var="userProfile" value="user"/>
  </c:if>
  <jsp:useBean id="userProfile" type="java.lang.String"/>
  <c:set var="highestUserRole" value='<%=SilverpeasRole.fromString(request.getParameter("Profile"))%>' scope="page"/>
  <c:set var="contextualMenuEnabled" value="${'admin' eq userProfile || 'publisher' eq userProfile || 'writer' eq userProfile}" scope="page" />
  <view:componentParam var="xmlForm" componentId="${param.ComponentId}" parameter="XmlFormForFiles" />
  <c:choose>
    <c:when test="${contextualMenuEnabled}">
      <c:set var="iconStyle" scope="page">style="cursor:move"</c:set>
    </c:when>
    <c:otherwise>
      <c:set var="iconStyle" scope="page" value="${''}" />
    </c:otherwise>
  </c:choose>
  <c:choose>
    <c:when test="${silfn:isDefined(param.AttachmentPosition)}">
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
    <c:when test="${silfn:isDefined(param.ShowTitle)}">
      <c:set var="showTitle" scope="page" value="${silfn:booleanValue(param.ShowTitle)}" />
    </c:when>
    <c:otherwise>
      <c:set var="showTitle" scope="page" value="${true}" />
    </c:otherwise>
  </c:choose>
  <c:choose>
    <c:when test="${silfn:isDefined(param.ShowFileSize)}">
      <c:set var="showFileSize" scope="page" value="${silfn:booleanValue(param.ShowFileSize)}" />
    </c:when>
    <c:otherwise>
      <c:set var="showFileSize" scope="page" value="${true}" />
    </c:otherwise>
  </c:choose>
  <c:choose>
    <c:when test="${silfn:isDefined(param.ShowInfo)}">
      <c:set var="showInfo" scope="page" value="${silfn:booleanValue(param.ShowInfo)}" />
    </c:when>
    <c:otherwise>
      <c:set var="showInfo" scope="page" value="${true}" />
    </c:otherwise>
  </c:choose>
  <c:choose>
    <c:when test="${silfn:isDefined(param.ShowIcon)}">
      <c:set var="showIcon" scope="page" value="${silfn:booleanValue(param.ShowIcon)}" />
    </c:when>
    <c:otherwise>
      <c:set var="showIcon" scope="page" value="${true}" />
    </c:otherwise>
  </c:choose>
  <c:set var="aliasContext" value="${param.Alias}" />
  <c:set var="useXMLForm" value="${silfn:isDefined(xmlForm)}" />
  <c:set var="indexIt" value="${silfn:booleanValue(param.IndexIt)}" />
  <c:set var="showMenuNotif" value="${silfn:booleanValue(param.ShowMenuNotif)}" />
  <c:set var="displayUniversalLinks"><%=URLUtil.displayUniversalLinks()%></c:set>

  <c:set var="domIdSuffix" value="${silfn:formatForDomId(param.Id)}"/>
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
  <c:set var="_isI18nHandled" value="${silfn:isI18n() && silfn:isDefined(contentLanguage)}" />
<%
  final String userProfileForLambda = userProfile;
  final List<SimpleDocument> attachments = AttachmentServiceProvider.getAttachmentService().
          listDocumentsByForeignKeyAndType(new ResourceReference(request.getParameter("Id"), request.getParameter("ComponentId")),
          DocumentType.valueOf((String)session.getAttribute("Silverpeas_Attachment_Context")),
          (String) pageContext.getAttribute("contentLanguage")).stream()
      .map(a -> {
        if (a.isVersioned() && SilverpeasRole.USER.getName().equals(userProfileForLambda)) {
          return a.getLastPublicVersion();
        }
        return a;
      })
      .filter(Objects::nonNull)
      .collect(Collectors.toList());
  pageContext.setAttribute("attachments", attachments);
%>

<fmt:message key="attachment.suppressionConfirmation" var="deleteConfirmMsg" />
<fmt:message key="attachment.dialog.delete" var="deleteFileMsg"/>
<fmt:message key="attachment.dialog.delete.lang" var="deleteFileLangMsg"/>

<c:if test="${!empty pageScope.attachments || 'user' != userProfile}">
<div id="attachmentDragAndDrop${domIdSuffix}" class="attachments bgDegradeGris">
  <div class="bgDegradeGris header"><h4 class="clean"><fmt:message key="GML.attachments" /></h4></div>
  <c:if test="${contextualMenuEnabled}">
  <div class="attachment-creation-actions"><a class="menubar-creation-actions-item menubar-creation-actions-move-ignored" href="javascript:_afManager${domIdSuffix}.addAttachment('<c:out value="${sessionScope.Silverpeas_Attachment_ObjectId}" />');"><span><img alt="" src="<c:url value="/util/icons/create-action/add-file.png" />"/><fmt:message key="attachment.add"/></span></a></div>
  </c:if>
    <ul id="attachmentList${domIdSuffix}" class="attachmentList">
      <c:forEach items="${pageScope.attachments}" var="currentAttachment" >
        <%-- Download variable handling --%>
        <c:set var="canUserDownloadFile" value="${true}"/>
        <c:set var="forbiddenDownloadClass" value=""/>
        <fmt:message key="GML.download.forbidden.readers" var="forbiddenDownloadHelp"/>
        <c:if test="${!currentAttachment.downloadAllowedForReaders}">
          <c:set var="canUserDownloadFile" value="${currentAttachment.isDownloadAllowedForRolesFrom(mainSessionController.currentUserDetail)}"/>
          <c:if test="${!canUserDownloadFile}">
            <c:set var="forbiddenDownloadClass" value="forbidden-download"/>
            <fmt:message key="GML.download.forbidden" var="forbiddenDownloadHelp"/>
          </c:if>
        </c:if>

        <c:if test="${isAttachmentPositionRight}">
          <li id='attachment_<c:out value="${currentAttachment.oldSilverpeasId}"/>' class='attachmentListItem' <c:out value="${iconStyle}" escapeXml="false"/> >
        </c:if>
        <menu:simpleDocument attachment="${currentAttachment}"
                             showMenuNotif="${showMenuNotif}"
                             useWebDAV="${webdavEditingEnable}"
                             useXMLForm="${useXMLForm}"
                             fromAlias="${fromAlias}"
                             userRole="${highestUserRole}"/>
        <span class="lineMain ${forbiddenDownloadClass}">
            <img alt="" id='edit_<c:out value="${currentAttachment.oldSilverpeasId}"/>' src='<c:url value="/util/icons/arrow/menuAttachment.gif" />' class="moreActions"/>
            <c:if test="${showIcon}">
              <img alt="" id='img_<c:out value="${currentAttachment.oldSilverpeasId}"/>' src='<c:out value="${currentAttachment.displayIcon}" />' class="icon" />
            </c:if>
            <c:if test="${_isI18nHandled}">
              <span class="">[${currentAttachment.language}]</span>
            </c:if>
            <c:choose>
              <c:when test="${! silfn:isDefined(currentAttachment.title) || ! showTitle}">
                <c:set var="title" value="${currentAttachment.filename}" />
              </c:when>
              <c:otherwise>
                <c:set var="title" value="${silfn:escapeHtml(currentAttachment.title)}" />
              </c:otherwise>
            </c:choose>
            <c:choose>
              <c:when test="${canUserDownloadFile}">
                <c:url var="attachmentUrl" value="${currentAttachment.attachmentURL}"/>
                <a id='url_<c:out value="${currentAttachment.oldSilverpeasId}"/>' href="${attachmentUrl}" target="_blank"><c:out value="${title}"/></a>
              </c:when>
              <c:otherwise>
                <c:out value="${title}"/>
              </c:otherwise>
            </c:choose>
            <c:if test="${currentAttachment.versioned}">
              &nbsp;<span class="version-number" id="version_<c:out value="${currentAttachment.oldSilverpeasId}"/>">v<c:out value="${currentAttachment.majorVersion}"/>.<c:out value="${currentAttachment.minorVersion}"/></span>
            </c:if>
          </span>
          <span class="lineSize">
            <c:if test="${displayUniversalLinks and canUserDownloadFile}">
              <c:set var="permalink" value="${currentAttachment.universalURL}"/>
              <c:if test="${fromAlias}">
                <c:set var="permalink" value="${currentAttachment.universalURL}&ComponentId=${aliasContext}"/>
              </c:if>
              <a href='<c:out value="${permalink}" escapeXml="false" />'><img src='<c:url value="/util/icons/link.gif"/>' border="0" alt="<fmt:message key="CopyLink"/>" title="<fmt:message key="CopyLink"/>"/></a>
            </c:if>
            <c:if test="${showFileSize}">
              <c:out value="${view:humanReadableSize(currentAttachment.size)}"/>
            </c:if>
               - <view:formatDateTime value="${currentAttachment.lastUpdateDate}"/>
            <c:if test="${silfn:isPreviewable(currentAttachment.attachmentPath)}">
              <img onclick="javascript:preview(this, '<c:out value="${currentAttachment.id}" />');" class="preview-file" src='<c:url value="/util/icons/preview.png"/>' alt="<fmt:message key="GML.preview.file"/>" title="<fmt:message key="GML.preview.file" />"/>
            </c:if>
            <c:if test="${silfn:isViewable(currentAttachment.attachmentPath)}">
              <img onclick="javascript:view(this, '<c:out value="${currentAttachment.id}" />');" class="view-file" src='<c:url value="/util/icons/view.png"/>' alt="<fmt:message key="GML.view.file"/>" title="<fmt:message key="GML.view.file" />"/>
            </c:if>
            <c:if test="${!currentAttachment.downloadAllowedForReaders}">
              <img class="forbidden-download-file" src='<c:url value="/util/icons/forbidden-download.png"/>' alt="${forbiddenDownloadHelp}" title="${forbiddenDownloadHelp}"/>
            </c:if>
          </span>
            <c:if test="${silfn:isDefined(currentAttachment.title) && showTitle}">
              <span class="fileName"><c:out value="${currentAttachment.filename}" /></span>
          </c:if>
            <c:if test="${silfn:isDefined(currentAttachment.description) && showInfo}">
              <span class="description"><view:encodeHtmlParagraph string="${currentAttachment.description}" /></span>
          </c:if>
            <c:if test="${silfn:isDefined(currentAttachment.xmlFormId)}">
              <a class="extraForm-file-more" rel='<c:url value="/RformTemplate/jsp/View">
                      <c:param name="width" value="400"/>
                      <c:param name="ObjectId" value="${currentAttachment.id}"/>
                      <c:param name="ObjectLanguage" value="${contentLanguage}"/>
                      <c:param name="ComponentId" value="${componentId}"/>
                      <c:param name="ObjectType" value="${'Attachment'}"/>
                      <c:param name="XMLFormName" value="${currentAttachment.xmlFormId}"/>
                    </c:url>' href="javascript:void(0)" title='<c:out value="${title}"/>' ><fmt:message key="attachment.xmlForm.View" /></a>
          </c:if>
          <view:componentParam var="hideAllVersionsLink" componentId="${param.ComponentId}" parameter="hideAllVersionsLink" />
          <c:set var="shouldHideAllVersionsLink" scope="page" value="${silfn:booleanValue(hideAllVersionsLink) && 'user' eq userProfile}" />
          <c:set var="shouldShowAllVersionLink" scope="page" value="${currentAttachment.versioned && ( ('user' eq userProfile && currentAttachment.public) || !('user' eq userProfile  || empty currentAttachment.functionalHistory))}" />
          <c:if test="${shouldShowAllVersionLink && !shouldHideAllVersionsLink}" >
              <span class="linkAllVersions">
                <img alt='<fmt:message key="allVersions" />' src='<c:url value="/util/icons/bullet_add_1.gif" />' /> <a href="javaScript:_afManager${domIdSuffix}.viewPublicVersions('<c:out value="${currentAttachment.id}" />')"><fmt:message key="allVersions" /></a>
              </span>
          </c:if>
          <c:if test="${contextualMenuEnabled}">
            <c:choose>
              <c:when test="${currentAttachment.edited}">
                <div class='workerInfo'  id='worker<c:out value="${currentAttachment.oldSilverpeasId}" />' style="visibility:visible"><fmt:message key="readOnly" /> <view:username zoom="false" userId="${currentAttachment.editedBy}" /> <fmt:message key="at" /> <view:formatDateTime value="${currentAttachment.reservation}" /></div>
              </c:when>
              <c:otherwise>
                <div class='workerInfo'  id='worker<c:out value="${currentAttachment.oldSilverpeasId}" />' style="visibility:hidden"> </div>
              </c:otherwise>
            </c:choose>
          </c:if>
                <c:if test="${spinfireViewerEnable && spinfire eq silfn:mimeType(currentAttachment.filename)}">
                  <div name="switchView" style="display: none">
            <a href="#" onClick="_afManager${domIdSuffix}.changeView3d('<c:out value="${currentAttachment.id}" />')"><img alt="iconeView<c:out value="${currentAttachment.id}" />" valign="top" border="0" src="<c:url value="/util/icons/masque3D.gif" />"></a>
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
        </c:forEach>
    </ul>
</div>
</c:if>

<script type="text/javascript">
if (!window.attachmentEventManager) {
  window.attachmentEventManager = {
    dispatchEvent : function(eventName, data) {
      document.body.dispatchEvent(new CustomEvent(eventName, {
        detail : {
          from : this,
          data : data
        },
        bubbles : true,
        cancelable : true
      }));
    }
  };

  String.prototype.format = function() {
    const args = arguments;
    return this.replace(/\{(\d+)\}/g, function(m, n) { return args[n]; });
  };

  jQuery(document).ajaxError(function(event, jqXHR, settings, errorThrown) {
    $.closeProgressMessage();
  });

  function addAttachment(foreignId) {
    _afManager${domIdSuffix}.addAttachment(foreignId);
  }

  function preview(target, attachmentId) {
    $(target).preview("previewAttachment", {
      componentInstanceId: '<c:out value="${sessionScope.Silverpeas_Attachment_ComponentId}" />',
      attachmentId: attachmentId,
      lang: '${contentLanguage}'
    });
    return false;
  }

  function view(target, attachmentId) {
    $(target).view("viewAttachment", {
      componentInstanceId: "<c:out value="${sessionScope.Silverpeas_Attachment_ComponentId}" />",
      attachmentId: attachmentId,
      lang: '${contentLanguage}'
    });
    return false;
  }
}

const _afManager${domIdSuffix} = new function() {
  const _self = this;
  const get$containerEl = function() { return jQuery('#attachmentList${domIdSuffix}') };
  const _self_ui = {
    get$addAttachment : function() { return $("#dialog-attachment-add${domIdSuffix}") },
    get$updateAttachment : function() { return $("#dialog-attachment-update${domIdSuffix}") },
    get$deleteAttachment : function() { return $("#dialog-attachment-delete${domIdSuffix}") },
    get$switchAttachmentState : function() { return $("#dialog-attachment-switch${domIdSuffix}") },
    get$onlineEditingCustomProtocol : function() { return $("#dialog-attachment-onlineEditing-customProtocol${domIdSuffix}") },
    get$attachmentCheckin : function() { return $("#dialog-attachment-checkin${domIdSuffix}") }
  };
  let publicVersionsWindow = window;
  <c:if test="${spinfireViewerEnable}">
  _self.changeView3d = function(objectId) {
    if (document.getElementById(objectId).style.display === 'none') {
      document.getElementById(objectId).style.display = '';
      eval("iconeView" + objectId).src = '<c:url value="/util/icons/visible3D.gif" />';
    } else {
      document.getElementById(objectId).style.display = 'none';
      eval("iconeView" + objectId).src = '<c:url value="/util/icons/masque3D.gif" />';
    }
  }
  </c:if>
  <c:url var="allVersionsUrl" value="/RVersioningPeas/jsp/ViewAllVersions">
    <c:param name="ComponentId" value="${componentId}" />
    <c:param name="fromAlias" value="${fromAlias}"/>
    <c:param name="Language" value="${contentLanguage}"/>
  </c:url>
  _self.viewPublicVersions = function(docId) {
    const url = '${allVersionsUrl}&DocId=' + docId;
    const windowName = "publicVersionsWindow";
    const windowParams = "directories=0,menubar=0,toolbar=0,scrollbars=1,alwaysRaised";
    if (!publicVersionsWindow.closed && publicVersionsWindow.name === "publicVersionsWindow") {
      publicVersionsWindow.close();
    }
    publicVersionsWindow = SP_openWindow(url, windowName, "800", "475", windowParams);
  }

  // Create the tooltips only on document load
  $(document).ready(function() {

    jQuery.fn.extend({
      findByName : function(name) {
        return this.find("[name='" + name + "']");
      },
      findByValue : function(value) {
        return this.find("[value='" + value + "']");
      }
    });

    // Use the each() method to gain access to each elements attributes
    get$containerEl().find('a[rel]').each(function() {
      const url = $(this).attr('rel');
      if (!url || !url.startsWith(webContext)) {
        return;
      }
      $(this).qtip({
        content : {
          // Set the text to an image HTML string with the correct src URL to the loading image you want to use
          text : '<img class="throbber" src="<c:url value="/util/icons/inProgress.gif" />" alt="Loading..." />',
          ajax: {
            url : url // Use the rel attribute of each element for the url to load
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
          classes : "qtip-shadow qtip-light qtip-attachment-extraForm"
        }
      })
    });

    // function to transform insecable string into secable one
    get$containerEl().find(".lineMain a").html(function() {
      const initialName = $(this).text();
      let maxLength = 38;
      let finalName = "";
      let currentPartLength = 0;
      for (let i = 0; i < initialName.length; i++) {
        const currentChar = initialName.charAt(i);
        if (currentChar === ' ' || currentChar === '-') {
          currentPartLength = 0;
        }
        if (currentPartLength > maxLength) {
          finalName += " ";
          currentPartLength = 0;
        }
        currentPartLength++;
        finalName += currentChar;
      }
      $(this).html(finalName);
    });
  });

  <c:if test="${contextualMenuEnabled}" >
    let pageMustBeReloadingAfterSorting = false;

    _self.checkout = function(id, oldId, webdav, wbe, edit, download, lang) {
      if (id.length > 0) {
        pageMustBeReloadingAfterSorting = true;
        $.post('<c:url value="/Attachment" />', {Id:id, FileLanguage:'<c:out value="${contentLanguage}" />', Action:'Checkout'}, function(formattedDateTimeData) {
          let openWebBrowserEdition = function() {
            sp.formRequest(_getOnlineEditionLauncherURL(id, lang))
                .withParam('wbe', true)
                .toTarget('_blank')
                .submit();
          };
          if (formattedDateTimeData !== 'nok') {
            let oMenu = eval("oMenu" + oldId);
            oMenu.getItem(oMenu._getItemGroup().length - 1).cfg.setProperty("disabled", false); // checkin
            oMenu.getItem(0).cfg.setProperty("disabled", true); // checkout
            oMenu.getItem(1).cfg.setProperty("disabled", true);	// checkout and download
            if (!webdav) {
              oMenu.getItem(2).cfg.setProperty("disabled", true);  // edit online
            }
            //disable delete
            oMenu.getItem(3, 1).cfg.setProperty("disabled", true); // delete
            oMenu.getItem(2, 1).cfg.setProperty("disabled", true); // switch
            let $worker = $('#worker' + oldId);
            $worker.html("<fmt:message key="readOnly"/> <%=m_MainSessionCtrl.getCurrentUserDetail().getDisplayedName()%> <fmt:message key="at"/> " + formattedDateTimeData);
            $worker.css({'visibility':'visible'});
            if (edit) {
              if (!wbe) {
                // display alert popin
                _showInformationAboutOnlineEditingWithCustomProtocol(id, lang);
              } else {
                openWebBrowserEdition();
              }
            } else if (download) {
              let url = $('#url_' + oldId).attr('href');
              window.open(url);
            }
          } else if(wbe) {
            openWebBrowserEdition();
          } else {
            SilverpeasError.add('<fmt:message key="attachment.dialog.checkout.nok"/>').show().then(function() {
              $.progressMessage();
              window.location.href = window.location.href;
            });
          }
        }, 'text');
        pageMustBeReloadingAfterSorting = true;
      }
    };

    _self.checkoutAndDownload = function(id, oldId, webdav) {
      _self.checkout(id, oldId, webdav, false, false, true);
    };

    _self.checkoutAndEdit = function(id, oldId, lang, wbe) {
      _self.checkout(id, oldId, true, wbe, true, false, lang);
    };

    _self.switchState = function(id, isVersioned, isLastPublicVersion) {
      <fmt:message key="attachment.switch.warning.simple" var="warningSimple"/>
      <fmt:message key="attachment.switch.warning.versioned" var="warningVersioned"/>
      <fmt:message key="attachment.switchState.toVersioned" var="warningTitleSimple"/>
      <fmt:message key="attachment.switchState.toSimple" var="warningTitleVersioned"/>
      let $switchAttachmentState = _self_ui.get$switchAttachmentState();
      const $attachmentSwitchVersioned = $switchAttachmentState.find(".attachment-switch-versioned");
      const $attachmentSwitchSimple = $switchAttachmentState.find(".attachment-switch-simple");
      $attachmentSwitchVersioned.hide();
      $attachmentSwitchSimple.hide();
      if(isVersioned) {
        $switchAttachmentState.dialog( "option" , 'title' , '<c:out value="${silfn:escapeJs(warningTitleVersioned)}" />' );
        $switchAttachmentState.find(".attachment-switch-warning-message").empty().append('<c:out value="${silfn:escapeJs(warningSimple)}" />');
        if (isLastPublicVersion) {
          $attachmentSwitchSimple.show();
        } else {
          $switchAttachmentState.findByValue("last").prop( "checked", true );
        }
      } else {
        $switchAttachmentState.dialog( "option" , 'title' , '<c:out value="${silfn:escapeJs(warningTitleSimple)}" />' );
        $switchAttachmentState.find(".attachment-switch-warning-message").empty().append('<c:out value="${silfn:escapeJs(warningVersioned)}" />');
        $attachmentSwitchVersioned.show();
      }
      $switchAttachmentState.data("id", id).dialog("open");
      pageMustBeReloadingAfterSorting = true;
    };

    _self.checkin = function(id, oldId, webdav, forceRelease, isVersioned, webdavContentLanguageLabel) {
      const $attachmentCheckin = _self_ui.get$attachmentCheckin();
      <c:if test="${_isI18nHandled}">
      const $checkinWebdavLanguageBlock = $attachmentCheckin.find('.webdav-attachment-checkin-language');
      if (webdav === true) {
        $checkinWebdavLanguageBlock.show();
        $checkinWebdavLanguageBlock.find('#langCreate${domIdSuffix}').html(webdavContentLanguageLabel);
      } else {
        $checkinWebdavLanguageBlock.hide();
      }
      </c:if>
      const $attachmentCheckinBlock = $attachmentCheckin.find(".simple_fields_attachment-checkin");
      const $versionedAttachmentCheckinBlock = $attachmentCheckin.find(".versioned_fields_attachment-checkin");
      if(isVersioned === true) {
        $attachmentCheckinBlock.hide();
        $versionedAttachmentCheckinBlock.show();
      } else {
        $versionedAttachmentCheckinBlock.hide();
        $attachmentCheckinBlock.css('display', 'inline-block');
      }
      $attachmentCheckin.data("attachmentId", id).data("oldId", oldId).data("webdav", webdav).data("forceRelease", forceRelease).dialog("open");
      pageMustBeReloadingAfterSorting = true;
    };

    _self.addAttachment = function(foreignId) {
      const $addAttachment = _self_ui.get$addAttachment();
      $addAttachment.find("form").findByName('foreignId').val(foreignId);
      $addAttachment.dialog("open");
    };

    _self.deleteAttachment = function(id, filename) {
      _self_ui.get$deleteAttachment().data("id", id).data("filename", filename).dialog("open");
    };

    _self.reloadIncludingPage = function() {
      <c:choose>
        <c:when test="${! silfn:isDefined(param.CallbackUrl)}">document.location.reload();</c:when>
        <c:otherwise>document.location.href = "<c:url value="${param.CallbackUrl}" />";</c:otherwise>
      </c:choose>
    }

    _self.updateAttachment = function(attachmentId, lang) {
      const $updateAttachment = _self_ui.get$updateAttachment();
      $updateAttachment.data('attachmentId', attachmentId);
      _loadAttachmentToUpdate(attachmentId, lang).then(function() {
        $updateAttachment.dialog("open");
      });
    }

    _self.switchDownloadAllowedForReaders = function(attachmentId, allowed) {
      __switchActionEnabled(attachmentId, "switchDownloadAllowedForReaders", "allowed", allowed);
    }

    _self.switchDisplayAsContentEnabled = function(attachmentId, enabled) {
      __switchActionEnabled(attachmentId, "switchDisplayAsContentEnabled", "enabled", enabled);
    }

    _self.switchEditSimultaneouslyEnabled = function(attachmentId, enabled) {
      __switchActionEnabled(attachmentId, "switchEditSimultaneouslyEnabled", "enabled", enabled);
    }

    function __switchActionEnabled(attachmentId, action, parameter, enabled) {
      let data = {};
      data[parameter] = enabled ? enabled : false;
      $.progressMessage();
      $.ajax({
        url : '<c:url value="/services/documents/${sessionScope.Silverpeas_Attachment_ComponentId}/document/"/>' +
            attachmentId + '/' + action,
        type : "POST",
        cache : false,
        dataType : "json",
        data : data,
        success : function() {
          _self.reloadIncludingPage();
        }
      });
    }

    <c:if test="${useXMLForm}">
      _self.EditXmlForm = function(id, lang) {
        const url = '<c:url value="/RformTemplate/jsp/Edit"><c:param name="IndexIt" value="${indexIt}" /><c:param name="ComponentId" value="${param.ComponentId}" /><c:param name="type" value="Attachment" /><c:param name="ObjectType" value="Attachment" /><c:param name="XMLFormName" value="${xmlForm}" /></c:url>&ReloadOpener=true&ObjectLanguage=' + lang + '&ObjectId=' + id;
        SP_openWindow(url, "test", "600", "400", "scrollbars=yes, resizable, alwaysRaised");
      }
    </c:if>

    $(document).ready(function() {
      _self_ui.get$updateAttachment().findByName("fileLang").on("change", function(event) {
        const $updateAttachment = _self_ui.get$updateAttachment();
        $updateAttachment.findByName("fileLang").find("option:selected").each(function() {
          _loadAttachmentToUpdate($updateAttachment.findByName("IdAttachment").val(), $(this).val());
        });
    });

    _self.verifyVersionType = function(domRadioSelector) {
      let lastVersionType = 'public';
      if (domRadioSelector) {
        const $radio = $(domRadioSelector);
        if ($radio.length === 2 && $radio.parent().parent().css('display') !== 'none') {
          const value = $radio.filter(':checked').val();
          lastVersionType = (!value || value === 'false' || value === '0') ? 'public' : 'work';
        }
      }
      return lastVersionType;
    }

    const _performActionWithContributionModificationManagement = function(callback, options) {
      let mustPerform = ${isHandledModificationContext};
      if (mustPerform) {
        const params = typeof options === 'object' ? options : {};
        const checkInWebDav = typeof params.checkInWebDav === 'undefined' || params.checkInWebDav;
        mustPerform = _self.verifyVersionType(params.versionTypeDomRadioSelector) === 'public' && checkInWebDav;
        if (mustPerform) {
          jQuery.contributionModificationContext.validateOnUpdate({
            contributionId : {
              componentInstanceId : '${componentId}',
              localId : '${param.Id}',
              type : '${param.Type}'
            },
            callback : function(userResponse) {
              _performActionWithPotentialNotification(callback, options, userResponse);
            }});
        }
      }
      if (!mustPerform) {
        _performActionWithPotentialNotification(callback, options);
      }
    }

    function _performActionWithPotentialNotification(callback, options, userModificationContextResponse) {
      function __applyModificationContextResponse(ajaxOptions) {
        if (userModificationContextResponse) {
          ajaxOptions = userModificationContextResponse.applyOnAjaxOptions(ajaxOptions);
        }
        return ajaxOptions;
      }
      <c:choose>
      <c:when test="${isHandledSubscriptionConfirmation}">
      const params = typeof options === 'object' ? options : {};
      const checkInWebDav = typeof params.checkInWebDav === 'undefined' || params.checkInWebDav;
      if (_self.verifyVersionType(params.versionTypeDomRadioSelector) === 'public' && checkInWebDav) {
        $.subscription.confirmNotificationSendingOnUpdate({
          contribution : {
            contributionId : {
              componentInstanceId : '${componentId}',
              localId : '${param.Id}',
              type : '${param.Type}'
            },
            locationId : '${handledSubscriptionLocationId}',
            indexable : ${indexIt}
          },
          comment : {
            saveNote : ${silfn:booleanValue(commentActivated)}
          },
          subscription : {
            componentInstanceId : '${componentId}',
            type : '${handledSubscriptionType}',
            resourceId : '${handledSubscriptionResourceId}'
          }, callback : function(userResponse) {
            const userResponseWrapper = new function() {
              this.applyOnAjaxOptions = function(ajaxOptions) {
                ajaxOptions = __applyModificationContextResponse(ajaxOptions);
                return userResponse.applyOnAjaxOptions(ajaxOptions);
              };
            };
            callback.call(this, userResponseWrapper);
          }
        });
      } else {
        callback.call(this, {
          applyOnAjaxOptions : function(ajaxOptions) {
            return __applyModificationContextResponse(ajaxOptions);
          }
        });
      }
      </c:when>
      <c:otherwise>
      callback.call(this, {
        applyOnAjaxOptions : function(ajaxOptions) {
          return __applyModificationContextResponse(ajaxOptions);
        }
      });
      </c:otherwise>
      </c:choose>
    }

    const iframeSendComplete = function() {
      _self.reloadIncludingPage();
    };

    const iframeSendError = function() {
      $.closeProgressMessage();
    };

    _self_ui.get$updateAttachment().find('form').iframeAjaxFormSubmit ({
      complete : iframeSendComplete,
      error : iframeSendError
    });

    _self_ui.get$addAttachment().find('form').iframeAjaxFormSubmit ({
      complete : iframeSendComplete,
      error : iframeSendError
    });

      function __deleteDocumentEntirely($thisOfDeleteAttachmentDialog) {
        const attachmentId = $thisOfDeleteAttachmentDialog.data("id");
        _performActionWithContributionModificationManagement(function(userResponse) {
          $.progressMessage();
          const deleteUrl = '<c:url value="/services/documents/${sessionScope.Silverpeas_Attachment_ComponentId}/document/"/>' + attachmentId;
          $.ajax(userResponse.applyOnAjaxOptions({
            url : deleteUrl,
            type : "DELETE",
            cache : false,
            success : function(data) {
              _self.reloadIncludingPage();
              $thisOfDeleteAttachmentDialog.dialog("close");
            }, error : function(jqXHR, textStatus, errorThrown) {
              alert(jqXHR.responseText + ' : ' + textStatus + ' :' + errorThrown);
            }
          }));
        });
      }

      _self_ui.get$deleteAttachment().dialog({
      autoOpen: false,
      open:function() {
        const filename = $(this).data("filename");
        const $dialog = _self_ui.get$deleteAttachment().parent();
        $dialog.find(".attachment-delete-warning-message").html('${silfn:escapeJs(deleteConfirmMsg)}'.replace(/([ ?]+)$/, ' <b>' + filename + '</b>$1'));
        $dialog.find("#button-delete-all${domIdSuffix}").hide();
      <c:if test="${_isI18nHandled && not isVersionActive}">
        const translationsUrl = '<c:url value="/services/documents/${sessionScope.Silverpeas_Attachment_ComponentId}/document/"/>' + $(this).data("id") + '/translations';
        $.ajax({
          url: translationsUrl,
          type: "GET",
          cache: false,
          success: function(data) {
            if (data.length > 1) {
              $dialog.find(".attachment-delete-warning-message").html('<fmt:message key="attachment.suppressionWhichTranslations" />');
              for(let i = 0 ; i < data.length; i++) {
                $dialog.find(".delete-language-" + data[i].lang).show();
              }
              $dialog.find(".attachment-delete-select-lang").show();
              $dialog.find("#button-delete-all${domIdSuffix}").show();
            } else {
              $dialog.find(".attachment-delete-select-lang").hide();
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
          text: "<fmt:message key="GML.delete"/>",
          click: function() {
            const $this = $(this);
            const attachmentId = $this.data("id");
            <c:choose>
              <c:when test="${_isI18nHandled && not isVersionActive}">
            const $dialog = _self_ui.get$deleteAttachment().parent();
            if ($dialog.find(".attachment-delete-select-lang").css('display') === 'none') {
              __deleteDocumentEntirely($this);
              return;
            }
            $.progressMessage();
            const deleteOperations = [];
            $dialog.findByName("languagesToDelete").filter(':checked').each(function() {
              const me = this;
              deleteOperations.push(function($deferred){
                const deleteUrl = '<c:url value="/services/documents/${sessionScope.Silverpeas_Attachment_ComponentId}/document/"/>' + attachmentId + '/content/' + me.value;
                $.ajax({
                  url: deleteUrl,
                  type: "DELETE",
                  cache: false,
                  success: function(data) {
                    $deferred.resolve();
                  },
                  error: function(jqXHR, textStatus, errorThrown) {
                    alert(jqXHR.responseText + ' : ' + textStatus + ' :' + errorThrown);
                    $deferred.reject();
                  }
                });
              });
            });
            const _consumeDeleteOperations = function() {
              if (deleteOperations.length) {
                const $deferred = $.Deferred();
                deleteOperations.shift().call(this, $deferred);
                $deferred.always(function() {
                  _consumeDeleteOperations();
                });
              } else {
                _self.reloadIncludingPage();
                $this.dialog("close");
              }
            };
            _consumeDeleteOperations();
            $this.dialog("close");
              </c:when>
              <c:otherwise>
            __deleteDocumentEntirely($this);
              </c:otherwise>
            </c:choose>
          }
        },
        '<fmt:message key="attachment.dialog.button.deleteAll"/>': {
          id: "button-delete-all${domIdSuffix}",
          text: "<fmt:message key="attachment.dialog.button.deleteAll"/>",
          click: function() {
            const $this = $(this);
            __deleteDocumentEntirely($this);
          }
        },
        '<fmt:message key="GML.cancel"/>': function() {
          $(this).dialog("close");
        }
      },
      close: function() {
      }
    });

      _self_ui.get$addAttachment().dialog({
        autoOpen : false,
        title : "<fmt:message key="attachment.dialog.add" />",
        height : 'auto',
        width : 550,
        modal : true,
        buttons : {
          '<fmt:message key="GML.ok"/>' : function() {
            const $this = $(this);
            const $addAttachmentForm = _self_ui.get$addAttachment().find('form');
            const filename = $.trim($addAttachmentForm.findByName("file_upload").val().split('\\').pop());
            if (filename === '') {
              return SilverpeasError.add('<fmt:message key="attachment.dialog.error.file.mandatory"/>').show();
            }
            let submitUrl = '<c:url value="/services/documents/${sessionScope.Silverpeas_Attachment_ComponentId}/document/create"/>';
            submitUrl = submitUrl + '/' + encodeURIComponent(filename);
            _performActionWithContributionModificationManagement(function() {
              $.progressMessage();
              if ("FormData" in window) {
                const formData = new FormData($addAttachmentForm[0]);
                $.ajax(submitUrl, {
                  processData : false,
                  contentType : false,
                  type : 'POST',
                  dataType : "json",
                  data : formData,
                  success : function(data) {
                    _self.reloadIncludingPage();
                    $this.dialog("close");
                  }
                });
              } else {
                $addAttachmentForm.attr('action', submitUrl);
                $addAttachmentForm.submit();
              }
            }, {
              versionTypeDomRadioSelector : '#dialog-attachment-add${domIdSuffix} input[name="versionType"]'
            });
          },
          '<fmt:message key="GML.cancel"/>' : function() {
            $(this).dialog("close");
          }
        }, close : function() {
        }
      });

      _self_ui.get$updateAttachment().dialog({
        autoOpen : false,
        title : "<fmt:message key="attachment.dialog.update" />",
        height : 'auto',
        width : 550,
        modal : true,
    <c:if test="${_isI18nHandled && not isVersionActive}">
        open : function() {
          const $updateAttachmentForm = _self_ui.get$updateAttachment()
          const $delButton = $updateAttachmentForm.parent().find('#attachment-update-delete-lang${domIdSuffix}');
          const fileLangDisplay = $updateAttachmentForm.findByName('fileLang').css('display') || 'block';
          if (fileLangDisplay === 'none') {
            $delButton.hide();
          } else {
            $delButton.show();
          }
        },
    </c:if>
        buttons : {
          '<fmt:message key="GML.ok"/>' : function() {
            const $this = $(this);
            const $updateAttachmentForm = _self_ui.get$updateAttachment().find('form');
            let submitUrl = '<c:url value="/services/documents/${sessionScope.Silverpeas_Attachment_ComponentId}/document/"/>' +
                $(this).data('attachmentId');
            const filename = $.trim($updateAttachmentForm.findByName("file_upload").val().split('\\').pop());
            if (filename !== '') {
              submitUrl = submitUrl + '/' + encodeURIComponent(filename);
            } else {
              submitUrl = submitUrl + '/no_file';
            }
            _performActionWithContributionModificationManagement(function() {
              $.progressMessage();
              if ("FormData" in window) {
                const formData = new FormData($updateAttachmentForm[0]);
                $.ajax(submitUrl, {
                  processData : false,
                  contentType : false,
                  type : 'POST',
                  dataType : "json",
                  data : formData,
                  success : function(data) {
                    _self.reloadIncludingPage();
                    $this.dialog("close");
                  }
                });
              } else {
                $updateAttachmentForm.attr('action', submitUrl);
                $updateAttachmentForm.submit();
              }
            }, {
              versionTypeDomRadioSelector : '#dialog-attachment-update${domIdSuffix} input[name="versionType"]'
            });
          },
          <c:if test="${_isI18nHandled && not isVersionActive}">
          'delete_button' : {
            id : 'attachment-update-delete-lang${domIdSuffix}',
            text : '${silfn:escapeJs(deleteFileLangMsg)}',
            click : function() {
              const $this = $(this);
              const $updateAttachmentForm = _self_ui.get$updateAttachment().find('form');
              jQuery.popup.confirm('${silfn:escapeJs(deleteConfirmMsg)}', function() {
                $.progressMessage();
                $.ajax({
                  url : '<c:url value="/services/documents/${sessionScope.Silverpeas_Attachment_ComponentId}/document/"/>' +
                      $this.data('attachmentId') + '/content/' + $updateAttachmentForm.findByName("fileLang").val(),
                  type : "DELETE",
                  contentType : "application/json",
                  dataType : "json",
                  cache : false,
                  success : function(data) {
                    _self.reloadIncludingPage();
                    $this.dialog("close");
                  }
                });
                $this.dialog("close");
              });
            }
          },
          </c:if>
          '<fmt:message key="GML.cancel"/>' : function() {
            $(this).dialog("close");
          }
        },
        close : function() {
        }
      });

        get$containerEl().sortable({opacity: 0.4, axis: 'y', cursor: 'move', placeholder: 'ui-state-highlight', forcePlaceholderSize: true});

        const _submitCheckin = function(submitUrl) {
          $.progressMessage();
          const $this = $(this);
          $.ajax(submitUrl, {
            type : 'POST',
            dataType : "json",
            data : _self_ui.get$attachmentCheckin().find("form").serialize(),
            success : function(result) {
              _self.reloadIncludingPage();
              $this.dialog("close");
            },
            error : function(jqXHR, textStatus, errorThrown) {
              alert(jqXHR.responseText + ' : ' + textStatus + ' :' + errorThrown);
              _self.reloadIncludingPage();
            }
          });
        }

      _self_ui.get$switchAttachmentState().dialog({
        autoOpen: false,
        title: "<fmt:message key="attachment.dialog.switch"/>",
        height: 'auto',
        width: 550,
        modal: true,
        buttons: {
          '<fmt:message key="GML.ok"/>': function() {
            const $this = $(this);
            let $switchAttachmentState = _self_ui.get$switchAttachmentState();
            const submitUrl = '<c:url value="/services/documents/${sessionScope.Silverpeas_Attachment_ComponentId}/document/"/>' + $switchAttachmentState.data('id') + '/switchState';
            $.progressMessage();
            $.ajax(submitUrl, {
              type : 'PUT',
              dataType : "json",
              data : $switchAttachmentState.find("form").serialize(),
              success : function(result) {
                _self.reloadIncludingPage();
                $this.dialog("close");
              },
              error : function(jqXHR, textStatus, errorThrown) {
                alert(jqXHR.responseText + ' : ' + textStatus + ' :' + errorThrown);
                _self.reloadIncludingPage();
              }
            });
          },
          '<fmt:message key="GML.cancel"/>': function() {
            $(this).dialog("close");
          }
        }
      });

      _self_ui.get$attachmentCheckin().dialog({
        autoOpen: false,
        title: "<fmt:message key="attachment.dialog.checkin"/>",
        height: 'auto',
        width: 550,
        modal: true,
        buttons: {
          '<fmt:message key="GML.ok"/>': function() {
            const $attachmentCheckin = _self_ui.get$attachmentCheckin();
            const $webDav = $attachmentCheckin.findByName('webdav');
            $webDav.val($attachmentCheckin.data('webdav'));
            $attachmentCheckin.findByName('force').val($attachmentCheckin.data('forceRelease'));
            $attachmentCheckin.findByName('checkin_oldId').val($attachmentCheckin.data('oldId'));
            const submitUrl = '<c:url value="/services/documents/${sessionScope.Silverpeas_Attachment_ComponentId}/document/"/>' + $(this).data('attachmentId') + '/unlock';
            const $this = $(this);
            _performActionWithContributionModificationManagement(function() {
              _submitCheckin.call($this, submitUrl);
            }, {
              versionTypeDomRadioSelector : '#dialog-attachment-checkin${domIdSuffix} input[name="private"]',
              checkInWebDav : $webDav.val() === 'true'
            });
          },
          '<fmt:message key="attachment.revert"/>': function() {
              const $attachmentCheckin = _self_ui.get$attachmentCheckin();
              $attachmentCheckin.findByName('force').val('true');
              $attachmentCheckin.findByName('webdav').val('false');
              const submitUrl = '<c:url value="/services/documents/${sessionScope.Silverpeas_Attachment_ComponentId}/document/"/>' + $(this).data('attachmentId') + '/unlock';
              const $this = $(this);
              _submitCheckin.call($this, submitUrl);
            },
            '<fmt:message key="GML.cancel"/>': function() {
              const $this = $(this);
              _clearCheckin();
              $this.dialog("close");
            }
          },
          close: function() {
            _clearCheckin();
          }
        });

      _self_ui.get$onlineEditingCustomProtocol().dialog({
        autoOpen: false,
        title: "<fmt:message key="attachment.dialog.onlineEditing.customProtocol.title"/>",
        height: 'auto',
        width: 550,
        dialogClass: 'help-modal-message',
        modal: true,
        buttons: {
          "<fmt:message key="attachment.dialog.onlineEditing.customProtocol.button.edit"/>": function() {
            $.cookie(customProtocolCookieName, "IKnowIt", { expires: 3650, path: '/', secure: ${pageContext.request.secure} });
            _openDocViaCustomProtocol($(this).data('docId'), $(this).data('lang'));
            $(this).dialog("close");
          },
          "<fmt:message key="attachment.dialog.onlineEditing.customProtocol.button.cancel"/>": function() {
            $(this).dialog("close");
          }
        }
      });

      get$containerEl().bind('sortupdate', function(event, ui) {
        const sortedIds = get$containerEl().sortable('toArray').map(function(id) {
          return id.replace('attachment_', '');
        });
        _sortAttachments('${param.Id}', sortedIds);
      });

      });


      const _sortAttachments = function(objectId, sortedIds) {
        $.post('<c:url value="/Attachment" />', { orderedList:sortedIds.join(','), Action:'Sort'}, function(data){
          data = data.replace(/^\s+/g, '').replace(/\s+$/g, '');
          if (data === "error") {
            SilverpeasError.add("Une erreur s'est produite !").show();
            return;
          }
          attachmentEventManager.dispatchEvent('resource-attached-file-sorted', {
            resourceId : objectId,
            sortedAttachedFileIds : sortedIds
          });
        }, 'text');
        if (pageMustBeReloadingAfterSorting) {
          _self.reloadIncludingPage();
        }
      }

      _self.uploadCompleted = function(s) {
        _self.reloadIncludingPage();
      }

  </c:if>

  const _displayAttachmentToUpdate = function(attachment) {
    const $updateAttachment = _self_ui.get$updateAttachment();
    $updateAttachment.findByName("fileLang").val(attachment.lang);
    $updateAttachment.find('.fileName').text(attachment.fileName);
    $updateAttachment.findByName('fileTitle').val(attachment.title);
    $updateAttachment.findByName('fileDescription').val(attachment.description);
    if(attachment.versioned === 'true') {
      $updateAttachment.find('.fileName_label').text('<fmt:message key="attachment.version.actual" />');
      $updateAttachment.find('.file_upload_label').text('<fmt:message key="attachment.version.new" />');
      $updateAttachment.find('.versioned_fields_attachment-update').show();
    } else {
      $updateAttachment.find('.versioned_fields_attachment-update').hide();
      $updateAttachment.find('.fileName_label').text('<fmt:message key="GML.file"/>');
      $updateAttachment.find('.file_upload_label').text('<fmt:message key="fichierJoint" />');
    }
    $updateAttachment.find('.mandatory').hide();
  }

  const _clearUpdateAttachmentAndSetId = function(id) {
    const $updateAttachment = _self_ui.get$updateAttachment();
    $updateAttachment.find('.fileName').html('');
    $updateAttachment.findByName('fileTitle').val('');
    $updateAttachment.findByName('fileDescription').val('');
    $updateAttachment.find('.versioned_fields_attachment-update').hide();
    $updateAttachment.find('.mandatory').show();
    $updateAttachment.findByName('IdAttachment').val(id);
  }

  const _handleAttachmentUpdateWarningOnTranslations = function(attachments) {
    try {
      const $updateAttachment = _self_ui.get$updateAttachment();
      const $fileLang = $updateAttachment.findByName('fileLang');
      const $translationWarningPart = $updateAttachment.find('.translationWarningPart');
      $updateAttachment.find('.fileLangText').remove();
      if (attachments.length === 1) {
        $translationWarningPart.hide();
        $fileLang.css('display', 'none');
        const $fileLangText = $('<span>', {'class':'fileLangText'});
        $fileLang.parent().append($fileLangText.text($('option[value="' + attachments[0].lang + '"]', $fileLang).text()));
      } else {
        $translationWarningPart.show();
        $fileLang.css('display', 'block');
      }
    } catch (e) {
      // in case elements are not in DOM
    }
  }

  const _clearCheckin = function() {
    const $attachmentCheckin = _self_ui.get$attachmentCheckin();
    $attachmentCheckin.findByName('checkin_oldId').val('');
    $attachmentCheckin.findByName('force').val('false');
    $attachmentCheckin.findByName('webdav').val('false');
    $attachmentCheckin.findByName('comment').val('');
  }

  const _loadAttachmentToUpdate = function(id, lang) {
    spProgressMessage.show();
    const deferred = sp.promise.deferred();
    const translationsUrl = '<c:url value="/services/documents/${sessionScope.Silverpeas_Attachment_ComponentId}/document/"/>' + id + '/translations';
    $.ajax({
      url: translationsUrl,
      type: "GET",
      contentType: "application/json",
      dataType: "json",
      cache: false,
      success: function(attachments) {
        _clearUpdateAttachmentAndSetId(id);
        _handleAttachmentUpdateWarningOnTranslations(attachments);
        $.each(attachments, function(index, attachment) {
          if (attachment.lang === lang) {
            _displayAttachmentToUpdate(attachment);
            return false;
          }
          return true;
        });
        deferred.resolve();
        spProgressMessage.hide();
      }
    });
    return deferred.promise;
  }

  _self.ShareAttachment = function(id) {
    const sharingObject = {
      componentId: "${param.ComponentId}",
      type       : "Attachment",
      id         : id,
      name   : $("#url_" + id).text()
    };
    createSharingTicketPopup(sharingObject);
  }

  _self.notifyAttachment = function(attachmentId) {
    alertUsersAttachment(attachmentId); //dans publication.jsp
  }

  const customProtocolCookieName = "Silverpeas_OnlineEditing_CustomProtocol";
  function _showInformationAboutOnlineEditingWithCustomProtocol(id, lang) {
    const customProtocolCookieValue = $.cookie(customProtocolCookieName);
    if (${onlineEditingWithCustomProtocolAlert} && ("IKnowIt" !== customProtocolCookieValue)) {
      _self_ui.get$onlineEditingCustomProtocol().data({
        'docId' : id,
        'lang' : lang
      }).dialog("open");
    } else {
      _openDocViaCustomProtocol(id, lang);
    }
  }

  const _getOnlineEditionLauncherURL = function(docId, lang) {
    return "<%=URLUtil.getFullApplicationURL(request)%>/attachment/jsp/launch.jsp?id="+docId+"&lang="+lang;
  }

  const _openDocViaCustomProtocol = function(docId, lang) {
    $.get(_getOnlineEditionLauncherURL(docId, lang));
  }
};
</script>

<div id="dialog-attachment-update${domIdSuffix}" class="dialog-attachment-update" style="display:none">
  <form name="update-attachment-form" class="update-attachment-form" method="post" enctype="multipart/form-data;charset=utf-8" accept-charset="UTF-8">
    <input type="hidden" name="IdAttachment"/>
        <c:if test="${_isI18nHandled}">
          <div class="translationWarningPart">
            <label class="label-ui-dialog"><fmt:message key="attachment.warning.translations.label"/></label>
            <span class="champ-ui-dialog warning translationWarning"><fmt:message key="attachment.warning.translations"/></span>
          </div>
          <label for="fileLang${domIdSuffix}" class="label-ui-dialog"><fmt:message key="GML.language"/></label>
          <span class="champ-ui-dialog"><view:langSelect elementName="fileLang" elementId="fileLang${domIdSuffix}" langCode="${contentLanguage}" includeLabel="false" /></span>
        </c:if>
        <label class="label-ui-dialog fileName_label"><fmt:message key="GML.file" /></label>
        <span class="champ-ui-dialog fileName"></span>

        <label for="file_upload${domIdSuffix}" class="label-ui-dialog file_upload_label"><fmt:message key="fichierJoint" /></label>
        <span class="champ-ui-dialog"><input type="file" name="file_upload" size="50" id="file_upload${domIdSuffix}" />
          <span class="mandatory" style="display: none">&nbsp;<img alt="<fmt:message key="GML.mandatory"/>" src="${mandatoryFieldUrl}" width="5" height="5"/></span></span>

        <label for="fileTitle${domIdSuffix}" class="label-ui-dialog"><fmt:message key="Title"/></label>
        <span class="champ-ui-dialog"><input type="text" name="fileTitle" size="60" id="fileTitle${domIdSuffix}" /></span>

        <label for="fileDescription${domIdSuffix}" class="label-ui-dialog"><fmt:message key="GML.description" /></label>
        <span class="champ-ui-dialog"><textarea name="fileDescription" cols="60" rows="3" id="fileDescription${domIdSuffix}"></textarea></span>

        <div class="versioned_fields_attachment-update" style="display:none">
          <label for="versionType-update-${domIdSuffix}" class="label-ui-dialog"><fmt:message key="attachment.version.label"/></label>
          <span class="champ-ui-dialog"><input value="0" type="radio" name="versionType" id="versionType-update-${domIdSuffix}" checked="checked"/><fmt:message key="attachment.version_public.label"/>
          <input value="1" type="radio" name="versionType"/><fmt:message key="attachment.version_wip.label"/></span>

          <label for="commentMessage-update-${domIdSuffix}" class="label-ui-dialog"><fmt:message key="attachment.dialog.comment"/></label>
          <span class="champ-ui-dialog"><textarea name="commentMessage" cols="60" rows="3" id="commentMessage-update-${domIdSuffix}"></textarea></span>
        </div>
    <div class="mandatory" style="display: none">
      <span class="label-ui-dialog"><img src="${mandatoryFieldUrl}" width="5" height="5" alt=""/> : <fmt:message key="GML.requiredField"/></span>
    </div>
    <input type="submit" value="Submit" style="display:none" />
  </form>
</div>

<div id="dialog-attachment-add${domIdSuffix}" class="dialog-attachment-add" style="display:none">
  <form name="add-attachment-form" method="post" enctype="multipart/form-data;charset=utf-8" accept-charset="UTF-8">
    <input type="hidden" name="foreignId" value="<c:out value="${sessionScope.Silverpeas_Attachment_ObjectId}" />" />
    <input type="hidden" name="indexIt" value="<c:out value="${indexIt}" />" />
    <c:if test="${_isI18nHandled}">
      <label for="langCreate${domIdSuffix}" class="label-ui-dialog"><fmt:message key="GML.language"/></label>
      <span class="champ-ui-dialog"><view:langSelect elementName="fileLang" elementId="langCreate${domIdSuffix}" langCode="${contentLanguage}" includeLabel="false"/></span>
    </c:if>
    <label for="file_create${domIdSuffix}" class="label-ui-dialog"><fmt:message key="fichierJoint"/></label>
    <span class="champ-ui-dialog"><input type="file" name="file_upload" size="50" id="file_create${domIdSuffix}" />
          <span>&nbsp;<img alt="<fmt:message key="GML.mandatory"/>" src="${mandatoryFieldUrl}" width="5" height="5"/></span></span>
    <label for="fileTitleCreate${domIdSuffix}" class="label-ui-dialog"><fmt:message key="Title"/></label>
    <span class="champ-ui-dialog"><input type="text" name="fileTitle" size="60" id="fileTitleCreate${domIdSuffix}" /></span>
    <label for="fileDescriptionCreate${domIdSuffix}" class="label-ui-dialog"><fmt:message key="GML.description" /></label>
    <span class="champ-ui-dialog"><textarea name="fileDescription" rows="3" id="fileDescriptionCreate${domIdSuffix}"></textarea></span>
    <c:if test="${isVersionActive}">
      <label for="versionType-add-${domIdSuffix}" class="label-ui-dialog"><fmt:message key="attachment.version.label"/></label>
      <span class="champ-ui-dialog">
        <input value="0" type="radio" name="versionType" id="versionType-add-${domIdSuffix}" checked="checked"/><fmt:message key="attachment.version_public.label"/>
        <input value="1" type="radio" name="versionType"/><fmt:message key="attachment.version_wip.label"/>
      </span>
      <label for="commentMessage-add-${domIdSuffix}" class="label-ui-dialog"><fmt:message key="attachment.dialog.comment"/></label>
      <span class="champ-ui-dialog"><textarea name="commentMessage" cols="60" rows="3" id="commentMessage-add-${domIdSuffix}"></textarea></span>
    </c:if>
    <div>
      <span class="label-ui-dialog"><img src="${mandatoryFieldUrl}" width="5" height="5" alt=""/> : <fmt:message key="GML.requiredField"/></span>
    </div>
    <input type="submit" value="Submit" style="display:none" />
  </form>
</div>

<div id="dialog-attachment-delete${domIdSuffix}" class="dialog-attachment-delete" style="display:none">
  <span class="attachment-delete-warning-message">${deleteConfirmMsg}</span>
    <c:if test="${_isI18nHandled}">
      <div class="attachment-delete-select-lang" style="display:none">
        <div class="languages">
          <c:forEach items="<%=I18NHelper.getAllSupportedLanguages()%>" var="supportedLanguage">
            <span class='delete-language-<c:out value="${supportedLanguage}"/>' style="display:none"><input type="checkbox" id='<c:out value="${supportedLanguage}"/>ToDelete' name="languagesToDelete" value='<c:out value="${supportedLanguage}"/>'/><c:out value="${silfn:i18nLanguageLabel(supportedLanguage, sessionScope.SilverSessionController.favoriteLanguage)}"/></span>
          </c:forEach>
        </div>
      </div>
    </c:if>
</div>

  <div id="dialog-attachment-switch${domIdSuffix}" class="dialog-attachment-switch" style="display:none">
    <p class="attachment-switch-warning-message">dummy</p>
    <form name="attachment-switch-form" method="put" accept-charset="UTF-8">
      <div class="attachment-switch-simple" style="display:none">
        <label for="switch-version-major${domIdSuffix}" class="label-ui-dialog"><fmt:message key="attachment.switch.version.major" /></label>
        <span class="champ-ui-dialog attachment-switch-major"><input value="lastMajor" type="radio" name="switch-version" id="switch-version-major${domIdSuffix}" checked="checked"/></span>
        <label for="switch-version-last${domIdSuffix}" class="label-ui-dialog"><fmt:message key="attachment.switch.version.last" /></label>
        <span class="champ-ui-dialog attachment-switch-last"><input value="last" type="radio" name="switch-version" id="switch-version-last${domIdSuffix}"/></span>
      </div>
      <div class="attachment-switch-versioned" style="display:none">
       <label for="switch-version-comment${domIdSuffix}" class="label-ui-dialog"><fmt:message key="attachment.dialog.comment" /></label>
      <span class="champ-ui-dialog"><textarea name="switch-version-comment" cols="60" rows="3" id="switch-version-comment${domIdSuffix}"></textarea></span>
      </div>
      <input type="submit" value="Submit" style="display:none" />
    </form>
  </div>

 <div id="dialog-attachment-checkin${domIdSuffix}" class="dialog-attachment-checkin" style="display:none">
  <form name="checkin-attachment-form" method="post" accept-charset="UTF-8">
    <input type="hidden" name="checkin_oldId" value="-1" />
    <input type="hidden" name="force" value="false" />
    <input type="hidden" name="webdav" value="false" />
    <c:if test="${_isI18nHandled}">
      <div class="webdav-attachment-checkin-language" style="display: none">
        <fmt:message var="tmpLabel" key="attachment.dialog.checkin.webdav.multilang.language.help"/>
        <label for="langCreate${domIdSuffix}" class="label-ui-dialog"><fmt:message key="GML.language"/></label>
        <div class="champ-ui-dialog">
          <span style="vertical-align: middle"><view:langSelect readOnly="${true}" elementName="langCreate" elementId="langCreate${domIdSuffix}" langCode="fr" includeLabel="false"/></span>
          <img style="vertical-align: middle; margin-left: 20px" class="infoBulle" title="${tmpLabel}" src="<c:url value="/util/icons/help.png"/>" alt="info"/>
        </div>
      </div>
    </c:if>
    <div class="versioned_fields_attachment-checkin" style="display:none">
      <label for="public-checkin${domIdSuffix}" class="label-ui-dialog"><fmt:message key="attachment.version.label"/></label>
      <span class="champ-ui-dialog">
        <input value="false" type="radio" name="private" id="public-checkin${domIdSuffix}" checked="checked"/><fmt:message key="attachment.version_public.label"/>
        <input value="true" type="radio" name="private"/><fmt:message key="attachment.version_wip.label"/>
      </span>
      <label for="comment${domIdSuffix}" class="label-ui-dialog"><fmt:message key="attachment.dialog.comment" /></label>
      <span class="champ-ui-dialog"><textarea name="comment" cols="60" rows="3" id="comment${domIdSuffix}"></textarea></span>
    </div>
    <div class="simple_fields_attachment-checkin" style="display:none; text-wrap: none"><fmt:message key="confirm.checkin.message" /></div>
    <input type="submit" value="Submit" style="display:none" />
  </form>
</div>

<div id="dialog-attachment-onlineEditing-customProtocol${domIdSuffix}" class="dialog-attachment-onlineEditing-customProtocol" style="display: none">
  <fmt:message key="attachment.dialog.onlineEditing.customProtocol.content"/>
</div>

<view:progressMessage/>
<c:if test="${contextualMenuEnabled && dragAndDropEnable}">
  <viewTags:attachmentDragAndDrop domSelector="#attachmentDragAndDrop${domIdSuffix}"
                                  highestUserRole="${highestUserRole}"
                                  componentInstanceId="${componentId}"
                                  resourceId="${param.Id}"
                                  resourceType="${param.Type}"
                                  contentLanguage="${contentLanguage}"
                                  hasToBeIndexed="${indexIt}"
                                  documentType="${param.Context}"
                                  isHandledModificationContext="${isHandledModificationContext}"
                                  handledSubscriptionType="${handledSubscriptionType}"
                                  handledSubscriptionResourceId="${handledSubscriptionResourceId}"
                                  handledSubscriptionLocationId="${handledSubscriptionLocationId}"
                                  completedUrlSuccessCallback="_afManager${domIdSuffix}.uploadCompleted"/>
</c:if>