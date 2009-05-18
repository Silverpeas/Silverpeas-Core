<%@ include file="check.jsp" %>
<%
    Board board = gef.getBoard();

    Domain domObject = (Domain)request.getAttribute("domainObject");
    String action =(String)request.getAttribute("action");
    
	browseBar.setDomainName(resource.getString("JDP.jobDomain"));
	if (action.equals("domainSQLCreate"))
    {
		browseBar.setComponentName(resource.getString("JDP.domainSQLAdd") + "...");
	} else {
	 	browseBar.setComponentName(Encode.javaStringToHtmlString((String)request.getAttribute("domainName")), (String)request.getAttribute("domainURL"));
        browseBar.setPath(resource.getString("JDP.domainSQLUpdate") + "...");
	}

%>
<html>
<head>
<% out.println(gef.getLookStyleSheet()); %>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/checkForm.js"></script>
<script language="JavaScript">
function SubmitWithVerif(verifParams)
{
    var namefld = stripInitialWhitespace(document.domainForm.domainName.value);
    var urlfld = stripInitialWhitespace(document.domainForm.silverpeasServerURL.value);
    var errorMsg = "";

    if (verifParams)
    {
         if (isWhitespace(namefld)) 
            errorMsg = "<% out.print(resource.getString("JDP.missingFieldStart")+resource.getString("JDP.name")+resource.getString("JDP.missingFieldEnd")); %>";
         if (isWhitespace(urlfld)) 
            errorMsg = "<% out.print(resource.getString("JDP.missingFieldStart")+resource.getString("JDP.silverpeasServerURL")+resource.getString("JDP.missingFieldEnd")); %>";
    }
    if (errorMsg == "")
    {
        document.domainForm.submit();
    }
    else
    {
        window.alert(errorMsg);
    }
}
</script>
</head>
<body  marginheight=5 marginwidth=5 leftmargin=5 topmargin=5 bgcolor="#FFFFFF">

<%
out.println(window.printBefore());
out.println(frame.printBefore());
%>
<center>
<%
out.println(board.printBefore());
%>
<form name="domainForm" action="<%=action%>" method="POST">
    <table CELLPADDING=5 CELLSPACING=0 BORDER=0 WIDTH="100%">
                    <tr>			
                        <td valign="baseline" align=left  class="txtlibform">
                            <%=resource.getString("GML.name")%> :
                        </td>
                        <td align=left valign="baseline">
                            <input type="text" name="domainName" size="70" maxlength="99" VALUE="<%=Encode.javaStringToHtmlString(domObject.getName())%>">&nbsp;<img border="0" src="<%=resource.getIcon("JDP.mandatory")%>" width="5" height="5"> 
                        </td>
                    </tr>
                    <tr>			
                        <td valign="baseline" align=left  class="txtlibform">
                            <%=resource.getString("GML.description")%> :
                        </td>
                        <td align=left valign="baseline">
                            <input type="text" name="domainDescription" size="70" maxlength="399" VALUE="<%=Encode.javaStringToHtmlString(domObject.getDescription())%>"> 
                        </td>
                    </tr>
                    <tr>			
                        <td valign="baseline" align=left  class="txtlibform">
                            <%=resource.getString("JDP.silverpeasServerURL")%> :
                        </td>
                        <td align=left valign="baseline">
                            <input type="text" name="silverpeasServerURL" size="70" maxlength="399" VALUE="<%=Encode.javaStringToHtmlString(domObject.getSilverpeasServerURL())%>">&nbsp;<img border="0" src="<%=resource.getIcon("JDP.mandatory")%>" width="5" height="5">
                        </td>
                    </tr>
                    <tr>			
                        <td valign="top" align=left  class="txtlibform">
                        </td>
                        <td align=left valign="top">
                            <%=resource.getString("JDP.silverpeasServerURLEx")%>
                        </td>
                    </tr>
                    <tr> 
                        <td colspan="2">(<img border="0" src="<%=resource.getIcon("JDP.mandatory")%>" width="5" height="5"> 
                  : <%=resource.getString("GML.requiredField")%>)
              			</td>
                    </tr>             
    </table>

<%
out.println(board.printAfter());
%>
</form>
<br/>
		<%
		  ButtonPane bouton = gef.getButtonPane();
		  bouton.addButton((Button) gef.getFormButton(resource.getString("GML.validate"), "javascript:SubmitWithVerif(true)", false));
	      bouton.addButton((Button) gef.getFormButton(resource.getString("GML.cancel"), "domainContent", false));
		  out.println(bouton.print());
		%>
</center>
<%
out.println(frame.printAfter());
out.println(window.printAfter());
	%>

</body>
</html>