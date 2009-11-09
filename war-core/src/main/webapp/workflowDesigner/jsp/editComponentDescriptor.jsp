<%@ include file="check.jsp" %>
<%@ taglib prefix="designer" uri="/WEB-INF/workflowEditor.tld" %>

<%
    String          strCancelAction = "ModifyWorkflow",
                    strCurrentScreen = "EditComponentDescription";
    String[]        astrRoleNames = (String[])request.getAttribute( "RoleNames" ),
                    astrRoleValues = (String[])astrRoleNames.clone();
    ArrayPane       parameterPane = gef.getArrayPane( "parameterName", strCurrentScreen, request, session );
%>
<HTML>
<HEAD>
<% out.println(gef.getLookStyleSheet()); %>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/checkForm.js"></script>
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

        if ( isWhitespace(document.parameterForm.label.value) ) 
        {
            errorMsg+="  - '<%=resource.getString("GML.label")%>' <%=resource.getString("GML.MustBeFilled")%>\n";
            errorNb++;
        }
         
        if ( isWhitespace(document.parameterForm.description.value) ) 
        {
            errorMsg+="  - '<%=resource.getString("GML.description")%>' <%=resource.getString("GML.MustBeFilled")%>\n";
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
    browseBar.setComponentName(resource.getString("workflowDesigner.editor.componentDescriptor") );

    parameterPane.setTitle(resource.getString("workflowDesigner.component.parameters"));
    
    row = parameterPane.addArrayLine();
    cellText = row.addArrayCellText( resource.getString("GML.label") );
    cellText.setStyleSheet( "txtlibform" );
    cellInput = row.addArrayCellInputText( "label", "" );
    cellInput.setSize( "50" );

		
    row = parameterPane.addArrayLine();
    cellText = row.addArrayCellText( resource.getString("GML.description") );
    cellText.setStyleSheet( "txtlibform" );
    cellInput = row.addArrayCellInputText( "description", "" );
    cellInput.setSize( "80" );
    
    row = parameterPane.addArrayLine();
    cellText = row.addArrayCellText( resource.getString("workflowDesigner.component.labelSelection") );
    cellText.setStyleSheet( "txtlibform" );
    row.addArrayEmptyCell();

    // Role
    //
    row = parameterPane.addArrayLine();
    cellText = row.addArrayCellText( resource.getString("workflowDesigner.role") );
    cellText.setStyleSheet( "txtlibform" );
    
    astrRoleNames[0] = resource.getString("workflowDesigner.default");
    cellSelect = row.addArrayCellSelect( "role", astrRoleNames, astrRoleValues ); 
    cellSelect.setSize( "1" );
    
    // Language
    //
    row = parameterPane.addArrayLine();
    cellText = row.addArrayCellText( resource.getString("GML.language") );
    cellText.setStyleSheet( "txtlibform" );
    cellSelect = row.addArrayCellSelect( "language",
                                         (String[])request.getAttribute( "LanguageNames" ),
                                         (String[])request.getAttribute( "LanguageCodes") );
    cellSelect.setSize( "1" );

    out.println(window.printBefore());
    out.println(frame.printBefore());

    // help
    //
    out.println(boardHelp.printBefore());
    out.println("<table border=\"0\"><tr>");
    out.println("<td valign=\"absmiddle\"><img border=\"0\" src=\""+resource.getIcon("workflowDesigner.info")+"\"></td>");
    out.println("<td>"+resource.getString("workflowDesigner.help.componentDescriptor")+"</td>");
    out.println("</tr></table>");
    out.println(boardHelp.printAfter());
    out.println("<br/>");
	
    out.println(board.printBefore());
%>
<FORM NAME="parameterForm" METHOD="POST" ACTION="GenerateComponentDescription">
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