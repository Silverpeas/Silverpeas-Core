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
<%@page import="org.silverpeas.kernel.util.StringUtil" %>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page isELIgnored="false" %>

<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib uri="jakarta.tags.functions" prefix="fn"  %>
<%@ taglib uri="jakarta.tags.fmt" prefix="fmt" %>
<%@ taglib uri="silverpeas.tags.viewGenerator" prefix="view" %>
<%@ taglib uri="silverpeas.tags.silverFunctions" prefix="silfn" %>

<%@ page import="org.silverpeas.core.web.mvc.controller.MainSessionController" %>
<%@ page import="org.silverpeas.core.contribution.attachment.model.SimpleDocument" %>
<%@ page import="org.silverpeas.core.admin.user.model.UserDetail" %>
<%@ page import="jakarta.ws.rs.core.UriBuilder" %>
<%@ page errorPage="../../admin/jsp/errorpage.jsp" %>

<%
  MainSessionController mainSessionCtrl = (MainSessionController) session
      .getAttribute(MainSessionController.MAIN_SESSION_CONTROLLER_ATT);
  SimpleDocument theDocument = (SimpleDocument) request.getAttribute("Document");

  String componentId = theDocument.getPk().getInstanceId();
  String id = theDocument.getPk().getId();
  String contentLanguage = (String) request.getAttribute("ContentLanguage");
  boolean fromAlias = (boolean) request.getAttribute("fromAlias");

  UriBuilder uriBuilder = UriBuilder.fromPath("ViewAllVersions")
      .queryParam("DocId", id)
      .queryParam("ComponentId", componentId)
      .queryParam("fromAlias", fromAlias);
  if (StringUtil.isDefined(contentLanguage)) {
    uriBuilder = uriBuilder.queryParam("Language", contentLanguage);
  }
  String routingAdress = uriBuilder.build().toString();

%>

<c:set var="routingAdress" value="<%=routingAdress%>"/>
<c:set var="context" value="${pageContext.request.contextPath}"/>
<c:set var="document" value="${pageContext.request.getAttribute('Document')}"/>
<c:set var="versions" value="${pageContext.request.getAttribute('Versions')}"/>
<jsp:useBean id="versions" type="java.util.List<org.silverpeas.core.contribution.attachment.model.HistorisedDocument>"/>
<c:set var="language" value="${sessionScope.SilverSessionController.favoriteLanguage}"/>
<c:set var="mainSessionController" value="<%=mainSessionCtrl%>" />

<fmt:setLocale value="${sessionScope.SilverSessionController.favoriteLanguage}"/>
<view:setBundle basename="org.silverpeas.util.attachment.multilang.attachment" var="attachmentBundle" />
<view:setBundle basename="org.silverpeas.versioningPeas.multilang.versioning" var="versioningBundle"/>
<view:settings settings="org.silverpeas.versioningPeas.settings.versioningSettings" key="Pagination.NbItemPerPage" var="nbItemPerPage"/>
<view:setBundle basename="org.silverpeas.multilang.generalMultilang" var="GMLBundle" />

