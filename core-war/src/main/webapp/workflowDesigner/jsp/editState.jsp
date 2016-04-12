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
    State           state = (State)request.getAttribute("State");
    AllowedActions  allowedActions = state.getAllowedActionsEx();
    String          strCancelAction = "ViewStates",
                    strCurrentScreen = "ModifyState?state=" + state.getName(),
                    strDescriptionContext = "states/" + state.getName() + "/descriptions",
                    strLabelContext = "states/" + state.getName() + "/labels",
                    strActivityContext = "states/" + state.getName() + "/activities",
                    strWorkingContext = URLEncoder.encode( "states/" + state.getName() + "/workingUsers", UTF8 ),
                    strInterestedContext = URLEncoder.encode( "states/" + state.getName() + "/interestedUsers", UTF8 );
    ArrayPane       statePane = gef.getArrayPane( "statePane", strCurrentScreen, request, session ),
                    usersPane = gef.getArrayPane( "usersPane", strCurrentScreen, request, session ),
                    allowedActionsPane = gef.getArrayPane( "allowedActionsPane", strCurrentScreen, request, session );
    String[]        astrActionNames = (String[])request.getAttribute( "ActionNames" ),
                    astrActionValues = (String[])astrActionNames.clone();
    Action          timeoutAction = state.getTimeoutAction();
    boolean         fExistingState = ( (Boolean)request.getAttribute( "IsExisitingState" ) ).booleanValue();
    StringBuffer    sb = new StringBuffer();
%>
<HTML>
<HEAD>
<view:looknfeel withCheckFormScript="true"/>
<script type="text/javascript" src="<%=m_context%>/workflowDesigner/jsp/JavaScript/forms.js"></script>
<script language="javaScript">
    function sendData()
    {
        if ( isCorrectlyFilled() )
            document.stateForm.submit();
    }

    function activateTimeout()
    {
        if ( document.stateForm.timeoutAction.options.selectedIndex == 0 )
        {
            // Clear and block timeout interval & notify admin
            //
            document.stateForm.notifyAdmin.checked = false;
            document.stateForm.notifyAdmin.disabled = true;
            document.stateForm.timeoutInterval.value = '';
            document.stateForm.timeoutInterval.readOnly = true;
            document.stateForm.timeoutInterval.disabled= true;
        }
        else
        {
            // Activate timeout interval & notify admin
            //
            document.stateForm.notifyAdmin.disabled = false;
            document.stateForm.notifyAdmin.checked = true;
            document.stateForm.timeoutInterval.readOnly = false;
            document.stateForm.timeoutInterval.disabled = false;
        }
    }

    function isCorrectlyFilled()
    {
        var errorMsg = "";
        var errorNb = 0;

        if ( isWhitespace(document.stateForm.name.value) )
        {
            errorMsg+="  - '<%=resource.getString("GML.name")%>' <%=resource.getString("GML.MustBeFilled")%>\n";
            errorNb++;
        }

        // If timeout action has been specified then an integer value must be given for timeout interval
        //
        if ( document.stateForm.timeoutAction.options.selectedIndex != 0
             && ( isEmpty(document.stateForm.timeoutInterval.value)
                  || !isInteger(document.stateForm.timeoutInterval.value) ) )
        {
            errorMsg+="  - '<%=resource.getString("workflowDesigner.timeoutInterval")%>'"
                      + " <%=resource.getString("GML.MustContainsNumber")%>\n";
            errorNb++;
        }

        switch(errorNb)
        {
            case 0 :
                result = true;
                break;
            case 1 :
                errorMsg = "<%=resource.getString("GML.ThisFormContains")%> 1 <%=resource.getString("GML.error").toLowerCase()%> : \n"
                           + errorMsg;
                window.alert(errorMsg);
                result = false;
                break;
            default :
                errorMsg = "<%=resource.getString("GML.ThisFormContains")%> "
                           + errorNb
                           + " <%=resource.getString("GML.errors").toLowerCase()%> :\n"
                           + errorMsg;
                window.alert(errorMsg);
                result = false;
                break;
        }
        return result;
    }
