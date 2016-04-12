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
    Form            form = (Form)request.getAttribute("Form");
    String          strCancelAction = "ViewForms",
                    strFormType = (String)request.getAttribute("type"),
                    strContext = (String)request.getAttribute("context"),
                    strInputContext = strContext + "/inputs",
                    strCurrentScreen = "ModifyForm?context=" + URLEncoder.encode( strContext, UTF8 ),
                    strTitleContext = strContext + "/titles",
                    strEditInput,
                    strInputContextEncoded,
                    strItemName;
    ArrayPane       formPane = gef.getArrayPane( "formPane", strCurrentScreen, request, session ),
                    inputsPane = gef.getArrayPane( "inputsPane", strCurrentScreen, request, session );
    String[]        astrRoleNames = (String[])request.getAttribute( "RoleNames" ),
                    astrRoleValues = (String[])astrRoleNames.clone();
    Input           input;
    Iterator        iterInputs = form.iterateInput();
    int             idx = 0;
    boolean         fExistingForm = ( (Boolean)request.getAttribute( "IsExisitingForm" ) ).booleanValue();
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
            document.formForm.submit();
    }

    function switchType()
    {
        // Which type of form is it?
        //
        if ( document.formForm.type[0].checked )
        {
            // Action form
            //
            document.formForm.name.readOnly = false;
            document.formForm.role.options.selectedIndex = 0;
            document.formForm.role.disabled = true;
        }
        else if ( document.formForm.type[1].checked )
        {
            // Presentation form
            //
		document.formForm.name.value = '<%=WorkflowDesignerSessionController.FORM_TYPE_PRESENTATION%>';
            document.formForm.name.readOnly = true;
		document.formForm.role.disabled = false;
        }
        else
        {
            // Must be the print form
            //
		document.formForm.name.value = '<%=WorkflowDesignerSessionController.FORM_TYPE_PRINT%>';
            document.formForm.name.readOnly = true;
            document.formForm.role.options.selectedIndex = 0;
            document.formForm.role.disabled = true;
        }
    }

    function isCorrectlyFilled()
    {
        var errorMsg = "";
        var errorNb = 0;
        var result = true;

        // Which type of form is it?
        //
        if ( document.formForm.type[0].checked
             && isWhitespace( document.formForm.name.value ) )
        {
            // Action form
            //
            errorMsg+="  - '<%=resource.getString("GML.name")%>' <%=resource.getString("GML.MustBeFilled")%>\n";
            errorNb++;
        }
        else if ( document.formForm.type[2].checked
                  && isWhitespace( document.formForm.HTMLFileName.value ) )
        {
            // Print form
            //
            errorMsg+="  - '<%=resource.getString("workflowDesigner.HTMLFileName")%>' <%=resource.getString("GML.MustBeFilled")%>\n";
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
<BODY leftmargin="5" topmargin="5" marginwidth="5" marginheight="5" onLoad="switchType()">
<%
    browseBar.setDomainName(resource.getString("workflowDesigner.toolName"));
    browseBar.setComponentName(resource.getString("workflowDesigner.forms"));
    browseBar.setExtraInformation(resource.getString("workflowDesigner.editor.form"));

    formPane.setTitle(resource.getString("workflowDesigner.form"));

    // Type
    //
    row = formPane.addArrayLine();
    cellRadio = row.addArrayCellRadio( "type",
                                       WorkflowDesignerSessionController.FORM_TYPE_ACTION,
                                       WorkflowDesignerSessionController.FORM_TYPE_ACTION.equals( strFormType ) );
    cellRadio.setAction( "onChange=\"switchType()\"");
    cellText = row.addArrayCellText( resource.getString("workflowDesigner.form.action") );
    cellText.setStyleSheet( "txtlibform" );

    row = formPane.addArrayLine();
    cellRadio = row.addArrayCellRadio( "type",
                                       WorkflowDesignerSessionController.FORM_TYPE_PRESENTATION,
                                       WorkflowDesignerSessionController.FORM_TYPE_PRESENTATION.equals( strFormType ) );
    cellRadio.setAction( "onChange=\"switchType()\"");
    cellText = row.addArrayCellText( resource.getString("workflowDesigner.form.presentation") );
    cellText.setStyleSheet( "txtlibform" );

    row = formPane.addArrayLine();
    cellRadio = row.addArrayCellRadio( "type",
                                       WorkflowDesignerSessionController.FORM_TYPE_PRINT,
                                       WorkflowDesignerSessionController.FORM_TYPE_PRINT.equals( strFormType ) );
    cellRadio.setAction( "onChange=\"switchType()\"");
    cellText = row.addArrayCellText( resource.getString("workflowDesigner.form.print") );
    cellText.setStyleSheet( "txtlibform" );

    // Name
    //
    row = formPane.addArrayLine();
    cellText = row.addArrayCellText( resource.getString("GML.name") );
    cellText.setStyleSheet( "txtlibform" );
    cellInput = row.addArrayCellInputText( "name", EncodeHelper.javaStringToHtmlString( form.getName() ) );
    cellInput.setSize( "80" );

    // Role
    //
    row = formPane.addArrayLine();
    cellText = row.addArrayCellText( resource.getString("workflowDesigner.role") );
    cellText.setStyleSheet( "txtlibform" );

    astrRoleNames[0] = resource.getString( "GML.none" );
    cellSelect = row.addArrayCellSelect( "role", astrRoleNames, astrRoleValues );
    cellSelect.setSelectedValues( new String[] { form.getRole() } );
    cellSelect.setSize( "1" );

    // HTMLFileName
    //
    row = formPane.addArrayLine();
    cellText = row.addArrayCellText( resource.getString("workflowDesigner.HTMLFileName") );
    cellText.setStyleSheet( "txtlibform" );
    cellInput = row.addArrayCellInputText( "HTMLFileName", EncodeHelper.javaStringToHtmlString( form.getHTMLFileName() ) );
    cellInput.setSize( "80" );

    //Fill the 'inputs' section
    //
    inputsPane.setTitle(resource.getString("workflowDesigner.list.input"));
    column = inputsPane.addArrayColumn(resource.getString("workflowDesigner.folderItem"));
    column = inputsPane.addArrayColumn(resource.getString("workflowDesigner.value"));
    column = inputsPane.addArrayColumn(resource.getString("workflowDesigner.readonly"));
    column = inputsPane.addArrayColumn(resource.getString("workflowDesigner.displayerName"));
    column = inputsPane.addArrayColumn(resource.getString("GML.requiredField"));
    column = inputsPane.addArrayColumn(resource.getString("GML.operations"));
    column.setSortable(false);

    if ( fExistingForm )
        operationPane.addOperation(resource.getIcon("workflowDesigner.add"),
            resource.getString("workflowDesigner.add.input"),
            "AddInput?context=" + URLEncoder.encode(strInputContext, UTF8) );

    while ( iterInputs.hasNext() )
    {
        input = (Input)iterInputs.next();
        iconPane = gef.getIconPane();
        iconPane.setSpacing("30px");
        updateIcon = iconPane.addIcon();
        delIcon = iconPane.addIcon();

        strItemName = input.getItem() == null ? "" : input.getItem().getName();
        strInputContextEncoded = URLEncoder.encode( strInputContext + "[" + Integer.toString( idx ) + "]", UTF8 );
        strEditInput = "ModifyInput?context=" + strInputContextEncoded;

        // Create the remove link
        //
        sb.setLength(0);
        sb.append("javascript:confirmRemove('RemoveInput?context=" );
        sb.append( strInputContextEncoded );
        sb.append( "', '" );
        sb.append( resource.getString("workflowDesigner.confirmRemoveJS") );
        sb.append( " " );
        sb.append( EncodeHelper.javaStringToJsString( resource.getString("workflowDesigner.input") ) );
        sb.append( " ?');" );

        row = inputsPane.addArrayLine();
        row.addArrayCellLink( strItemName, strEditInput );
        row.addArrayCellLink( input.getValue() == null ? "" : input.getValue(), strEditInput );
        row.addArrayCellLink( resource.getString( input.isReadonly() ? "GML.yes" : "GML.no" ), strEditInput );
        row.addArrayCellLink( input.getDisplayerName() == null ? "" : input.getDisplayerName(), strEditInput );
        row.addArrayCellLink( resource.getString( input.isMandatory() ? "GML.yes" : "GML.no" ), strEditInput );

        updateIcon.setProperties(resource.getIcon("workflowDesigner.smallUpdate"),
                                                  resource.getString("GML.modify"),
                                                  strEditInput);
        delIcon.setProperties(resource.getIcon("workflowDesigner.smallDelete"),
                                               resource.getString("GML.delete"),
                                               sb.toString() );
        row.addArrayCellIconPane(iconPane);
        idx++;
    }

    if ( fExistingForm )
        addContextualDesignation( operationPane, resource, strTitleContext, "workflowDesigner.add.title", strCurrentScreen );

    out.println(window.printBefore());
    out.println(frame.printBefore());

    //help
    //
    out.println(boardHelp.printBefore());
    out.println("<table border=\"0\"><tr>");
    out.println("<td valign=\"absmiddle\"><img border=\"0\" src=\""+resource.getIcon("workflowDesigner.info")+"\"></td>");
    out.println("<td>"+resource.getString("workflowDesigner.help.form")+"</td>");
    out.println("</tr></table>");
    out.println(boardHelp.printAfter());
    out.println("<br/>");

    out.println(board.printBefore());
%>
<FORM NAME="formForm" METHOD="POST" ACTION="UpdateForm">
	<input type="hidden" name="name_original" value="<%=EncodeHelper.javaStringToHtmlString(form.getName())%>"/>
    <input type="hidden" name="role_original" value="<%=EncodeHelper.javaStringToHtmlString(form.getRole())%>"/>
    <input type="hidden" name="context" value="<%=EncodeHelper.javaStringToHtmlString(strContext)%>" />
<%
    out.println( formPane.print() );
    out.println( "<br>");

    //Inputs
    //
    out.println( inputsPane.print() );

    // Titles
    //
%>
</FORM>
<br>
<designer:contextualDesignationList
    designations="<%=form.getTitles()%>"
    context="<%=strTitleContext%>"
    parentScreen="<%=strCurrentScreen%>"
    columnLabelKey="GML.title"
    paneTitleKey="workflowDesigner.list.title"/>
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