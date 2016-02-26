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

<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>

<!--Load the resource bundle for the page -->
<view:setBundle basename="DesktopMessages" />

<div class="portlet">

  <div class="portlet-header">

    <h2 class="portlet-title"><c:out value="${portlet.title}" escapeXml="false"/></h2>

    <ul class="portlet-options">

      <c:if test="${portlet.minimized==true}">
        <li>
          <a href="<c:out value="${portlet.normalizedURL}"/>">
            <img src="/silverpeas/portlet/jsp/jsr/images/unminimize_button.gif" alt="<fmt:message key="unminimize"/>" title="<fmt:message key="unminimize"/>" />
          </a>
        </li>
      </c:if>
      <c:if test="${portlet.minimized==false}">
          <li>
            <a href="<c:out value="${portlet.minimizedURL}"/>">
              <img src="/silverpeas/portlet/jsp/jsr/images/minimize_button.gif" alt="<fmt:message key="minimize"/>" title="<fmt:message key="minimize"/>" />
            </a>
          </li>
      </c:if>
      <c:if test="${portlet.maximized==true}">
          <li>
            <a href="<c:out value="${portlet.normalizedURL}"/>">
              <img src="/silverpeas/portlet/jsp/jsr/images/unmaximize_button.gif" alt="<fmt:message key="unmaximize"/>" title="<fmt:message key="unmaximize"/>" />
            </a>
          </li>
      </c:if>
      <c:if test="${portlet.maximized==false}">
          <li>
            <a href="<c:out value="${portlet.maximizedURL}"/>">
              <img src="/silverpeas/portlet/jsp/jsr/images/maximize_button.gif" alt="<fmt:message key="maximize"/>" title="<fmt:message key="maximize"/>" />
            </a>
          </li>
      </c:if>
      <c:if test="${portlet.help==true}">
        <li>
          <a href="<c:out value="${portlet.helpURL}"/>">
            <img src="/silverpeas/portlet/jsp/jsr/images/help_button.gif" alt="<fmt:message key="help"/>" title="<fmt:message key="help"/>" />
          </a>
        </li>
      </c:if>
      <c:if test="${portlet.edit==true}">
        <li>
          <a href="<c:out value="${portlet.editURL}"/>">
            <img src="/silverpeas/portlet/jsp/jsr/images/edit_button.gif" alt="<fmt:message key="edit"/>" title="<fmt:message key="edit"/>" />
          </a>
        </li>
      </c:if>
      <c:if test="${portlet.view==true}">
        <li>
          <a href="<c:out value="${portlet.viewURL}"/>">
            <img src="/silverpeas/portlet/jsp/jsr/images/view_button.gif" alt="<fmt:message key="view"/>" title="<fmt:message key="view"/>" />
          </a>
        </li>
      </c:if>
      <li>
        <a href="<c:out value="${portlet.removeURL}"/>">
          <img src="/silverpeas/portlet/jsp/jsr/images/remove_button.gif" alt="<fmt:message key="remove"/>" title="<fmt:message key="remove"/>" />
        </a>
      </li>
    </ul>

  </div> <!-- closes portlet-header -->

  <c:choose>
    <c:when test="${portlet.minimized==false}">
      <div class="portlet-content">
        <c:out value="${portlet.content}" escapeXml="false"/>
      </div> <!-- closes portlet-content -->
    </c:when>
    <c:otherwise>
      <div class="portlet-content-minimized"></div> <!-- portlet content minimized -->
    </c:otherwise>
  </c:choose>

</div> <!-- closes portlet -->
