<%--

    Copyright (C) 2000 - 2024 Silverpeas

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    As a special exception to the terms and conditions of version 3.0 of
    the GPL, you may redistribute this Program in connection with Free/Libre
    Open Source Software ("FLOSS") applications as described in Silverpeas's
    FLOSS exception.  You should have received a copy of the text describing
    the FLOSS exception, and it is also available here:
    "https://www.silverpeas.org/legal/floss_exception.html"

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.

--%>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<%@ include file="checkPersonalization.jsp" %>

<%
  String notifName = "" ;
  String channelId = "" ;
  String address = "" ;

  String id = request.getParameter("id") ;
  String action = request.getParameter("Action");

  if (id == null || id.equalsIgnoreCase("-1")) {
    id = "" ;
  }

  if (action == null) {
    action = "NotificationView";
  }

  if (action.equals("save"))
  {
        notifName = WebEncodeHelper.htmlStringToJavaString(request.getParameter("txtNotifName")) ;
        channelId = request.getParameter("channelId") ;
        address = WebEncodeHelper.htmlStringToJavaString(request.getParameter("txtAddress")) ;
        personalizationScc.saveNotifAddress(id, notifName, channelId, address, null) ;
        %>
        <HTML>
          <HEAD>
          <script language="Javascript">
            function closeAndReplace() {
              window.opener.location.href = "ParametrizeNotification";
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
        if (! id.equals(""))
        {
            Properties p = personalizationScc.getNotificationAddressProperties(id) ;
            notifName = p.getProperty("name") ;
            channelId = p.getProperty("channelId") ;
            address = p.getProperty("address") ;
        }
  }
%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title><%=resource.getString("GML.popupTitle")%></title>
<view:looknfeel withCheckFormScript="true"/>
<script>
  function validate() {
    var title = stripInitialWhitespace(document.personalizationForm.txtNotifName.value);
    var dest = stripInitialWhitespace(document.personalizationForm.txtAddress.value);
    var errorMsg = "";

    if (isWhitespace(title))
    {
        errorMsg = "<% out.print(resource.getString("GML.theField")+" '"+resource.getString("GML.name")+"' "+resource.getString("GML.MustBeFilled")); %>";
    }
    if (isWhitespace(dest))
    {
        if (errorMsg !== "")
            errorMsg = errorMsg + "\n";
        errorMsg = errorMsg + "<% out.print(resource.getString("GML.theField")+" '"+resource.getString("adresse")+"' "+resource.getString("GML.MustBeFilled")); %>";
    }
    if (errorMsg === "") {
        document.personalizationForm.submit() ;
    } else {
      jQuery.popup.error(errorMsg);
    }
  }
</script>
</head>
<body onload="document.personalizationForm.txtNotifName.focus()">
<%
    browseBar.setComponentName(resource.getString("MesNotifications"));
    if (! id.equals("")) //modification
	browseBar.setPath(resource.getString("browseBar_Path4"));
    else //ajout
	browseBar.setPath(resource.getString("browseBar_Path2"));

    out.println(window.printBefore());
    out.println(frame.printBefore());
    out.println(board.printBefore());
%>

<form name="personalizationForm" action="editNotification.jsp?Action=save&id=<%=id%>" method="post">
      <input type="hidden" name="id" value="<%=id%>"/>
      <table cellpadding="5" cellspacing="0" border="0" width="100%">
        <tr>
          <td valign="baseline" align=left  class="txtlibform">
            <%=resource.getString("GML.name")%> :&nbsp;
          </td>
          <td align=left valign="baseline">
            <input type="text" name="txtNotifName" size="50" maxlength="20" value="<%=Encode.forHtml(notifName)%>"/> <img border="0" src="<%=mandatoryField%>" width="5" height="5"/>
          </td>
        </tr>

        <tr>
          <td class="txtlibform">
            <%=resource.getString("type")%> :
          </td>
          <td align=left valign="baseline" class="txtnav">
              <select name="channelId">
                <% out.println(personalizationScc.buildOptions(personalizationScc.getNotifChannels(), channelId, null)); %>
              </select>
          </td>
        </tr>

        <tr>
          <td valign="baseline" align=left  class="txtlibform">
            <%=resource.getString("adresse")%> :&nbsp;
          </td>
          <td align=left valign="baseline">
            <input type="text" name="txtAddress" size="50" maxlength="250" VALUE="<%=Encode.forHtml(address)%>"/> <img border="0" src="<%=mandatoryField%>" width="5" height="5"/>
          </td>
        </tr>

	<tr>
          <td colspan="2">
	    <img border="0" src="<%=mandatoryField%>" width="5" height="5"/> : <%=resource.getString("GML.requiredField")%>
          </td>
        </tr>
      </table>
</form>
<%
	out.println(board.printAfter());

    ButtonPane buttonPane = gef.getButtonPane();
    buttonPane.addButton(gef.getFormButton(resource.getString("GML.validate"), "javascript:onClick=validate();", false));
    buttonPane.addButton(gef.getFormButton(resource.getString("GML.cancel"), "javascript:window.close();", false));
    out.println("<br/>"+buttonPane.print());

    out.println(frame.printAfter());
    out.println(window.printAfter());
%>
</body>
</html>