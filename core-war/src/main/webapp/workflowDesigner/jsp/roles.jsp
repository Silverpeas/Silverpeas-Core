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
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%@ include file="check.jsp" %>
<%@ taglib prefix="designer" uri="/WEB-INF/workflowEditor.tld" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>

<%
String     strRoleName,
           strCurrentTab = "ViewRoles";
Roles      roles = (Roles)request.getAttribute( "Roles" );
ArrayPane  rolesPane = gef.getArrayPane("rolesList", strCurrentTab, request, session);
%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<view:looknfeel/>
<script type="text/javascript" src="<%=m_context%>/workflowDesigner/jsp/JavaScript/forms.js"></script>
<script type="text/javascript">
    function sendData()
    {
        document.workflowHeaderForm.submit();
    }
</script>
</head>
<body class="page_content_admin">
<%
browseBar.setDomainName(resource.getString("workflowDesigner.toolName") );
browseBar.setComponentName(resource.getString("workflowDesigner.roles") );

operationPane.addOperationOfCreation(resource.getIcon("workflowDesigner.add"),
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
                                                        + " " + WebEncodeHelper.javaStringToJsString( strRoleName ) + " ?');" );
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
<view:frame>
<view:areaOfOperationOfCreation/>
<!-- help -->
<div class="inlineMessage">
	<table border="0"><tr>
		<td valign="absmiddle"><img border="0" src="<%=resource.getIcon("workflowDesigner.info") %>"/></td>
		<td><%=resource.getString("workflowDesigner.help.roles") %></td>
	</tr></table>
</div>
<br/>
<%
out.println(rolesPane.print());
%>
<form name="workflowHeaderForm" action="UpdateWorkflow" method="post">
<designer:buttonPane cancelAction="Main" />
</form>
</view:frame>
<%
out.println(window.printAfter());
%>
</body>
</html>