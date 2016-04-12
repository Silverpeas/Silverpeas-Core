<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.buttons.Button" %><%--

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
<%@ include file="checkICenter.jsp" %>
 <HTML>
   <HEAD>
     <TITLE><%=resource.getString("GML.popupTitle")%></TITLE>
     <view:looknfeel/>
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