<%@ include file="check.jsp" %>
<%@ taglib prefix="designer" uri="/WEB-INF/workflowEditor.tld" %>
<%
     String[]                astrRoleNames = (String[])request.getAttribute( "RoleNames" ),
                             astrRoleValues = (String[])astrRoleNames.clone();
    ContextualDesignation    designation;
    ArrayPane                designationPane;
    String                   strParentScreen = (String)request.getAttribute( "parentScreen" ),  // context 
                             strEditorName = (String)request.getAttribute( "EditorName" ),
                             strContext = (String)request.getAttribute( "context" ); // context
%>
<HTML>
<HEAD>
<% out.println(gef.getLookStyleSheet()); %>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/checkForm.js"></script>
<script language="javaScript">
    function sendData() 
    {
        if (isCorrectlyFilled()) 
        {
            document.designationForm.submit();
        }
    }
    
    function isCorrectlyFilled() 
    {
        var errorMsg = "";
        var errorNb = 0;

        if ( isWhitespace(document.designationForm.content.value) ) 
        {
            errorMsg+="  - '<%=resource.getString("workflowDesigner.content")%>' <%=resource.getString("GML.MustBeFilled")%>\n";
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
    browseBar.setComponentName( resource.getString(strEditorName) );
    
    designationPane = gef.getArrayPane( "contextualDesignation", "", request, session );
    designationPane.setTitle(resource.getString(strEditorName));

    designation = (ContextualDesignation)request.getAttribute("ContextualDesignation");
    
    // Language
    //
    row = designationPane.addArrayLine();
    cellText = row.addArrayCellText( resource.getString("GML.language") );
    cellText.setStyleSheet( "txtlibform" );
    cellSelect = row.addArrayCellSelect( "lang",
                                           (String[])request.getAttribute( "LanguageNames" ),
                                           (String[])request.getAttribute( "LanguageCodes") );
    cellSelect.setSize( "1" );
    cellSelect.setSelectedValues( new String[]{ designation.getLanguage() } );
    
    // Roles
    //
    row = designationPane.addArrayLine();
    cellText = row.addArrayCellText( resource.getString("workflowDesigner.role") );
    cellText.setStyleSheet( "txtlibform" );
    
    astrRoleNames[0] = resource.getString("workflowDesigner.default");
    cellSelect = row.addArrayCellSelect( "role", astrRoleNames, astrRoleValues ); 
    cellSelect.setSelectedValues( new String[] { designation.getRole() } );
    cellSelect.setSize( "1" );

    // Content
    //
    row = designationPane.addArrayLine();
    cellText = row.addArrayCellText( resource.getString("workflowDesigner.content") );
    cellText.setStyleSheet( "txtlibform" );
    cellInput = row.addArrayCellInputText( "content", designation.getContent() );
    cellInput.setSize( "50" );
        
    out.println(window.printBefore());
    out.println(frame.printBefore());

    //help
    //
    out.println(boardHelp.printBefore());
    out.println("<table border=\"0\"><tr>");
    out.println("<td valign=\"absmiddle\"><img border=\"0\" src=\""+resource.getIcon("workflowDesigner.info")+"\"></td>");
    out.println("<td>"+resource.getString("workflowDesigner.help.contextualDesignation")+"</td>");
    out.println("</tr></table>");
    out.println(boardHelp.printAfter());
    out.println("<br/>");
    
    out.println(board.printBefore());
%>
<FORM NAME="designationForm" METHOD="POST" ACTION="UpdateContextualDesignation">
    <input type="hidden" name="lang_original" value="<%=designation.getLanguage()%>"/>
    <input type="hidden" name="role_original" value="<%=designation.getRole()%>"/>
    <input type="hidden" name="context" value="<%=strContext%>" />
    <input type="hidden" name="parentScreen"  value="<%=strParentScreen%>" />
<%
    out.println( designationPane.print() );
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