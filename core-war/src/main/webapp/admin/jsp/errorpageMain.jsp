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
<%
if (response.isCommitted() == false) {
  response.resetBuffer();
}
%>

<%--
 % This page is invoked when an error happens at the server.  The
 % error details are available in the implicit 'exception' object.
 % We set the error page to this file in each of our screens.
 % (via the template.jsp)
--%>

<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<%@ taglib uri="http://www.silverpeas.com/tld/silverFunctions" prefix="silfn" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ page import="org.silverpeas.core.exception.SilverpeasTrappedException"%>
<%@ page import="org.silverpeas.core.util.StringUtil" %>
<%@ page import="org.silverpeas.core.util.WebEncodeHelper" %>
<%@ page import="org.silverpeas.core.web.mvc.util.HomePageUtil" %>
<%@ page import="java.io.PrintWriter" %>
<%@ page import="org.silverpeas.core.security.authorization.ForbiddenRuntimeException" %>
<%@ page import="javax.ws.rs.WebApplicationException" %>
<%@ page import="org.silverpeas.core.util.logging.SilverLogger" %>
<%@ page import="org.silverpeas.core.web.mvc.webcomponent.SilverpeasHttpServlet" %>
<%@ page import="org.silverpeas.core.NotFoundException" %>

<%@ include file="import.jsp" %>

<%
Throwable exception = (Throwable) request.getAttribute("javax.servlet.jsp.jspException");
Throwable toDisplayException = HomePageUtil.getExceptionToDisplay(exception);
String exStr = HomePageUtil.getMessageToDisplay(exception , language);
String detailedString = HomePageUtil.getMessagesToDisplay(exception , language);
boolean isGobackPage = false;
String gobackPage = null;
String extraInfos = null;
if (exception instanceof SilverpeasTrappedException) {
  final SilverpeasTrappedException ste = (SilverpeasTrappedException) exception;
  gobackPage = ste.getGoBackPage();
  isGobackPage = StringUtil.isDefined(gobackPage);
  extraInfos = StringUtil.defaultStringIfNotDefined(ste.getExtraInfos(), null);
  // Trace the exception
  HomePageUtil.traceException(exception);
} else if (exception instanceof NotFoundException) {
  SilverLogger.getLogger(SilverpeasHttpServlet.class).error(exception.getMessage());
  response.sendError(HttpServletResponse.SC_NOT_FOUND, exception.getMessage());
  return;
} else if (exception instanceof ForbiddenRuntimeException) {
  SilverLogger.getLogger(SilverpeasHttpServlet.class).error(exception.getMessage());
  response.sendError(HttpServletResponse.SC_FORBIDDEN, exception.getMessage());
  return;
} else if (exception instanceof WebApplicationException) {
  final WebApplicationException wae = (WebApplicationException) exception;
  if (wae.getResponse().getStatus() == HttpServletResponse.SC_FORBIDDEN) {
    SilverLogger.getLogger(SilverpeasHttpServlet.class).error(wae.getMessage());
  }
  if (!response.isCommitted()) {
    response.sendError(wae.getResponse().getStatus(), exception.getMessage());
  }
  return;
} else {
  // Trace the exception
  HomePageUtil.traceException(toDisplayException);
}
gobackPage = StringUtil.defaultStringIfNotDefined(gobackPage, "javascript:void(0)");
%>

<view:sp-page>
  <fmt:setLocale value="${requestScope.userLanguage}"/>
  <view:setBundle basename="org.silverpeas.multilang.generalMultilang"/>
  <view:sp-head-part/>
  <view:sp-body-part>
    <form name="formError" action="<%=gobackPage%>" method="POST" style="display: none">
      <input type="hidden" name="message" value="<% if (exStr != null){out.print(WebEncodeHelper.javaStringToHtmlString(exStr));}%>"/>
      <input type="hidden" name="messageExtra" value="<% if (extraInfos != null){out.print(WebEncodeHelper.javaStringToHtmlString(extraInfos));}%>"/>
      <input type="hidden" name="detailedMessage" value="<% out.print(WebEncodeHelper.javaStringToHtmlString(detailedString));%>"/>
      <input type="hidden" name="stack" value="<% if (toDisplayException != null) {toDisplayException.printStackTrace(new PrintWriter(out));}%>"/>
    </form>
    <view:window>
      <view:frame>
        <div class="inlineMessage-nok"><fmt:message key="GML.error.help"/></div>
        <c:set var="formNameErrorDataProvider" value="formError" scope="request"/>
        <c:if test="<%=isGobackPage%>">
          <c:set var="formNameErrorDataProviderSubmitButtonLabel" scope="request"><fmt:message key="GML.ok"/></c:set>
        </c:if>
        <jsp:include page="errorContentFragment.jsp"/>
      </view:frame>
    </view:window>
  </view:sp-body-part>
</view:sp-page>