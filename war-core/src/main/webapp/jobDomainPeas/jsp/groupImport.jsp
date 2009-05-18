<%@ include file="check.jsp" %>
<%
    Board board = gef.getBoard();

    String groupsPath = (String)request.getAttribute("groupsPath");

    browseBar.setDomainName(resource.getString("JDP.jobDomain"));
    browseBar.setComponentName(Encode.javaStringToHtmlString((String)request.getAttribute("domainName")), (String)request.getAttribute("domainURL"));
    browseBar.setPath(groupsPath);
%>
<html>
<head>
<% out.println(gef.getLookStyleSheet()); %>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/checkForm.js"></script>
<script language="JavaScript">
function SubmitWithVerif(verifParams)
{
    var namefld = stripInitialWhitespace(document.groupForm.groupName.value);
    var errorMsg = "";

    if (verifParams)
    {
         if (isWhitespace(namefld)) 
            errorMsg = "<% out.print(resource.getString("JDP.missingFieldStart")+resource.getString("GML.name")+resource.getString("JDP.missingFieldEnd")); %>";
    }
    if (errorMsg == "")
    {
        document.groupForm.submit();
    }
    else
    {
        window.alert(errorMsg);
    }
}
</script>
</head>
<body marginheight=5 marginwidth=5 leftmargin=5 topmargin=5 bgcolor="#FFFFFF">

<%
out.println(window.printBefore());
out.println(frame.printBefore());
%>
<center>
<%
out.println(board.printBefore());
%>
<form name="groupForm" action="groupImport" method="POST">
    <table CELLPADDING=5 CELLSPACING=0 BORDER=0 WIDTH="100%">
                    <tr>			
                        <td valign="baseline" align=left  class="txtlibform">
                             <%=resource.getString("GML.name")%> :
                        </td>
                        <td align=left valign="baseline">
                            <input type="text" name="groupName" size="50" maxlength="99" VALUE="">&nbsp;<img border="0" src="<%=resource.getIcon("JDP.mandatory")%>" width="5" height="5"> 
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
		<%
		  ButtonPane bouton = gef.getButtonPane();
		  bouton.addButton((Button) gef.getFormButton(resource.getString("GML.validate"), "javascript:SubmitWithVerif(true)", false));
          bouton.addButton((Button) gef.getFormButton(resource.getString("GML.cancel"), "groupContent", false));
		  out.println(bouton.print());
		%>
</center>
<%
out.println(frame.printAfter());
out.println(window.printAfter());
	%>

</body>
</html>