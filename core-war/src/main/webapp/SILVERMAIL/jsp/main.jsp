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

<%
response.setHeader("Cache-Control","no-store"); //HTTP 1.1
response.setHeader("Pragma","no-cache");        //HTTP 1.0
response.setDateHeader ("Expires",-1);          //prevents caching at the proxy server
%>

<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>

<%@ include file="graphicBox.jsp" %>
<%@ include file="checkSilvermail.jsp" %>
<%@ include file="tabManager.jsp" %>
<%@ page import="org.silverpeas.core.notification.user.server.channel.silvermail.SILVERMAILMessage"%>
<%@ page import="org.silverpeas.core.util.URLUtil"%>
<%@ page import="java.util.Date"%>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.browsebars.BrowseBar" %>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.tabs.TabbedPane" %>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.iconpanes.IconPane" %>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.operationpanes.OperationPane" %>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.window.Window" %>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.icons.Icon" %>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
  <title></title>
  <view:looknfeel/>
<script type="text/javascript">
function readMessage(id){
	SP_openWindow("ReadMessage.jsp?ID=" + id,"readMessage","600","380","scrollable=yes,scrollbars=yes");
}

function deleteMessage(id, skipConfirmation, spaceId, from) {
    if(skipConfirmation || window.confirm("<%=silvermailScc.getString("ConfirmDeleteMessage")%>")){
      var $form = jQuery('#genericForm');
      jQuery('#ID', $form).val(id);
      if (spaceId) {
        jQuery('#SpaceId', $form).val(spaceId);
      }
      if (from) {
        jQuery('#from', $form).val(from);
      }
      $form.attr('action', "DeleteMessage.jsp").submit();
    }
}

function deleteAllMessages() {
    if(window.confirm("<%=silvermailScc.getString("ConfirmDeleteAllNotif")%>")){
      var $form = jQuery('#genericForm');
      jQuery('#folder', $form).val('INBOX');
      $form.attr('action', "DeleteAllMessages.jsp").submit();
    }
}

function newMessage() {
	SP_openWindow("<%=m_Context%>/RnotificationUser/jsp/Main.jsp?popupMode=Yes", 'notifyUserPopup', '700', '430', 'menubar=no,scrollbars=yes,statusbar=no');
}

</script>
</head>
<body>
<%
  Window window = gef.getWindow();

  OperationPane operationPane = window.getOperationPane();
  operationPane.addOperationOfCreation(addNotif, silvermailScc.getString("Notifier"), "javascript:newMessage()");
  operationPane.addOperation(deleteAllNotif, silvermailScc.getString("DeleteAllNotif"), "javascript:deleteAllMessages()");

  BrowseBar browseBar = window.getBrowseBar();
  browseBar.setComponentName(silvermailScc.getString("silverMail"));
  browseBar.setPath(silvermailScc.getString("bbar1_inbox"));

  // Barre d'onglet
  TabbedPane tabbedPane = gef.getTabbedPane();
  tabbedPane.addTab(silvermailScc.getString("LireNotification"), "Main", true);
  tabbedPane.addTab(silvermailScc.getString("SentUserNotifications"), "SentUserNotifications", false);
  tabbedPane.addTab(silvermailScc.getString("ParametrerNotification"), m_Context + URLUtil.getURL(
      URLUtil.CMP_PERSONALIZATION) + "ParametrizeNotification", false);

  out.println(window.printBefore());
  out.println(tabbedPane.print());
%>
<view:frame>
<view:areaOfOperationOfCreation/>
<%
  // Arraypane
  ArrayPane list = gef.getArrayPane( "silvermail", "Main.jsp", request,session );
  ArrayColumn col = list.addArrayColumn( silvermailScc.getString("date") );
  col.setSortable(true);
  col = list.addArrayColumn( silvermailScc.getString("source") );
  col.setSortable(true);
  col = list.addArrayColumn( silvermailScc.getString("from") );
  col.setSortable(true);
  col = list.addArrayColumn( silvermailScc.getString("url") );
  col.setSortable(true);
  col = list.addArrayColumn( silvermailScc.getString("subject") );
  col.setSortable(true);
  col = list.addArrayColumn( silvermailScc.getString("operation") );
  col.setSortable(false);

  Iterator	messageIterator = silvermailScc.getFolderMessageList( "INBOX" ).iterator();
  String	hasBeenReadenOrNotBegin	= "";
  String	hasBeenReadenOrNotEnd	= "";
  while( messageIterator.hasNext() == true )
  {
	hasBeenReadenOrNotBegin = "";
	hasBeenReadenOrNotEnd = "";
    SILVERMAILMessage message = (SILVERMAILMessage)messageIterator.next();
	if (message.getReaden() == 0) {
		hasBeenReadenOrNotBegin = "<b>";
		hasBeenReadenOrNotEnd = "</b>";
	}
    String link = "<a href=\"javascript:onClick=readMessage(" + message.getId() + ");\">";
    ArrayLine line = list.addArrayLine();
	Date date = message.getDate();
    ArrayCellText cell = line.addArrayCellText(hasBeenReadenOrNotBegin + resource.getOutputDate(date) + hasBeenReadenOrNotEnd);
    cell.setCompareOn(date);
    line.addArrayCellText(hasBeenReadenOrNotBegin + EncodeHelper.javaStringToHtmlString(message.getSource()) + "</a>" + hasBeenReadenOrNotEnd);
    line.addArrayCellText(hasBeenReadenOrNotBegin + link + EncodeHelper.javaStringToHtmlString(message.getSenderName()) + "</a>" + hasBeenReadenOrNotEnd);
    if ( message.getUrl()!=null && message.getUrl().length()>0 )
	line.addArrayCellText(hasBeenReadenOrNotBegin + "<a href =\"" + EncodeHelper.javaStringToHtmlString(message.getUrl()) + "\" target=\"_top\"><img src=\""+resource.getIcon("silvermail.link")+"\" border=\"0\"/></a>" + hasBeenReadenOrNotEnd);
    else
		line.addArrayCellText( "" );

    line.addArrayCellText(hasBeenReadenOrNotBegin + link + message.getSubject() + "</a>" + hasBeenReadenOrNotEnd);

    // Ajout des icones de modification et de suppression
    IconPane actions = gef.getIconPane();
    Icon del = actions.addIcon();
    //del.setProperties(delete, silvermailScc.getString("delete") , "DeleteMessage.jsp?ID=" + message.getId() );
	del.setProperties(delete, silvermailScc.getString("delete") , "javascript:onClick=deleteMessage('" + message.getId() +"');");
    line.addArrayCellIconPane(actions);
  }

  out.println(list.print());
 %>
</view:frame>
<%
out.println(window.printAfter());
%>
<form id="genericForm" action="" method="POST">
  <input id="ID" name="ID" type="hidden"/>
  <input id="folder" name="folder" type="hidden"/>
  <input id="SpaceId" name="SpaceId" type="hidden"/>
  <input id="from" name="from" type="hidden"/>
</form>
</body>
</html>