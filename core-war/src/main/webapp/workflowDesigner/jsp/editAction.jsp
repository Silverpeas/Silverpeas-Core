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

<%
    Action          action = (Action)request.getAttribute("Action");
    String          strCancelAction = "ViewActions",
                    strCurrentScreen = "ModifyAction?action=" + URLEncoder.encode( action.getName(), UTF8 ),
                    strDescriptionContext = "actions/" + action.getName() + "/descriptions",
                    strLabelContext = "actions/" + action.getName() + "/labels",
                    strAllowedContext = URLEncoder.encode( "actions/" + action.getName() + "/allowedUsers", UTF8),
                    strEditConsequence,
                    strItem,
                    strConsequenceContext = URLEncoder.encode( "actions/" + action.getName() + "/consequences", UTF8),
                    strContextEncoded;
    ArrayPane       actionPane = gef.getArrayPane( "actionPane", strCurrentScreen, request, session ),
                    usersPane = gef.getArrayPane( "usersPane", strCurrentScreen, request, session ),
                    consequencesPane = gef.getArrayPane( "consequencesPane", strCurrentScreen, request, session );
    String[]        astrFormNames = (String[])request.getAttribute( "FormNames" ),
                    astrKindValues = (String[])request.getAttribute( "KindValues" );
    boolean         fExistingAction = ( (Boolean)request.getAttribute( "IsExisitingAction" ) ).booleanValue();
    StringBuffer    sb = new StringBuffer();
