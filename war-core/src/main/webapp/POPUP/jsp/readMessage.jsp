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
    <c:set var="popupMsgUrl" value="${popupMsg.url}"/>
    <c:set var="senderId" value="${popupMsg.senderId}"/>
    <c:set var="senderName" value="${silfn:defaultEmptyString(popupMsg.senderName)}"/>
    <c:set var="popupMsgSource" value="${(popupMsg.source eq popupMsg.senderName) ? '' : popupMsg.source}"/>
    <c:set var="answerAllowed" value="${popupMsg.answerAllowed}"/>

    <!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
	<html xmlns="http://www.w3.org/1999/xhtml">
    <head>
      <title><fmt:message key="GML.popupTitle"/></title>
      <view:looknfeel/>
      <script type="text/javascript">
        var actionWindow = window.opener.top;
        window.opener.location =
            "../../Rclipboard/jsp/Idle.jsp?message=DELMSG&messageTYPE=POPUP&messageID=${popupMsgId}";
        function closeWindow() {
          window.close();
        }

        <c:if test="${!empty popupMsgUrl}">
        function goTo() {
          actionWindow.location = "<c:url value="${popupMsgUrl}" />";
          window.close();
        }
        </c:if>

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
    <view:window popup="true" browseBarVisible="false">
    <div class="popup-read-notification">
    <div class="entete">
      <div class="from">
        <c:if test="${!empty senderName}">
        <span class="label"><fmt:message key="from" /> : </span>
        </c:if>
        <c:out value="${senderName}" />${silfn:isDefined(senderName) ? '' : '&#160;'}
      </div>
      <div class="date">${silfn:formatStringDate(popupMsgDate, language)} ${popupMsgTime}</div>
      </div>
      <c:if test="${!empty popupMsgSource}">
        <div class="source"> <span class="label"><fmt:message key="source" /> :</span> <c:out value="${popupMsgSource}" /> </div>
      </c:if>
      <c:if test="${!empty popupMsgUrl}">
        <div class="link"> <a href="javaScript:goTo();"><fmt:message key="popup.link.text" /></a> </div>
      </c:if>
      <div class="content-notification">
        ${popupMsgBody}
      </div>
     </div>
    </view:window>
    </body>
    </html>
  </c:otherwise>
</c:choose>