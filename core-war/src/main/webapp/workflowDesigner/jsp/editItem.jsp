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
    Item            item = (Item)request.getAttribute("Item");
    String          strCancelAction = (String)request.getAttribute("parentScreen"),
                    strContext = (String)request.getAttribute("context"),
                    strParameterContext = URLEncoder.encode( strContext + "/parameters", UTF8 ),
                    strCurrentScreen = "ModifyItem?context=" + URLEncoder.encode( strContext, UTF8 ),
                    strDescriptionContext = strContext + "/descriptions",
                    strLabelContext = strContext + "/labels",
                    strEditParameter,
                    strParameterName;
    ArrayPane       itemPane = gef.getArrayPane( "itemPane", strCurrentScreen, request, session ),
                    parametersPane = gef.getArrayPane( "parametersPane", strCurrentScreen, request, session );
    String[]        astrUserInfosNames = (String[])request.getAttribute( "UserInfosNames" ),
                    astrUserInfosValues = astrUserInfosNames.clone(),
                    astrTypeValues = (String[])request.getAttribute( "TypeValues" ),
                    astrTypeNames = astrTypeValues.clone();
    Parameter       parameter;
    Iterator        iterParameters = item.iterateParameter();
    boolean         fExistingItem = ( (Boolean)request.getAttribute( "IsExisitingItem" ) ).booleanValue();
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
               document.itemForm.submit();
    }

    function isCorrectlyFilled()
    {
        var errorMsg = "";
        var errorNb = 0;

        if ( isWhitespace(document.itemForm.name.value) )
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
    browseBar.setComponentName(resource.getString("workflowDesigner.editor.item") );

    itemPane.setTitle(resource.getString("workflowDesigner.item"));

    // Name
    //
    row = itemPane.addArrayLine();
    cellText = row.addArrayCellText( resource.getString("GML.name") );
    cellText.setStyleSheet( "txtlibform" );
    row.addArrayCellInputText( "name", EncodeHelper.javaStringToHtmlString( item.getName() ) );

    // Computed
    //
    row = itemPane.addArrayLine();
    cellText = row.addArrayCellText( resource.getString("workflowDesigner.computed") );
    cellText.setStyleSheet( "txtlibform" );
    row.addArrayCellCheckbox( "computed", "true", item.isComputed() );

    // Map to
    //
    row = itemPane.addArrayLine();
    cellText = row.addArrayCellText( resource.getString("workflowDesigner.directoryEntry") );
    cellText.setStyleSheet( "txtlibform" );
    astrUserInfosNames[0] = resource.getString( "GML.noneF" );
    cellSelect = row.addArrayCellSelect( "mapTo", astrUserInfosNames, astrUserInfosValues );
    cellSelect.setSize( "1" );
    cellSelect.setSelectedValues( new String[] { item.getMapTo() == null ? "" : item.getMapTo() } );

    // Type
    //
    row = itemPane.addArrayLine();
    cellText = row.addArrayCellText( resource.getString("GML.type") );
    cellText.setStyleSheet( "txtlibform" );
    astrTypeNames[0] = resource.getString( "GML.none" );
    cellSelect = row.addArrayCellSelect( "type", astrTypeNames, astrTypeValues );
    cellSelect.setSize( "1" );
    cellSelect.setSelectedValues( new String[] { item.getType() == null ? "" : item.getType() } );

    // Readonly
    //
    row = itemPane.addArrayLine();
    cellText = row.addArrayCellText( resource.getString("workflowDesigner.readonly") );
    cellText.setStyleSheet( "txtlibform" );
    row.addArrayCellCheckbox( "readonly", "true", item.isReadonly() );

    // Formula
    //
    row = itemPane.addArrayLine();
    cellText = row.addArrayCellText( resource.getString("workflowDesigner.formula") );
    cellText.setStyleSheet( "txtlibform" );
    cellInput = row.addArrayCellInputText( "formula", EncodeHelper.javaStringToHtmlString( item.getFormula() ) );
    cellInput.setSize( "80" );

    //Fill the 'parameters' section
    //
    parametersPane.setTitle(resource.getString("workflowDesigner.list.parameter"));
    column = parametersPane.addArrayColumn(resource.getString("GML.name"));
    column = parametersPane.addArrayColumn(resource.getString("workflowDesigner.value"));
    column.setSortable(false);
    column = parametersPane.addArrayColumn(resource.getString("GML.operations"));
    column.setSortable(false);

    if ( fExistingItem )
        operationPane.addOperation(resource.getIcon("workflowDesigner.add"),
            resource.getString("workflowDesigner.add.parameter"),
            "AddParameter?context=" + strParameterContext );

    while ( iterParameters.hasNext() )
    {
        parameter = (Parameter)iterParameters.next();
        iconPane = gef.getIconPane();
        iconPane.setSpacing("30px");
        updateIcon = iconPane.addIcon();
        delIcon = iconPane.addIcon();
        strParameterName = "&name=" + URLEncoder.encode( parameter.getName(), UTF8 );
        strEditParameter = "ModifyParameter?context=" + strParameterContext + strParameterName;

        // Create the remove link
        //
        sb.setLength(0);
        sb.append("javascript:confirmRemove('RemoveParameter?context=" );
        sb.append( strParameterContext );
        sb.append( strParameterName );
        sb.append( "', '" );
        sb.append( resource.getString("workflowDesigner.confirmRemoveJS") );
        sb.append( " " );
        sb.append( EncodeHelper.javaStringToJsString( parameter.getName() ) );
        sb.append( " ?');" );

        row = parametersPane.addArrayLine();
        row.addArrayCellLink( parameter.getName(), strEditParameter );
        row.addArrayCellLink( parameter.getValue(), strEditParameter );

        updateIcon.setProperties(resource.getIcon("workflowDesigner.smallUpdate"),
                                                  resource.getString("GML.modify"),
                                                  strEditParameter);
        delIcon.setProperties(resource.getIcon("workflowDesigner.smallDelete"),
                                               resource.getString("GML.delete"),
                                               sb.toString() );
        row.addArrayCellIconPane(iconPane);
    }

    if ( fExistingItem )
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
    out.println("<td>"+resource.getString("workflowDesigner.help.item")+"</td>");
    out.println("</tr></table>");
    out.println(boardHelp.printAfter());
    out.println("<br/>");

    out.println(board.printBefore());
%>
<FORM NAME="itemForm" METHOD="POST" ACTION="UpdateItem">
	<input type="hidden" name="name_original" value="<%=EncodeHelper.javaStringToHtmlString(item.getName())%>"/>
    <input type="hidden" name="context" value="<%=EncodeHelper.javaStringToHtmlString(strContext)%>" />
<%
    out.println( itemPane.print() );
    out.println( "<br>" );

    //Parameters
    //
    out.println( parametersPane.print() );

    // Labels
    //
%>
</FORM>
<br>
<designer:contextualDesignationList
    designations="<%=item.getLabels()%>"
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
    designations="<%=item.getDescriptions()%>"
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