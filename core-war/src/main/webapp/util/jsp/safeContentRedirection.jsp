<%--
  ~ Copyright (C) 2000 - 2022 Silverpeas
  ~
  ~ This program is free software: you can redistribute it and/or modify
  ~ it under the terms of the GNU Affero General Public License as
  ~ published by the Free Software Foundation, either version 3 of the
  ~ License, or (at your option) any later version.
  ~
  ~ As a special exception to the terms and conditions of version 3.0 of
  ~ the GPL, you may redistribute this Program in connection with Free/Libre
  ~ Open Source Software ("FLOSS") applications as described in Silverpeas's
  ~ FLOSS exception.  You should have received a copy of the text describing
  ~ the FLOSS exception, and it is also available here:
  ~ "https://www.silverpeas.org/legal/floss_exception.html"
  ~
  ~ This program is distributed in the hope that it will be useful,
  ~ but WITHOUT ANY WARRANTY; without even the implied warranty of
  ~ MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  ~ GNU Affero General Public License for more details.
  ~
  ~ You should have received a copy of the GNU Affero General Public License
  ~ along with this program.  If not, see <http://www.gnu.org/licenses/>.
  --%>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags/silverpeas/util" prefix="viewTags" %>
<%@ taglib uri="http://www.silverpeas.com/tld/silverFunctions" prefix="silfn" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>

<%
  response.setHeader("Cache-Control","no-store"); //HTTP 1.1
  response.setHeader("Pragma","no-cache"); //HTTP 1.0
  response.setDateHeader ("Expires",-1); //prevents caching at the proxy server
%>
<c:set var="lookContextManagerCallbackOnly"
       value="${not silfn:booleanValue(requestScope.IsInternalLink) or silfn:booleanValue(requestScope.IsPermalink)}"/>
<view:sp-page>
<view:sp-head-part noLookAndFeel="true" lookContextManagerCallbackOnly="${lookContextManagerCallbackOnly}"/>
<view:sp-body-part>
<c:choose>
  <c:when test="${silfn:booleanValue(requestScope.IsInternalLink)}">
    <c:choose>
      <c:when test="${silfn:booleanValue(requestScope.IsPermalink)}">
        <script type="text/javascript">
          const url = '${silfn:escapeJs(requestScope.URL)}';
          if (top.spWindow) {
            top.spWindow.loadPermalink(url);
          } else {
            top.location = url;
          }
        </script>
      </c:when>
      <c:otherwise>
        <c:redirect url="${requestScope.URL}"/>
      </c:otherwise>
    </c:choose>
  </c:when>
  <c:otherwise>
    <viewTags:displayExternalFullIframe url="${requestScope.URL}"/>
  </c:otherwise>
</c:choose>
</view:sp-body-part>
</view:sp-page>