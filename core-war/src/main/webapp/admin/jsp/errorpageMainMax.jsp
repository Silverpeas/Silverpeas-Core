<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.buttons.Button" %><%--

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
<%--
 % This page is invoked when an error happens at the server.  The
 % error details are available in the implicit 'exception' object.
 % We set the error page to this file in each of our screens.
 % (via the template.jsp)
--%>

<%@ include file="import.jsp" %>

<%
//R�cup�ration des param�tres
String message = (String) request.getParameter("message");
String detailedMessage = (String) request.getParameter("detailedMessage");
String pile = (String) request.getParameter("pile");
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
   "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title><%= generalMessage.getString("GML.popupTitle")%></title>
<view:looknfeel/>
</head>
<body>
<%
    Window window = gef.getWindow();
	out.println(window.printBefore());
	Frame frame = gef.getFrame();
	out.println(frame.printBefore());
%>
<center>
<form name="formError" action="<%=m_context%>/admin/jsp/errorpageMainMin.jsp" method="post">
	<input type="hidden" name="message" value="<%=message %>"/>
	<input type="hidden" name="detailedMessage" value="<%=detailedMessage %>"/>
	<input type="hidden" name="pile" value="<%=pile %>"/>
<table cellpadding="0" cellspacing="2" border=0 width="98%" class="intfdcolor">
	<tr>
		<td class="intfdcolor4" nowrap="nowrap">
			<center>
				<br/>
				<span class="txtnav">
				<%=detailedMessage %>
				</span>
				<br/><br/>
				<textarea rows="12" cols="90" name="pile"><%=pile %></textarea>
			</center>
			<br/>
		</td>
	</tr>
</table>
</form>
<br/>
<%
	ButtonPane buttonPane = gef.getButtonPane();
	buttonPane.addButton((Button) gef.getFormButton(generalMessage.getString("GML.minimize"), "javascript:document.formError.submit();", false));
	out.println(buttonPane.print());
%>
</center>
<%
	out.println(frame.printAfter());
	out.println(window.printAfter());
%>
</body>
</html>