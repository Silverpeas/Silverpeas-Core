<%--

    Copyright (C) 2000 - 2012 Silverpeas

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

<% 
    if (response.isCommitted() == false)
        response.resetBuffer();
%>
<%--
 % This page is invoked when an error happens at the server.  The
 % error details are available in the implicit 'exception' object.
 % We set the error page to this file in each of our screens.
 % (via the template.jsp)
--%>

<%@ page import="javax.servlet.*"%>
<%@ page import="javax.servlet.http.*"%>
<%@ page import="javax.servlet.jsp.*"%>
<%@ page import="java.io.PrintWriter"%>
<%@ page import="java.io.IOException"%>
<%@ page import="java.io.FileInputStream"%>
<%@ page import="java.io.ObjectInputStream"%>
<%@ page import="java.util.Vector"%>
<%@ page import="java.beans.*"%>

<%@ page import="javax.ejb.EJBException, javax.ejb.FinderException, javax.ejb.NoSuchEntityException, java.rmi.RemoteException, java.sql.SQLException, javax.ejb.RemoveException, javax.ejb.CreateException, javax.naming.NamingException, javax.transaction.TransactionRolledbackException"%>
<%@ page import="java.util.Collection, java.util.Iterator, java.lang.Throwable"%>
<%@ page import="com.stratelia.webactiv.util.*"%>
<%@ page import="com.stratelia.silverpeas.silvertrace.*"%>
<%@ page import="com.stratelia.webactiv.util.exception.*"%>
<%@ page import="com.stratelia.webactiv.homepage.*"%>

<%@ include file="import.jsp" %>

<% 
	Exception exception = (Exception) request.getAttribute("javax.servlet.jsp.jspException");
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
    <title><%= generalMessage.getString("GML.popupTitle")%></title>
<%
out.println(gef.getLookStyleSheet());
%>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/animation.js"></script>
<script type="text/javascript">
    function displayPopup()
    {
        SP_openWindow("<%=m_context%>/admin/jsp/popupError.jsp?action=NOBack","popup","650","180","");
    }
</script>
</head>
<body onload="javascript:displayPopup()">
<%
	Window window = gef.getWindow();
	out.println(window.printBefore());
	Frame frame = gef.getFrame();
	out.println(frame.printBefore());
%>
<center>
    <form name="formulaire" method="post">
        <input type="hidden" name="message" value="<% if (exStr != null){out.print(exStr);}%>"/>
        <input type="hidden" name="detailedMessage" value="<% if (detailedString != null){out.print(detailedString);}%>"/>
        <input type="hidden" name="pile" value="<% if (toDisplayException != null) {toDisplayException.printStackTrace(new PrintWriter(out));}%>"/>
    </form>
<table cellpadding="0" cellspacing="2" border="0" width="98%" class="intfdcolor">
	<tr>
		<td class="intfdcolor4" nowrap="nowrap">
			<center>
				<br/>
				<span class="txtnav">
					<% if (exStr != null){out.print(exStr);}%>
				</span>
			</center>
			<br/>
		</td>
	</tr>
</table>
<br/>
</center>
<%
	out.println(frame.printAfter());
	out.println(window.printAfter());
%>
</body>
</html>