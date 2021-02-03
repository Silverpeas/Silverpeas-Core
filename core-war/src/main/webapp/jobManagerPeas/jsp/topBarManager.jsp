<%--

    Copyright (C) 2000 - 2021 Silverpeas

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
<%@ page import="org.silverpeas.core.web.look.LookHelper" %>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

<%@ include file="check.jsp" %>

<%-- Include tag library --%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>

<fmt:setLocale value="${sessionScope['SilverSessionController'].favoriteLanguage}"/>
<view:setBundle bundle="${requestScope.resources.multilangBundle}"/>
<view:setBundle bundle="${requestScope.resources.iconsBundle}" var="icons"/>
<%
  LookHelper helper = LookHelper.getLookHelper(session);
%>

<fmt:message var="backToSilverpeasLabel" key="JMP.backSilverpeas"/>
<fmt:message var="clipboardLabel" key="JMP.clipboard"/>
<fmt:message var="clipboardIconUrl" key="JMP.clipboardIcon" bundle="${icons}"/>
<c:url var="clipboardIconUrl" value="${clipboardIconUrl}"/>
<c:url var="clipboardUrl" value='<%=URLUtil.getURL(URLUtil.CMP_CLIPBOARD) + "Idle.jsp?message=SHOWCLIPBOARD"%>'/>
<c:set var="helpUrl" value='<%=helper.getSettings("helpURL", "")%>'/>
<fmt:message var="helpLabel" key="JMP.help"/>
<fmt:message var="helpIconUrl" key="JMP.help" bundle="${icons}"/>
<c:url var="helpIconUrl" value="${helpIconUrl}"/>
<fmt:message var="logoutLabel" key="JMP.exit"/>
<fmt:message var="logoutIconUrl" key="JMP.login" bundle="${icons}"/>
<c:url var="logoutIconUrl" value="${logoutIconUrl}"/>

<c:set var="serviceItems" value="${requestScope.Services}"/>
<jsp:useBean id="serviceItems" type="org.silverpeas.web.jobmanager.JobManagerService[]"/>
<c:set var="operationItems" value="${requestScope.Operation}"/>
<jsp:useBean id="operationItems" type="org.silverpeas.web.jobmanager.JobManagerService[]"/>

<div id="outilsAdmin">
  <a class="sp_back_front" href="javascript:void(0);" onclick="window.top.spWindow.leaveAdmin()">${backToSilverpeasLabel}</a>
  <a href="${clipboardUrl}" target="IdleFrame"><img src="${clipboardIconUrl}" border="0" alt="${clipboardLabel}" onfocus="self.blur()" title="${clipboardLabel}"/><span>${clipboardLabel}</span></a>
  <c:if test="${not empty helpUrl}">
    <a href="${helpUrl}" target="_blank"><img border="0" src="${helpIconUrl}" alt="${helpLabel}" title="${helpLabel}"/><span>${helpLabel}</span></a>
  </c:if>
  <a class="sp_logout" href="javascript:onclick=window.top.spUserSession.logout();"><img border="0" src="${logoutIconUrl}" alt="${logoutLabel}" title="${logoutLabel}"/><span>${logoutLabel}</span></a>
</div>
<ul class="sp_menuAdmin">
  <c:forEach var="serviceItem" items="${serviceItems}">
    <c:choose>
      <c:when test="${not serviceItem.actif}">
        <li>
          <a href="javascript:void(0)" onclick="spAdminWindow.loadService(${serviceItem.id})">
            <span><fmt:message key="${serviceItem.label}"/></span>
          </a>
        </li>
      </c:when>
      <c:otherwise>
        <li class="select">
          <span class="textePetitBold"><fmt:message key="${serviceItem.label}"/></span>
        </li>
      </c:otherwise>
    </c:choose>
  </c:forEach>
</ul>
<c:if test="${not empty operationItems}">
  <div class="sp_sousMenuAdmin">
    <c:forEach var="operationItem" items="${operationItems}">
      <c:choose>
        <c:when test="${not operationItem.actif}">
          <a href="javascript:void(0)" onclick="spAdminWindow.loadOperation(${operationItem.id})">
            <fmt:message key="${operationItem.label}"/>
          </a>
        </c:when>
        <c:otherwise>
          <span class="select"><fmt:message key="${operationItem.label}"/></span>
        </c:otherwise>
      </c:choose>
    </c:forEach>
  </div>
</c:if>
<c:if test="${empty param.layoutInitialization}">
  <script type="text/javascript">
    spAdminLayout.getBody().load('${requestScope.adminBodyUrl}');
  </script>
</c:if>