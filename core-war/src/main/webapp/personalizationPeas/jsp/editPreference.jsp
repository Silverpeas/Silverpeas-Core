<%--

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
<%@ include file="checkPersonalization.jsp" %>

<%

  String componentId = "";
  String notificationId = "";
  String id = request.getParameter("id") ;
  String action = request.getParameter("Action");

  if ((id == null) || ("-1".equalsIgnoreCase(id))) {
    id = "" ;
  }

  if (action == null) {
    action = "NotificationView";
  }

  if (action.equals("addPref"))
  {
        componentId = request.getParameter("componentId");
        notificationId = request.getParameter("notificationId");
        if ((componentId != null) && (componentId.length() > 0) && (notificationId != null) && (notificationId.length() > 0))
        {
            if (id.length() > 0) // Edit mode and not create mode -> remove the previous one before
            {
                personalizationScc.deletePreference(id);
            }
            personalizationScc.addPreference(componentId,null,notificationId);
        }
        %>
        <HTML>
          <HEAD>
          <script language="Javascript">
            function closeAndReplace() {
              window.opener.location.href = "paramNotif.jsp";
              window.close();
            }
          </script>
          </HEAD>
          <BODY onLoad="closeAndReplace()">
          </BODY>
        </HTML>
        <%
  }
  else
  {
        if (id != "")
        {
            Properties p = personalizationScc.getNotifPreference(id) ;
            componentId = p.getProperty("componentId");
            notificationId = p.getProperty("notifAddressId");
        }
  }
%>

<HTML>
<HEAD>
<TITLE><%=resource.getString("GML.popupTitle")%></TITLE>
<view:looknfeel/>
<script>
  function validate() {
        document.personalizationForm.submit() ;
  }
</script>
</HEAD>
<BODY marginwidth=5 marginheight=5 leftmargin=5 topmargin=5 bgcolor="#FFFFFF">
<%
    browseBar.setComponentName(resource.getString("MesNotifications"));
    browseBar.setPath(resource.getString("browseBar_Path4"));

    out.println(window.printBefore());
    out.println(frame.printBefore());
    out.println(board.printBefore());
%>

<CENTER>
      <table CELLPADDING=5 CELLSPACING=0 BORDER=0 WIDTH="100%">
<form name="personalizationForm" Action="editPreference.jsp?Action=addPref&id=<%=id%>" Method="POST">
        <input type="hidden" name="id" value="<%=id%>">
        <tr>
          <td class="txtlibform">
            <%=resource.getString("GML.jobPeas")%> :
          </td>
          <td align=left valign="baseline" class="txtnav">
              <select name="componentId">
                   <% out.println(personalizationScc.buildOptions(personalizationScc.getInstanceList(), componentId, null)); %>
              </select>
          </td>
        </tr>

        <tr>
          <td valign="baseline" align=left  class="txtlibform">
            <%=resource.getString("dest")%> :
          </td>
          <td align=left valign="baseline">
              <select name="notificationId">
                   <% out.println(personalizationScc.buildOptions(personalizationScc.getNotificationAddresses(), notificationId, null)); %>
              </select>
          </td>
        </tr>
</form>
      </table>
<BR>
<%
	out.println(board.printAfter());

    ButtonPane buttonPane = gef.getButtonPane();
    buttonPane.addButton( gef.getFormButton(resource.getString("GML.validate"), "javascript:onClick=validate();", false));
    buttonPane.addButton( gef.getFormButton(resource.getString("GML.cancel"), "javascript:window.close();", false));
    out.println("<BR><center>"+buttonPane.print()+"</center>");
%>
</CENTER>
<%
    out.println(frame.printAfter());
    out.println(window.printAfter());
%>
</BODY>
</HTML>