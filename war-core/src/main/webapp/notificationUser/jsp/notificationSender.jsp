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

<%@page import="com.stratelia.webactiv.beans.admin.Group"%>
<%@page import="com.stratelia.webactiv.beans.admin.UserDetail"%>
<%@page import="com.stratelia.silverpeas.notificationUser.Notification"%>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>

<%
response.setHeader("Cache-Control","no-store"); //HTTP 1.1
response.setHeader("Pragma","no-cache");        //HTTP 1.0
response.setDateHeader ("Expires",-1);          //prevents caching at the proxy server
%>

<%@ page import="com.stratelia.silverpeas.notificationUser.control.NotificationUserSessionController"%>
<%@ page import="com.stratelia.silverpeas.notificationManager.NotificationParameters"%>
<%@ page import="com.stratelia.silverpeas.peasCore.URLManager"%>

<%@ page import="java.util.*"%>
<%@ page import="com.stratelia.webactiv.util.*"%>

<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.GraphicElementFactory"%>
<%@ page import=" com.silverpeas.util.EncodeHelper"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.buttons.*"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.buttonPanes.*"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.browseBars.BrowseBar"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.window.Window"%>

<%@ page errorPage="../../admin/jsp/errorpage.jsp"%>

<fmt:setLocale value="${sessionScope[sessionController].language}" />
<view:setBundle bundle="${requestScope.resources.multilangBundle}" />
 
<%
	NotificationUserSessionController notificationScc = (NotificationUserSessionController) request.getAttribute("notificationUser");

	// Ze graffik factory
	GraphicElementFactory gef = (GraphicElementFactory) session.getAttribute("SessionGraphicElementFactory");

	String m_context        = URLManager.getApplicationURL();
	String mandatoryField    = m_context + "/util/icons/mandatoryField.gif";

   Notification notification = (Notification) request.getAttribute("Notification");
	
   boolean popupMode = (Boolean) request.getAttribute("popupMode");
   boolean editTargets = (Boolean) request.getAttribute("editTargets");
   
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<view:looknfeel/>
<view:includePlugin name="tags" />
<script type="text/javascript" src="<%=m_context%>/util/javaScript/checkForm.js"></script>
<script type="text/javascript">
function Submit(){
  	processRecipients();
	SP_openUserPanel('about:blank', 'OpenUserPanel', 'menubar=no,scrollbars=no,statusbar=no');
	document.notificationSenderForm.action = "SetTarget";
	document.notificationSenderForm.target = "OpenUserPanel";
	document.notificationSenderForm.submit();
}
function sendNotification() {
    var title = stripInitialWhitespace(document.notificationSenderForm.txtTitle.value);
    
    processRecipients();
    
    var errorMsg = "";
    if (isWhitespace(title)) { 
       errorMsg = "<fmt:message key="GML.thefield"/> <fmt:message key="name"/> <fmt:message key="GML.isRequired"/>";
    }
    if (errorMsg == "") {
		document.notificationSenderForm.action = "SendNotif";
		document.notificationSenderForm.submit();
    } else {
        window.alert(errorMsg);
    }
}

function processRecipients() {
  var userIds = "";
  var groupIds = "";
  
  <% if (editTargets) { %>
  var tags = $("#dest").tagit("tags");
  for (var i in tags) {
    var value = tags[i].value;
    if (value.charAt(0) === "u") {
      userIds += value.substring(1, value.length) + " ";
    } else {
      groupIds += value.substring(1, value.length) + " ";
    }
  }
  <% } else { %>
  	$(".readonlyUser").each(function(index) {
    	userIds += $(this).attr("data-value") + " ";
    });
  <% } %>
  $("#selectedUsers").val(userIds);
  $("#selectedGroups").val(groupIds);
}
$(function () {
  <% if (editTargets) { %>
	$('#dest').tagit({allowNewTags:false});
  <% } %>
});
</script>
</head>
<body onload="document.notificationSenderForm.txtTitle.focus();">
<fmt:message key="GML.notification.send" var="msgAction"/>
<view:browseBar extraInformations="${msgAction}"/>
<view:window popup="true">
<view:frame>
<view:board>
<form name="notificationSenderForm" action="" method="post" accept-charset="UTF-8">
      <table cellpadding="5" cellspacing="0" border="0" width="100%">
      	<tr>
          <td valign="top" class="txtlibform"><fmt:message key="addressees"/> :</td>
          <td>
          	<% if (editTargets) { %>
             <ul id="dest" data-name="dest">
             	<% for (UserDetail user : notification.getUsers()) { %>
             	<li data-value="u<%=user.getId()%>"><%=user.getDisplayedName() %></li>
             	<% } %>
             	<% for (Group group : notification.getGroups()) { %>
             	<li data-value="g<%=group.getId()%>"><%=group.getName() %> (<%=group.getNbUsers() %>)</li>
             	<% } %>
             </ul>
             <a href="#" onclick="javascript:Submit()" title="<fmt:message key="Opane_addressees"/>"><img src="<%=m_context %>/util/icons/notification_assign.gif" alt="<fmt:message key="Opane_addressees"/>"/></a>
             <% } else { %>
             	<% for (UserDetail user : notification.getUsers()) { %>
             		<span data-value="<%=user.getId()%>" class="readonlyUser"><%=user.getDisplayedName() %></span>
             	<% } %>
             <% } %>
          </td>
        </tr>
        <tr>
          <td class="txtlibform"><fmt:message key="GML.notification.subject"/> :</td>
          <td>
           <input type="text" name="txtTitle" size="90" maxlength="<%=NotificationParameters.MAX_SIZE_TITLE%>" value="<%=EncodeHelper.javaStringToHtmlString(notification.getSubject())%>"/>
			 <img src="<%=mandatoryField%>" width="5" height="5"/>
          </td>
        </tr>
        <tr>
          <td class="txtlibform" valign="top"><fmt:message key="GML.notification.message"/> :</td>
          <td>
			<textarea name="txtMessage" cols="90" rows="6"><%=EncodeHelper.javaStringToHtmlString(notification.getBody())%></textarea>
          </td>
        </tr>
        <tr> 
          <td colspan="2"> (<img src="<%=mandatoryField%>" width="5" height="5"/> : <fmt:message key="GML.requiredField"/>)</td>
        </tr>
				<input type="hidden" name="selectedUsers" id="selectedUsers" value=""/>
				<input type="hidden" name="selectedGroups" id="selectedGroups" value=""/>
				<input type="hidden" name="popupMode" value="<%=popupMode%>"/>
				<input type="hidden" name="editTargets" value="<%=editTargets%>"/> 
      </table>
</form>
</view:board>
<view:buttonPane>
<fmt:message key="Envoyer" var="msgSend"/>
<fmt:message key="GML.cancel" var="msgCancel"/>
<view:button label="${msgSend}" action="javascript:sendNotification()"/>
<view:button label="${msgCancel}" action="javascript:window.close()"/>
</view:buttonPane>
</view:frame>
</view:window>
</body>
</html>
