<%--

    Copyright (C) 2000 - 2021 Silverpeas

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    As a special exception to the terms and conditions of version 3.0 of
    the GPL, you may redistribute this Program in connection with Free/Libre
    Open Source Software ("FLOSS") applications as described in Silverpeas's
    FLOSS exception.  You should have received a copy of the text describing
    the FLOSS exception, and it is also available here:
    "https://www.silverpeas.org/legal/floss_exception.html"

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

--%>
<%@ page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ page isELIgnored="false" %>
<%@ include file="graphicBox.jsp" %>
<%@ include file="checkSilvermail.jsp" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.silverpeas.com/tld/silverFunctions" prefix="silfn" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>
<c:set var="componentId" value="${requestScope.componentId}"/>
<c:set var="sessionController" value="${requestScope.SILVERMAIL}"/>
<c:set var="from" value="${param.from}"/>
<c:set var="msg" value="${sessionController.currentMessage}"/>
<fmt:setLocale value="${sessionScope[sessionController].language}"/>
<view:setBundle bundle="${requestScope.resources.multilangBundle}"/>
<view:setBundle bundle="${requestScope.resources.iconsBundle}" var="icons"/>

<c:set var="senderName" value="${silfn:defaultEmptyString(msg.senderName)}"/>
<c:set var="msgSource" value="${(msg.source eq msg.senderName) ? '' : msg.source}"/>

<%
  response.setHeader("Cache-Control", "no-store"); //HTTP 1.1
  response.setHeader("Pragma", "no-cache"); //HTTP 1.0
  response.setDateHeader("Expires", -1); //prevents caching at the proxy server
%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
  <title></title>
  <view:looknfeel/>
  <script type="text/javascript">
    function deleteMessage(id) {
      var ajaxConfig = sp.ajaxRequest("DeleteMessage").byPostMethod();
      ajaxConfig.withParam("ID", id);
      silverpeasAjax(ajaxConfig).then(function() {
        window.close();
      });
    }

    <c:if test="${!empty msg.url}">
    <c:url value="${msg.url}" var="url"/>
    function goTo() {
      spWindow.loadLink('${silfn:escapeJs(url)}');
      window.close();
    }
    </c:if>
  </script>
</head>
<body>
<fmt:message key="silverMail" var="browseLabel"/>
<view:browseBar>
  <view:browseBarElt link="" label="${browseLabel}"/>
  <view:browseBarElt link="" label="${msg.subject}"/>
</view:browseBar>
<view:window popup="true">
  <div class="popup-read-notification">
    <div class="entete">
      <div class="from">
        <c:if test="${!empty senderName}">
          <span class="label"><fmt:message key="from"/> : </span>
        </c:if>
        <c:out value="${senderName}"/>${silfn:isDefined(senderName) ? '' : '&#160;'}
      </div>
      <div class="date"><view:formatDateTime value="${msg.date}"/></div>
    </div>
    <c:if test="${!empty msgSource}">
      <div class="source"><span class="label"><fmt:message key="source"/> :</span>
        <c:out value="${msgSource}"/></div>
    </c:if>
    <c:if test="${!empty msg.url}">
      <div class="link"><a href="javaScript:goTo();"><fmt:message key="silvermail.link.text"/></a>
      </div>
    </c:if>
    <div class="content-notification">
        ${msg.body}
    </div>

    <view:buttonPane>
      <fmt:message var="closeLabel" key="close"/>
      <fmt:message var="deleteLabel" key="delete"/>
      <c:set var="deleteAction">javascript:onClick=deleteMessage(<c:out value="${sessionController.currentMessageId}"/>);</c:set>
      <view:button label="${deleteLabel}" action="${deleteAction}"/>
      <view:button label="${closeLabel}" action="javascript:onClick=window.close();"/>
    </view:buttonPane>
  </div>
</view:window>
</body>
</html>