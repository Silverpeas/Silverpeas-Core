<%@ include file="check.jsp" %>
<%@ taglib prefix="designer" uri="/WEB-INF/workflowEditor.tld" %>
<%
String      strCurrentTab = "ViewStates",
            strStateName;
States      states = (States)request.getAttribute( "States" );
ArrayPane   arrayPane = gef.getArrayPane("stateList", strCurrentTab, request, session);
State       state;
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
browseBar.setDomainName(resource.getString("workflowDesigner.toolName"));
browseBar.setComponentName(resource.getString("workflowDesigner.states") );

operationPane.addOperation(resource.getIcon("workflowDesigner.add"),
        resource.getString("workflowDesigner.add.state"),
        "AddState");

arrayPane.setVisibleLineNumber(20);
arrayPane.setTitle(resource.getString("workflowDesigner.list.state"));
arrayPane.addArrayColumn(resource.getString("GML.name"));
arrayPane.addArrayColumn(resource.getString("workflowDesigner.timeoutAction"));
column = arrayPane.addArrayColumn(resource.getString("GML.operations"));
column.setSortable(false);

if ( states != null )
{
    Iterator    iterState = states.iterateState();

    while ( iterState.hasNext() )
    {
        Action timeoutAction;
        
        state = (State)iterState.next();
        strStateName = state.getName();
        timeoutAction = state.getTimeoutAction();
        
        row    = arrayPane.addArrayLine();
        iconPane = gef.getIconPane();
        iconPane.setSpacing("30px");
        updateIcon = iconPane.addIcon();
        delIcon = iconPane.addIcon();
        delIcon.setProperties(resource.getIcon("workflowDesigner.smallDelete"),
                              resource.getString("GML.delete"),
                              "javascript:confirmRemove('RemoveState?state=" 
                              + URLEncoder.encode(strStateName, UTF8) + "', '"
                              + resource.getString("workflowDesigner.confirmRemoveJS") + " "
                              + Encode.javaStringToJsString( strStateName ) + " ?');" );
        updateIcon.setProperties(resource.getIcon("workflowDesigner.smallUpdate"),
                                 resource.getString("GML.modify"),
                                 "ModifyState?state=" + strStateName );
        
        
        row.addArrayCellLink( strStateName, "ModifyState?state=" + strStateName );
        row.addArrayCellLink( timeoutAction == null ? "" : timeoutAction.getName(),
                                "ModifyState?state=" + strStateName );
        row.addArrayCellIconPane(iconPane);
    }
}

out.println(window.printBefore());

%>
<designer:processModelTabs currentTab="ViewStates"/>
<%
out.println(frame.printBefore());

//help
//
out.println(boardHelp.printBefore());
out.println("<table border=\"0\"><tr>");
out.println("<td valign=\"absmiddle\"><img border=\"0\" src=\""+resource.getIcon("workflowDesigner.info")+"\"></td>");
out.println("<td>"+resource.getString("workflowDesigner.help.states")+"</td>");
out.println("</tr></table>");
out.println(boardHelp.printAfter());
out.println("<br/>");

out.println(board.printBefore());

out.println( arrayPane.print() );
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
