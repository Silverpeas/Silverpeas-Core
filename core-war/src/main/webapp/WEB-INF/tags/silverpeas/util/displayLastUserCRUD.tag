<%@ tag import="org.silverpeas.core.admin.user.model.UserDetail" %>
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

<%-- Fragments --%>
<%@ attribute name="beforeCommonContentBloc" required="false" fragment="true"
              description="Bloc displayed before common elements" %>
<%@ attribute name="afterCommonContentBloc" required="false" fragment="true"
              description="Bloc displayed after common elements" %>

<%-- Creator --%>
<%@ attribute name="createDate" required="false" type="java.util.Date"
              description="The date of create" %>
<%@ attribute name="createdBy" required="false" type="org.silverpeas.core.admin.user.model.UserDetail"
              description="The user responsible of the create" %>
<%@ attribute name="createdById" required="false" type="java.lang.String"
              description="The user id responsible of the create" %>
<c:if test="${createdBy == null && silfn:isDefined(createdById)}">
  <c:set var="createdBy" value="<%=UserDetail.getById(createdById)%>"/>
</c:if>

<%-- Updater --%>
<%@ attribute name="updateDate" required="false" type="java.util.Date"
              description="The date of update" %>
<%@ attribute name="updatedBy" required="false" type="org.silverpeas.core.admin.user.model.UserDetail"
              description="The user responsible of the update" %>
<%@ attribute name="updatedById" required="false" type="java.lang.String"
              description="The user id responsible of the update" %>
<c:if test="${updatedBy == null && silfn:isDefined(updatedById)}">
  <c:set var="updatedBy" value="<%=UserDetail.getById(updatedById)%>"/>
</c:if>

<%-- Publisher --%>
<%@ attribute name="publishDate" required="false" type="java.util.Date"
              description="The date of publishing" %>
<%@ attribute name="publishedBy" required="false" type="org.silverpeas.core.admin.user.model.UserDetail"
              description="The user who have published " %>
<%@ attribute name="publishedById" required="false" type="java.lang.String"
              description="The user id who have published" %>
<c:if test="${publishedBy == null && silfn:isDefined(publishedById)}">
  <c:set var="publishedBy" value="<%=UserDetail.getById(publishedById)%>"/>
</c:if>

<%-- Permalink --%>
<%@ attribute name="permalink" required="false" type="java.lang.String"
              description="A permalink to display" %>
<%@ attribute name="permalinkHelp" required="false" type="java.lang.String"
              description="The permalink help." %>
<%@ attribute name="permalinkIconUrl" required="false" type="java.lang.String"
              description="The permalink url" %>
<c:if test="${permalinkIconUrl == null}">
  <c:set var="permalinkIconUrl" value="../../util/icons/link.gif"/>
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

<div class="bgDegradeGris crud-container" id="link-domain-content">
  <jsp:invoke fragment="beforeCommonContentBloc"/>

  <c:if test="${publishDate != null && publishedBy != null}">
    <div class="paragraphe" id="publishedAtInfo">
      <fmt:message key="GML.publishedAt" bundle="${generalBundle}"/>
      <b><c:choose><c:when test="${displayHour}">${silfn:formatDateAndHour(publishDate, _language)}</c:when><c:otherwise>${silfn:formatDate(publishDate, _language)}</c:otherwise></c:choose></b>
      <fmt:message key="GML.by" bundle="${generalBundle}"/>
      <view:username userId="${publishedBy.id}" zoom="${displayUserZoom}"/>
      <div class="profilPhoto">
        <view:image src="${publishedBy.avatar}" alt="" type="avatar" css="defaultAvatar"/>
      </div>
    </div>
  </c:if>

  <c:if test="${updateDate != null && updatedBy != null && (createDate == null || fn:endsWith(createDate.time, '000') || createDate.time < updateDate.time)}">
    <div class="paragraphe" id="lastModificationInfo">
      <fmt:message key="GML.updatedAt" bundle="${generalBundle}"/>
      <b><c:choose><c:when test="${displayHour}">${silfn:formatDateAndHour(updateDate, _language)}</c:when><c:otherwise>${silfn:formatDate(updateDate, _language)}</c:otherwise></c:choose></b>
      <fmt:message key="GML.by" bundle="${generalBundle}"/>
      <view:username userId="${updatedBy.id}" zoom="${displayUserZoom}"/>
      <div class="profilPhoto">
        <view:image src="${updatedBy.avatar}" alt="" type="avatar" css="defaultAvatar"/>
      </div>
    </div>
  </c:if>

  <c:if test="${createDate != null && createdBy != null}">
    <div class="paragraphe" id="createdInfo">
      <fmt:message key="GML.createdAt" bundle="${generalBundle}"/>
      <b><c:choose><c:when test="${displayHour}">${silfn:formatDateAndHour(createDate, _language)}</c:when><c:otherwise>${silfn:formatDate(createDate, _language)}</c:otherwise></c:choose></b>
      <fmt:message key="GML.by" bundle="${generalBundle}"/>
      <view:username userId="${createdBy.id}" zoom="${displayUserZoom}"/>
      <div class="profilPhoto">
        <view:image src="${createdBy.avatar}" alt="" type="avatar" css="defaultAvatar"/>
      </div>
    </div>
  </c:if>

  <c:if test="${not empty permalink}">
    <c:url value="/" var="applicationPrefix"/>
    <c:set value="/${fn:replace(permalink, applicationPrefix, '')}" var="permalink"/>
    <p id="permalinkInfo">
      <a title="${permalinkHelp}" href="<c:url value="${permalink}"/>">
      <img border="0" alt='${permalinkHelp}' title='${permalinkHelp}' src="${permalinkIconUrl}"/>
      </a> <fmt:message key="GML.permalink" bundle="${generalBundle}"/>
      <input type="text" value="${silfn:fullApplicationURL(pageContext.request)}${permalink}" onfocus="select();" class="inputPermalink"/>
    </p>
  </c:if>

  <jsp:invoke fragment="afterCommonContentBloc"/>
  <br clear="all"/>
</div>
