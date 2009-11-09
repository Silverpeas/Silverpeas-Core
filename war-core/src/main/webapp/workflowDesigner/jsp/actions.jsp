<%@ include file="check.jsp" %>
<%@ taglib prefix="designer" uri="/WEB-INF/workflowEditor.tld" %>

<%
String     strActionName,
           strModifyAction,
           strCurrentTab = "ViewActions";
Actions    actions = (Actions)request.getAttribute( "Actions" );
ArrayPane  actionsPane = gef.getArrayPane("actionsList", strCurrentTab, request, session);
%>
<HTML>
<HEAD>
<% out.println(gef.getLookStyleSheet()); %>
<script type="text/javascript" src="<%=m_context%>/workflowDesigner/jsp/JavaScript/forms.js"></script>
<script language="javaScript">
function sendData() 
{
    document.workflowHeaderForm.submit();
}
</script>
</HEAD>
<body>
<%
browseBar.setDomainName(resource.getString("workflowDesigner.toolName") );
browseBar.setComponentName(resource.getString("workflowDesigner.actions") );

operationPane.addOperation(resource.getIcon("workflowDesigner.add"),
        resource.getString("workflowDesigner.add.action"),
        "AddAction");

actionsPane.setVisibleLineNumber(20);
actionsPane.setTitle(resource.getString("workflowDesigner.list.action"));
actionsPane.addArrayColumn(resource.getString("GML.name"));
actionsPane.addArrayColumn(resource.getString("workflowDesigner.form"));
actionsPane.addArrayColumn(resource.getString("workflowDesigner.kind"));
column = actionsPane.addArrayColumn(resource.getString("GML.operations"));
column.setSortable(false);

if ( actions != null )
{
    Action     action;
    Iterator   iterAction = actions.iterateAction();
    
    while ( iterAction.hasNext() )
    {
        action = (Action)iterAction.next();
        strActionName = action.getName();
        strModifyAction = "ModifyAction?action=" + strActionName;
        row    = actionsPane.addArrayLine();
        iconPane = gef.getIconPane();
        iconPane.setSpacing("30px");
        updateIcon = iconPane.addIcon();
        delIcon = iconPane.addIcon();
        delIcon.setProperties(resource.getIcon("workflowDesigner.smallDelete"),
                              resource.getString("GML.delete"),
                              "javascript:confirmRemove('RemoveAction?action=" 
                              + URLEncoder.encode(strActionName, UTF8) + "', '"
                              + resource.getString("workflowDesigner.confirmRemoveJS")
                              + " " + Encode.javaStringToJsString( strActionName ) + " ?');" );
        
        updateIcon.setProperties(resource.getIcon("workflowDesigner.smallUpdate"),
                                 resource.getString("GML.modify"),
                                 strModifyAction );
        
        
        row.addArrayCellLink( strActionName, strModifyAction );
        row.addArrayCellLink( action.getForm() == null ? "" : action.getForm().getName(),
                              strModifyAction );
        row.addArrayCellLink( action.getKind(), strModifyAction );
        row.addArrayCellIconPane(iconPane);
    }
}

out.println(window.printBefore());
%>
<designer:processModelTabs currentTab="ViewActions"/>
<%
out.println(frame.printBefore());

// help
//
out.println(boardHelp.printBefore());
out.println("<table border=\"0\"><tr>");
out.println("<td valign=\"absmiddle\"><img border=\"0\" src=\""+resource.getIcon("workflowDesigner.info")+"\"></td>");
out.println("<td>"+resource.getString("workflowDesigner.help.actions")+"</td>");
out.println("</tr></table>");
out.println(boardHelp.printAfter());
out.println("<br/>");

out.println(board.printBefore());
out.println( actionsPane.print() );
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
