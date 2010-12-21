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

<%@ include file="check.jsp" %>
<%
Iterator  workflows = ((List) request.getAttribute("ProcessFileNames")).iterator();
ArrayPane arrayPane;
String    strProcessFileName,
          strProcessFileNameURLEncoded,
          strProcessFileNameJSEncoded; 
%>
<html>
<head>
<%
	out.println(gef.getLookStyleSheet());
%>
<script type="text/javascript" src="<%=m_context%>/workflowDesigner/jsp/JavaScript/forms.js"></script>
</head>
<body>
<%
browseBar.setDomainName(resource.getString("workflowDesigner.toolName"));
browseBar.setComponentName(resource.getString("workflowDesigner.list.workflow"));

operationPane.addOperation(resource.getIcon("workflowDesigner.add"),
        resource.getString("workflowDesigner.addWorkflow"),
        "AddWorkflow");

operationPane.addOperation(resource.getIcon("workflowDesigner.import"),
        resource.getString("workflowDesigner.importWorkflow"),
        "ImportWorkflow");

arrayPane = gef.getArrayPane("workflowList", "Main", request, session);
arrayPane.setVisibleLineNumber(20);
arrayPane.setTitle(resource.getString("workflowDesigner.workflowList"));
arrayPane.addArrayColumn(resource.getString("GML.path"));
column = arrayPane.addArrayColumn(resource.getString("GML.operations"));
column.setSortable( false );

while ( workflows.hasNext() )
{
	strProcessFileName = (String) workflows.next();
    strProcessFileNameURLEncoded = URLEncoder.encode( strProcessFileName, UTF8);
    strProcessFileNameJSEncoded = EncodeHelper.javaStringToJsString( strProcessFileName );
	
    row       = arrayPane.addArrayLine();
	iconPane = gef.getIconPane();
	updateIcon = iconPane.addIcon();
	delIcon = iconPane.addIcon();
	
	row.addArrayCellLink( EncodeHelper.javaStringToHtmlString( strProcessFileName ),
	                     "EditWorkflow?ProcessFileName=" + strProcessFileNameURLEncoded );
	
	updateIcon.setProperties(resource.getIcon("workflowDesigner.smallUpdate"),
	                         resource.getString("GML.modify"),
	                         "EditWorkflow?ProcessFileName=" 
                              + strProcessFileNameURLEncoded );
	delIcon.setProperties(resource.getIcon("workflowDesigner.smallDelete"),
	                      resource.getString("GML.delete"),
                          "javascript:confirmRemove('RemoveWorkflow?ProcessFileName=" 
                          + URLEncoder.encode( strProcessFileNameJSEncoded, UTF8)
                          + "', '"
                          + resource.getString("workflowDesigner.confirmRemoveJS") + " "
                          + strProcessFileNameJSEncoded + " ?');" );
	iconPane.setSpacing("30px");
	row.addArrayCellIconPane(iconPane);
}

out.println(window.printBefore());
out.println(frame.printBefore());

//help
//
out.println(boardHelp.printBefore());
out.println("<table border=\"0\"><tr>");
out.println("<td valign=\"absmiddle\"><img border=\"0\" src=\""+resource.getIcon("workflowDesigner.info")+"\"></td>");
out.println("<td>"+resource.getString("workflowDesigner.help.toolName")+"</td>");
out.println("</tr></table>");
out.println(boardHelp.printAfter());
out.println("<br/>");


out.println(arrayPane.print());

out.println(frame.printAfter());
out.println(window.printAfter());
%>
</body>
</html>