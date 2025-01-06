<%--

    Copyright (C) 2000 - 2024 Silverpeas

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
    along with this program.  If not, see <https://www.gnu.org/licenses/>.

--%>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.silverpeas.com/tld/silverFunctions" prefix="silfn" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>

<%-- Set resource bundle --%>
<fmt:setLocale value="${requestScope.resources.language}"/>
<view:setBundle bundle="${requestScope.resources.multilangBundle}"/>

<%
response.setHeader("Cache-Control","no-store"); //HTTP 1.1
response.setHeader("Pragma","no-cache"); //HTTP 1.0
response.setDateHeader ("Expires",-1); //prevents caching at the proxy server
%>

<%@ page import="org.silverpeas.core.clipboard.ClipboardSelection"%>

<c:set var="clipboardScc" value="${requestScope.clipboardScc}"/>
<jsp:useBean id="clipboardScc"
             type="org.silverpeas.web.clipboard.control.ClipboardSessionController"/>
<!DOCTYPE>
<html lang="${requestScope.resources.language}">
<head>
    <title>Clipboard</title>
  <view:looknfeel/>
<script src="../../util/javaScript/formUtil.js"></script>
<script>
  function selectClipboardObject(index, field) {
    opener.top.IdleFrame.location.href = '../../Rclipboard/jsp/selectObject.jsp?Id=' + index +
        '&Status=' + field.checked;
  }

  function beforeClosing() {
    opener.top.ClipboardWindowClosed = true;
  }

  function clipboardClose() {
    beforeClosing();
    window.close();
  }

  function clipboardDoDelete() {
    document.pasteform.action = "../../Rclipboard/jsp/delete.jsp";
    document.pasteform.target = "_self";
    document.pasteform.submit();
  }

  function init() {
    opener.top.ClipboardWindow = window;
    opener.top.ClipboardWindowClosed = false;
  }

  function view(url) {
    opener.top.location.href = url;
    clipboardClose();
  }
</script>
</head>
<body onLoad="init();" onUnload="beforeClosing();">
<fmt:message var="tmp" key="clipboard"/>
<view:browseBar componentId="" path="${tmp}"/>
<view:window popup="true">
  <view:frame>
<form name="pasteform" action="" method="post" target="MyMain">
  <input type="hidden" name="compR" value="<%=clipboardScc.getComponentRooterName()%>">
  <input type="hidden" name="SpaceFrom" value="<%=clipboardScc.getSpaceId()%>">
  <input type="hidden" name="ComponentFrom" value="<%=clipboardScc.getComponentId()%>">
  <input type="hidden" name="JSPPage" value="<%=clipboardScc.getJSPPage()%>">
  <input type="hidden" name="TargetFrame" value="<%=clipboardScc.getTargetFrame()%>">
  <input type="hidden" name="message">
  <input type="hidden" name="temp">
    <div id="clipboard-items">
        <view:arrayPane var="clipboard" export="false">
            <view:arrayColumn title="${clipboardScc.getString('vide')}"/>
            <view:arrayColumn title="${clipboardScc.getString('titre')}"/>
            <view:arrayColumn title="${clipboardScc.getString('composant')}"/>
            <view:arrayColumn title="${clipboardScc.getString('vide')}"/>
            <c:set var="index" value="${0}"/>
            <view:arrayLines var="item" items="${clipboardScc.objects}">
                <jsp:useBean id="item"
                             type="org.silverpeas.core.clipboard.ClipboardSelection"/>
                <%
                    pageContext.setAttribute("data",
                            item.getTransferData(ClipboardSelection.SilverpeasKeyDataFlavor));
                %>
                <c:set var="data" value="${pageContext.getAttribute('data')}"/>
                <jsp:useBean id="data" type="org.silverpeas.core.clipboard.SilverpeasKeyData"/>
                <c:set var="link" value="${data.link}"/>
                <c:set var="type" value="${data.type}"/>
                <c:set var="icon" value="${requestScope.resources.getIcon(type.toLowerCase())}"/>
                <c:if test="${icon.equalsIgnoreCase(silfn:applicationURL())}">
                    <c:set var="icon" value="${requestScope.resources.getIcon('publication')}"/>
                </c:if>
                <c:set var="componentLabel" value=""/>
                <c:if test="${data.componentInstanceId != null}">
                    <c:set var="componentLabel"
                           value="${clipboardScc.getComponentLabel(data.componentInstanceId)}"/>
                </c:if>
                <c:set var="checked" value=""/>
                <c:if test="${item.selected}">
                    <c:set var="checked" value="checked"/>
                </c:if>
                <view:arrayLine>
                    <view:arrayCellText>
                        <img src="${icon}" alt="${type}"/>
                    </view:arrayCellText>
                    <view:arrayCellText>
                        <c:choose>
                            <c:when test="${silfn:isDefined(link)}">
                                <button class="link" type="button" onclick="view('${link}');">
                                        ${silfn:escapeHtml(data.title)}
                                </button>
                            </c:when>
                            <c:otherwise>
                                ${silfn:escapeHtml(data.title)}
                            </c:otherwise>
                        </c:choose>
                    </view:arrayCellText>
                    <view:arrayCellText text="${componentLabel}"/>
                    <view:arrayCellText>
                        <input type=checkbox ${checked}
                               name='clipboardId${index}'
                               value=''
                               onchange='selectClipboardObject(${index}, this)'/>
                    </view:arrayCellText>
                </view:arrayLine>
                <c:set var="index" value="${index + 1}"/>
            </view:arrayLines>
        </view:arrayPane>
    </div>
    <div id="clipboard-actions" class="center" style="padding-top: 1em;">
        <view:buttonPane>
            <view:button label="${clipboardScc.getString('reset')}"
                         action="javascript:onClick=clipboardDoDelete()"/>
            <view:button label="${clipboardScc.getString('fermer')}"
                         action="javascript:onClick=clipboardClose()"/>
        </view:buttonPane>
    </div>
</form>
    </view:frame>
</view:window>
</body>
</html>