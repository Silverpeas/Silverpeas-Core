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
<%@ include file="checkPopup.jsp" %>
<%@ page import="com.stratelia.silverpeas.notificationserver.channel.popup.POPUPMessage"%>

<%
      String action = (String) request.getAttribute("action");
      if ("Close".equals(action)) {
%>
<html>
  <body onload="javascript:window.close();">
  </body>
</html>
<%    } else {
      POPUPMessage msg = popupScc.getMessage(popupScc.getCurrentMessageId());
      String senderId = msg.getSenderId();
      boolean answerAllowed = msg.isAnswerAllowed();
%>

<html>
  <head>
    <title><%=resource.getString("GML.popupTitle")%></title>
    <%
          out.println(gef.getLookStyleSheet());
    %>

    <script type="text/javascript" src="<%=m_context%>/util/javaScript/animation.js"></script>
    <script type="text/javascript">
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

  </head>
  <body marginwidth=5 marginheight=5 leftmargin=5 topmargin=5>
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
          <% if (answerAllowed) {%>
          <tr>
            <td>&nbsp;</td>
            <td align=left valign="baseline">
              <span class="txtlibform"><%=popupScc.getString("messageFrom")%>
                <%=msg.getSenderName()%>&nbsp;</span>
          				-&nbsp;<%=DateUtil.getOutputDate(msg.getDate(), popupScc.getLanguage())%>
              <%=popupScc.getString("messageAt")%>&nbsp;<%=msg.getTime()%>
            </td>
          </tr>
          <% }%>
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
          <% if (answerAllowed) {%>
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
          <% }%>
        </form>
      </table>
    </center>
    <%
          out.println(board.printAfter());
          ButtonPane buttonPane = gef.getButtonPane();
          if (answerAllowed) {
            buttonPane.addButton((Button) gef.getFormButton(popupScc.getString("send"), "javascript:onClick=answerMessage();", false));
          }
          buttonPane.addButton((Button) gef.getFormButton(popupScc.getString("close"), "javascript:onClick=closeWindow();", false));
      out.println("<br/><center>" + buttonPane.print() + "</center>");%>
    <%
          out.println(frame.printAfter());
          out.println(window.printAfter());
    %>
  </BODY>
</HTML>
<% if (answerAllowed) {%>
<script type="text/javascript" language="javascript">
  document.popupForm.messageAux.focus();
</script>
<% }%>
<%
      }
%>