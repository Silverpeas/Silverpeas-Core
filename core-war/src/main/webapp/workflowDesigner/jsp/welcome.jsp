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

<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<%@ include file="check.jsp" %>
<%
Iterator  workflows = ((List) request.getAttribute("ProcessFileNames")).iterator();
ArrayPane arrayPane;
String    strProcessFileName,
          strProcessFileNameURLEncoded,
          strProcessFileNameJSEncoded;
%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<view:looknfeel/>
<script type="text/javascript" src="<%=m_context%>/workflowDesigner/jsp/JavaScript/forms.js"></script>
</head>
<body>
<%
browseBar.setDomainName(resource.getString("workflowDesigner.toolName"));
browseBar.setComponentName(resource.getString("workflowDesigner.list.workflow"));

operationPane.addOperationOfCreation(resource.getIcon("workflowDesigner.add.workflow"),
        resource.getString("workflowDesigner.addWorkflow"),
        "AddWorkflow");

operationPane.addOperationOfCreation(resource.getIcon("workflowDesigner.import"),
        resource.getString("workflowDesigner.importWorkflow"),
        "ImportWorkflow");

arrayPane = gef.getArrayPane("workflowList", "Main", request, session);
arrayPane.setVisibleLineNumber(20);
arrayPane.addArrayColumn(resource.getString("GML.path"));
column = arrayPane.addArrayColumn(resource.getString("GML.operations"));
column.setSortable( false );

while ( workflows.hasNext() )
{
	strProcessFileName = (String) workflows.next();
    strProcessFileNameURLEncoded = URLEncoder.encode( strProcessFileName, UTF8);
    strProcessFileNameJSEncoded = EncodeHelper.javaStringToJsString( strProcessFileName );

    row       = arrayPane.addArrayLine();
	iconPane = gef.getIconPane();
	updateIcon = iconPane.addIcon();
	delIcon = iconPane.addIcon();

	row.addArrayCellLink( EncodeHelper.javaStringToHtmlString( strProcessFileName ),
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
%>
<view:frame>
<view:areaOfOperationOfCreation/>
<!-- help -->
<div class="inlineMessage">
	<%=resource.getString("workflowDesigner.help.toolName") %>
</div>
<br/>
<%
out.println(arrayPane.print());
%>
</view:frame>
<%
out.println(window.printAfter());
%>
</body>
</html>