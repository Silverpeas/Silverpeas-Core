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
    strProcessFileNameJSEncoded = Encode.javaStringToJsString( strProcessFileName );
	
    row       = arrayPane.addArrayLine();
	iconPane = gef.getIconPane();
	updateIcon = iconPane.addIcon();
	delIcon = iconPane.addIcon();
	
	row.addArrayCellLink( Encode.javaStringToHtmlString( strProcessFileName ),
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