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

<%@page import="org.apache.commons.io.FileUtils"%>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<%@ page import="java.io.File"%>
<%@ include file="checkCommunicationUser.jsp" %>

<%
	String creationDate	= resources.getOutputDate(new Date());
	File fileDiscussion = (File) request.getAttribute("FileDiscussion");

	List<String> lines = FileUtils.readLines(fileDiscussion, "UTF-8");
%>

<html>
<head>
<TITLE><%=resources.getString("exportDiscussion")+" "+creationDate%></TITLE>
<view:looknfeel/>
</head>
<body bgcolor="#FFFFFF" leftmargin="1" topmargin="1" marginwidth="1" marginheight="1">
<%
	out.print("<br/><br/>");
	for (String line : lines) {
		out.print("<font color=\"#000000\" size=\"2\" face=\"Courier New, Courier, mono\">"+Encode.convertHTMLEntities(line)+"</font>");
		out.print("<br/>");
	}
%>
</body>
</html>