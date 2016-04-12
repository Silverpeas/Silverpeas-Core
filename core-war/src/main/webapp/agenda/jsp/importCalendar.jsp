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
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>
<%@ taglib uri="http://www.silverpeas.com/tld/silverFunctions" prefix="silfn" %>

<%
  response.setHeader("Cache-Control", "no-store"); //HTTP 1.1
  response.setHeader("Pragma", "no-cache"); //HTTP 1.0
  response.setDateHeader("Expires", -1); //prevents caching at the proxy server
%>
<%@ page import="org.silverpeas.core.web.tools.agenda.model.CalendarImportSettings"%>

<%@ include file="checkAgenda.jsp" %>

<%
  CalendarImportSettings importSettings = (CalendarImportSettings) request.getAttribute(
        "ImportSettings");
    boolean doSynchro = (importSettings != null && importSettings.isOutlookSynchro(request
        .getRemoteHost()));
    pageContext.setAttribute("doSynchro", doSynchro);
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
  "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
  <head>
    <c:if test="${pageScope.doSynchro}">
      <meta http-equiv="refresh" content="<%=importSettings.getSynchroDelay() * 60%>; URL=<c:url value="/Ragenda/jsp/importCalendar" />"/>
    </c:if>
      <title>Synchro <c:choose><c:when test="${pageScope.doSynchro}">active</c:when><c:otherwise>inactive</c:otherwise></c:choose></title>
      <script type="text/javascript" src='<c:url value="/util/javaScript/outlook_applet.js" />' ></script>
  </head>

  <body id="agenda">
    <c:if test="${pageScope.doSynchro}">
      <c:set var="baseURL" value="${fn:replace(pageContext.request.requestURL, pageContext.request.requestURI, pageContext.request.contextPath)}" />
      <div id="outlook"> </div>
      <script language="JavaScript" type="text/javascript">
        try {
          loadApplet('outlook', '<c:out value="${pageContext.request.contextPath}"/>', '<c:out value="${pageContext.session.id}"/>', '${baseURL}/ImportCalendar/', 'Can\'t display applet');
        } catch (e) {
          alert(e);
        }
      </script>
    </c:if>
  </body>
</html>