%>
<HTML>
<HEAD>
<view:looknfeel withCheckFormScript="true"/>
<script type="text/javascript" src="<%=m_context%>/workflowDesigner/jsp/JavaScript/forms.js"></script>
<script type="text/javascript">
    function sendData()
    {
        if ( isCorrectlyFilled() )
            document.actionForm.submit();
    }

    function move(direction, iConsequence)
    {
        location.href="MoveConsequence?consequenceNo=" + iConsequence +"&direction="+ direction
                      + "&context=" + "<%=strConsequenceContext%>";
    }

    function isCorrectlyFilled()
    {
        var errorMsg = "";
        var errorNb = 0;

        var actionName = document.actionForm.name.value;
        var formName = document.actionForm.form.value;

        if ( isWhitespace(actionName) )
        {
            errorMsg+="  - '<%=resource.getString("GML.name")%>' <%=resource.getString("GML.MustBeFilled")%>\n";
            errorNb++;
        }

        if (actionName.toLowerCase() == formName.toLowerCase()) {
		errorMsg+="  - <%=resource.getString("workflowDesigner.action.js.different")%>\n";
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
    browseBar.setComponentName(resource.getString("workflowDesigner.actions"), strCancelAction);
    browseBar.setExtraInformation(resource.getString("workflowDesigner.editor.action") );

    actionPane.setTitle(resource.getString("workflowDesigner.action"));

    // Name
    //
    row = actionPane.addArrayLine();
    cellText = row.addArrayCellText( resource.getString("GML.name") );
    cellText.setStyleSheet( "txtlibform" );
    row.addArrayCellInputText( "name", EncodeHelper.javaStringToHtmlString( action.getName() ) );

    // Form
    //
    row = actionPane.addArrayLine();
    cellText = row.addArrayCellText( resource.getString("workflowDesigner.form") );
    cellText.setStyleSheet( "txtlibform" );
    cellSelect = row.addArrayCellSelect( "form", astrFormNames, astrFormNames );
    cellSelect.setSize( "1" );
    cellSelect.setSelectedValues( new String[] { action.getForm() == null ? "" : action.getForm().getName() } );

    // Kind (radio buttons)
    //
    row = actionPane.addArrayLine();
    cellText = row.addArrayCellText( resource.getString("workflowDesigner.kind") );
    cellText.setStyleSheet( "txtlibform" );
    row.addArrayCellText( "" );

    for ( int i = 0; i < astrKindValues.length; i++ )
    {
        row = actionPane.addArrayLine();
        row.addArrayCellRadio( "kind", astrKindValues[i], astrKindValues[i].equals( action.getKind() ) );
        row.addArrayCellText( astrKindValues[i] );
    }

    // Allowed users
    //
    column = usersPane.addArrayColumn(resource.getString("workflowDesigner.list.allowedUsers"));
    column.setSortable(false);
    column = usersPane.addArrayColumn(resource.getString("GML.operations"));
    column.setSortable(false);

    // Only add the links if the action has been previously saved
    //
    if ( fExistingAction )
    {
        row = usersPane.addArrayLine();
        // Check if allowed users have been already defined
        //
        if ( action.getAllowedUsers() == null )
        {
            Icon addIcon;

            row.addArrayCellLink( resource.getString("workflowDesigner.allowedUsers"),
                                  "AddQualifiedUsers?context=" + strAllowedContext );

            iconPane = gef.getIconPane();
            addIcon = iconPane.addIcon();
            addIcon.setProperties(resource.getIcon("workflowDesigner.add"),
                                  resource.getString("GML.add"),
                                  "AddQualifiedUsers?context=" + strAllowedContext );

            iconPane.setSpacing("30px");
            row.addArrayCellIconPane(iconPane);
        }
        else  // Allowed users have been defined...
        {
            iconPane = gef.getIconPane();
            iconPane.setSpacing("30px");
            updateIcon = iconPane.addIcon();
            delIcon = iconPane.addIcon();

            // Create the remove link
            //
            sb.setLength(0);
            sb.append( "javascript:confirmRemove('RemoveQualifiedUsers?context=" );
            sb.append( strAllowedContext );
            sb.append( "', '" );
            sb.append( resource.getString("workflowDesigner.confirmRemoveJS") );
            sb.append( " " );
            sb.append( EncodeHelper.javaStringToJsString( resource.getString("workflowDesigner.allowedUsers") ) );
            sb.append( " ?');" );

            row.addArrayCellLink( resource.getString("workflowDesigner.allowedUsers"),
                                                     "ModifyQualifiedUsers?context=" + strAllowedContext );
            updateIcon.setProperties(resource.getIcon("workflowDesigner.smallUpdate"),
                                     resource.getString("GML.modify"),
                                     "ModifyQualifiedUsers?context=" + strAllowedContext );
            delIcon.setProperties(resource.getIcon("workflowDesigner.smallDelete"),
                                  resource.getString("GML.delete"),
                                  sb.toString() );
            row.addArrayCellIconPane(iconPane);
        }
    }

    //Fill the 'consequences' section
    //
    consequencesPane.setTitle(resource.getString("workflowDesigner.list.consequence"));
    column = consequencesPane.addArrayColumn(resource.getString("workflowDesigner.folderItem"));
    column.setSortable(false);
    column = consequencesPane.addArrayColumn(resource.getString("workflowDesigner.operator"));
    column.setSortable(false);
    column = consequencesPane.addArrayColumn(resource.getString("workflowDesigner.value"));
    column.setSortable(false);
    column = consequencesPane.addArrayColumn(resource.getString("workflowDesigner.kill"));
    column.setSortable(false);
    column = consequencesPane.addArrayColumn(resource.getString("GML.operations"));
    column.setSortable(false);

    if ( fExistingAction )
        operationPane.addOperation(resource.getIcon("workflowDesigner.add"),
            resource.getString("workflowDesigner.add.consequence"),
            "AddConsequence?context=" + URLEncoder.encode( "actions/" + action.getName(), UTF8 ) );

    if ( action.getConsequences() != null )
    {
        Consequence   consequence;
        Iterator      iterConsequences = action.getConsequences().iterateConsequence();
        int           idx = 0;

        while ( iterConsequences.hasNext() )
        {
            consequence = (Consequence)iterConsequences.next();
            iconPane = gef.getIconPane();
            iconPane.setSpacing("30px");
            updateIcon = iconPane.addIcon();
            delIcon = iconPane.addIcon();
            strItem = consequence.getItem() == null
                      ? resource.getString( "workflowDesigner.default" )
                      : consequence.getItem();
            // Create the context
            //
            sb.setLength(0);
            sb.append( "actions/" );
            sb.append( action.getName() );
            sb.append( "/consequences/" );
            sb.append( idx );
            strContextEncoded = URLEncoder.encode( sb.toString(), "UTF-8" );

            strEditConsequence = "ModifyConsequence?context=" + strContextEncoded;

            // Create the remove link
            //
            sb.setLength(0);
            sb.append("javascript:confirmRemove('RemoveConsequence?context=" );
            sb.append( strContextEncoded );
            sb.append( "', '" );
            sb.append( resource.getString("workflowDesigner.confirmRemoveJS") );
            sb.append( " " );
            sb.append( EncodeHelper.javaStringToJsString( resource.getString("workflowDesigner.consequence") ) );
            sb.append( " ?');" );

            row = consequencesPane.addArrayLine();
            row.addArrayCellLink( strItem, strEditConsequence );
            row.addArrayCellLink( consequence.getOperator() == null ? "" : consequence.getOperator(),
                                  strEditConsequence );
            row.addArrayCellLink( consequence.getValue() == null ? "" : consequence.getValue(),
                                  strEditConsequence );
            row.addArrayCellLink( consequence.getKill() ? resource.getString( "GML.yes" ) : resource.getString( "GML.no" ),
                                  strEditConsequence );

            updateIcon.setProperties(resource.getIcon("workflowDesigner.smallUpdate"),
                                                      resource.getString("GML.modify"),
                                                      strEditConsequence);
            delIcon.setProperties(resource.getIcon("workflowDesigner.smallDelete"),
                                                   resource.getString("GML.delete"),
                                                   sb.toString() );
            if (idx == 0) // first field
            {
                Icon upIcon = iconPane.addEmptyIcon();
            }
            else // not first filed
            {
                Icon upIcon = iconPane.addIcon();
                upIcon.setProperties(resource.getIcon("workflowDesigner.arrowUp"),
                                     resource.getString("workflowDesigner.up"),
                                     "javascript:move(-1, "+ Integer.toString(idx) + ");");
            }

            if ( iterConsequences.hasNext() ) // not last field
            {
                Icon downIcon = iconPane.addIcon();
                downIcon.setProperties(resource.getIcon("workflowDesigner.arrowDown"),
                                       resource.getString("workflowDesigner.down"),
                                       "javascript:move(1, "+ Integer.toString(idx) + ");");
            }
            else // last field
            {
                Icon downIcon = iconPane.addEmptyIcon();
            }

            row.addArrayCellIconPane(iconPane);
            idx++;
        }
    }

    if ( fExistingAction )
    {
        addContextualDesignation( operationPane, resource, strLabelContext, "workflowDesigner.add.label", strCurrentScreen );
        addContextualDesignation( operationPane, resource, strDescriptionContext, "workflowDesigner.add.description", strCurrentScreen );
    }

    out.println(window.printBefore());
    out.println(frame.printBefore());

    //help
    //
    out.println(boardHelp.printBefore());
    out.println("<table border=\"0\"><tr>");
    out.println("<td valign=\"absmiddle\"><img border=\"0\" src=\""+resource.getIcon("workflowDesigner.info")+"\"></td>");
    out.println("<td>"+resource.getString("workflowDesigner.help.action")+"</td>");
    out.println("</tr></table>");
    out.println(boardHelp.printAfter());
    out.println("<br/>");

    out.println(board.printBefore());
%>
<FORM NAME="actionForm" METHOD="POST" ACTION="UpdateAction">
	<input type="hidden" name="name_original" value="<%=EncodeHelper.javaStringToHtmlString(action.getName())%>">
<%
    out.println( actionPane.print() );
    out.println( "<br>" );

    //Allowed users
    //
    out.println( usersPane.print() );
    out.println( "<br>" );

    //Consequences
    //
    out.println( consequencesPane.print() );

    // Labels
    //
%>
</FORM>
<br>
<designer:contextualDesignationList
    designations="<%=action.getLabels()%>"
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
    designations="<%=action.getDescriptions()%>"
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