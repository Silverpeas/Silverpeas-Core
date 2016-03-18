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
<%@ include file="checkCommunicationUser.jsp" %>

<%
	String userName = (String) request.getAttribute("UserName");
	String userIdDest = (String) request.getAttribute("UserIdDest");
	String userNameDest = (String) request.getAttribute("UserNameDest");

	operationPane.addOperation(resources.getIcon("communicationUser.chatroomClear"),resources.getString("clear"),"javascript:clear()");
	operationPane.addOperation(resources.getIcon("communicationUser.chatroomSave"),resources.getString("save"),"javascript:save()");
	operationPane.addOperation(resources.getIcon("communicationUser.chatroomLogout"),resources.getString("logout"),"javascript:logout()");

	window.setPopup(true);
%>

<html>
<head>
<TITLE><%=resources.getString("currentDiscussion")+" <"+userName+" - "+userNameDest+">"%></TITLE>
<view:looknfeel/>
<script type="text/javascript" src="<%=m_context%>/util/ajax/prototype.js"></script>
<script type="text/javascript" src="<%=m_context%>/util/ajax/rico.js"></script>
<script type="text/javascript" src="<%=m_context%>/util/ajax/ricoAjax.js"></script>
<script language="JavaScript">
function init()
{
	ajaxEngine.registerRequest('sendMessage', '<%=m_context%>/RAjaxCommunicationUserServlet/sendMessage');
	ajaxEngine.registerRequest('getMessage', '<%=m_context%>/RAjaxCommunicationUserServlet/getMessage');
	ajaxEngine.registerRequest('clearMessage', '<%=m_context%>/RAjaxCommunicationUserServlet/clearMessage');

	ajaxEngine.registerAjaxElement('message');

	document.discussion.msg.focus();

	checkMessage();
}

function checkSubmit(ev)
{
	var touche = ev.keyCode;
	if (touche == 13) {
		sendMessage();
		return false;
	}
	else
		return true;
}

function isCorrectForm() {
	var msg = document.discussion.msg.value;
	var textAreaLength = "<%=resources.getString("nbMaxTextArea")%>";
	if (msg.length > textAreaLength) {

       var errorMsg = "<%=resources.getString("messageTooLong")+" "+resources.getString("nbMaxTextArea")+" "+resources.getString("characters")%>\n";
       window.alert(errorMsg);
	   return false;
	}
	return true;
}

function sendMessage()
{
	if(isCorrectForm()) {
		var msg = document.discussion.msg.value;

		msg = msg.replace(/\+/g,"%2B");

		ajaxEngine.sendRequest('sendMessage', "Action=Post", "Msg="+escape(msg), "UserIdDest=<%=userIdDest%>");

		document.discussion.msg.value = "";
		document.discussion.msg.focus();
	}
}

function checkMessage()
{
	ajaxEngine.sendRequest('getMessage', "Action=Get", "UserIdDest=<%=userIdDest%>");

	document.getElementById('message').scrollTop = 999999;
}

function clear()
{
	ajaxEngine.sendRequest('clearMessage', "Action=Clear", "UserIdDest=<%=userIdDest%>");
}

function save()
{
	SP_openWindow("ExportDiscussion?userId=<%=userIdDest%>","Notification",650,400,"resizable=1,scrollbars=1,menubar=1");
}

function logout()
{
	window.close();
}
setInterval(checkMessage, 1000);

</script>

</head>
<body onLoad="init()">
<view:window popup="true">
<form method="get" action="javascript:sendMessage()" name="discussion" target="content">
<table width="100%" height="100%" border="0" cellspacing="1" cellpadding="0">
<tr>
<td align="left" valign="bottom">

				<!-- messages -->
				<div style="overflow:auto;height:240px;" id="message">
				 </div>
				<!-- /messages -->
 </td>
</tr>
</table>
<br/>
<table width="100%" border="0" cellspacing="1" cellpadding="1" align="center">
	<tr>
	<td>
			<textarea rows="6" name="msg" id="txtMsg" cols="60" onkeydown="checkSubmit(event);"></textarea>
        </td>
		<td align="left">
			<a href="javascript:onClick=sendMessage();"><img src="<%=resources.getIcon("ommunicationUser.chatroomSendMessage")%>" alt="<%=resources.getString("GML.validate")%>" title="<%=resources.getString("GML.validate")%>" border="0"></a>
		</td>
		<td width="100%">&nbsp;</td>
	</tr>
</table>
</form>
</view:window>
</body>
</html>