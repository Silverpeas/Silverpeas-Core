<%@ include file="check.jsp" %>
<%@ taglib prefix="designer" uri="/WEB-INF/workflowEditor.tld" %>

<%
    Consequence     consequence = (Consequence)request.getAttribute("Consequence");
    String          strParentScreen = (String)request.getAttribute( "parentScreen" ),
                    strContext = (String)request.getAttribute("context"),
                    strNotifiedContext = URLEncoder.encode( strContext + "/notifiedUsers", UTF8 ),
                    strCurrentScreen = "ModifyConsequence?context=" + URLEncoder.encode( strContext, UTF8 );
    ArrayPane       consequencePane = gef.getArrayPane( "consequencePane", strCurrentScreen, request, session ),
                    usersPane = gef.getArrayPane( "usersPane", strCurrentScreen, request, session ),
                    setUnsetPane = gef.getArrayPane( "setUnsetPane", strCurrentScreen, request, session );
    String[]        astrStateValues = (String[])request.getAttribute( "StateNames" ),
                    astrFolderItemNames = (String[])request.getAttribute( "FolderItemNames" ),
                    astrFolderItemValues = (String[])astrFolderItemNames.clone(),
                    astrOperatorNames = (String[])request.getAttribute( "Operators" ),
                    astrOperatorValues = (String[])astrOperatorNames.clone();
    boolean         fExistingConsequence = ( (Boolean)request.getAttribute( "IsExisitingConsequence" ) ).booleanValue();
    StringBuffer    sb = new StringBuffer();
