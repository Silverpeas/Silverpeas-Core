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

<%@page import="org.silverpeas.web.portlets.portal.DriverUtil,
                com.sun.portal.portletcontainer.driver.admin.AdminConstants" %>
<%@page import="com.sun.portal.portletcontainer.invoker.WindowInvokerConstants"%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>

<%@ include file="header.jsp"%>

<!--Load the resource bundle for the page -->
<view:setBundle basename="org.silverpeas.portlets.multilang.portletsBundle" />

<view:board>

<c:set value="${sessionScope['com.silverpeas.portletcontainer.driver.admin.silverpeasElementId']}" var="silverpeasElementId" />
<c:set value="${sessionScope['com.silverpeas.portletcontainer.driver.admin.silverpeasUserId']}" var="silverpeasUserId" />
<c:set value="${sessionScope['com.silverpeas.portletcontainer.driver.admin.silverpeasSpaceId']}" var="silverpeasSpaceId" />

<c:set value="${sessionScope['com.sun.portal.portletcontainer.driver.admin.creationFailed']}" var="msgFail" />
<c:if test="${msgFail != null}" >
    <h2 id="create-failed"><c:out value="${msgFail}" escapeXml="false"/></h2>
</c:if>

<c:set value="${sessionScope['com.sun.portal.portletcontainer.driver.admin.creationSucceeded']}" var="msgSuccess" />
<c:if test="${msgSuccess != null}" >
	<script>
	<c:choose>
	<c:when test="${silverpeasSpaceId!=null}">
		window.opener.location.href="<%=m_context%>/dt?<%=WindowInvokerConstants.DRIVER_SPACEID%>=<c:out value="${silverpeasSpaceId}" />&<%=WindowInvokerConstants.DRIVER_ROLE%>=Admin";
	</c:when>
	<c:otherwise>
		window.opener.location.href="<%=m_context%>/dt";
	</c:otherwise>
	</c:choose>
		window.close();
	</script>
    <h2 id="create-success"><c:out value="${msgSuccess}" escapeXml="false"/></h2>
</c:if>

<script type="text/javascript">
function selectPortlet() {
	$('#title').val($('#portletList option:selected').text());
}

$(document).ready(function() {
	selectPortlet();
});
</script>

<form id="create-portlet" name="createForm" method="post" action="<%=DriverUtil.getAdminURL(request)%>">
	<c:set var="list" value="${sessionScope['com.sun.portal.portletcontainer.driver.admin.portlets']}" />
	<table cellpadding="5">
		<tr>
			<td class="txtlibform"><fmt:message key="portlets.selectBasePortlet"/> :</td>
			<td>
				<select id="portletList" name="<%=AdminConstants.PORTLET_LIST%>" onchange="selectPortlet()">
		            <c:forEach items="${list}" var="portlet">
		                <option value="<c:out value="${portlet.name}" />"><c:out value="${portlet.label}" /></option>
		            </c:forEach>
		        </select>
				<input type="hidden" name="<%=AdminConstants.CREATE_PORTLET_WINDOW_SUBMIT%>" value="1"/>
				<input type="hidden" name="<%=WindowInvokerConstants.DRIVER_SPACEID%>" value="<c:out value="${silverpeasSpaceId}"/>"/>
			</td>
		</tr>
		<!-- <tr>
			<td class="txtlibform"><fmt:message key="portlets.portletWindow"/> :</td>
			<td><input id="portletWindowName" type="text" size="40" maxlength="20" name="<%=AdminConstants.PORTLET_WINDOW_NAME%>" value="" /></td>
		</tr> -->
		<tr>
			<td class="txtlibform"><fmt:message key="portlets.portletTitle"/> :</td>
			<td><input id="title" type="text" size="40" name="<%=AdminConstants.PORTLET_WINDOW_TITLE%>" value="" maxlength="50" /></td>
		</tr>
	</table>
</form>
</view:board>
<br/>
<view:buttonPane>
  <fmt:message var="createLabel" key="portlets.createPortletWindow"/>
  <view:button label="${createLabel}" action="javascript:document.createForm.submit();"/>
  <fmt:message var="cancelLabel" key="GML.cancel"/>
  <view:button label="${cancelLabel}" action="javascript:window.close();"/>
</view:buttonPane>