<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.buttonpanes.ButtonPane" %>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.board.Board" %><%--

    Copyright (C) 2000 - 2013 Silverpeas

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    As a special exception to the terms and conditions of version 3.0 of
    the GPL, you may redistribute this Program in connection with Free/Libre
    Open Source Software ("FLOSS") applications as described in Silverpeas's
    FLOSS exception.  You should have received a copy of the text describing
    the FLOSS exception, and it is also available here:
    "http://www.silverpeas.org/docs/core/legal/floss_exception.html"

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

--%>

<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<%@ include file="check.jsp" %>
<HTML>
<HEAD>
<TITLE><%=resource.getString("GML.popupTitle")%></TITLE>
<view:looknfeel/>
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