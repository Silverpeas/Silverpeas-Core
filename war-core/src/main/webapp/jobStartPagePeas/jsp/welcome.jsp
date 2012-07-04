<%--

    Copyright (C) 2000 - 2012 Silverpeas

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    As a special exception to the terms and conditions of version 3.0 of
    the GPL, you may redistribute this Program in connection with Free/Libre
    Open Source Software ("FLOSS") applications as described in Silverpeas's
    FLOSS exception.  You should have received a copy of the text describing
    the FLOSS exception, and it is also available here:
    "http://www.silverpeas.org/legal/licensing"

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

--%>

<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%@ include file="check.jsp" %>
<%@page import="com.silverpeas.jobStartPagePeas.JobStartPagePeasSettings"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>

<%-- Set resource bundle --%>
<fmt:setLocale value="${sessionScope['SilverSessionController'].favoriteLanguage}" />
<view:setBundle basename="com.silverpeas.jobStartPagePeas.multilang.jobStartPagePeasBundle"/>

<c:set var="isUserAdmin" value="${requestScope['isUserAdmin']}" />
<c:set var="globalMode" value="${requestScope['globalMode']}" />
<c:set var="isBackupEnable" value="${requestScope['IsBackupEnable']}" />
<c:set var="isBasketEnable" value="${requestScope['IsBasketEnable']}" />
<c:set var="clipboardNotEmpty" value="${requestScope['ObjectsSelectedInClipboard']}" />

<c:set var="content" value="${requestScope['Content']}" />

<html>
<head>
<title><%=resource.getString("GML.popupTitle")%></title>
<view:looknfeel/>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/animation.js"></script>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/checkForm.js"></script>
<script type="text/javascript">
<!--
function openPopup() {
	windowName = "actionWindow";
	windowParams = "directories=0,menubar=0,toolbar=0,alwaysRaised,scrollbars,resizable";
	actionWindow = SP_openWindow("CreateSpace", windowName, 750, 400, windowParams, false);
}
function clipboardPaste() {
	$.progressMessage();
	location.href="paste";
}

function recoverRights() {
	$.progressMessage();
	location.href = "RecoverSpaceRights";
}
-->
</script>
</head>
<body>
<c:if test="${isUserAdmin}">
<view:operationPane>
	<fmt:message var="spaceAdd" key="JSPP.SpacePanelCreateTitle" />
    <view:operation altText="${spaceAdd}" icon="" action="javascript:onClick=openPopup();"></view:operation>
    
    <c:if test="${clipboardNotEmpty}">
    	<fmt:message var="paste" key="GML.paste" />
    	<view:operation altText="${paste}" icon="" action="javascript:onClick=clipboardPaste();"></view:operation>
    </c:if>
    
    <c:choose>
		<c:when test="${globalMode}">
			<fmt:message var="maintenance" key="JSPP.maintenanceModeToOff" />
			<view:operationSeparator/>
    		<view:operation altText="${maintenance}" icon="" action="DesactivateMaintenance?allIntranet=1"></view:operation>
		</c:when>
		<c:otherwise>
			<fmt:message var="maintenance" key="JSPP.maintenanceModeToOn" />
			<view:operationSeparator/>
    		<view:operation altText="${maintenance}" icon="" action="ActivateMaintenance?allIntranet=1"></view:operation>
		</c:otherwise>
	</c:choose>
	
	<c:if test="${JobStartPagePeasSettings.recoverRightsEnable}">
		<view:operationSeparator/>
    	<fmt:message var="recover" key="JSPP.spaceRecover" />
    	<view:operation altText="${recover}" icon="" action="javascript:onClick=recoverRights();"></view:operation>
    </c:if>
    
    <c:if test="${isBasketEnable}">
		<view:operationSeparator/>
    	<fmt:message var="bin" key="JSPP.Bin" />
    	<view:operation altText="${bin}" icon="" action="ViewBin"></view:operation>
    </c:if>
</view:operationPane>
</c:if>
<view:window>
<view:frame>
<view:board>
<div id="spaces-welcome-message">
<c:choose>
  <c:when test="${globalMode}">
    <font color="#ff0000"><fmt:message key="JSPP.maintenanceTout" /></font>
  </c:when>
  <c:otherwise>
    <c:out value="${content}" escapeXml="false" />
  </c:otherwise>
</c:choose>
</div>
</view:board>
</view:frame>
</view:window>
<view:progressMessage/>
</body>
</html>