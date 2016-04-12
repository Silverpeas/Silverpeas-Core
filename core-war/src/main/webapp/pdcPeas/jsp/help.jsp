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
response.setHeader("Cache-Control","no-store"); //HTTP 1.1
response.setHeader("Pragma","no-cache"); //HTTP 1.0
response.setDateHeader ("Expires",-1); //prevents caching at the proxy server
%>

<%@ page import="org.apache.lucene.queryParser.QueryParser"%>
<%@ page import="org.silverpeas.core.index.search.model.IndexSearcher"%>
<%@ page import="org.silverpeas.core.util.MultiSilverpeasBundle"%>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.GraphicElementFactory"%>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.board.Board"%>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.browsebars.BrowseBar"%>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.frame.Frame"%>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.window.Window"%>

<%@ page errorPage="../../admin/jsp/errorpage.jsp"%>

<%
GraphicElementFactory gef = (GraphicElementFactory) session.getAttribute("SessionGraphicElementFactory");

MultiSilverpeasBundle resource = (MultiSilverpeasBundle)request.getAttribute("resources");

Window 		window 		= gef.getWindow();
BrowseBar 	browseBar 	= window.getBrowseBar();
Frame 		frame 		= gef.getFrame();
Board		board		= gef.getBoard();

QueryParser.Operator defaultOperand = IndexSearcher.defaultOperand;

%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title></title>
<view:looknfeel/>
</head>
<body>
<view:browseBar path='<%=resource.getString("pdcPeas.SearchEngine") + " > " + resource.getString("pdcPeas.AideContent") %>'/>
<view:window popup="true">
<view:board>
		<table border="0" width="100%"><tr><td valign="top" width="30%">
		<%=resource.getString("pdcPeas.helpCol1Header")%><br/><br/>
		<%=resource.getString("pdcPeas.helpCol1Content1")%><br/>
		<%=resource.getString("pdcPeas.helpCol1Content2")%><br/>
		<%=resource.getString("pdcPeas.helpCol1Content3")%><br/>
		</td>
		<td>&nbsp;</td>
		<td valign="top" width="30%">
		<%=resource.getString("pdcPeas.helpCol2Header")%><br/><br/>
		<%=resource.getStringWithParams("pdcPeas.helpCol2Content1", resource.getString("pdcPeas.help.operand."+defaultOperand.toString()))%><br/>
		<%=resource.getStringWithParams("pdcPeas.helpCol2Content2", defaultOperand.toString())%><br/>
		<%=resource.getString("pdcPeas.helpCol2Content3")%><br/>
		<%=resource.getString("pdcPeas.helpCol2Content4")%><br/>
		<%=resource.getString("pdcPeas.helpCol2Content5")%><br/>
		</td>
		<td>&nbsp;</td>
		<td valign="top" width="30%">
		<%=resource.getString("pdcPeas.helpCol3Header")%><br/><br/>
		<%=resource.getString("pdcPeas.helpCol3Content1")%><br/>
		<%=resource.getString("pdcPeas.helpCol3Content2")%><br/>
		<%=resource.getString("pdcPeas.helpCol3Content3")%><br/>
		<%=resource.getString("pdcPeas.helpCol3Content4")%><br/>
		</td>
		</tr></table>
</view:board>
</view:window>
</body>
</html>