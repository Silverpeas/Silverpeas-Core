<%--

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
<%@ page import="org.silverpeas.core.workflow.api.model.Actions" %>
<%@ page import="org.silverpeas.core.workflow.api.model.Action" %>
<%@ page import="org.silverpeas.core.util.WebEncodeHelper" %>
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
    "http://www.silverpeas.org/docs/core/legal/floss_exception.html"

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.

--%>

<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<%@ include file="check.jsp" %>
<%@ taglib prefix="designer" uri="/WEB-INF/workflowEditor.tld" %>

<%
String     strActionName,
           strModifyAction,
           strCurrentTab = "ViewActions";
Actions actions = (Actions)request.getAttribute( "Actions" );
ArrayPane  actionsPane = gef.getArrayPane("actionsList", strCurrentTab, request, session);
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
browseBar.setDomainName(resource.getString("workflowDesigner.toolName") );
browseBar.setComponentName(resource.getString("workflowDesigner.actions") );

operationPane.addOperationOfCreation(resource.getIcon("workflowDesigner.add"),
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
    Action action;
    Iterator<Action> iterAction = actions.iterateAction();

    while ( iterAction.hasNext() )
    {
        action = iterAction.next();
        strActionName = action.getName();
        strModifyAction = "ModifyAction?action=" + strActionName;
        row    = actionsPane.addArrayLine();
        iconPane = gef.getIconPane();
        iconPane.setSpacing("30px");
        updateIcon = iconPane.addIcon();
        delIcon = iconPane.addIcon();
        delIcon.setProperties(resource.getIcon("workflowDesigner.smallDelete"),
                              resource.getString("GML.delete"),
                              "javascript:confirmRemove('RemoveAction', {action: '"
                              + URLEncoder.encode(strActionName, StandardCharsets.UTF_8) + "'}, '"
                              + resource.getString("workflowDesigner.confirmRemoveJS")
                              + " " + WebEncodeHelper.javaStringToJsString(strActionName) + " ?');" );

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
<view:frame>
<view:areaOfOperationOfCreation/>
<!-- help -->
<div class="inlineMessage">
	<table>
        <tr><th></th></tr>
        <tr>
		<td class="absmiddle"><img alt="info"
                                    src="<%=resource.getIcon("workflowDesigner.info") %>"/></td>
		<td><%=resource.getString("workflowDesigner.help.actions") %></td>
	    </tr>
    </table>
</div>
<br/>
<%
out.println(actionsPane.print());
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