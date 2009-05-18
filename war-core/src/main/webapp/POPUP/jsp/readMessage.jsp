<%
response.setHeader("Cache-Control","no-store"); //HTTP 1.1
response.setHeader("Pragma","no-cache"); //HTTP 1.0
response.setDateHeader ("Expires",-1); //prevents caching at the proxy server
%>
<%@ include file="checkPopup.jsp" %>
<%@ page import="com.stratelia.silverpeas.notificationserver.channel.popup.POPUPMessage"%>

<%
    String action = (String)request.getAttribute("action");
    if ("Close".equals(action))
    {
%>
<HTML>
<BODY onload="javascript:window.close();">
</BODY>
</HTML>
<%
    }
    else
    {
								POPUPMessage msg = popupScc.getMessage(popupScc.getCurrentMessageId());
								String senderId = msg.getSenderId();
								boolean answerAllowed = msg.isAnswerAllowed();
%>

<HTML>
<HEAD>
<TITLE>___/ Silverpeas - Corporate Portal Organizer \________________________________________________________________________</TITLE>
<%
  out.println(gef.getLookStyleSheet());
%>

<script type="text/javascript" src="<%=m_context%>/util/javaScript/animation.js"></script>
<script>
				var messageId = <%=msg.getId()%>;
				window.opener.location = "../../Rclipboard/jsp/Idle.jsp?message=DELMSG&messageTYPE=POPUP&messageID="+messageId;
				
				function closeWindow()
				{
					window.close();
				}

				function answerMessage()
				{
								window.opener.location = "../../Rclipboard/jsp/Idle.jsp?message=DELMSG&messageTYPE=POPUP&messageID="+messageId;
								document.popupForm.submit();
				}
</script>

</HEAD>
<BODY marginwidth=5 marginheight=5 leftmargin=5 topmargin=5>
<%
    BrowseBar browseBar = window.getBrowseBar();
    browseBar.setComponentName(popupScc.getString("Popup"));
    browseBar.setPath(popupScc.getString("message")); 
				out.println(window.printBefore());
				out.println(frame.printBefore());
				out.println(board.printBefore());
%>

<center>
				<table border="0" cellspacing="0" cellpadding="0" width="100%">
						<form name="popupForm" Action="ToAlert" Method="POST">
       <input type="hidden" name="theUserId" value="<%=senderId%>">
       <% if (answerAllowed) { %>
        <tr>
          <td>&nbsp;</td>
          <td align=left valign="baseline">
          				<span class="txtlibform"><%=popupScc.getString("messageFrom")%>
          				<%=msg.getSenderName()%>&nbsp;</span>
          				-&nbsp;<%=DateUtil.getOutputDate(msg.getDate(), popupScc.getLanguage())%>
          				<%=popupScc.getString("messageAt")%>&nbsp;<%=msg.getTime()%>
          </td>
        </tr>
        <% } %>
        <tr>
          <td>&nbsp;</td>
          <td align=left valign="baseline">
				          <table class="">
				          				<tr>
								          				<td><%=Encode.javaStringToHtmlParagraphe(msg.getBody())%></td>
				          				</tr>
								      </table>
				      </td>
        </tr>
        <% if (answerAllowed) { %>
        <tr><td>&nbsp;</td></tr>
        <tr>
          <td>&nbsp;</td>
          <td align=left valign="baseline" class="txtlibform"><b><%=popupScc.getString("answer")%> :</b></td>
        </tr>
        <tr>
          <td>&nbsp;</td>
          <td align=left valign="baseline">
          				<textarea rows="5" cols="80" name="messageAux"></textarea>
          </td>
        </tr>
        <% } %>
				</form>
		</table>
</center>
<%
    out.println(board.printAfter());
    ButtonPane buttonPane = gef.getButtonPane();
    if (answerAllowed)
				    buttonPane.addButton((Button) gef.getFormButton(popupScc.getString("send"), "javascript:onClick=answerMessage();", false));
    buttonPane.addButton((Button) gef.getFormButton(popupScc.getString("close"), "javascript:onClick=closeWindow();", false));
				out.println("<BR><center>"+buttonPane.print()+"</center>");%>
<%
    out.println(frame.printAfter());
    out.println(window.printAfter());
%>
</BODY>
</HTML>
<% if (answerAllowed) { %>
				<script language="javascript">
								document.popupForm.messageAux.focus();
				</script>
<% } %>
<%
}
%>