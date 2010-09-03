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

<%
response.setHeader("Cache-Control","no-store"); //HTTP 1.1
response.setHeader("Pragma","no-cache");        //HTTP 1.0
response.setDateHeader ("Expires",-1);          //prevents caching at the proxy server
%>

<%@ include file="graphicBox.jsp" %>
<%@ include file="checkSilvermail.jsp" %>
<%@ include file="tabManager.jsp.inc" %>
<%@ page import="com.stratelia.silverpeas.peasCore.URLManager"%>
<%@ page import="com.stratelia.silverpeas.notificationserver.channel.silvermail.SILVERMAILMessage"%>
<%@ page import="com.stratelia.webactiv.util.DateUtil"%>
<%@ page import="java.util.Date"%>
<%@page import="com.stratelia.silverpeas.notificationManager.model.SendedNotificationDetail"%>

<%
   List sendedNotifs = (List) request.getAttribute("SendedNotifs");
%>
<HTML>
<HEAD>
<TITLE>___/ Silverpeas - Corporate Portal Organizer \________________________________________________________________________</TITLE>
<%
  out.println(gef.getLookStyleSheet());
%>
<script type="text/javascript" src="<%=graphicPath%>/util/javaScript/animation.js"></script>
<script>
function readMessage(id){
	SP_openWindow("ReadSendedNotification.jsp?NotifId=" + id,"ReadSendedNotification","600","380","scrollable=yes,scrollbars=yes");
}

function deleteMessage(id) {
    if(window.confirm("<%=silvermailScc.getString("ConfirmDeleteMessage")%>")){
		window.location = "DeleteSendedNotification.jsp?NotifId="+id;
    }
}

function deleteAllMessages() {
    if(window.confirm("<%=silvermailScc.getString("ConfirmDeleteAllSendedNotif")%>")){
		window.location = "DeleteAllSendedNotifications.jsp?";
    }
}

</script>
</head>
<body marginwidth=5 marginheight=5 leftmargin=5 topmargin=5>
<%
  
  Window window = gef.getWindow();

  OperationPane operationPane = window.getOperationPane();
  operationPane.addOperation(deleteAllNotif, silvermailScc.getString("DeleteAllSendedNotif"), "javascript:deleteAllMessages()");

  BrowseBar browseBar = window.getBrowseBar();
  browseBar.setComponentName(silvermailScc.getString("silverMail"));
  browseBar.setPath(silvermailScc.getString("bbar1_inbox"));
  
  // Barre d'onglet
  TabbedPane tabbedPane = gef.getTabbedPane();
  tabbedPane.addTab(silvermailScc.getString("LireNotification"), "Main", false);
  tabbedPane.addTab(silvermailScc.getString("SendedUserNotifications"), "SendedUserNotifications", true);
  tabbedPane.addTab(silvermailScc.getString("ParametrerNotification"), m_Context + URLManager.getURL(URLManager.CMP_PERSONALIZATION) + "personalization_Notification.jsp?Action=NotificationView", false);

  out.println(window.printBefore());
  out.println(tabbedPane.print());

  //Instanciation du cadre avec le view generator
  Frame frame = gef.getFrame();
  out.println(frame.printBefore());

  // Arraypane
  ArrayPane list = gef.getArrayPane( "silvermail", "SendedUserNotifications.jsp", request,session );
  ArrayColumn col = list.addArrayColumn( silvermailScc.getString("date") );
  col.setSortable(true);
  col = list.addArrayColumn( silvermailScc.getString("source") );
  col.setSortable(true);
  col = list.addArrayColumn( silvermailScc.getString("subject") );
  col.setSortable(true);
  col = list.addArrayColumn( silvermailScc.getString("operation") );
  col.setSortable(false);

  Iterator	it = sendedNotifs.iterator();
  while( it.hasNext() == true )
  {
		SendedNotificationDetail message = (SendedNotificationDetail) it.next();
			
    String link = "<A HREF =\"javascript:onClick=readMessage(" + message.getNotifId() + ");\">";
    ArrayLine line = list.addArrayLine();
	  Date notifDate = message.getNotifDate();
    ArrayCellText cell = line.addArrayCellText(resource.getOutputDate(notifDate));
    cell.setCompareOn(notifDate);
    line.addArrayCellText(EncodeHelper.javaStringToHtmlString(message.getSource()) + "</A>");
    line.addArrayCellText(link + EncodeHelper.javaStringToHtmlString(message.getTitle()) + "</A>");

    // Ajout des icones de modification et de suppression
    IconPane actions = gef.getIconPane();
    Icon del = actions.addIcon();
  	del.setProperties(delete, silvermailScc.getString("delete") , "javascript:onClick=deleteMessage('" + message.getNotifId() +"');");
    line.addArrayCellIconPane(actions);
  }

  out.println(list.print());
  out.println(frame.printAfter());
  out.println(window.printAfter());
%>
</body>
</html>