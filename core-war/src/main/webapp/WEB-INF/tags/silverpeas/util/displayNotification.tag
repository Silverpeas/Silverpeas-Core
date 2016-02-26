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
<%@ taglib prefix="plugins" tagdir="/WEB-INF/tags/silverpeas/plugins" %>

<c:set var="isNotifierRequested" value="${false}"/>

<%-- TAG attribute way --%>
<%-- Specify a messageType attribute and a body that contains the mesage --%>
<%@ attribute name="messageType" required="false" description="error, success or info" %>
<c:if test="${messageType == 'error' or messageType == 'success' or messageType == 'info'}">
  <jsp:doBody var="notifierBody"/>
  <c:if test="${not empty notifierBody}">
    <c:set var="isNotifierRequested" value="${true}"/>
    <div style="display: none" class="noty${silfn:capitalize(messageType)}">
        ${notifierBody}
    </div>
  </c:if>
</c:if>

<c:if test="${empty __DISPLAY_NOTIF_FROM_REQUEST}">
  <%--This handles several calls from same request--%>
  <c:set var="__DISPLAY_NOTIF_FROM_REQUEST" value="loaded" scope="request"/>

  <%-- Messages that are contained in the request --%>
  <c:if test="${not empty requestScope.notyErrorMessage}">
    <c:set var="isNotifierRequested" value="${true}"/>
    <div style="display: none" class="notyError">${requestScope.notyErrorMessage}</div>
  </c:if>
  <c:if test="${not empty requestScope.notySuccessMessage}">
    <c:set var="isNotifierRequested" value="${true}"/>
    <div style="display: none" class="notySuccess">${requestScope.notySuccessMessage}</div>
  </c:if>
  <c:if test="${not empty requestScope.notyInfoMessage}">
    <c:set var="isNotifierRequested" value="${true}"/>
    <div style="display: none" class="notyInfo">${requestScope.notyInfoMessage}</div>
  </c:if>
</c:if>

<c:if test="${isNotifierRequested and empty __DISPLAY_NOTIF_JQUERY}">
  <%--This handles several calls from same request--%>
  <c:set var="__DISPLAY_NOTIF_JQUERY" value="loaded" scope="request"/>

  <plugins:includeNotifier/>
  <script type="text/javascript">
    jQuery(document).ready(function() {

      <%--On page ending load, UI notifications are displayed--%>
      jQuery(".notyError").each(function() {
        notyError($(this).html());
      });
      jQuery(".notySuccess").each(function() {
        notySuccess($(this).html());
      });
      jQuery(".notyInfo").each(function() {
        notyInfo($(this).html());
      });
    });
  </script>
</c:if>