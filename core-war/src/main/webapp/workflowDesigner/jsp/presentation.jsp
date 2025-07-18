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
StringBuilder strColumnList;
    String strCurrentTab   = "ViewPresentation";
    ArrayPane       columnPane = gef.getArrayPane("columnsList", strCurrentTab, request, session);
Presentation    presentation = (Presentation)request.getAttribute( "Presentation" );
Iterator<Columns> iterColumns = presentation.iterateColumns();
Iterator<Column> iterColumn;

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
    Columns columns = iterColumns.next();

    row = columnPane.addArrayLine();
    iconPane = gef.getIconPane();
    updateIcon = iconPane.addIcon();
    delIcon = iconPane.addIcon();

    updateIcon.setProperties(resource.getIcon("workflowDesigner.smallUpdate"),
                             resource.getString("GML.modify"),
                             "ModifyColumns?columns=" + columns.getRoleName() );
    delIcon.setProperties(resource.getIcon("workflowDesigner.smallDelete"),
                          resource.getString("GML.delete"),
                          "javascript:confirmRemove('RemoveColumns', {columns: '"
                          + URLEncoder.encode(columns.getRoleName(), StandardCharsets.UTF_8) + "'}, '"
                          + resource.getString("workflowDesigner.confirmRemoveJS") + " "
                          + WebEncodeHelper.javaStringToJsString( columns.getRoleName() ) + " ?');" );
    iconPane.setSpacing("30px");

    // Build a comma-separated list of referenced items to put in the 'column list' column
    //
    strColumnList = new StringBuilder();
    iterColumn = columns.iterateColumn();

    while ( iterColumn.hasNext() )
    {
        if (strColumnList.length() > 0)
            strColumnList.append(", ");

        strColumnList.append(iterColumn.next().getItem().getName());
    }

    row.addArrayCellLink( columns.getRoleName(), "ModifyColumns?columns=" + columns.getRoleName() );
    row.addArrayCellLink(strColumnList.toString(), "ModifyColumns?columns=" + columns.getRoleName() );
    row.addArrayCellIconPane(iconPane);
}

out.println(window.printBefore());
%>
<designer:processModelTabs currentTab="ViewPresentation"/>
<view:frame>
<view:areaOfOperationOfCreation/>
<!-- help -->
<div class="inlineMessage">
	<table>
        <tr><th></th></tr>
        <tr>
		<td class="absmiddle"><img alt="info"
                                    src="<%=resource.getIcon("workflowDesigner.info") %>"/></td>
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
</view:sp-body-part>
</view:sp-page>