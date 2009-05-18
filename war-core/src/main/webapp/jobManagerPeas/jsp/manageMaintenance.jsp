<%@ include file="check.jsp" %>
<HTML>
<HEAD>
<TITLE><%=resource.getString("GML.popupTitle")%></TITLE>
<%
 Board board = gef.getBoard(); 
 String checked_on = "checked";
 String checked_off = "";
 
 boolean mode = new Boolean((String) request.getAttribute("mode")).booleanValue();
 if (!mode)
 {
     checked_on = "";
     checked_off = "checked";
 }    

 out.println(gef.getLookStyleSheet());
%>
</HEAD>
<BODY marginheight=5 marginwidth=5 leftmargin=5 topmargin=5 bgcolor="#FFFFFF">
<% 
out.println(window.printBefore());
out.println(frame.printBefore());
%>
<center>
<%
out.println(board.printBefore());
%>
<div align="left" class="txtNav"><%=resource.getString("JMAP.header1")%></div><br>
<%
if (mode)
{
    out.println("<div align=\"left\" class=\"txtPetitBold\">"+resource.getString("JMAP.maintenanceOnAir")+"</div><br>");
}
%>
<form NAME="frm_maintenance" method="post" action="SetMaintenanceMode">
<table>
 <tr>
  <td class="txtPetitBold">
    <%=resource.getString("JMAP.active")%> : 
  </td>
  <td><input type="radio" <%=checked_on%> name="mode" value="true">
  </td>
 </tr>
 <tr>
  <td class="txtPetitBold">
    <%=resource.getString("JMAP.desactive")%> : 
  </td>
  <td><input type="radio" <%=checked_off%> name="mode" value="false">
  </td>
 </tr>
</table>
</form> 
<%
out.println(board.printAfter());
%>
<br>
<%
ButtonPane bouton = gef.getButtonPane();
bouton.addButton((Button) gef.getFormButton(resource.getString("GML.validate"), "javascript:document.frm_maintenance.submit()", false));
out.println(bouton.print());
%>
<% 
out.println(frame.printAfter());
out.println(window.printAfter());
%>
</center>
</BODY>
</HTML>