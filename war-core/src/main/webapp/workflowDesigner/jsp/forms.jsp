<%--

    Copyright (C) 2000 - 2012 Silverpeas

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

<%
String     strContextEncoded,
           strRoleName,
           strEditForm,
           strCurrentTab = "ViewForms";
Forms      forms = (Forms)request.getAttribute( "Forms" );
ArrayPane  formsPane = gef.getArrayPane("formsList", strCurrentTab, request, session);
%>

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
browseBar.setDomainName(resource.getString("workflowDesigner.toolName") );
browseBar.setComponentName(resource.getString("workflowDesigner.forms") );

operationPane.addOperationOfCreation(resource.getIcon("workflowDesigner.add"),
        resource.getString("workflowDesigner.add.form"),
        "AddForm");

formsPane.setVisibleLineNumber(20);
formsPane.setTitle(resource.getString("workflowDesigner.list.form"));
formsPane.addArrayColumn(resource.getString("GML.name"));
formsPane.addArrayColumn(resource.getString("workflowDesigner.role"));
column = formsPane.addArrayColumn(resource.getString("workflowDesigner.HTMLFileName"));
column.setSortable( false );
column = formsPane.addArrayColumn(resource.getString("GML.operations"));
column.setSortable( false );

if ( forms != null )
{
    Form       form;
    Iterator   iterForm = forms.iterateForm();

    while ( iterForm.hasNext() )
    {
        form = (Form)iterForm.next();
        strRoleName = form.getRole() == null ? "" : form.getRole();
        strContextEncoded = URLEncoder.encode( "forms[" + form.getName() + "," + strRoleName + "]", UTF8);
        strEditForm = "ModifyForm?context=" + strContextEncoded;
        row    = formsPane.addArrayLine();
        iconPane = gef.getIconPane();
        iconPane.setSpacing("30px");
        updateIcon = iconPane.addIcon();
        delIcon = iconPane.addIcon();
        delIcon.setProperties(resource.getIcon("workflowDesigner.smallDelete"),
                              resource.getString("GML.delete"),
                              "javascript:confirmRemove('RemoveForm?context=" 
                              + strContextEncoded + "', '"
                              + resource.getString("workflowDesigner.confirmRemoveJS") + " " 
                              + EncodeHelper.javaStringToJsString( form.getName() + " " + strRoleName ) 
                              + " ?');" );
        updateIcon.setProperties(resource.getIcon("workflowDesigner.smallUpdate"),
                                 resource.getString("GML.modify"),
                                 strEditForm );
        
        
        row.addArrayCellLink( form.getName(), strEditForm );
        row.addArrayCellLink( strRoleName, strEditForm );
        row.addArrayCellLink( form.getHTMLFileName() == null ? "" : form.getHTMLFileName(),
                              strEditForm );
        row.addArrayCellIconPane(iconPane);
    }
}

out.println(window.printBefore());
%>
<designer:processModelTabs currentTab="ViewForms"/>
<view:frame>
<view:areaOfOperationOfCreation/>
<!-- help -->
<div class="inlineMessage">
	<table border="0"><tr>
		<td valign="absmiddle"><img border="0" src="<%=resource.getIcon("workflowDesigner.info") %>"/></td>
		<td><%=resource.getString("workflowDesigner.help.forms") %></td>
	</tr></table>
</div>
<br/>
<%
out.println(formsPane.print());
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