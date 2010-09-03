<%--

    Copyright (C) 2000 - 2009 Silverpeas

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    As a special exception to the terms and conditions of version 3.0 of
    the GPL, you may redistribute this Program in connection with Free/Libre
    Open Source Software ("FLOSS") applications as described in Silverpeas's
    FLOSS exception.  You should have received a copy of the text describing
    the FLOSS exception, and it is also available here:
    "http://repository.silverpeas.com/legal/licensing"

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

--%>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%@ page import="java.io.*"%>
<%@ include file="checkCommunicationUser.jsp" %>

<%
	String creationDate	= resources.getOutputDate(new Date());
	File fileDiscussion = (File) request.getAttribute("FileDiscussion");

	FileReader file_read = new FileReader(fileDiscussion);
    BufferedReader flux_in = new BufferedReader(file_read);
%>

<html>
<head>
<TITLE><%=resources.getString("exportDiscussion")+" "+creationDate%></TITLE>
<%
   out.println(gef.getLookStyleSheet());
%>
</head>
<body bgcolor="#FFFFFF" leftmargin="1" topmargin="1" marginwidth="1" marginheight="1">
<% 
	out.print("<BR><BR>");
	String ligne;
	while ((ligne = flux_in.readLine()) != null) {
		out.print("<font color=\"#000000\" size=\"2\" face=\"Courier New, Courier, mono\">"+Encode.convertHTMLEntities(ligne)+"</font>");
		out.print("<BR>");
	}
	flux_in.close();
	file_read.close();
%>
</body>
</html>