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

<%@ include file="check.jsp"%>
<%@ taglib prefix="designer" uri="/WEB-INF/workflowEditor.tld" %>
<%
String        strCurrentTab   = "ViewDataFolder",
              strContext = (String)request.getAttribute("context");
DataFolder    items = (DataFolder)request.getAttribute("Items");
%>
<HTML>
<HEAD>
<%
    out.println(gef.getLookStyleSheet());
%>
<script type="text/javascript" src="<%=m_context%>/workflowDesigner/jsp/JavaScript/forms.js"></script>
<script type="text/javascript">
function sendData() 
{
    document.workflowHeaderForm.submit();
}
</script>
</HEAD>
<body>
<%
browseBar.setDomainName(resource.getString("workflowDesigner.toolName"));
browseBar.setComponentName(resource.getString("workflowDesigner.list.dataFolder"));

addItem( operationPane, resource, strContext, "workflowDesigner.add.item" );

out.println(window.printBefore());
%>
<designer:processModelTabs currentTab="<%=strCurrentTab%>"/>
<%
out.println(frame.printBefore());

//help
//
out.println(boardHelp.printBefore());
out.println("<table border=\"0\"><tr>");
out.println("<td valign=\"absmiddle\"><img border=\"0\" src=\""+resource.getIcon("workflowDesigner.info")+"\"></td>");
out.println("<td>"+resource.getString("workflowDesigner.help.dataFolder")+"</td>");
out.println("</tr></table>");
out.println(boardHelp.printAfter());
out.println("<br/>");

out.println(board.printBefore());
%>
<designer:itemList currentScreen="<%=strCurrentTab%>" context="<%=strContext%>" 
                   items="<%=items%>" paneTitleKey="workflowDesigner.list.dataFolder" />
<%
out.println(board.printAfter());
%>
<form name="workflowHeaderForm" action="UpdateWorkflow" method="POST">
</form>
<designer:buttonPane cancelAction="Main" />
<%    
out.println(frame.printAfter());
out.println(window.printAfter());
%>
</body>
</html>
