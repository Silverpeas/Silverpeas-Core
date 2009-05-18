<%@ include file="checkCommunicationUser.jsp" %>

<%
	String userName = (String) request.getAttribute("UserName");
	String userIdDest = (String) request.getAttribute("UserIdDest");
	String userNameDest = (String) request.getAttribute("UserNameDest");

	operationPane.addOperation(resources.getIcon("communicationUser.chatroomClear"),resources.getString("clear"),"javascript:clear()");
	operationPane.addOperation(resources.getIcon("communicationUser.chatroomSave"),resources.getString("save"),"javascript:save()");
	operationPane.addOperation(resources.getIcon("communicationUser.chatroomLogout"),resources.getString("logout"),"javascript:logout()");
%>

<html>
<head>
<TITLE><%=resources.getString("currentDiscussion")+" <"+userName+" - "+userNameDest+">"%></TITLE>
<%
   out.println(gef.getLookStyleSheet());
%>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<script type="text/javascript" src="<%=m_context%>/util/ajax/prototype.js"></script>
<script type="text/javascript" src="<%=m_context%>/util/ajax/rico.js"></script>
<script type="text/javascript" src="<%=m_context%>/util/ajax/ricoAjax.js"></script>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/animation.js"></script>
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
setInterval("checkMessage();", 1000);

</script>

</head>
<body bgcolor="#FFFFFF" leftmargin="1" topmargin="1" marginwidth="1" marginheight="1" onLoad="init()">
<%
	out.println(window.printBefore());
%>
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
  			<textarea rows="6" name="msg" id="txtMsg" cols="60" onkeydown="javascript:return checkSubmit(event);"></textarea>
        </td>
		<td align="left">
			<a href="javascript:onClick=sendMessage();"><img src="<%=resources.getIcon("communicationUser.chatroomSendMessage")%>" alt="<%=resources.getString("GML.validate")%>" title="<%=resources.getString("GML.validate")%>" border="0"></a>
		</td>
		<td width="100%">&nbsp;</td>
	</tr>
</table>
</form>
<%
 out.println(window.printAfter());
%>
</body>
</html>