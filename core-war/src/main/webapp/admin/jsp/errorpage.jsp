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
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://www.silverpeas.com/tld/silverFunctions" prefix="silfn" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%
  if (!response.isCommitted()) {
    response.resetBuffer();
  }
%>

<%@ page import="org.silverpeas.core.util.WebEncodeHelper" %>
<%@ page import="org.silverpeas.core.web.mvc.util.HomePageUtil" %>
<%@ page import="java.io.PrintWriter" %>

<%
  //test si la page source n'est pas Main
  String uri = (String) request
      .getAttribute("org.silverpeas.servlets.ComponentRequestRouter.requestURI");
  if (uri == null || uri.contains("/Main")) {
    // le cas echeant, l'erreur est affichee dans la page
    getServletConfig().getServletContext().getRequestDispatcher("/admin/jsp/errorpageMain.jsp")
        .forward(request, response);
    return;
  }
%>

<%@ include file="import.jsp" %>

<%
  Throwable exception = (Throwable) request.getAttribute("javax.servlet.jsp.jspException");
  Throwable toDisplayException = HomePageUtil.getExceptionToDisplay(exception);
  String exStr = HomePageUtil.getMessageToDisplay(exception, language);
  String detailedString = HomePageUtil.getMessagesToDisplay(exception, language);
  // Trace the exception
  HomePageUtil.traceException(exception);
%>
<c:url var="popupUrl" value="/admin/jsp/popupError.jsp">
  <c:param name="formNameErrorDataProvider" value="formulaire"/>
  <c:param name="isPopup" value="true"/>
</c:url>

<view:sp-page>
  <fmt:setLocale value="${requestScope.userLanguage}"/>
  <view:setBundle basename="org.silverpeas.multilang.generalMultilang"/>
  <view:sp-head-part/>
  <view:sp-body-part>
    <%-- action="javascript:void(0)" in order ro avoid to perform again the request which has for sure thrown an error --%>
    <form name="formulaire" action="javascript:void(0)" method="GET" style="display: none">
      <input type="hidden" name="message" value="<% if (exStr != null){out.print(WebEncodeHelper.javaStringToHtmlString(exStr));}%>"/>
      <input type="hidden" name="detailedMessage" value="<% out.print(WebEncodeHelper.javaStringToHtmlString(detailedString));%>"/>
      <input type="hidden" name="stack" value="<% if (toDisplayException != null) {toDisplayException.printStackTrace(new PrintWriter(out));}%>"/>
    </form>
    <view:window>
      <view:frame>
        <div class="inlineMessage-nok"><fmt:message key="GML.error.help"/></div>
      </view:frame>
    </view:window>
    <script type="text/javascript">
      (function() {
        whenSilverpeasReady().then(function() {
          SP_openWindow("${popupUrl}", "popup", "650", "180", "");
        });
      })();
    </script>
  </view:sp-body-part>
</view:sp-page>