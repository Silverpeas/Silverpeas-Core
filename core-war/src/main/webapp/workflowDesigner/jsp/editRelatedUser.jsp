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
     String[]                astrRoleNames = (String[])request.getAttribute( "RoleNames" ),
                             astrRoleValues = (String[])astrRoleNames.clone(),
                             astrParticipantNames = (String[])request.getAttribute( "ParticipantNames" ),
                             astrParticipantValues = (String[])astrParticipantNames.clone(),
                             astrFolderItemNames = (String[])request.getAttribute( "FolderItemNames" ),
                             astrFolderItemValues = (String[])astrFolderItemNames.clone(),
                             astrUserInfoNames = (String[])request.getAttribute( "UserInfoNames" ),
                             astrUserInfoValues = (String[])astrUserInfoNames.clone();
    RelatedUser              relatedUser = (RelatedUser)request.getAttribute("RelatedUser");
    ArrayPane                relatedUserPane;
    String                   strParentScreen = (String)request.getAttribute( "parentScreen" ),
                             strContext = (String)request.getAttribute( "context" ),
                             strParticipant = relatedUser.getParticipant() == null
                                              ? ""
                                              : relatedUser.getParticipant().getName(),
                             strFolderItem = relatedUser.getFolderItem() == null
                                             ? ""
                                             : relatedUser.getFolderItem().getName();
%>
<HTML>
<HEAD>
<view:looknfeel withCheckFormScript="true"/>
<script language="javaScript">
    function sendData()
    {
        if ( isCorrectlyFilled() )
               document.relatedUserForm.submit();
    }

    function isCorrectlyFilled()
    {
        var errorMsg = "";
        var errorNb = 0;

        // Just one of the fields should be filled
        //
        if ( ( document.relatedUserForm.participant.options.selectedIndex == 0
               && document.relatedUserForm.folderItem.options.selectedIndex == 0 )
              || ( document.relatedUserForm.participant.options.selectedIndex != 0
                   && document.relatedUserForm.folderItem.options.selectedIndex != 0 ) )
        {
            errorMsg+=" - <%=resource.getString("workflowDesigner.Either")%>"
                      + " '<%=resource.getString("workflowDesigner.participant")%>'"
                      + " <%=resource.getString("workflowDesigner.or")%>"
                      + " '<%=resource.getString("workflowDesigner.folderItem")%>'"
                      + " <%=resource.getString("GML.MustBeFilled")%>\n";
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
    browseBar.setComponentName( resource.getString("workflowDesigner.editor.relatedUser") );

    relatedUserPane = gef.getArrayPane( "relatedUser", "", request, session );
    relatedUserPane.setTitle(resource.getString("workflowDesigner.relatedUser"));

    // Participant
    //
    row = relatedUserPane.addArrayLine();
    cellText = row.addArrayCellText( resource.getString("workflowDesigner.participant") );
    cellText.setStyleSheet( "txtlibform" );
    astrParticipantNames[0] = resource.getString("GML.none");
    cellSelect = row.addArrayCellSelect( "participant", astrParticipantNames, astrParticipantValues );
    cellSelect.setSize( "1" );
    cellSelect.setSelectedValues( new String[]{ strParticipant } );

    // Folder Item
    //
    row = relatedUserPane.addArrayLine();
    cellText = row.addArrayCellText( resource.getString("workflowDesigner.folderItem") );
    cellText.setStyleSheet( "txtlibform" );
    astrFolderItemNames[0] = resource.getString("GML.none");
    cellSelect = row.addArrayCellSelect( "folderItem", astrFolderItemNames, astrFolderItemValues );
    cellSelect.setSize( "1" );
    cellSelect.setSelectedValues( new String[]{ strFolderItem } );

    // Role
    //
    row = relatedUserPane.addArrayLine();
    cellText = row.addArrayCellText( resource.getString("workflowDesigner.role") );
    cellText.setStyleSheet( "txtlibform" );

    astrRoleNames[0] = resource.getString("GML.none");
    cellSelect = row.addArrayCellSelect( "role", astrRoleNames, astrRoleValues );
    cellSelect.setSelectedValues( new String[] { relatedUser.getRole() } );
    cellSelect.setSize( "1" );

    // Relation
    //
    row = relatedUserPane.addArrayLine();
    cellText = row.addArrayCellText( resource.getString("workflowDesigner.relation") );
    cellText.setStyleSheet( "txtlibform" );
    astrUserInfoNames[0] = resource.getString("GML.noneF");
    cellSelect = row.addArrayCellSelect( "relation", astrUserInfoNames, astrUserInfoValues );
    cellSelect.setSelectedValues( new String[] { relatedUser.getRelation() } );
    cellSelect.setSize( "1" );

    out.println(window.printBefore());
    out.println(frame.printBefore());

    //help
    //
    out.println(boardHelp.printBefore());
    out.println("<table border=\"0\"><tr>");
    out.println("<td valign=\"absmiddle\"><img border=\"0\" src=\""+resource.getIcon("workflowDesigner.info")+"\"></td>");
    out.println("<td>"+resource.getString("workflowDesigner.help.relatedUser")+"</td>");
    out.println("</tr></table>");
    out.println(boardHelp.printAfter());
    out.println("<br/>");

    out.println(board.printBefore());
%>
<FORM NAME="relatedUserForm" METHOD="POST" ACTION="UpdateRelatedUser">
    <input type="hidden" name="participant_original" value="<%=strParticipant%>"/>
    <input type="hidden" name="folderItem_original" value="<%=strFolderItem%>" />
    <input type="hidden" name="role_original" value="<%=relatedUser.getRole()%>"/>
    <input type="hidden" name="relation_original" value="<%=relatedUser.getRelation()%>" />
    <input type="hidden" name="context" value="<%=strContext%>" />
<%
    out.println( relatedUserPane.print() );
%>
</FORM>
<%
    out.println(board.printAfter());
%>
<designer:buttonPane cancelAction="<%=strParentScreen%>" />
<%
    out.println(frame.printAfter());
    out.println(window.printAfter());
%>
</BODY>
</HTML>