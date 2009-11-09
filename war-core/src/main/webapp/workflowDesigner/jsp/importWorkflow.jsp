<%@ include file="check.jsp" %>
<%@ taglib prefix="designer" uri="/WEB-INF/workflowEditor.tld" %>

<HTML>
<HEAD>
<% out.println(gef.getLookStyleSheet()); %>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/checkForm.js"></script>
<script type="text/javascript" src="<%=m_context%>/workflowDesigner/jsp/JavaScript/forms.js"></script>

<script language="javascript">

	function sendData() 
	{
		if ( isCorrectlyFilled() )
		{
			document.importWorkflowForm.submit();
		}
	}

	function isCorrectlyFilled() 
	{
     	var errorMsg = "";
     	var errorNb = 0;
     	var xmlFile = stripInitialWhitespace(document.importWorkflowForm.xmlFile.value);
        
     	if (xmlFile == "") 
     	{
           	errorMsg+="  - '<%=resource.getString("workflowDesigner.import.filename")%>' <%=resource.getString("GML.MustBeFilled")%>\n";
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
            	errorMsg = "<%=resource.getString("GML.ThisFormContains")%> " + errorNb + " <%=resource.getString("GML.errors")%> :\n" + errorMsg;
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
	browseBar.setDomainName(resource.getString("workflowDesigner.toolName") );
	browseBar.setComponentName(resource.getString("workflowDesigner.importWorkflow"), "#" );
	
    out.println(window.printBefore());
    out.println(frame.printBefore());

    //help
    //
    out.println(boardHelp.printBefore());
    out.println("<table border=\"0\"><tr>");
    out.println("<td valign=\"absmiddle\"><img border=\"0\" src=\""+resource.getIcon("workflowDesigner.info")+"\"></td>");
    out.println("<td>"+resource.getString("workflowDesigner.help.importWorkflow")+"</td>");
    out.println("</tr></table>");
    out.println(boardHelp.printAfter());
    out.println("<br/>");
    
    out.println(board.printBefore());
%>
	<FORM NAME="importWorkflowForm" METHOD="POST" ACTION="DoImportWorkflow" enctype="multipart/form-data">
		<table cellpadding=5 cellspacing=2 border=0 width="98%" >
			<tr>
				<td class="txtlibform"><%=resource.getString("workflowDesigner.import.filename")%> :</td>
				<td><input type="file" name="xmlFile" size="30"></td>
			</tr>
		</table>
	</FORM>

<%
	out.println(board.printAfter());
%>
<designer:buttonPane cancelAction="Main" />
<%    
    out.println(frame.printAfter());
    out.println(window.printAfter()); 
%>
</BODY>
</HTML>