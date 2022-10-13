<%@ tag import="org.silverpeas.core.util.URLUtil" %>
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

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://www.silverpeas.com/tld/silverFunctions" prefix="silfn" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>

<%-- Set resource bundle --%>
<fmt:setLocale value="${requestScope.resources.language}"/>
<view:setBundle bundle="${requestScope.resources.multilangBundle}"/>
<view:setBundle bundle="${requestScope.resources.iconsBundle}" var="icons"/>
<view:setBundle basename="org.silverpeas.mylinks.multilang.myLinksBundle" var="profile"/>

<fmt:message key="myLinks.updateLink" var="updateLinkLabel"/>
<fmt:message key="myLinks.update" var="updateLinkIcon" bundle="${icons}"/>
<c:url var="updateLinkIcon" value="${updateLinkIcon}"/>

<%-- Attributes --%>
<%@ attribute name="links" required="true"
              type="java.util.List<org.silverpeas.core.mylinks.model.LinkDetail>"
              description="List of favorite links"  %>
<%@ attribute name="prefix" required="false"
              type="java.lang.String"
              description="List of favorite links"  %>
<script type="text/javascript">
  checkboxMonitors.register('#link-list${prefix} input[name=linkCheck]');
</script>
<div id="link-list${prefix}">
  <input type="hidden" name="mode"/>
  <view:arrayPane var="linkList${prefix}" routingAddress="ViewLinks"
                  sortableLines="true" numberLinesPerPage="-1"
                  moveLineJsCallback="saveArrayLinesOrder(e, ui)">
    <fmt:message key="GML.nom" var="tmpLabel"/>
    <view:arrayColumn title="${tmpLabel}" sortable="false"/>
    <fmt:message key="GML.description" var="tmpLabel"/>
    <view:arrayColumn title="${tmpLabel}" sortable="false"/>
    <fmt:message key="GML.operations" var="tmpLabel"/>
    <view:arrayColumn title="${tmpLabel}" sortable="false" width="80px"/>
    <c:forEach items="${links}" var="link">
      <view:arrayLine classes="link-line">
        <c:set var="id" value="${link.linkId}"/>
        <c:set var="linkUrl" value="${link.url}"/>
        <c:set var="name" value="${silfn:isDefined(link.name) ? link.name : linkUrl}"/>
        <c:if test="${not fn:contains(linkUrl, '://') and not fn:startsWith(linkUrl, '/website')}">
          <c:url var="linkUrl" value="${link.url}"/>
        </c:if>
        <view:arrayCellText>
          <c:set var="nameTarget" value=""/>
          <c:set var="nameClass" value=""/>
          <c:choose>
            <c:when test="${link.popup}">
              <c:set var="nameTarget" value="_blank"/>
            </c:when>
            <c:when test="${fn:startsWith(linkUrl, silfn:applicationURL())}">
              <c:set var="nameClass" value="sp-link"/>
            </c:when>
          </c:choose>
          <a href="${linkUrl}" class="${nameClass}" target="${nameTarget}">${silfn:escapeHtml(name)}</a>
        </view:arrayCellText>
        <view:arrayCellText text="${silfn:escapeHtml(link.description)}"/>
        <view:arrayCellText>
          <a href="javaScript:editLink('${id}')" title="${updateLinkLabel}">
            <img src="${updateLinkIcon}" alt="${updateLinkLabel}" title="${updateLinkLabel}">
          </a>
          <span>&#160;&#160;</span>
          <input type="checkbox" name="linkCheck" value="${id}">
          <input type="hidden" name="hiddenLinkId" value="${id}">
        </view:arrayCellText>
      </view:arrayLine>
    </c:forEach>
  </view:arrayPane>
  <script type="text/javascript">
    whenSilverpeasReady(function() {
      checkboxMonitors.pageChanged();
      arrayPaneAjaxControls.register('#link-list${prefix}');
    });
  </script>
</div>