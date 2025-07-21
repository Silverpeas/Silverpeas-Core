<%@ page import="java.nio.charset.StandardCharsets" %><%--

    Copyright (C) 2000 - 2024 Silverpeas

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    As a special exception to the terms and conditions of version 3.0 of
    the GPL, you may redistribute this Program in connection with Free/Libre
    Open Source Software ("FLOSS") applications as described in Silverpeas's
    FLOSS exception.  You should have received a copy of the text describing
    the FLOSS exception, and it is also available here:
    "https://www.silverpeas.org/legal/floss_exception.html"

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.

--%>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%@ include file="check.jsp" %>
<%@ taglib prefix="designer" uri="/WEB-INF/workflowEditor.tld" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<%
String       strCurrentTab = "ViewParticipants",
             strParticipantName;
Participants participants = (Participants)request.getAttribute( "Participants" );
Iterator<Participant>     iterParticipant;
ArrayPane    arrayPane = gef.getArrayPane("participantList", strCurrentTab, request, session);
Participant  participant;
%>

<view:sp-page>
<view:sp-head-part>
<script type="text/javascript" src="<%=m_context%>/workflowDesigner/jsp/JavaScript/forms.js"></script>
<script type="text/javascript">
function sendData() {
    document.workflowHeaderForm.submit();
}
</script>
</view:sp-head-part>
<view:sp-body-part cssClass="page_content_admin">
<%
browseBar.setDomainName(resource.getString("workflowDesigner.toolName"));
browseBar.setComponentName(resource.getString("workflowDesigner.participants") );

operationPane.addOperationOfCreation(resource.getIcon("workflowDesigner.add"),
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
        participant = iterParticipant.next();
        strParticipantName = participant.getName();
        row    = arrayPane.addArrayLine();
        iconPane = gef.getIconPane();
        iconPane.setSpacing("30px");
        updateIcon = iconPane.addIcon();
        delIcon = iconPane.addIcon();
        delIcon.setProperties(resource.getIcon("workflowDesigner.smallDelete"),
                              resource.getString("GML.delete"),
                              "javascript:confirmRemove('RemoveParticipant', {participant: '"
                              + URLEncoder.encode(strParticipantName, StandardCharsets.UTF_8) +
                                      "'}, '"
                              + resource.getString("workflowDesigner.confirmRemoveJS") + " "
                              + WebEncodeHelper.javaStringToJsString( strParticipantName ) + " ?');" );
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
<view:frame>
<view:areaOfOperationOfCreation/>
<!-- help -->
<div class="inlineMessage">
	<table>
        <tr><th></th></tr>
        <tr>
		<td class="absmiddle"><img alt="info"
                                    src="<%=resource.getIcon("workflowDesigner.info") %>"/></td>
		<td><%=resource.getString("workflowDesigner.help.participants") %></td>
	</tr></table>
</div>
<br/>
<%
out.println(arrayPane.print());
%>
<form name="workflowHeaderForm" action="UpdateWorkflow" method="post">
<designer:buttonPane cancelAction="Main" />
</form>
</view:frame>
<%
out.println(window.printAfter());
%>
</view:sp-body-part>
</view:sp-page>