</script>
</HEAD>
<BODY leftmargin="5" topmargin="5" marginwidth="5" marginheight="5" onLoad="activateTimeout()">
<%
    browseBar.setDomainName(resource.getString("workflowDesigner.toolName"));
    browseBar.setComponentName(resource.getString("workflowDesigner.states"), strCancelAction);
    browseBar.setExtraInformation(resource.getString("workflowDesigner.editor.state") );

    statePane.setTitle(resource.getString("workflowDesigner.state"));

    if ( fExistingState )
    {
       // addContextualDesignation( operationPane, resource, strActivityContext, "workflowDesigner.add.activity", strCurrentScreen );
        addContextualDesignation( operationPane, resource, strLabelContext, "workflowDesigner.add.label", strCurrentScreen );
        addContextualDesignation( operationPane, resource, strDescriptionContext, "workflowDesigner.add.description", strCurrentScreen );
    }

    // Name
    //
    row = statePane.addArrayLine();
    cellText = row.addArrayCellText( resource.getString("GML.name") );
    cellText.setStyleSheet( "txtlibform" );
    row.addArrayCellInputText( "name", state.getName() );

    // Timeout Action
    //
    row = statePane.addArrayLine();
    cellText = row.addArrayCellText( resource.getString("workflowDesigner.timeoutAction") );
    cellText.setStyleSheet( "txtlibform" );
    astrActionNames[0] = resource.getString( "GML.noneF" );
    cellSelect = row.addArrayCellSelect( "timeoutAction", astrActionNames, astrActionValues );
    cellSelect.setSize( "1" );
    cellSelect.setSelectedValues( new String[] { timeoutAction == null ? "" : timeoutAction.getName() } );
    cellSelect.setAction( "onChange=\"activateTimeout()\"" );

    // Timeout Interval
    //
    row = statePane.addArrayLine();
    cellText = row.addArrayCellText( resource.getString("workflowDesigner.timeoutInterval") );
    cellText.setStyleSheet( "txtlibform" );
    row.addArrayCellInputText( "timeoutInterval",
                                 state.getTimeoutInterval() == -1
                                   ? ""
                                   : Integer.toString( state.getTimeoutInterval() ) );

    // Timeout Notify Admin
    //
    row = statePane.addArrayLine();
    cellText = row.addArrayCellText( resource.getString("workflowDesigner.notifyAdmin") );
    cellText.setStyleSheet( "txtlibform" );
    row.addArrayCellCheckbox( "notifyAdmin", "true", state.getTimeoutNotifyAdmin() );

    // Interested users and working users
    //
    column = usersPane.addArrayColumn(resource.getString("workflowDesigner.list.workingInterestedUsers"));
    column.setSortable(false);
    column = usersPane.addArrayColumn(resource.getString("GML.operations"));
    column.setSortable(false);

    // Only add the links if the state has been saved previously
    //
    if ( fExistingState )
    {
        // Check if working users have been already defined
        //
        row = usersPane.addArrayLine();
        if ( state.getWorkingUsersEx() == null )
        {
            Icon addIcon;

            row.addArrayCellLink( resource.getString("workflowDesigner.workingUsers"),
                                  "AddQualifiedUsers?context=" + strWorkingContext );

            iconPane = gef.getIconPane();
            addIcon = iconPane.addIcon();
            addIcon.setProperties(resource.getIcon("workflowDesigner.add"),
                                  resource.getString("GML.add"),
                                  "AddQualifiedUsers?context=" + strWorkingContext );

            iconPane.setSpacing("30px");
            row.addArrayCellIconPane(iconPane);
        }
        else  // Working users have been defined...
        {
            iconPane = gef.getIconPane();
            iconPane.setSpacing("30px");
            updateIcon = iconPane.addIcon();
            delIcon = iconPane.addIcon();

            // Create the remove link
            //
            sb.setLength(0);
            sb.append( "javascript:confirmRemove('RemoveQualifiedUsers?context=" );
            sb.append( strWorkingContext );
            sb.append( "', '" );
            sb.append( resource.getString("workflowDesigner.confirmRemoveJS") );
            sb.append( " " );
            sb.append( EncodeHelper.javaStringToJsString( resource.getString("workflowDesigner.workingUsers") ) );
            sb.append( " ?');" );

            row.addArrayCellLink( resource.getString("workflowDesigner.workingUsers"),
                                                     "ModifyQualifiedUsers?context=" + strWorkingContext );
            updateIcon.setProperties(resource.getIcon("workflowDesigner.smallUpdate"),
                                     resource.getString("GML.modify"),
                                     "ModifyQualifiedUsers?context=" + strWorkingContext );
            delIcon.setProperties(resource.getIcon("workflowDesigner.smallDelete"),
                                  resource.getString("GML.delete"),
                                  sb.toString() );
            row.addArrayCellIconPane(iconPane);
        }

        // Check if interested users have been already defined
        //
        row = usersPane.addArrayLine();

        if ( state.getInterestedUsersEx() == null )
        {
            Icon addIcon;

            row.addArrayCellLink( resource.getString("workflowDesigner.interestedUsers"),
                                  "AddQualifiedUsers?context=" + strInterestedContext );
            iconPane = gef.getIconPane();
            addIcon = iconPane.addIcon();
            addIcon.setProperties(resource.getIcon("workflowDesigner.add"),
                                  resource.getString("GML.add"),
                                  "AddQualifiedUsers?context=" + strInterestedContext );
            iconPane.setSpacing("30px");
            row.addArrayCellIconPane(iconPane);
        }
        else  // Interested users have been defined...
        {
            iconPane = gef.getIconPane();
            iconPane.setSpacing("30px");
            updateIcon = iconPane.addIcon();
            delIcon = iconPane.addIcon();

            // Create the remove link
            //
            sb.setLength(0);
            sb.append( "javascript:confirmRemove('RemoveQualifiedUsers?context=" );
            sb.append( strInterestedContext );
            sb.append( "', '" );
            sb.append( resource.getString("workflowDesigner.confirmRemoveJS") );
            sb.append( " " );
            sb.append( EncodeHelper.javaStringToJsString( resource.getString("workflowDesigner.interestedUsers") ) );
            sb.append( " ?');" );

            row.addArrayCellLink( resource.getString("workflowDesigner.interestedUsers"),
                                                     "ModifyQualifiedUsers?context=" + strInterestedContext );
            updateIcon.setProperties(resource.getIcon("workflowDesigner.smallUpdate"),
                                     resource.getString("GML.modify"),
                                     "ModifyQualifiedUsers?context=" + strInterestedContext );
            delIcon.setProperties(resource.getIcon("workflowDesigner.smallDelete"),
                                  resource.getString("GML.delete"),
                                  sb.toString() );
            row.addArrayCellIconPane(iconPane);
        }
    }

    //Fill the 'allowedAtions' section
    //
    allowedActionsPane.setTitle(resource.getString("workflowDesigner.list.allowedAction"));
    column = allowedActionsPane.addArrayColumn(resource.getString("workflowDesigner.allowedF"));
    column.setSortable(false);
    column = allowedActionsPane.addArrayColumn(resource.getString("workflowDesigner.action"));
    column.setSortable(false);

    // Print a list of action names, based on the 'actions' element
    // Starting form i = 1 since the '0' element holds the 'none' choice
    //
    for ( int i = 1; i < astrActionValues.length; i ++ )
    {
        boolean         fChecked;

        row = allowedActionsPane.addArrayLine();
        if ( allowedActions != null )
            fChecked = allowedActions.getAllowedAction( astrActionValues[i] ) != null;
        else
            fChecked = false;

        row.addArrayCellCheckbox( "allow", astrActionValues[i], fChecked );
        row.addArrayCellText( astrActionValues[i] );
    }


    out.println(window.printBefore());
    out.println(frame.printBefore());

    //help
    //
    out.println(boardHelp.printBefore());
    out.println("<table border=\"0\"><tr>");
    out.println("<td valign=\"absmiddle\"><img border=\"0\" src=\""+resource.getIcon("workflowDesigner.info")+"\"></td>");
    out.println("<td>"+resource.getString("workflowDesigner.help.state")+"</td>");
    out.println("</tr></table>");
    out.println(boardHelp.printAfter());
    out.println("<br/>");

    out.println(board.printBefore());
%>
<FORM NAME="stateForm" METHOD="POST" ACTION="UpdateState">
	<input type="hidden" name="name_original" value="<%=state.getName()%>">
<%
    out.println( statePane.print() );
    out.println( "<br>" );

    //Allowed Actions
    //
    out.println( allowedActionsPane.print() );
    out.println( "<br>" );

    //Working and interested users
    //
    out.println(usersPane.print());

    // Activities
    //
    //
    //< designer:contextualDesignationList
    //    designations="< % = state.getActivities() % >"
    //    context="< % = strActivityContext % >"
    //    parentScreen="< % =strCurrentScreen % >"
    //    columnLabelKey="workflowDesigner.activity"
    //    paneTitleKey="workflowDesigner.list.activity" / >
%>
</FORM>

<!--
// Labels
//
 -->
<designer:contextualDesignationList
    designations="<%=state.getLabels()%>"
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
    designations="<%=state.getDescriptions()%>"
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