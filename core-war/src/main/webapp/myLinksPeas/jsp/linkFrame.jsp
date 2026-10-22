<%--
  ~ Copyright (C) 2000 - 2026 Silverpeas
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
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib uri="jakarta.tags.fmt" prefix="fmt" %>
<%@ taglib uri="silverpeas.tags.silverFunctions" prefix="silfn" %>
<%@ taglib uri="silverpeas.tags.viewGenerator" prefix="view" %>

<%-- Set resource bundle --%>
<fmt:setLocale value="${requestScope.resources.language}"/>
<view:setBundle bundle="${requestScope.resources.multilangBundle}"/>

<fmt:message key="myLinks.frame.openInNewWindow" var="openInNewWindowLabel"/>
<fmt:message key="myLinks.frame.notDisplayed" var="notDisplayedLabel"/>

<c:set var="link" value="${requestScope.Link}"/>
<jsp:useBean id="link" type="org.silverpeas.core.mylinks.model.LinkDetail"/>
<c:set var="linkName" value="${silfn:isDefined(link.name) ? link.name : link.url}"/>

<%@ include file="check.jsp" %>

<%-- The external web page is displayed within a sandboxed frame: it is isolated from Silverpeas
     and can't navigate the windows of Silverpeas. As a site can forbid its pages to be framed by
     another site, a link to open the page in a new window is always provided. --%>
<view:sp-page>
  <view:sp-head-part>
    <view:link href="/myLinksPeas/jsp/styleSheets/linkFrame.css"/>
  </view:sp-head-part>
  <view:sp-body-part cssClass="link-frame-page">
    <div class="link-frame-bar">
      <span class="link-frame-name">${silfn:escapeHtml(linkName)}</span>
      <span class="link-frame-info">${notDisplayedLabel}</span>
      <a class="link-frame-open" href="${silfn:escapeHtml(link.url)}" target="_blank"
         rel="noopener noreferrer">${openInNewWindowLabel}</a>
    </div>
    <iframe class="link-frame" src="${silfn:escapeHtml(link.url)}" title="${silfn:escapeHtml(linkName)}"
            sandbox="allow-scripts allow-same-origin allow-forms allow-popups allow-popups-to-escape-sandbox allow-downloads"
            referrerpolicy="no-referrer"></iframe>
  </view:sp-body-part>
</view:sp-page>
