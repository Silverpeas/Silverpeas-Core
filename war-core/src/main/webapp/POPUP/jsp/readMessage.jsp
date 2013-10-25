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

<%@ page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.silverpeas.com/tld/silverFunctions" prefix="silfn" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>

<%-- Set resource bundle --%>
<c:set var="language" value="${requestScope.resources.language}"/>
<fmt:setLocale value="${language}"/>
<view:setBundle bundle="${requestScope.resources.multilangBundle}"/>

<%
  response.setHeader("Cache-Control", "no-store"); //HTTP 1.1
  response.setHeader("Pragma", "no-cache"); //HTTP 1.0
  response.setDateHeader("Expires", -1); //prevents caching at the proxy server
%>
<%@ include file="checkPopup.jsp" %>

<c:choose>
  <c:when test="${requestScope.action == 'Close'}">
    <html>
    <body onload="window.close();">
    </body>
    </html>
  </c:when>
  <c:otherwise>
    <c:set var="popupMsg" value="<%=popupScc.getMessage(popupScc.getCurrentMessageId())%>"/>
    <c:set var="popupMsgId" value="${popupMsg.id}"/>
    <c:set var="popupMsgDate" value="${popupMsg.date}"/>
    <c:set var="popupMsgTime" value="${popupMsg.time}"/>
    <c:set var="popupMsgBody" value="${popupMsg.body}"/>
    <c:set var="senderId" value="${popupMsg.senderId}"/>
    <c:set var="senderName" value="${popupMsg.senderName}"/>
    <c:set var="answerAllowed" value="${popupMsg.answerAllowed}"/>

    <html>
    <head>
      <title><fmt:message key="GML.popupTitle"/></title>
      <view:looknfeel/>

      <script type="text/javascript" src="<%=m_context%>/util/javaScript/animation.js"></script>
      <script type="text/javascript">
        window.opener.location =
            "../../Rclipboard/jsp/Idle.jsp?message=DELMSG&messageTYPE=POPUP&messageID=${popupMsgId}";
        function closeWindow() {
          window.close();
        }

        function answerMessage() {
          document.popupForm.submit();
        }

        $(document).ready(function() {
          $('#messageAux').focus();
        });
      </script>

    </head>
    <body>
    <view:browseBar path="<fmt:message key='message' />"/>
    <view:window>
      <view:frame>
        <view:board>

          <form name="popupForm" Action="ToAlert" Method="POST">
            <div style="text-align: center;">
              <table border="0" cellspacing="0" cellpadding="0" width="100%">
                <input type="hidden" name="theUserId" value="${senderId}">
                <c:if test="${answerAllowed}">
                  <tr>
                    <td>&nbsp;</td>
                    <td align=left valign="baseline">
              <span class="txtlibform"><fmt:message key="messageFrom"/>
                  ${senderName}</span><span> - ${silfn:formatStringDate(popupMsgDate, language)}
              <fmt:message key="messageAt"/> ${popupMsgTime}</span>
                    </td>
                  </tr>
                </c:if>
                <tr>
                  <td>&nbsp;</td>
                  <td align=left valign="baseline">
                    <table class="">
                      <tr>
                        <td>${popupMsgBody}</td>
                      </tr>
                    </table>
                  </td>
                </tr>
                <c:if test="${answerAllowed}">
                  <tr>
                    <td>&nbsp;</td>
                  </tr>
                  <tr>
                    <td>&nbsp;</td>
                    <td align=left valign="baseline" class="txtlibform" style="font-weight: bold;">
                      <fmt:message key="answer"/> :
                    </td>
                  </tr>
                  <tr>
                    <td>&nbsp;</td>
                    <td align=left valign="baseline">
                      <textarea id="messageAux" rows="5" cols="80" name="messageAux"></textarea>
                    </td>
                  </tr>
                </c:if>
              </table>
            </div>
          </form>
        </view:board>
        <view:buttonPane>
          <c:if test="${answerAllowed}">
            <fmt:message key='send' var="label"/>
            <view:button label="${label}" action="javascript:onClick=answerMessage();" disabled="false"/>
          </c:if>
          <fmt:message key='close' var="label"/>
          <view:button label="${label}" action="javascript:onClick=closeWindow();" disabled="false"/>
        </view:buttonPane>
      </view:frame>
    </view:window>
    </BODY>
    </HTML>
  </c:otherwise>
</c:choose>