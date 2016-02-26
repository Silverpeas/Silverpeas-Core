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
    Parameter       parameter = (Parameter)request.getAttribute("Parameter");
    String          strCancelAction = (String)request.getAttribute("parentScreen"),
                    strContext = (String)request.getAttribute("context"),
                    strCurrentScreen = "ModifyParameter?context=" + URLEncoder.encode(strContext, UTF8)
                                       + "parameter=" + URLEncoder.encode(parameter.getName(), UTF8);
    ArrayPane       parameterPane = gef.getArrayPane( "parameterName", strCurrentScreen, request, session );
%>
<HTML>
<HEAD>
<view:looknfeel withCheckFormScript="true"/>
<script language="javaScript">
    function sendData()
    {
        if ( isCorrectlyFilled() )
               document.parameterForm.submit();
    }

    function isCorrectlyFilled()
    {
        var errorMsg = "";
        var errorNb = 0;

        if ( isWhitespace(document.parameterForm.name.value) )
        {
            errorMsg+="  - '<%=resource.getString("GML.name")%>' <%=resource.getString("GML.MustBeFilled")%>\n";
            errorNb++;
        }

        if ( isWhitespace(document.parameterForm.value.value) )
        {
            errorMsg+="  - '<%=resource.getString("workflowDesigner.value")%>' <%=resource.getString("GML.MustBeFilled")%>\n";
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
    browseBar.setComponentName(resource.getString("workflowDesigner.editor.item"), strCancelAction);
    browseBar.setExtraInformation(resource.getString("workflowDesigner.editor.parameter") );

    parameterPane.setTitle(resource.getString("workflowDesigner.parameter"));

    row = parameterPane.addArrayLine();
    cellText = row.addArrayCellText( resource.getString("GML.name") );
    cellText.setStyleSheet( "txtlibform" );
    cellInput = row.addArrayCellInputText( "name", EncodeHelper.javaStringToHtmlString( parameter.getName() ) );
    cellInput.setSize( "50" );


    row = parameterPane.addArrayLine();
    cellText = row.addArrayCellText( resource.getString("workflowDesigner.value") );
    cellText.setStyleSheet( "txtlibform" );
    cellInput = row.addArrayCellInputText( "value", EncodeHelper.javaStringToHtmlString( parameter.getValue() ) );
    cellInput.setSize( "80" );


    out.println(window.printBefore());
    out.println(frame.printBefore());
    out.println(board.printBefore());
%>
<FORM NAME="parameterForm" METHOD="POST" ACTION="UpdateParameter">
	<input type="hidden" name="name_original" value="<%=EncodeHelper.javaStringToHtmlString( parameter.getName() )%>">
    <input type="hidden" name="context" value="<%=EncodeHelper.javaStringToHtmlString( strContext)%>" />
<%
    out.println( parameterPane.print() );
%>
</FORM>
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