<%@ include file="checkVersion.jsp" %>
<%

    ResourceLocator messages = new ResourceLocator("com.stratelia.silverpeas.versioningPeas.multilang.versioning", m_MainSessionCtrl.getFavoriteLanguage());
    String docId 			= (String) request.getAttribute("DocId");
    String url				= (String) request.getAttribute("Url");
%>
 <HTML>
   <HEAD>
     <TITLE><%=messages.getString("popupTitle")%></TITLE>
        <meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
          <% out.println(gef.getLookStyleSheet()); %>

 <script language="Javascript">
   function sendCancel() {
     window.opener.focus();
     window.close();
    }

    function sendOK() {
        document.forms[0].submit();
      }
  </script>
  </HEAD>

  <BODY>
<%
    out.println(frame.printBefore());
%>
<center>
<form name="essai" method="post" action="DeleteDocument">
	<input type="hidden" name="DocId" value="<%=docId%>">
	<input type="hidden" name="Url" value="<%=url%>">
</form>
<%
	Board board = gef.getBoard();
	out.println(board.printBefore());
%>
<table width="100%">
       <tr>
         <td align="center"><B><%=messages.getString("confirmDelete") %></B></td>
       </tr>
</table>
 <%
	out.println(board.printAfter()+"<br>");
	
	ButtonPane buttonPane = gef.getButtonPane();
	Button cancelButton = gef.getFormButton(messages.getString("cancel"), "javascript:onClick=sendCancel()", false);
	Button okButton = gef.getFormButton(messages.getString("ok"), "javascript:onClick=sendOK()", false);
	buttonPane.addButton(okButton);
	buttonPane.addButton(cancelButton);
	out.println(buttonPane.print());
	out.println(frame.printMiddle());
	out.println(frame.printAfter());
%>
 </BODY>
<script language='javascript'>
   window.focus();
</script>
</HTML>