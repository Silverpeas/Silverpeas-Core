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
<%@ page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%
  response.setHeader("Cache-Control", "no-store"); //HTTP 1.1
  response.setHeader("Pragma", "no-cache"); //HTTP 1.0
  response.setDateHeader("Expires", -1); //prevents caching at the proxy server
%>
<%@ include file="graphicBox.jsp"%>
<%@ include file="checkSilvermail.jsp"%>
<%@ page
	import="com.stratelia.silverpeas.notificationserver.channel.silvermail.SILVERMAILMessage"%>
<%@ page import="com.stratelia.webactiv.util.DateUtil"%>
<%@ page import="java.util.Date"%>

<%
  String from = request.getParameter("from");
  boolean fromHomePage = "homePage".equals(from);
  SendedNotificationDetail notif = (SendedNotificationDetail) request.getAttribute("SendedNotification");
%>
<%@page	import="com.stratelia.silverpeas.notificationManager.model.SendedNotificationDetail"%>
<html>
<head>
  <title>___/ Silverpeas - Corporate Portal Organizer
  \________________________________________________________________________</title>
  <%
    out.println(gef.getLookStyleSheet());
  %>
  <script type="text/javascript" src="<%=m_context%>/util/javaScript/animation.js"></script>

  <script type="text/javascript" >
    function deleteMessage( notifId )
    {
      window.opener.location = "DeleteSendedNotification.jsp?NotifId=" + notifId;
      window.close();
    }

    function goTo()
    {
      window.opener.location = "<%=m_context%>" + "<%=notif.getLink()%>";
      window.close();
    }

    function closeWindow()
    {
      <%if (fromHomePage) {%>
          window.opener.location.reload();
      <%} else {%>
          window.opener.location = "SendedUserNotifications.jsp";
      <%}%>
      window.close();
    }
  </script>
</head>
<BODY marginwidth=5 marginheight=5 leftmargin=5 topmargin=5>
<%
  Window window = gef.getWindow();
  BrowseBar browseBar = window.getBrowseBar();
  browseBar.setComponentName(silvermailScc.getString("silverMail"));
  browseBar.setPath(silvermailScc.getString("message"));
  out.println(window.printBefore());
  //Instanciation du cadre avec le view generator
  Frame frame = gef.getFrame();
  out.println(frame.printBefore());
%>

<CENTER>
<table CELLPADDING=0 CELLSPACING=2 BORDER=0 WIDTH="98%" CLASS=intfdcolor>
	<tr>
		<td CLASS=intfdcolor4 NOWRAP>
		<table CELLPADDING=5 CELLSPACING=0 BORDER=0 WIDTH="100%">
			<form name="silvermailForm" Action="" Method="POST">
			<tr>
				<td valign="baseline" align=left class="txtlibform"><%=silvermailScc.getString("date")%>
				:&nbsp;</td>
				<td align=left valign="baseline"><%=EncodeHelper.javaStringToHtmlString(resource
					.getOutputDate(notif.getNotifDate()))%></td>
			</tr>
			<tr>
				<td valign="baseline" align=left class="txtlibform"><%=silvermailScc.getString("source")%>
				:&nbsp;</td>
				<td align=left valign="baseline"><%=EncodeHelper.javaStringToHtmlString(notif.getSource())%></td>
			</tr>
			<tr>
				<td valign="baseline" align=left class="txtlibform"><%=silvermailScc.getString("url")%>
				:&nbsp;</td>
				<td align=left valign="baseline">
				<%
				  if (notif.getLink() != null && notif.getLink().length() > 0)
								out.println("<A HREF =\"javaScript:goTo();\"><img src=\""
										+ resource.getIcon("silvermail.link")
										+ "\" border=\"0\"></A>");
							else
								out.println("");
				%>
				</td>
			</tr>
			<tr>
				<td valign="baseline" align=left class="txtlibform"><%=silvermailScc.getString("title")%>
				:&nbsp;</td>
				<td align=left valign="baseline"><%=EncodeHelper.javaStringToHtmlString(notif.getTitle())%></td>
			</tr>
			<tr>
				<td valign="baseline" align=left class="txtlibform"></td>
				<td align=left valign="baseline"><%=EncodeHelper.javaStringToHtmlParagraphe(notif.getBody())%></td>
			</tr>
			</form>
		</table>
		</td>
	</tr>
</table>
<%=separator%> <%
   ButtonPane buttonPane = gef.getButtonPane();
 			buttonPane.addButton((Button) gef.getFormButton(silvermailScc
 					.getString("delete"), "javascript:onClick=deleteMessage("
 					+ notif.getNotifId() + ");", false));
 			buttonPane.addButton((Button) gef.getFormButton(silvermailScc
 					.getString("close"), "javascript:onClick=closeWindow();",
 					false));
 			out.println(buttonPane.print());
 %>
</CENTER>
<%
  out.println(frame.printAfter());
			out.println(window.printAfter());
%>
</BODY>
</HTML>