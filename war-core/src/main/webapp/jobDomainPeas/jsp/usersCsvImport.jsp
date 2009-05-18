<%@ include file="check.jsp" %>
<%
    Board board = gef.getBoard();

    browseBar.setDomainName(resource.getString("JDP.jobDomain"));
    browseBar.setComponentName(Encode.javaStringToHtmlString((String)request.getAttribute("domainName")), (String)request.getAttribute("domainURL"));
    browseBar.setPath((String)request.getAttribute("groupsPath"));
%>
<html>
<head>
<% out.println(gef.getLookStyleSheet()); %>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/checkForm.js"></script>
<script language="JavaScript">

function SubmitWithVerif(verifParams)
{
    var csvFilefld = stripInitialWhitespace(document.csvFileForm.file_upload.value);
    var errorMsg = "";

    if (verifParams)
    {
         if (isWhitespace(csvFilefld)) {
            errorMsg = "<% out.print(resource.getString("JDP.missingFieldStart")+resource.getString("JDP.csvFile")+resource.getString("JDP.missingFieldEnd")); %>";
         } else {
			var ext = csvFilefld.substring(csvFilefld.length - 4);
	        
    	    if (ext.toLowerCase() != ".csv") {
    			errorMsg = "<% out.print(resource.getString("JDP.errorCsvFile")); %>";		
    		}
		}
    }
    if (errorMsg == "")
    {
        document.csvFileForm.submit();
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
<form name="csvFileForm" action="usersCsvImport" method="POST" enctype="multipart/form-data">
    <table CELLPADDING=5 CELLSPACING=0 BORDER=0 WIDTH="100%">
        <tr>			
            <td valign="baseline" align=left  class="txtlibform">
                <%=resource.getString("JDP.csvFile") %> :
            </td>
            <td align=left valign="baseline">
                <input type="file" name="file_upload" size="50" maxlength="50" VALUE="">&nbsp;<img border="0" src="<%=resource.getIcon("JDP.mandatory")%>" width="5" height="5"> 
            </td>
        </tr>
        <tr> 
            <td colspan="2">(<img border="0" src="<%=resource.getIcon("JDP.mandatory")%>" width="5" height="5"> 
      : <%=resource.getString("GML.requiredField")%>)</td>
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