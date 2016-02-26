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
    QualifiedUsers           qualifiedUsers = (QualifiedUsers)request.getAttribute( "QualifiedUsers" );
    Boolean                  fDisplayRoleSelector = (Boolean)request.getAttribute( "RoleSelector" ),
                             fNotifiedUser = (Boolean)request.getAttribute( "NotifiedUser"),
                             fExistingQualifiedUser = (Boolean)request.getAttribute("IsExisitingQualifiedUser");
    String                   strParentScreen = (String)request.getAttribute( "parentScreen" ),
                             strEditorName = (String)request.getAttribute( "EditorName" ),
                             strContext = (String)request.getAttribute( "context" ),
                             strCurrentScreen = "ModifyQualifiedUsers?context=" + strContext;
    String[]                 astrRoleNames = (String[])request.getAttribute( "RoleNames" ),
                             astrRoleValues = (String[])astrRoleNames.clone();
    ArrayPane                headerPane = gef.getArrayPane( "headerPane", "", request, session ),
                             inRolePane = gef.getArrayPane( "inRolePane", "", request, session );
%>

<html>
<head>
<view:looknfeel withCheckFormScript="true"/>
<script type="text/javascript" src="<%=m_context%>/workflowDesigner/jsp/JavaScript/forms.js"></script>
<script type="text/javascript">
    function sendData()
    {
        if ( isCorrectlyFilled() )
        {
            document.qualifiedUsersForm.submit();
        }
    }

    function isCorrectlyFilled()
    {
        var errorMsg = '';
        var errorNb = 0;
        var result;
        var fExistingQualifiedUser = <%=fExistingQualifiedUser.toString()%>;
        var fUserInRoleVisible = true;
        var fMessageVisible = <%=fNotifiedUser.toString()%>;
        var fRelatedUserDefined = <%=Boolean.toString( qualifiedUsers.iterateRelatedUser().hasNext() )%>;
        var fUserInRoleDefined = false;
        var i = 0;

        // Verify only for an existing (not new object)
        //
        if ( fExistingQualifiedUser )
        {
            if ( fUserInRoleVisible && document.qualifiedUsersForm.userInRole != null )
                for ( i = 0; i < document.qualifiedUsersForm.userInRole.length; i++ )
                    fUserInRoleDefined = fUserInRoleDefined || document.qualifiedUsersForm.userInRole[i].checked;

            if ( fUserInRoleVisible && !fUserInRoleDefined && !fRelatedUserDefined )
            {
                // If Both UserInRole and RelatedUser can be entered, one of them must be set
                //
                errorMsg +=" - '<%=resource.getString("workflowDesigner.list.userInRole")%>'"
                         + " <%=resource.getString("workflowDesigner.or")%>"
                         + " '<%=resource.getString("workflowDesigner.list.relatedUser")%>'"
                         + " <%=resource.getString("GML.MustBeFilled")%>\n";
                errorNb++;
            }
            else if ( !fUserInRoleVisible && !fRelatedUserDefined  )
            {
                // If only RelatedUser input is visible at least one must be defined
                //
                errorMsg +=" - '<%=resource.getString("workflowDesigner.list.relatedUser")%>'"
                         + " <%=resource.getString("GML.MustBeFilled")%>\n";
                errorNb++;
            }
        }

        // If the message is visible it should be filled
        //
        if ( fMessageVisible && isWhitespace(document.qualifiedUsersForm.message.value) )
        {
            errorMsg+="  - '<%=resource.getString("workflowDesigner.message")%>' <%=resource.getString("GML.MustBeFilled")%>\n";
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
</head>
<body leftmargin="5" topmargin="5" marginwidth="5" marginheight="5" >
<%
    browseBar.setDomainName(resource.getString("workflowDesigner.toolName"));
    browseBar.setComponentName( resource.getString(strEditorName) );

    if ( fExistingQualifiedUser.booleanValue() )
        addRelatedUser( operationPane, resource, strContext );

    // Role - display for workingUsers and InterestedUsers, omit for AllowedUsers and Notified users
    //
    if ( fDisplayRoleSelector.booleanValue() )
    {
        headerPane.setTitle(resource.getString(strEditorName));
        row = headerPane.addArrayLine();
        cellText = row.addArrayCellText( resource.getString("workflowDesigner.role") );
        cellText.setStyleSheet( "txtlibform" );

        astrRoleNames[0] = resource.getString( "GML.none" );
        cellSelect = row.addArrayCellSelect( "role", astrRoleNames, astrRoleValues );
        cellSelect.setSelectedValues( new String[] { qualifiedUsers.getRole() } );
        cellSelect.setSize( "1" );
    }

    // add a row with the message to the header pane
    //
    if ( fNotifiedUser.booleanValue() )
    {
        row = headerPane.addArrayLine();
        cellText = row.addArrayCellText( resource.getString("workflowDesigner.message") );
        cellText.setStyleSheet( "txtlibform" );

        cellInput = row.addArrayCellInputText( "message", qualifiedUsers.getMessage() );
        cellInput.setSize( "100" );
    }

    // User In Role - print a list of role names, based on the 'roles' element
    // Starting form i = 1 since the '0' element holds the 'none' choice
    //
    inRolePane.setTitle( resource.getString( "workflowDesigner.list.userInRole" ) );

    for ( int i = 1; i < astrRoleValues.length; i ++ )
    {
        boolean         fChecked;

        row = inRolePane.addArrayLine();
        fChecked = qualifiedUsers.getUserInRole( astrRoleValues[i] ) != null;

        row.addArrayCellCheckbox( "userInRole", astrRoleValues[i], fChecked );
        row.addArrayCellText( astrRoleValues[i] );
    }

    out.println(window.printBefore());
    out.println(frame.printBefore());

    //help
    //
    out.println(boardHelp.printBefore());
    out.println("<table border=\"0\"><tr>");
    out.println("<td valign=\"absmiddle\"><img border=\"0\" src=\""+resource.getIcon("workflowDesigner.info")+"\"></td>");

    if ( fNotifiedUser.booleanValue() )
    {
        out.println("<td>"+resource.getString("workflowDesigner.help.notifiedUsers")+"</td>");
    }
    else  // allowed, interested or working users
    {
        if ( fDisplayRoleSelector.booleanValue() ) // interested or working users
            out.println("<td>"+resource.getString("workflowDesigner.help.interestedOrWorkingUsers")+"</td>");
        else                                       // allowed users
            out.println("<td>"+resource.getString("workflowDesigner.help.allowedUsers")+"</td>");
    }

    out.println("</tr></table>");
    out.println(boardHelp.printAfter());
    out.println("<br/>");

    out.println(board.printBefore());
%>
<form name="qualifiedUsersForm" method="post" action="UpdateQualifiedUsers">
    <input type="hidden" name="role_original" value="<%=qualifiedUsers.getRole()%> "/>
    <input type="hidden" name="context" value="<%=strContext%>" />
<%
    if ( fDisplayRoleSelector.booleanValue() || fNotifiedUser.booleanValue() ) {
        out.println( headerPane.print() );
    }

    // List of users in role
    out.println( inRolePane.print() );
%>
</form>

<!-- List of related users -->
<designer:relatedUsersList iterRelatedUser="<%=qualifiedUsers.iterateRelatedUser()%>"
    context="<%=strContext%>" currentScreen="<%=strCurrentScreen%>" />

<%
    out.println(board.printAfter());
%>
<designer:buttonPane cancelAction="<%=strParentScreen%>" />
<%
    out.println(frame.printAfter());
    out.println(window.printAfter());
%>
</body>
</html>