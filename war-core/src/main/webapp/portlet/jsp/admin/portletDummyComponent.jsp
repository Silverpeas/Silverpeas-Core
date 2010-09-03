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

<%
response.setHeader("Cache-Control","no-store"); //HTTP 1.1
response.setHeader("Pragma","no-cache");       //HTTP 1.0
response.setDateHeader ("Expires",-1);        //prevents caching at the proxy server
%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.*"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.window.Window"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.operationPanes.OperationPane"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.browseBars.BrowseBar"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.frame.Frame"%>
<%@ page import="com.stratelia.webactiv.util.*"%>
<%@ page import="com.stratelia.silverpeas.peasCore.URLManager"%>
<%@ page import="com.stratelia.webactiv.beans.admin.OrganizationController"%>
<%@ page import="com.stratelia.silverpeas.silvertrace.*"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.Encode"%>
<%@ include file="language.jsp" %>
<%



String m_context = GeneralPropertiesManager.getGeneralResourceLocator().getString("ApplicationURL");

	
Window window = gef.getWindow();
BrowseBar browseBar = window.getBrowseBar();
Frame frame = gef.getFrame();




%>


<HTML>
<HEAD>
<jsp:useBean id="portlet" scope="request" class="com.stratelia.silverpeas.portlet.Portlet"/>
<link rel="stylesheet" href="<%=m_context %>/util/styleSheets/globalSP.css" type="text/css">
</HEAD>
<body bgcolor="#FFFFFF" leftmargin="5" topmargin="5" marginwidth="5" marginheight="5">
<%	out.println(window.printBefore());
 
	//Instanciation du cadre avec le view generator
    
	out.println(frame.printBefore());	
	
%>
<center>
<table width="98%" border="0" cellspacing="0" cellpadding="0" class=intfdcolor4><!--tablcontour-->
	<tr> 
		<td nowrap>
			<table border="0" cellspacing="0" cellpadding="5" class="contourintfdcolor" width="100%"><!--tabl1-->
				<tr align=center> 
					<td  class="intfdcolor4" valign="baseline" align="center">

						<span class="textePetitBold"><%=portlet.getName()%></span>

					</td>
				</tr>
			</table>
		</td>
	</tr>
</table>
</center>
<% // Ici se termine le code de la page %>

<%
out.println(frame.printAfter());
out.println(window.printAfter());
%>
</BODY>
</HTML>
