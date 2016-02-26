<%--

    Copyright (C) 2000 - 2013 Silverpeas

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
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

--%>

<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%@ include file="check.jsp" %>
<%@ taglib prefix="designer" uri="/WEB-INF/workflowEditor.tld" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<view:looknfeel/>
<script type="text/javascript" src="<%=m_context%>/workflowDesigner/jsp/JavaScript/forms.js"></script>
<script type="text/javascript">
function sendData() {
    document.workflowHeaderForm.submit();
}
</script>
</head>
<body>
<%
String          strColumnList,
                strCurrentTab   = "ViewPresentation";
ArrayPane       columnPane = gef.getArrayPane("columnsList", strCurrentTab, request, session);
Presentation    presentation = (Presentation)request.getAttribute( "Presentation" );
Iterator        iterColumns = presentation.iterateColumns(),
                iterColumn;

browseBar.setDomainName(resource.getString("workflowDesigner.toolName"));
browseBar.setComponentName(resource.getString("workflowDesigner.presentationTab"));

operationPane.addOperationOfCreation(resource.getIcon("workflowDesigner.add"),
                           resource.getString("workflowDesigner.addColumns"),
                           "AddColumns");
addContextualDesignation( operationPane, resource, "presentation/titles", "workflowDesigner.add.title", strCurrentTab );

columnPane.setVisibleLineNumber(20);
columnPane.setTitle(resource.getString("workflowDesigner.list.columns"));
columnPane.addArrayColumn(resource.getString("workflowDesigner.role" ) );
columnPane.addArrayColumn(resource.getString("workflowDesigner.list.columns"));
column = columnPane.addArrayColumn(resource.getString("GML.operations"));
column.setSortable(false);

// Fill the 'columns' section
//
while ( iterColumns.hasNext() )
{
    Columns columns = (Columns)iterColumns.next();

    row = columnPane.addArrayLine();
    iconPane = gef.getIconPane();
    updateIcon = iconPane.addIcon();
    delIcon = iconPane.addIcon();

    updateIcon.setProperties(resource.getIcon("workflowDesigner.smallUpdate"),
                             resource.getString("GML.modify"),
                             "ModifyColumns?columns=" + columns.getRoleName() );
    delIcon.setProperties(resource.getIcon("workflowDesigner.smallDelete"),
                          resource.getString("GML.delete"),
                          "javascript:confirmRemove('RemoveColumns?columns="
                          + URLEncoder.encode(columns.getRoleName(), UTF8) + "', '"
                          + resource.getString("workflowDesigner.confirmRemoveJS") + " "
                          + EncodeHelper.javaStringToJsString( columns.getRoleName() ) + " ?');" );
    iconPane.setSpacing("30px");

    // Build a comma-separated list of refrenced items to put in the 'column list' column
    //
    strColumnList = "";
    iterColumn = columns.iterateColumn();

    while ( iterColumn.hasNext() )
    {
        if ( strColumnList.length() > 0 )
            strColumnList += ", ";

        strColumnList += ((Column)iterColumn.next()).getItem().getName();
    }

    row.addArrayCellLink( columns.getRoleName(), "ModifyColumns?columns=" + columns.getRoleName() );
    row.addArrayCellLink( strColumnList, "ModifyColumns?columns=" + columns.getRoleName() );
    row.addArrayCellIconPane(iconPane);
}

out.println(window.printBefore());
%>
<designer:processModelTabs currentTab="ViewPresentation"/>
<view:frame>
<view:areaOfOperationOfCreation/>
<!-- help -->
<div class="inlineMessage">
	<table border="0"><tr>
		<td valign="absmiddle"><img border="0" src="<%=resource.getIcon("workflowDesigner.info") %>"/></td>
		<td><%=resource.getString("workflowDesigner.help.presentation") %></td>
	</tr></table>
</div>
<br/>
<%
out.println( columnPane.print() );
%>
<br/>
<designer:contextualDesignationList
    designations="<%=presentation.getTitles()%>"
    context="presentation/titles"
    parentScreen="<%=strCurrentTab%>"
    columnLabelKey="GML.title"
    paneTitleKey="workflowDesigner.list.title"/>
<form name="workflowHeaderForm" action="UpdateWorkflow" method="post">
<designer:buttonPane cancelAction="Main" />
</form>
</view:frame>
<%
out.println(window.printAfter());
%>
</body>
</html>