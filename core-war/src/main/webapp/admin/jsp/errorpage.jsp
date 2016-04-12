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
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<%
    if (response.isCommitted() == false)
        response.resetBuffer();
%>

<%@ page import="org.silverpeas.core.web.mvc.util.HomePageUtil"%>
<%@ page import="java.io.PrintWriter"%>

<%
	//test si la page source n'est pas Main
	String uri = (String)request.getAttribute("org.silverpeas.servlets.ComponentRequestRouter.requestURI");
	if (uri == null || (uri != null && uri.indexOf("/Main") != -1)) {
		// le cas echeant, l'erreur est affichee dans la page
		getServletConfig().getServletContext().getRequestDispatcher("/admin/jsp/errorpageMain.jsp").forward(request, response);
		return;
	}
%>

<%@ include file="import.jsp" %>

<%
    Throwable exception = (Throwable) request.getAttribute("javax.servlet.jsp.jspException");
	Throwable toDisplayException = HomePageUtil.getExceptionToDisplay(exception);
	String exStr = HomePageUtil.getMessageToDisplay(exception , language);
	String detailedString = HomePageUtil.getMessagesToDisplay(exception , language);

    // Trace the exception
    HomePageUtil.traceException(exception);
%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
   "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
	<head>
		<title></title>
		<view:looknfeel/>
        <script type="text/javascript">
            function displayPopup() {
                SP_openWindow("<%=m_context%>/admin/jsp/popupError.jsp","popup","650","180","");
            }
        </script>
	</head>
	<body onload="javascript:displayPopup()">
		<center>
			<form name="formulaire" method="post">
				<input type="hidden" name="message" value="<% if (exStr != null){out.print(exStr);}%>"/>
				<input type="hidden" name="detailedMessage" value="<% if (detailedString != null){out.print(detailedString);}%>"/>
				<input type="hidden" name="pile" value="<% if (toDisplayException != null) {toDisplayException.printStackTrace(new PrintWriter(out));}%>"/>
			</form>
		</center>
	</body>
</html>