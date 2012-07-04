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
    "http://www.silverpeas.org/legal/licensing"

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

--%>

<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%
response.setHeader("Cache-Control","no-store"); //HTTP 1.1
response.setHeader("Pragma","no-cache"); //HTTP 1.0
response.setDateHeader ("Expires",-1); //prevents caching at the proxy server
%>

<%@ page import="com.stratelia.webactiv.util.GeneralPropertiesManager"%>
<%@ page import="com.stratelia.webactiv.util.ResourceLocator"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.*"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.board.Board"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.browseBars.BrowseBar"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.arrayPanes.ArrayPane"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.arrayPanes.ArrayLine"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.window.Window"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.frame.Frame"%>
<%@ page import="com.stratelia.silverpeas.util.ResourcesWrapper"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.Encode"%>

<%@ page errorPage="../../admin/jsp/errorpage.jsp"%>

<%
String m_context = GeneralPropertiesManager.getGeneralResourceLocator().getString("ApplicationURL");

GraphicElementFactory gef = (GraphicElementFactory) session.getAttribute("SessionGraphicElementFactory");

ResourcesWrapper resource = (ResourcesWrapper)request.getAttribute("resources");

Window 		window 		= gef.getWindow();
BrowseBar 	browseBar 	= window.getBrowseBar();
Frame 		frame 		= gef.getFrame();
Board		board		= gef.getBoard();
%>
<HTML>
<HEAD>
<TITLE></TITLE>
<%
out.println(gef.getLookStyleSheet());
%>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/animation.js"></script>
</HEAD>
<body marginheight=5 marginwidth=5 leftmargin=5 topmargin=5 bgcolor="#FFFFFF">
<%
  	browseBar.setDomainName(resource.getString("pdcPeas.SearchEngine"));
  	browseBar.setComponentName(resource.getString("pdcPeas.AideContent"));

  	out.println(window.printBefore());
  	out.println(frame.printBefore());
	out.println(board.printBefore());
%>
		<table border="0" width="100%"><tr><td valign="top" width="30%">
		<%=resource.getString("pdcPeas.helpCol1Header")%><br><br>
		<%=resource.getString("pdcPeas.helpCol1Content1")%><br>
		<%=resource.getString("pdcPeas.helpCol1Content2")%><br>
		<%=resource.getString("pdcPeas.helpCol1Content3")%><br>
		</td>
		<td>&nbsp;</td>
		<td valign="top" width="30%">
		<%=resource.getString("pdcPeas.helpCol2Header")%><br><br>
		<%=resource.getString("pdcPeas.helpCol2Content1")%><br>
		<%=resource.getString("pdcPeas.helpCol2Content2")%><br>
		<%=resource.getString("pdcPeas.helpCol2Content3")%><br>
		<%=resource.getString("pdcPeas.helpCol2Content4")%><br>
		<%=resource.getString("pdcPeas.helpCol2Content5")%><br>
		</td>
		<td>&nbsp;</td>
		<td valign="top" width="30%">
		<%=resource.getString("pdcPeas.helpCol3Header")%><br><br>
		<%=resource.getString("pdcPeas.helpCol3Content1")%><br>
		<%=resource.getString("pdcPeas.helpCol3Content2")%><br>
		<%=resource.getString("pdcPeas.helpCol3Content3")%><br>
		<%=resource.getString("pdcPeas.helpCol3Content4")%><br>
		</td>
		</tr></table>
<%
	out.println(board.printAfter());
    out.println(frame.printAfter());
    out.println(window.printAfter());
%>
</BODY>
</HTML>