<view:sp-page>
  <view:sp-head-part>
    <view:includePlugin name="qtip"/>
    <view:includePlugin name="iframeajaxtransport"/>
    <view:includePlugin name="attachment"/>

    <script type="text/javascript">
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
                text : '<fmt:message key="attachment.xmlForm.ToolTip" bundle="${attachmentBundle}"/> \"' + $(this).attr('title') + "\"", // Give the tooltip a title using each elements text
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
          });
        });
      });

      function preview(target, attachmentId) {
        $(target).preview("document", {
          documentId: encodeURIComponent(attachmentId),
          documentType: 'attachment',
          lang: '${language}',
          versioned: true
        });
        return false;
      }

      function view(target, attachmentId) {
        $(target).view("document", {
          documentId: encodeURIComponent(attachmentId),
          documentType: 'attachment',
          lang: '${language}'
        });
        return false;
      }

    </script>
  </view:sp-head-part>
  <view:sp-body-part>
    <view:window popup="true">
      <view:browseBar extraInformations="${requestScope.Document.title}" clickable="false"/>

      <fmt:message bundle="${GMLBundle}" key="GML.attachments" var="gmlAttachments"/>
      <fmt:message bundle="${GMLBundle}" key="GML.title" var="gmlTitle"/>
      <fmt:message bundle="${attachmentBundle}" key="CopyLink" var="copyLinkLabel"/>
      <fmt:message bundle="${versioningBundle}" key="version" var="versionLabel"/>
      <fmt:message bundle="${versioningBundle}" key="description" var="descriptionLabel"/>
      <fmt:message bundle="${versioningBundle}" key="creator" var="creatorLabel"/>
      <fmt:message bundle="${versioningBundle}" key="date" var="dateLabel"/>
      <fmt:message bundle="${versioningBundle}" key="comments" var="commentsLabel"/>

      <view:arrayPane var="List" routingAddress="${routingAdress}" export="true" numberLinesPerPage="${nbItemPerPage}">
        <view:arrayColumn title="${versionLabel}" compareOn="${publicVersion -> publicVersion.majorVersion}"/>
        <view:arrayColumn title="${gmlAttachments}" sortable="false"/>
        <view:arrayColumn title="${gmlTitle}" sortable="false"/>
        <view:arrayColumn title="${descriptionLabel}" sortable="false"/>
        <view:arrayColumn title="${creatorLabel}" sortable="true"/>
        <view:arrayColumn title="${dateLabel}" sortable="false"/>
        <view:arrayColumn title="${commentsLabel}" sortable="false"/>

        <view:arrayLines var="publicVersion" items="${versions}">
          <view:arrayLine id="line">
            <c:set var="canUserDownloadFile" value="${publicVersion.isDownloadAllowedForRolesFrom(mainSessionController.currentUserDetail)}"/>
            <c:set var="url" value="${context}${publicVersion.attachmentURL}"/>
            <c:if test="${canUserDownloadFile}">
              <c:set var="imgLink" value='<img src="${context}/util/icons/link.gif" alt="${copyLinkLabel}" title="${copyLinkLabel}"'/>
              <c:set var="permalink" value='<a href="${publicVersion.universalURL}" target="_blank">${imgLink}"></a>'/>
            </c:if>
            <view:arrayCellText>
              ${publicVersion.version}
              <c:if test="${canUserDownloadFile}">
                ${permalink}
              </c:if>
              <c:set var="versionId" value="${publicVersion.id}"/>
              <c:if test="${silfn:isPreviewable(publicVersion.attachmentPath)}">
                <a href="javascript:preview(this,'${versionId}')"><img class="preview-file" src='<c:url value="/util/icons/preview.png"/>' alt="<fmt:message bundle="${GMLBundle}" key="GML.preview.file"/>" title="<fmt:message bundle="${GMLBundle}" key="GML.preview.file" />"/></a>
              </c:if>
              <c:if test="${silfn:isViewable(publicVersion.attachmentPath)}">
                <a href="javascript:view(this, '${versionId}')"><img class="view-file" src='<c:url value="/util/icons/view.png"/>' alt="<fmt:message bundle="${GMLBundle}" key="GML.view.file"/>" title="<fmt:message bundle="${GMLBundle}" key="GML.view.file" />"/></a>
              </c:if>
            </view:arrayCellText>

            <view:arrayCellText>
              <c:choose>
                <c:when test="${canUserDownloadFile}">
                  <img src="${silfn:fileIcon(publicVersion.filename)}" alt="">
                  <a href='<c:out value="${publicVersion.universalURL}" />' target="_blank"><c:out value="${publicVersion.filename}" /></a>
                </c:when>
                <c:otherwise>
                  <c:set var="forbiddenDownloadClass" value="forbidden-download"/>
                  <fmt:message key="GML.download.forbidden" var="forbiddenDownloadHelp"/>
                  <c:out value="${publicVersion.filename}" />
                </c:otherwise>
              </c:choose>
            </view:arrayCellText>

            <view:arrayCellText>
              ${publicVersion.title}
            </view:arrayCellText>

            <view:arrayCellText>
              <view:encodeHtmlParagraph string="${publicVersion.description}" />
            </view:arrayCellText>

            <view:arrayCellText>
              <c:choose>
                <c:when test="${silfn:isDefined(publicVersion.updatedBy)}">
                  <c:set var="lastUpdater" value="${UserDetail.getById(publicVersion.updatedBy).displayedName}"/>
                </c:when>
                <c:otherwise>
                  <c:set var="lastUpdater" value=""/>
                </c:otherwise>
              </c:choose>
              <c:choose>
                <c:when test="${silfn:isDefined(lastUpdater)}">
                  ${lastUpdater}
                </c:when>
                <c:otherwise>
                  <c:out value="????"/>
                </c:otherwise>
              </c:choose>
            </view:arrayCellText>

            <view:arrayCellText>
              ${silfn:formatDateAndHour(publicVersion.lastUpdateDate,language)}
            </view:arrayCellText>

            <view:arrayCellText>
              <view:encodeHtmlParagraph string="${publicVersion.comment}" />
            </view:arrayCellText>
          </view:arrayLine>
        </view:arrayLines>
      </view:arrayPane>
    </view:window>
  </view:sp-body-part>
</view:sp-page>
