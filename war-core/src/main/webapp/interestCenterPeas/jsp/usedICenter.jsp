<%@ include file="checkICenter.jsp" %>
 <HTML>
   <HEAD>
     <TITLE><%=resource.getString("GML.popupTitle")%></TITLE>
        <meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
          <% out.println(gef.getLookStyleSheet()); %>

 <script language="Javascript">
  // if user clicks cancel button - close popup window and set focus on edit field
    function sendCancel() {
     window.opener.focus();
     window.opener.icForm.icName.focus();
     window.close();
    }

  // if user clicks ok button - close popup window and perform closeAndReplace function on newICenter page
    function sendOK() {
        window.opener.closeAndReplace();
        window.close();
      }
  </script>
  </HEAD>

  <BODY>

<%
    out.println(frame.printBefore());
%>
<center>
<table width="98%" border="0" cellspacing="0" cellpadding="0" class=intfdcolor4><!--tablcontour-->

  <tr>
    <td valign="top" align="center"> <!-- SEPARATION NAVIGATION / CONTENU DU COMPOSANT -->

      <!--  -->
      <table border="0" cellspacing="0" cellpadding="5" width="100%" align="center" class="contourintfdcolor">
        <tr>
          <td align="center">
                <B>
                <%=resource.getString("NameExists") %>

                     </td>
                 </tr>
                </table>
                        </td>
                 </tr>
                </table> <br>
         <%
        ButtonPane buttonPane = gef.getButtonPane();
        Button cancelButton = (Button) gef.getFormButton(resource.getString("GML.cancel"), "javascript:onClick=sendCancel()", false);
        Button okButton = (Button) gef.getFormButton(resource.getString("GML.ok"), "javascript:onClick=sendOK()", false);
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