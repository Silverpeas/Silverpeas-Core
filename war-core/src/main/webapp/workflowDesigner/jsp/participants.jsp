<%@ include file="check.jsp" %>
<%@ taglib prefix="designer" uri="/WEB-INF/workflowEditor.tld" %>
<%
String       strCurrentTab = "ViewParticipants",
             strParticipantName;
Participants participants = (Participants)request.getAttribute( "Participants" );
Iterator     iterParticipant;
ArrayPane    arrayPane = gef.getArrayPane("participantList", strCurrentTab, request, session);
Participant  participant;
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
browseBar.setComponentName(resource.getString("workflowDesigner.participants") );

operationPane.addOperation(resource.getIcon("workflowDesigner.add"),
        resource.getString("workflowDesigner.add.participant"),
        "AddParticipant");

arrayPane.setVisibleLineNumber(20);
arrayPane.setTitle(resource.getString("workflowDesigner.list.participant"));
arrayPane.addArrayColumn(resource.getString("GML.name"));
arrayPane.addArrayColumn(resource.getString("workflowDesigner.state"));
column = arrayPane.addArrayColumn(resource.getString("GML.operations"));
column.setSortable(false);

if ( participants != null )
{
    iterParticipant = participants.iterateParticipant();
    
    while ( iterParticipant.hasNext() )
    {
        participant = (Participant)iterParticipant.next();
        strParticipantName = participant.getName();
        row    = arrayPane.addArrayLine();
        iconPane = gef.getIconPane();
        iconPane.setSpacing("30px");
        updateIcon = iconPane.addIcon();
        delIcon = iconPane.addIcon();
        delIcon.setProperties(resource.getIcon("workflowDesigner.smallDelete"),
                              resource.getString("GML.delete"),
                              "javascript:confirmRemove('RemoveParticipant?participant=" 
                              + URLEncoder.encode(strParticipantName, UTF8) + "', '"
                              + resource.getString("workflowDesigner.confirmRemoveJS") + " "
                              + Encode.javaStringToJsString( strParticipantName ) + " ?');" );
        updateIcon.setProperties(resource.getIcon("workflowDesigner.smallUpdate"),
                                 resource.getString("GML.modify"),
                                 "ModifyParticipant?participant=" + strParticipantName );
        
        
        row.addArrayCellLink( strParticipantName, "ModifyParticipant?participant=" + strParticipantName );
        row.addArrayCellLink( participant.getResolvedState() == null ? "" : participant.getResolvedState(),
                                "ModifyParticipant?participant=" + strParticipantName );
        row.addArrayCellIconPane(iconPane);
    }
}

out.println(window.printBefore());

%>
<designer:processModelTabs currentTab="ViewParticipants"/>
<%
out.println(frame.printBefore());

//help
//
out.println(boardHelp.printBefore());
out.println("<table border=\"0\"><tr>");
out.println("<td valign=\"absmiddle\"><img border=\"0\" src=\""+resource.getIcon("workflowDesigner.info")+"\"></td>");
out.println("<td>"+resource.getString("workflowDesigner.help.participants")+"</td>");
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
