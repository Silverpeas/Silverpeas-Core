<%@ include file="check.jsp" %>
<%@ taglib prefix="designer" uri="/WEB-INF/workflowEditor.tld" %>

<%
String     strRoleName,
           strCurrentTab = "ViewRoles";
Roles      roles = (Roles)request.getAttribute( "Roles" );
ArrayPane  rolesPane = gef.getArrayPane("rolesList", strCurrentTab, request, session);
%>
<HTML>
<HEAD>
<% out.println(gef.getLookStyleSheet()); %>
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
browseBar.setDomainName(resource.getString("workflowDesigner.toolName") );
browseBar.setComponentName(resource.getString("workflowDesigner.roles") );

operationPane.addOperation(resource.getIcon("workflowDesigner.add"),
        resource.getString("workflowDesigner.addRole"),
        "AddRole");

rolesPane.setVisibleLineNumber(20);
rolesPane.setTitle(resource.getString("workflowDesigner.list.role"));
rolesPane.addArrayColumn(resource.getString("GML.name"));
column = rolesPane.addArrayColumn(resource.getString("GML.operations"));
column.setSortable(false);

if ( roles != null )
{
    Iterator   iterRole = roles.iterateRole();

    while ( iterRole.hasNext() )
    {
        strRoleName = ( (Role)iterRole.next() ).getName();
    	row    = rolesPane.addArrayLine();
    	iconPane = gef.getIconPane();
    	iconPane.setSpacing("30px");
    	updateIcon = iconPane.addIcon();
    	delIcon = iconPane.addIcon();
    	delIcon.setProperties(resource.getIcon("workflowDesigner.smallDelete"),
    	                      resource.getString("GML.delete"),
    	                      "javascript:confirmRemove('RemoveRole?role=" 
                                                        + URLEncoder.encode(strRoleName, UTF8) + "', '"
                                                        + resource.getString("workflowDesigner.confirmRemoveJS")
                                                        + " " + Encode.javaStringToJsString( strRoleName ) + " ?');" );
    	updateIcon.setProperties(resource.getIcon("workflowDesigner.smallUpdate"),
    	                         resource.getString("GML.modify"),
    	                         "ModifyRole?role=" + URLEncoder.encode(strRoleName, UTF8) );
    	
    	
    	row.addArrayCellLink( strRoleName, "ModifyRole?role=" + URLEncoder.encode(strRoleName, UTF8) );
    	row.addArrayCellIconPane(iconPane);
    }
}

out.println(window.printBefore());
%>
<designer:processModelTabs currentTab="ViewRoles"/>
<%
out.println(frame.printBefore());

//help
//
out.println(boardHelp.printBefore());
out.println("<table border=\"0\"><tr>");
out.println("<td valign=\"absmiddle\"><img border=\"0\" src=\""+resource.getIcon("workflowDesigner.info")+"\"></td>");
out.println("<td>"+resource.getString("workflowDesigner.help.roles")+"</td>");
out.println("</tr></table>");
out.println(boardHelp.printAfter());
out.println("<br/>");

out.println(board.printBefore());
out.println( rolesPane.print() );
out.println(board.printAfter());

%>
<form name="workflowHeaderForm" action="UpdateWorkflow" method="POST">
<designer:buttonPane cancelAction="Main" />
</form>
<%    

out.println(frame.printAfter());
out.println(window.printAfter());
%>
</body>
</html>
