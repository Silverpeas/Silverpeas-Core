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

<%@ include file="check.jsp" %>

<%
Form 				 formUpdate = (Form) request.getAttribute("Form");
DataRecord 			data 		= (DataRecord) request.getAttribute("Data");
PagesContext		context	= (PagesContext) request.getAttribute("context"); 
%>
<HTML>
<HEAD>
<% out.println(gef.getLookStyleSheet()); %>
<% formUpdate.displayScripts(out, context); %>
</HEAD>
<BODY class="yui-skin-sam">
<%
browseBar.setDomainName(resource.getString("templateDesigner.toolName"));
browseBar.setComponentName(resource.getString("templateDesigner.templateList"), "Main");
browseBar.setPath(resource.getString("templateDesigner.template"));

TabbedPane tabbedPane = gef.getTabbedPane();
tabbedPane.addTab(resource.getString("templateDesigner.preview"), "#", true);
tabbedPane.addTab(resource.getString("templateDesigner.template"), "EditTemplate", false);
tabbedPane.addTab(resource.getString("templateDesigner.fields"), "ViewFields", false);

	out.println(window.printBefore());
	
	out.println(tabbedPane.print());
    out.println(frame.printBefore());
    out.println(board.printBefore());
%>
<FORM NAME="myForm" METHOD="POST" ACTION="UpdateXMLForm" ENCTYPE="multipart/form-data">
	<% 
		formUpdate.display(out, context, data); 
	%>
</FORM>
<%
	out.println(board.printAfter());
    out.println(frame.printAfter());
    out.println(window.printAfter()); 
%>
</BODY>
</HTML>