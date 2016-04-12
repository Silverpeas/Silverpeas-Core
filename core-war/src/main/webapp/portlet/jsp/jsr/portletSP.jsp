<%--

    Copyright (C) 2000 - 2013 Silverpeas

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

<%@page import="org.silverpeas.core.web.portlets.FormNames"%>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>

<!--Load the resource bundle for the page -->
<fmt:setLocale value="${sessionScope['SilverSessionController'].favoriteLanguage}"/>
<view:setBundle basename="org.silverpeas.portlets.multilang.portletsBundle"/>

<c:set var="disableMove" value="${requestScope['DisableMove']}"/>

<c:choose>
  <c:when test="${disableMove==true}">
    <dt style="cursor:default">
  </c:when>
  <c:otherwise>
    <dt>
  </c:otherwise>
</c:choose>

<div>
  <h2 class="portlet-title"><c:out value="${portlet.title}" escapeXml="false"/></h2>
  <ul class="portlet-options">
    <c:if test="${portlet.minimized==true}">
      <li>
        <a href="<c:out value="${portlet.normalizedURL}"/>">
          <img src="/silverpeas/portlet/jsp/jsr/images/set2/unminimize_button.gif" alt="<fmt:message key="portlets.window.unminimize"/>" title="<fmt:message key="portlets.window.unminimize"/>"/>
        </a>
      </li>
    </c:if>
    <c:if test="${portlet.minimized==false && portlet.maximized==false}">
      <li>
        <a href="<c:out value="${portlet.minimizedURL}"/>">
          <img src="/silverpeas/portlet/jsp/jsr/images/set2/minimize_button.gif" alt="<fmt:message key="portlets.window.minimize"/>" title="<fmt:message key="portlets.window.minimize"/>"/>
        </a>
      </li>
    </c:if>
    <c:if test="${portlet.maximized==true}">
      <li>
        <a href="<c:out value="${portlet.normalizedURL}"/>">
          <img src="/silverpeas/portlet/jsp/jsr/images/set2/unmaximize_button.gif" alt="<fmt:message key="portlets.window.unmaximize"/>" title="<fmt:message key="portlets.window.unmaximize"/>"/>
        </a>
      </li>
    </c:if>
    <c:if test="${portlet.maximized==false}">
      <li>
        <a href="<c:out value="${portlet.maximizedURL}"/>">
          <img src="/silverpeas/portlet/jsp/jsr/images/set2/maximize_button.gif" alt="<fmt:message key="portlets.window.maximize"/>" title="<fmt:message key="portlets.window.maximize"/>"/>
        </a>
      </li>
    </c:if>
    <c:if test="${portlet.help==true}">
      <li>
        <a href="<c:out value="${portlet.helpURL}"/>">
          <img src="/silverpeas/portlet/jsp/jsr/images/set2/help_button.gif" alt="<fmt:message key="portlets.window.help"/>" title="<fmt:message key="portlets.window.help"/>"/>
        </a>
      </li>
    </c:if>
    <c:if test="${portlet.edit==true}">
      <li>
        <a href="<c:out value="${portlet.editURL}"/>">
          <img src="/silverpeas/portlet/jsp/jsr/images/set2/edit_button.gif" alt="<fmt:message key="portlets.window.edit"/>" title="<fmt:message key="portlets.window.edit"/>"/>
        </a>
      </li>
    </c:if>
    <!--<c:if test="${portlet.view==true}">
	        <li>
	          <a href="<c:out value="${portlet.viewURL}"/>">
	            <img src="/silverpeas/portlet/jsp/jsr/images/set2/view_button.gif" alt="<fmt:message key="portlets.window.view"/>" title="<fmt:message key="portlets.window.view"/>" />
	          </a>
	        </li>
	      </c:if>-->
    <c:if test="${portlet.remove==true}">
      <li>
        <a href="<c:out value="${portlet.removeURL}"/>">
          <img src="/silverpeas/portlet/jsp/jsr/images/set2/remove_button.gif" alt="<fmt:message key="portlets.window.remove"/>" title="<fmt:message key="portlets.window.remove"/>"/>
        </a>
      </li>
    </c:if>
  </ul>
</div>
</dt>
<dd>
  <c:choose>
    <c:when test="${portlet.minimized==false}">
      <div class="portlet-content">
        <c:out value="${portlet.content}" escapeXml="false"/>
        <c:if test="${portlet.currentMode == 'help'}">
        <div class="portlet-backFromHelp"><input class="portlet-form-button" name="<%=FormNames.SUBMIT_FINISHED%>" type="submit" onclick="location.href='<c:out value="${portlet.viewURL}"/>'" value="<fmt:message key="GML.back"/>"/></div>
        </c:if>
      </div>
      <!-- closes portlet-content -->
    </c:when>
    <c:otherwise>
      <div class="portlet-content-minimized"></div>
      <!-- portlet content minimized -->
    </c:otherwise>
  </c:choose>
</dd>