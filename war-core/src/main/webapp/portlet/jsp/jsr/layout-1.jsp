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

<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

<fmt:setLocale value="${sessionScope['SilverSessionController'].favoriteLanguage}"/>
<c:set value="${sessionScope['com.sun.portal.portletcontainer.driver.portletWindows']}" var="map"/>
<c:set var="thinportlets" value='${map["thin"]}'/>
<c:set var="thickportlets" value='${map["thick"]}'/>

<c:set var="disableMove" value="${requestScope['DisableMove']}"/>

<c:set var="isMaximized" value="false"/>

<c:if test="${thickportlets != null}">
  <c:forEach items="${thickportlets}" var="portlet2Test">
    <c:choose>
      <c:when test="${portlet2Test.maximized==true}">
        <c:set var="isMaximized" value="true"/>
        <c:set var="portlet" value="${portlet2Test}"/>
      </c:when>
      <c:otherwise>
      </c:otherwise>
    </c:choose>
  </c:forEach>
</c:if>

<c:if test="${thinportlets != null}">
  <c:forEach items="${thinportlets}" var="portlet2Test">
    <c:choose>
      <c:when test="${portlet2Test.maximized==true}">
        <c:set var="isMaximized" value="true"/>
        <c:set var="portlet" value="${portlet2Test}"/>
      </c:when>
      <c:otherwise>
      </c:otherwise>
    </c:choose>
  </c:forEach>
</c:if>

<div id="portal-content-layout">

  <c:choose>
  <c:when test="${isMaximized==true}">
  <!-- <div style="width:100%; height:100%"> -->
  <dl id="portlet_<c:out value="${portlet.portletWindowName}"/>" class="sort" style="width:100%; height:100%">
    <%@include file="portletSP.jsp" %>
  </dl>
  <!-- </div> -->
  </c:when>
  <c:otherwise>
  <c:choose>
  <c:when test="${disableMove==true}">
  <div id="thick">
    </c:when>
    <c:otherwise>
    <div id="thick" class="ui-sortable">
      </c:otherwise>
      </c:choose>
      <c:if test="${thickportlets != null}">
        <c:forEach items="${thickportlets}" var="portlet">
          <dl id="portlet_<c:out value="${portlet.portletWindowName}"/>" class="sort">
            <%@include file="portletSP.jsp" %>
          </dl>
        </c:forEach>
      </c:if>
    </div>

    <c:choose>
    <c:when test="${disableMove==true}">
    <div id="thin">
      </c:when>
      <c:otherwise>
      <div id="thin" class="ui-sortable">
        </c:otherwise>
        </c:choose>
        <c:if test="${thinportlets != null}">
          <c:forEach items="${thinportlets}" var="portlet">
            <dl id="portlet_<c:out value="${portlet.portletWindowName}"/>" class="sort">
              <%@include file="portletSP.jsp" %>
            </dl>
          </c:forEach>
        </c:if>
      </div>
      </c:otherwise>
      </c:choose>
    </div>