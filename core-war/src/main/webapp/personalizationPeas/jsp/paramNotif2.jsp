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

<HTML>
<HEAD>
<TITLE><%=resource.getString("GML.popupTitle")%></TITLE>
<view:looknfeel withCheckFormScript="true"/>
<script>
  function validate() {
    var comp = stripInitialWhitespace(document.paramNotifForm.componentId.value);
    var dest = stripInitialWhitespace(document.paramNotifForm.notificationId.value);
    var errorMsg = "";

    if (isWhitespace(comp))
    {
        if (errorMsg != "")
            errorMsg = errorMsg + "\n";
        errorMsg = errorMsg + "<% out.print(resource.getString("GML.theField")+" '"+resource.getString("GML.jobPeas")+"' "+resource.getString("GML.MustBeFilled")); %>";
    }

    if (isWhitespace(dest))
    {
        if (errorMsg != "")
            errorMsg = errorMsg + "\n";
        errorMsg = errorMsg + "<% out.print(resource.getString("GML.theField")+" '"+resource.getString("dest")+"' "+resource.getString("GML.MustBeFilled")); %>";
    }
    if (errorMsg == "")
    {
        document.paramNotifForm.submit();
    }
    else
    {
        window.alert(errorMsg);
    }
  }
</script>

</HEAD>

<BODY marginwidth=5 marginheight=5 leftmargin=5 topmargin=5 onload="javascript:resizePopup(750,550);">

<%
    browseBar.setComponentName(resource.getString("MesNotifications"));
    browseBar.setPath(resource.getString("browseBar_Path3"));

    out.println(window.printBefore());
    out.println(frame.printBefore());
%>

<%
// Add commun code that display the Rules list
%>
<%@ include file="paramNotif_Commun.jsp" %>
<BR>
<form name="paramNotifForm" Action="paramNotif.jsp?Action=addPref" Method="POST">
<table CELLPADDING=5 CELLSPACING=2 BORDER=0 WIDTH="98%" CLASS=intfdcolor>
  <tr>
    <td CLASS=intfdcolor4 NOWRAP>
      <TABLE width="100%" align="center" border=0 cellPadding=0 cellSpacing=0>
        <TR>
          <TD align="center">
                  <tr>
                    <td class=intfdcolor4 height="20">
                      <span class=txtlibform>
                      &nbsp;<%=resource.getString("GML.jobPeas")%> : </span>&nbsp;
                    </td>
                     <td class=intfdcolor4 height="20">
                      <span class=selectNS>
                      <select name="componentId">
                         <% out.println(personalizationScc.buildOptions(personalizationScc.getInstanceList(), "", resource.getString("GML.select"))); %>
                      </select>
                      </span>
                    </td>
				 </tr>

				 <tr>
                    <td class=intfdcolor4 height="20">
                      <span class=txtlibform>
                      &nbsp;<%=resource.getString("dest")%> : </span>&nbsp;
                    </td>
                     <td class=intfdcolor4 height="20">
                      <span class=selectNS>
                      <select name="notificationId">
                         <% out.println(personalizationScc.buildOptions(personalizationScc.getNotificationAddresses(), "", resource.getString("GML.select"))); %>
                      </select>
                      </span>
                    </td>
				 </tr>
	</TABLE>
	</td>
	</tr>
</table>
<br>
<%
	ButtonPane buttonPane = gef.getButtonPane();
	buttonPane.addButton((Button) gef.getFormButton(resource.getString("GML.add"), "javascript:validate()", false));
	buttonPane.addButton((Button) gef.getFormButton(resource.getString("GML.cancel"), "paramNotif.jsp", false));
	out.print(buttonPane.print());
%>
</form>
<%
out.println(frame.printAfter());
out.println(window.printAfter());
%>
</CENTER>
</BODY>
</HTML>