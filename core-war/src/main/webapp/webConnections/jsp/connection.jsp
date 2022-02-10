<%--

    Copyright (C) 2000 - 2022 Silverpeas

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
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page isELIgnored="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>
<%@ taglib tagdir="/WEB-INF/tags/silverpeas/util" prefix="viewTags" %>
<%@ include file="check.jsp" %>

<c:set var="connection" value="${requestScope.Connection}" scope="page"/>
<c:set var="newTabRequired" value="${connection.newWindow and !requestScope.IgnoreNewWindow}" scope="page"/>
<c:set var="isAlreadyNewTab" value="${connection.newWindow and requestScope.IgnoreNewWindow}" scope="page"/>
<c:set var="connectionParams" value="${connection.param}" scope="page"/>
<c:choose>
  <c:when test="${newTabRequired}">
    <c:set var="target" value="_blank"/>
  </c:when>
  <c:when test="${isAlreadyNewTab}">
    <c:set var="target" value=""/>
  </c:when>
  <c:otherwise>
    <c:set var="target" value="SpExternalFullIFrameContainer"/>
  </c:otherwise>
</c:choose>

<view:sp-page>
<view:sp-head-part lookContextManagerCallbackOnly="${not newTabRequired}">
  <script type="text/javascript">
    function sendForm() {
      document.connectionForm.submit();
    }
  </script>
</view:sp-head-part>
<view:sp-body-part onLoad="sendForm()">
<c:if test="${newTabRequired}">
  <view:browseBar path='<%=resource.getString("webConnections.label")%>' extraInformations=" > ${connection.componentName}"/>
  <view:window>
    <view:frame>
      <div class="inlineMessage">
        <%=resource.getString("webConnections.explanation")%>
      </div>
    </view:frame>
  </view:window>
</c:if>
<% pageContext.setAttribute("entries",
    ((java.util.Map) pageContext.getAttribute("connectionParams")).entrySet()); %>
<form name="connectionForm" action="<c:out value="${connection.url}"/>" method="<c:out value="${connection.method}"/>" target="${target}">
  <c:forEach items="${pageScope.entries}" var="connectionParam">
    <input type="hidden" name="<c:out value="${connectionParam.key}" />" value="<c:out value="${connectionParam.value}" />"/>
  </c:forEach>
</form>
<c:if test="${!newTabRequired and !isAlreadyNewTab}">
  <viewTags:displayExternalFullIframe url="javascript:void(0)"/>
</c:if>
</view:sp-body-part>
</view:sp-page>