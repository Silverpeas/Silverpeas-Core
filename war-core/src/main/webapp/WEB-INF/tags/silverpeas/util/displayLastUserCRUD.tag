<%@ tag import="com.stratelia.webactiv.beans.admin.UserDetail" %>
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

<%-- Creator --%>
<%@ attribute name="createDate" required="false" type="java.util.Date"
              description="The date of create" %>
<%@ attribute name="createdBy" required="false" type="com.stratelia.webactiv.beans.admin.UserDetail"
              description="The user responsible of the create" %>
<%@ attribute name="createdById" required="false" type="java.lang.String"
              description="The user id responsible of the create" %>
<c:if test="${createdBy == null && silfn:isDefined(createdById)}">
  <c:set var="createdBy" value="<%=UserDetail.getById(createdById)%>"/>
</c:if>

<%-- Updater --%>
<%@ attribute name="updateDate" required="false" type="java.util.Date"
              description="The date of update" %>
<%@ attribute name="updatedBy" required="false" type="com.stratelia.webactiv.beans.admin.UserDetail"
              description="The user responsible of the update" %>
<%@ attribute name="updatedById" required="false" type="java.lang.String"
              description="The user id responsible of the update" %>
<c:if test="${updatedBy == null && silfn:isDefined(updatedById)}">
  <c:set var="updatedBy" value="<%=UserDetail.getById(updatedById)%>"/>
</c:if>

<%@ attribute name="displayHour" required="false" type="java.lang.Boolean"
              description="Display the hour of the dates" %>
<c:if test="${displayHour == null}">
  <c:set var="displayHour" value="${false}"/>
</c:if>

<%@ attribute name="displayUserZoom" required="false" type="java.lang.Boolean"
              description="Activate the user zoom plugin on each user displayed" %>
<c:if test="${displayUserZoom == null}">
  <c:set var="displayUserZoom" value="${true}"/>
</c:if>

<div class="bgDegradeGris" id="link-domain-content">
  <c:if test="${updateDate != null && updatedBy != null}">
    <div class="paragraphe" id="lastModificationInfo"><fmt:message key="GML.updatedAt" bundle="${generalBundle}"/><br>
      <b><c:choose><c:when test="${displayHour}">${silfn:formatDateAndHour(updateDate, _language)}</c:when><c:otherwise>${silfn:formatDate(updateDate, _language)}</c:otherwise></c:choose></b>
      <fmt:message key="GML.by" bundle="${generalBundle}"/>
      <view:username userId="${updatedBy.id}" zoom="${displayUserZoom}"/>
      <div class="profilPhoto">
        <img src='<c:url value="${updatedBy.avatar}" />' alt="" class="defaultAvatar"/>
      </div>
    </div>
  </c:if>

  <c:if test="${createDate != null && createdBy != null}">
    <div class="paragraphe" id="lastModificationInfo"><fmt:message key="GML.createdAt" bundle="${generalBundle}"/><br>
      <b><c:choose><c:when test="${displayHour}">${silfn:formatDateAndHour(createDate, _language)}</c:when><c:otherwise>${silfn:formatDate(createDate, _language)}</c:otherwise></c:choose></b>
      <fmt:message key="GML.by" bundle="${generalBundle}"/>
      <view:username userId="${createdBy.id}" zoom="${displayUserZoom}"/>
      <div class="profilPhoto">
        <img src='<c:url value="${createdBy.avatar}"/>' alt="" class="defaultAvatar"/>
      </div>
    </div>
  </c:if>

  <br clear="all"/>
</div>
