<%@ include file="check.jsp" %>
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
    cellInput = row.addArrayCellInputText( "name", Encode.javaStringToHtmlString( parameter.getName() ) );
    cellInput.setSize( "50" );

		
    row = parameterPane.addArrayLine();
    cellText = row.addArrayCellText( resource.getString("workflowDesigner.value") );
    cellText.setStyleSheet( "txtlibform" );
    cellInput = row.addArrayCellInputText( "value", Encode.javaStringToHtmlString( parameter.getValue() ) );
    cellInput.setSize( "80" );


    out.println(window.printBefore());
    out.println(frame.printBefore());
    out.println(board.printBefore());
%>
<FORM NAME="parameterForm" METHOD="POST" ACTION="UpdateParameter">
	<input type="hidden" name="name_original" value="<%=Encode.javaStringToHtmlString( parameter.getName() )%>">
    <input type="hidden" name="context" value="<%=Encode.javaStringToHtmlString( strContext)%>" />
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