%>
<HTML>
<HEAD>
<% out.println(gef.getLookStyleSheet()); %>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/checkForm.js"></script>
<script type="text/javascript" src="<%=m_context%>/workflowDesigner/jsp/JavaScript/forms.js"></script>
<script language="javaScript">
    function sendData()
    {
        if ( isCorrectlyFilled() )
            document.consequenceForm.submit();
    }

    function activateCondition()
    {
        if ( document.consequenceForm.item.options.selectedIndex == 0 )
        {
            // Clear and block operator & value
            //
        	document.consequenceForm.operator.options.selectedIndex = 0;
        	document.consequenceForm.operator.disabled = true;
        	document.consequenceForm.value.value = '';
            document.consequenceForm.value.readOnly = true;
            document.consequenceForm.value.disabled= true;
        }
        else
        {
            // Activate operator & value
            //
            document.consequenceForm.operator.disabled = false;
        	document.consequenceForm.value.readOnly = false;
            document.consequenceForm.value.disabled = false;
        }
    }

    function isCorrectlyFilled() 
    {
        var errorMsg = "";
        var errorNb = 0;
        var result = true;

        if ( document.consequenceForm.item.options.selectedIndex != 0 )
        {
            if ( document.consequenceForm.operator.options.selectedIndex == 0 )
            {
                errorMsg+="  - '<%=resource.getString("workflowDesigner.operator")%>' <%=resource.getString("GML.MustBeFilled")%>\n";
                errorNb++;
            } 

            if ( isEmpty( document.consequenceForm.value.value ) )
            {
                errorMsg+="  - '<%=resource.getString("workflowDesigner.value")%>' <%=resource.getString("GML.MustBeFilled")%>\n";
                errorNb++;
            }
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
<BODY leftmargin="5" topmargin="5" marginwidth="5" marginheight="5" onLoad="activateCondition()">
<%
    browseBar.setDomainName(resource.getString("workflowDesigner.toolName"));
    browseBar.setComponentName(resource.getString("workflowDesigner.consequences"), strParentScreen);
    browseBar.setExtraInformation(resource.getString("workflowDesigner.editor.consequence") );

    consequencePane.setTitle(resource.getString("workflowDesigner.consequence"));

    // Item
    //
    row = consequencePane.addArrayLine();
    cellText = row.addArrayCellText( resource.getString("workflowDesigner.folderItem") );
    cellText.setStyleSheet( "txtlibform" );
    astrFolderItemNames[0] = resource.getString( "GML.none" );
    cellSelect = row.addArrayCellSelect( "item", astrFolderItemNames, astrFolderItemValues );
    cellSelect.setSize( "1" );
    cellSelect.setSelectedValues( new String[] { consequence.getItem() } );
    cellSelect.setAction( "onChange=\"activateCondition()\"");
    
    // Operator
    //
    row = consequencePane.addArrayLine();
    cellText = row.addArrayCellText( resource.getString("workflowDesigner.operator") );
    cellText.setStyleSheet( "txtlibform" );
    astrOperatorNames[0] = resource.getString( "GML.none" );
    cellSelect = row.addArrayCellSelect( "operator", astrOperatorNames, astrOperatorValues );
    cellSelect.setSize( "1" );
    cellSelect.setSelectedValues( new String[] { consequence.getOperator() } );

    // Value
    //
    row = consequencePane.addArrayLine();
    cellText = row.addArrayCellText( resource.getString("workflowDesigner.value") );
    cellText.setStyleSheet( "txtlibform" );
    row.addArrayCellInputText( "value", Encode.javaStringToHtmlString( consequence.getValue() ) );
    
    // Kill
    //
    row = consequencePane.addArrayLine();
    cellText = row.addArrayCellText( resource.getString("workflowDesigner.kill") );
    cellText.setStyleSheet( "txtlibform" );
    row.addArrayCellCheckbox( "kill", "true", consequence.getKill() );
    
    //Fill the 'set/unset' state section
    //
    setUnsetPane.setTitle(resource.getString("workflowDesigner.list.setUnset"));
    column = setUnsetPane.addArrayColumn(resource.getString("GML.none"));
    column.setSortable(false);
    column = setUnsetPane.addArrayColumn(resource.getString("workflowDesigner.set"));
    column.setSortable(false);
    column = setUnsetPane.addArrayColumn(resource.getString("workflowDesigner.unset"));
    column.setSortable(false);
    column = setUnsetPane.addArrayColumn(resource.getString("workflowDesigner.state"));
    column.setSortable(false);
    
    // Print a list of state names, based on the 'states' element
    //
    for ( int i = 0; i < astrStateValues.length; i ++ )
    {
        boolean  fSet = consequence.getTargetState( astrStateValues[i] ) != null,
                 fUnSet = consequence.getUnsetState( astrStateValues[i] ) != null;

        row = setUnsetPane.addArrayLine();
        row.addArrayCellRadio( Encode.javaStringToHtmlString( "setUnset_" + astrStateValues[i] ),
                               "", 
                               ! (fSet || fUnSet) );
        row.addArrayCellRadio( Encode.javaStringToHtmlString( "setUnset_" + astrStateValues[i] ),
                               "set",
                               fSet );
        row.addArrayCellRadio( Encode.javaStringToHtmlString( "setUnset_" + astrStateValues[i] ),
                               "unset",
                               fUnSet );
        row.addArrayCellText( astrStateValues[i] );
    }
    
    // Notified users
    //
    column = usersPane.addArrayColumn(resource.getString("workflowDesigner.list.notifiedUsers"));
    column.setSortable(false);
    column = usersPane.addArrayColumn(resource.getString("GML.operations"));
    column.setSortable(false);

    // Only print the link if the consequence has been saved
    //
    if ( fExistingConsequence )
    {
        // Check if notified users have been already defined
        //
        row = usersPane.addArrayLine();
        if ( consequence.getNotifiedUsersEx() == null )
        {
            Icon addIcon;
    
            row.addArrayCellLink( resource.getString("workflowDesigner.notifiedUsers"),
                                  "AddQualifiedUsers?context=" + strNotifiedContext );
    
            iconPane = gef.getIconPane();
            addIcon = iconPane.addIcon();
            addIcon.setProperties(resource.getIcon("workflowDesigner.add"),
                                  resource.getString("GML.add"),
                                  "AddQualifiedUsers?context=" + strNotifiedContext );
            
            iconPane.setSpacing("30px");
            row.addArrayCellIconPane(iconPane);
        }
        else  // Notified users have been defined...
        {
            iconPane = gef.getIconPane();
            iconPane.setSpacing("30px");
            updateIcon = iconPane.addIcon();
            delIcon = iconPane.addIcon();
    
            // Create the remove link
            //
            sb.setLength(0);
            sb.append( "javascript:confirmRemove('RemoveQualifiedUsers?context=" ); 
            sb.append( strNotifiedContext );
            sb.append( "', '" );
            sb.append( resource.getString("workflowDesigner.confirmRemoveJS") );
            sb.append( " " );
            sb.append( Encode.javaStringToJsString( resource.getString("workflowDesigner.notifiedUsers") ) );
            sb.append( " ?');" );

            row.addArrayCellLink( resource.getString("workflowDesigner.notifiedUsers"),
                                                     "ModifyQualifiedUsers?context=" + strNotifiedContext ); 
            updateIcon.setProperties(resource.getIcon("workflowDesigner.smallUpdate"),
                                     resource.getString("GML.modify"),
                                     "ModifyQualifiedUsers?context=" + strNotifiedContext );
            delIcon.setProperties(resource.getIcon("workflowDesigner.smallDelete"),
                                  resource.getString("GML.delete"),
                                  sb.toString() );
            row.addArrayCellIconPane(iconPane);
        }
    }
    
    out.println(window.printBefore());
    out.println(frame.printBefore());

    //help
    //
    out.println(boardHelp.printBefore());
    out.println("<table border=\"0\"><tr>");
    out.println("<td valign=\"absmiddle\"><img border=\"0\" src=\""+resource.getIcon("workflowDesigner.info")+"\"></td>");
    out.println("<td>"+resource.getString("workflowDesigner.help.consequence")+"</td>");
    out.println("</tr></table>");
    out.println(boardHelp.printAfter());
    out.println("<br/>");

    out.println(board.printBefore());
%>
<FORM NAME="consequenceForm" METHOD="POST" ACTION="UpdateConsequence">
	<input type="hidden" name="context" value="<%=Encode.javaStringToHtmlString( strContext )%>">
<%
    out.println( consequencePane.print() );
 
    // Stet /unset states
    //
    out.println( setUnsetPane.print() );
    
%>
</FORM>
<%
    // Notified users
    //
    out.println(usersPane.print());
	out.println(board.printAfter());
%>
<designer:buttonPane cancelAction="<%=strParentScreen%>" />
<%    
    out.println(frame.printAfter());
    out.println(window.printAfter()); 
%>
</BODY>
</HTML>