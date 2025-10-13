<%--

    Copyright (C) 2000 - 2024 Silverpeas

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
    along with this program.  If not, see <https://www.gnu.org/licenses/>.

--%>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://www.silverpeas.com/tld/silverFunctions" prefix="silfn" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>

<fmt:setLocale value="${sessionScope['SilverSessionController'].favoriteLanguage}"/>
<view:setBundle bundle="${requestScope.resources.multilangBundle}"/>
<view:setBundle bundle="${requestScope.resources.iconsBundle}" var="icons"/>

<fmt:message var="domainsLabel" key="JDP.domains"/>
<fmt:message var="mixedDomainLabel" key="JDP.domainMixt"/>
<fmt:message var="groupIconUrl" key="JDP.group" bundle="${icons}"/>
<c:url var="groupIconUrl" value="${groupIconUrl}"/>
<fmt:message var="synchronizedGroupIconUrl" key="JDP.groupSynchronized" bundle="${icons}"/>
<c:url var="synchronizedGroupIconUrl" value="${synchronizedGroupIconUrl}"/>
<fmt:message var="selectLabel" key="GML.select"/>
<fmt:message var="groupLabel" key="GML.groupe"/>

<c:set var="allDomains" value="${requestScope.allDomains}"/>
<jsp:useBean id="allDomains" type="java.util.List<org.silverpeas.core.admin.domain.model.Domain>"/>
<c:set var="currentDomainId" value=""/>
<c:if test="${requestScope.CurrentDomain != null}">
  <c:set var="currentDomainId" value="${requestScope.CurrentDomain.id}"/>
</c:if>
<c:set var="domainRootGroups" value="${requestScope.domainRootGroups}"/>
<c:set var="appRootGroups" value="${requestScope.appRootGroups}"/>
<jsp:useBean id="domainRootGroups" type="org.silverpeas.core.admin.user.model.Group[]"/>
<jsp:useBean id="appRootGroups" type="org.silverpeas.core.admin.user.model.Group[]"/>

<%@ include file="check.jsp" %>

<script type="text/javascript">
  function viewDomain(domainId) {
    spAdminWindow.loadDomain(domainId);
  }

  function viewGroup(groupId) {
    spAdminWindow.loadGroup(groupId);
  }

  function refreshCurrentLevel() {
    spAdminLayout.getBody().getNavigation().load(webContext + "/RjobDomainPeas/jsp/domainRefreshCurrentLevel");
  }
</script>
<div class="intfdcolor">
  <span class="domains-label">${domainsLabel} : </span>
</div>
<div class="intfdcolor51">
  <c:choose>
    <c:when test="${fn:length(allDomains) > 1}">
      <form class="domainsNamesForm" name="domainsNamesForm" action="javascript:viewDomain(document.domainsNamesForm.Iddomain.value)" Method="POST">
        <span class="selectNS">
        <select name="Iddomain" size="1" onchange="document.domainsNamesForm.submit()">
          <option value="">${selectLabel}</option>
          <c:forEach var="domain" items="${allDomains}">
            <c:set var="domainName" value="${domain.name}"/>
            <c:if test="${domain.mixedOne}">
              <c:set var="domainName" value="${mixedDomainLabel}"/>
              <option value="">-----------------</option>
            </c:if>
            <option value="${domain.id}" ${domain.id eq currentDomainId ? 'selected' : ''}>${domainName}</option>
            <c:if test="${domain.mixedOne}">
              <option value="">-----------------</option>
            </c:if>
          </c:forEach>
        </select>
        </span>
      </form>
    </c:when>
    <c:otherwise>
      <span class="txtlibform">${allDomains[0].name}</span>
    </c:otherwise>
  </c:choose>
</div>
<c:if test="${fn:length(domainRootGroups) > 0}">
  <div class="intfdcolor51 domain-group-list">
    <c:forEach var="group" items="${domainRootGroups}">
      <img class="GroupIcon" src="${group.synchronized ? synchronizedGroupIconUrl : groupIconUrl}" alt="${groupLabel}" title="${groupLabel}"/>
      &nbsp;<button type="button" class="link" onclick="viewGroup('${group.id}');">${silfn:escapeHtml(group.name).concat(' (').concat(group.totalUsersCount).concat(')')}</button><br/>
    </c:forEach>
  </div>
</c:if>
<c:if test="${fn:length(appRootGroups) > 0}">
    <c:if test="${fn:length(domainRootGroups) > 0}">
     <hr/>
    </c:if>
    <div class="intfdcolor51 domain-group-list">
        <c:forEach var="group" items="${appRootGroups}">
            <img class="GroupIcon" src="${groupIconUrl}" alt="${groupLabel}" title="${groupLabel}"/>
            &nbsp;<button type="button" class="link" onclick="viewGroup('${group.id}');">${silfn:escapeHtml(group.name).concat(' (').concat(group.totalUsersCount).concat(')')}</button><br/>
        </c:forEach>
    </div>
</c:if>
<script type="text/javascript">
  spAdminLayout.getBody().getNavigation().dispatchEvent('load');
</script>