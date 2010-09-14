<%--

    Copyright (C) 2000 - 2009 Silverpeas

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    As a special exception to the terms and conditions of version 3.0 of
    the GPL, you may redistribute this Program in connection with Free/Libre
    Open Source Software ("FLOSS") applications as described in Silverpeas's
    FLOSS exception.  You should have received a copy of the text describing
    the FLOSS exception, and it is also available here:
    "http://repository.silverpeas.com/legal/licensing"

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

--%>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%@ include file="checkVersion.jsp" %>
<%

    ResourceLocator messages = new ResourceLocator("com.stratelia.silverpeas.versioningPeas.multilang.versioning", m_MainSessionCtrl.getFavoriteLanguage());
    String docId 			= (String) request.getAttribute("DocId");
    String url				= (String) request.getAttribute("Url");
%>
 <HTML>
   <HEAD>
     <TITLE><%=messages.getString("popupTitle")%></TITLE>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
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