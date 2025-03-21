<%--
  Copyright (C) 2000 - 2024 Silverpeas

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

<%@ tag import="org.silverpeas.core.contribution.content.wysiwyg.service.WysiwygController" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://www.silverpeas.com/tld/silverFunctions" prefix="silfn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>
<%@ taglib prefix="plugins" tagdir="/WEB-INF/tags/silverpeas/plugins" %>

<c:set var="_language" value="${requestScope.resources.language}"/>
<fmt:setLocale value="${_language}"/>
<view:setBundle basename="org.silverpeas.multilang.generalMultilang" var="generalBundle"/>
<view:setBundle basename="org.silverpeas.wysiwyg.multilang.wysiwygBundle" var="wysiwygBundle"/>

<%@ attribute name="editorName" required="true" type="java.lang.String"
              description="Name of the wysiwyg editor" %>
<%@ attribute name="componentId" required="true" type="java.lang.String"
              description="Id of current component" %>
<%@ attribute name="objectId" required="true" type="java.lang.String"
              description="Id of current object" %>
<%@ attribute name="path" required="false" type="java.lang.String"
              description="Path used in websites application" %>

<c:set var="_context" value="${silfn:applicationURL()}"/>
<c:set var="_webSites" value="<%=WysiwygController.WYSIWYG_WEBSITES%>"/>

<c:if test="${fn:startsWith(componentId, _webSites) and silfn:isLongValue(objectId)}">

  <c:set var="_specificURL">/website/${componentId}/${objectId}/</c:set>
  <c:set var="_tabImages" value="${silfn:webSiteImages(path, componentId)}"/>

  <!-- list of images already uploaded for the current object -->
  <c:if test="${fn:startsWith(componentId, _webSites)}">
    <c:if test="${not empty _tabImages}">
      <select id="images" name="images" onchange="choixImage('${editorName}');this.selectedIndex=0">
        <option selected="selected"><fmt:message key="Image" bundle="${wysiwygBundle}"/></option>
        <c:forEach var="image" items="${_tabImages}">
          <option value="${_specificURL}${image[0]}">${image[1]}</option>
        </c:forEach>
      </select>
    </c:if>
  </c:if>

</c:if>