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
<%@ page isELIgnored="false"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<c:set var="sessionController" value="${requestScope.SILVERMAIL}" />
<c:set var="from" value="${param.from}" />
<fmt:setLocale value="${sessionScope[sessionController].language}" />
<view:setBundle bundle="${requestScope.resources.multilangBundle}" />
<view:setBundle bundle="${requestScope.resources.iconsBundle}" var="icons" />
<c:set var="notif" value="${requestScope.SentNotification}"/>
<%
      response.setHeader("Cache-Control", "no-store"); //HTTP 1.1
      response.setHeader("Pragma", "no-cache"); //HTTP 1.0
      response.setDateHeader("Expires", -1); //prevents caching at the proxy server
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html>
<head>
<title><fmt:message key="GML.popupTitle"/></title>
<view:looknfeel/>
    <script type="text/javascript" >
      function deleteMessage( notifId )
      {
        window.opener.deleteMessage(notifId, true);
        window.close();
      }

      <c:if test="${!empty notif.link}">
      function goTo()
      {
        window.opener.location="<c:url value="${notif.link}"/>";
        window.close();
      }
      </c:if>

      function closeWindow()
      {
      <c:choose>
        <c:when test="${'homePage' eq from}">window.opener.location.reload();</c:when>
        <c:otherwise>window.opener.location="SentUserNotifications.jsp";</c:otherwise>
      </c:choose>
          window.close();
        }
    </script>
  </head>
  <body>
    <fmt:message key="silverMail" var="browseLabel" />
    <view:browseBar clickable="false">
      <view:browseBarElt link="#" label="${browseLabel}" />
      <view:browseBarElt link="#" label="${notif.title}" />
    </view:browseBar>
    <view:window popup="true">
    <div class="popup-read-notification">
    <div class="entete">
      <div class="from"><span class="label">&nbsp;</span></div>
        <div class="date"><view:formatDateTime value="${notif.notifDate}" /></div>
      </div>
      <div class="source">
        <span class="label"><fmt:message key="source" /> :</span> <c:out value="${notif.source}" /> </div>
      <c:if test="${!empty notif.link}">
        <div class="link"> <a href="javaScript:goTo();"><fmt:message key="silvermail.link.text" /> </a> </div>
      </c:if>
      <div class="content-notification">
        ${notif.body}
      </div>
       <view:buttonPane>
         <fmt:message var="deleteLabel" key="delete" />
         <c:set var="deleteAction">javascript:onclick=deleteMessage(<c:out value="${notif.notifId}"/>);</c:set>
       <view:button label="${deleteLabel}" action="${deleteAction}"/>
         <fmt:message var="closeLabel" key="close" />
         <view:button label="${closeLabel}" action="javascript:onclick=closeWindow();"/>
       </view:buttonPane>
    </div>
    </view:window>
  </body>
</html>