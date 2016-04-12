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
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<%@ taglib prefix="designer" uri="/WEB-INF/workflowEditor.tld" %>

<%
    Role            role = (Role)request.getAttribute("Role");
    String          strCancelAction = "ViewRoles",
                    strCurrentScreen = "ModifyRole?role=" + role.getName(),
                    strDescriptionContext = "roles/" + role.getName() + "/descriptions",
                    strLabelContext = "roles/" + role.getName() + "/labels";
    ArrayPane       rolePane = gef.getArrayPane( "roleName", strCurrentScreen, request, session );
    boolean         fExistingRole = ( (Boolean)request.getAttribute( "IsExisitingRole" ) ).booleanValue();
%>
<HTML>
<HEAD>
<view:looknfeel withCheckFormScript="true"/>
<script type="text/javascript" src="<%=m_context%>/workflowDesigner/jsp/JavaScript/forms.js"></script>
<script language="javaScript">
    function sendData()
    {
        if ( isCorrectlyFilled() )
		   document.roleForm.submit();
    }

    function isCorrectlyFilled()
    {
        var errorMsg = "";
        var errorNb = 0;

        if ( isWhitespace(document.roleForm.name.value) )
        {
            errorMsg+="  - '<%=resource.getString("GML.name")%>' <%=resource.getString("GML.MustBeFilled")%>\n";
            errorNb++;
        }

        switch(errorNb)
        {
            case 0 :
                result = true;
                break;
            case 1 :
                errorMsg = "<%=resource.getString("GML.ThisFormContains")%> 1 <%=resource.getString("GML.error").toLowerCase()%> : \n" + errorMsg;
                window.alert(errorMsg);
                result = false;
                break;
            default :
                errorMsg = "<%=resource.getString("GML.ThisFormContains")%> " + errorNb + " <%=resource.getString("GML.errors").toLowerCase()%> :\n" + errorMsg;
                window.alert(errorMsg);
                result = false;
                break;
        }
        return result;
    }
</script>
</HEAD>
<BODY leftmargin="5" topmargin="5" marginwidth="5" marginheight="5" >
<%
    browseBar.setDomainName(resource.getString("workflowDesigner.toolName"));
    browseBar.setComponentName(resource.getString("workflowDesigner.roles"), strCancelAction);
    browseBar.setExtraInformation(resource.getString("workflowDesigner.editor.role") );

    rolePane.setTitle(resource.getString("workflowDesigner.role"));

    if ( fExistingRole )
    {
        addContextualDesignation( operationPane, resource, strLabelContext, "workflowDesigner.add.label", strCurrentScreen );
        addContextualDesignation( operationPane, resource, strDescriptionContext, "workflowDesigner.add.description", strCurrentScreen );
    }

    row = rolePane.addArrayLine();
    cellText = row.addArrayCellText( resource.getString("GML.name") );
    cellText.setStyleSheet( "txtlibform" );
    row.addArrayCellInputText( "name", role.getName() );

    out.println(window.printBefore());
    out.println(frame.printBefore());

    //help
    //
    out.println(boardHelp.printBefore());
    out.println("<table border=\"0\"><tr>");
    out.println("<td valign=\"absmiddle\"><img border=\"0\" src=\""+resource.getIcon("workflowDesigner.info")+"\"></td>");
    out.println("<td>"+resource.getString("workflowDesigner.help.role")+"</td>");
    out.println("</tr></table>");
    out.println(boardHelp.printAfter());
    out.println("<br/>");

    out.println(board.printBefore());
%>
<FORM NAME="roleForm" METHOD="POST" ACTION="UpdateRole">
	<input type="hidden" name="name_original" value="<%=role.getName()%>">
<%
    out.println( rolePane.print() );
    // Labels
    //
%>
</FORM>
<br>
<designer:contextualDesignationList
    designations="<%=role.getLabels()%>"
    context="<%=strLabelContext%>"
    parentScreen="<%=strCurrentScreen%>"
    columnLabelKey="GML.label"
    paneTitleKey="workflowDesigner.list.label"/>
<!--
// Descriptions
//
-->
<br>
<designer:contextualDesignationList
    designations="<%=role.getDescriptions()%>"
    context="<%=strDescriptionContext%>"
    parentScreen="<%=strCurrentScreen%>"
    columnLabelKey="GML.description"
    paneTitleKey="workflowDesigner.list.description"/>

<%
	out.println(board.printAfter());
%>
<designer:buttonPane cancelAction="<%=strCancelAction%>" />
<%
    out.println(frame.printAfter());
    out.println(window.printAfter());
%>
</BODY>
</HTML>