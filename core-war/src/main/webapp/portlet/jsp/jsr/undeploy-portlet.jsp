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

<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.buttons.Button "%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>

<%@ include file="header.jsp"%>

<!--Load the resource bundle for the page -->
<view:setBundle basename="org.silverpeas.portlets.multilang.portletsBundle" />

<c:set value="${sessionScope['com.sun.portal.portletcontainer.driver.admin.portletApplications']}" var="list"/>
<c:if test="${list != null}">

	<view:board>

	<c:set value="${sessionScope['com.sun.portal.portletcontainer.driver.admin.undeploymentFailed']}" var="msgFail" />
	<c:if test="${msgFail != null}" >
	    <h2 id="undeploy-failed"><c:out value="${sessionScope['com.sun.portal.portletcontainer.driver.admin.undeploymentFailed']}" escapeXml="false"/></h2>
	</c:if>

	<c:set value="${sessionScope['com.sun.portal.portletcontainer.driver.admin.undeploymentSucceeded']}" var="msgSuccess" />
	<c:if test="${msgSuccess != null}" >
	    <h2 id="undeploy-success"><c:out value="${sessionScope['com.sun.portal.portletcontainer.driver.admin.undeploymentSucceeded']}" escapeXml="false"/></h2>
	</c:if>

	<form id="undeploy-portlet" name="undeployForm" action="<%=DriverUtil.getDeployerURL(request)%>" method="post">
		<table>
			<tr><td class="txtlibform"><fmt:message key="portlets.admin.selectPortlets"/> :</td>
				<td>
					<select id="undeploy-portlets" name="<%=AdminConstants.PORTLETS_TO_UNDEPLOY%>" size="1">
	                        <c:if test="${list != null}">
	                            <c:forEach items="${list}" var="portlet">
									<c:if test="${portlet != 'silverpeas'}">
						<option><c:out value="${portlet}"/></option>
									</c:if>
	                            </c:forEach>
	                        </c:if>
	                </select><input type="hidden" name="<%=AdminConstants.UNDEPLOY_PORTLET_SUBMIT%>" value="true"/>
				</td>
			</tr>
		</table>
	</form>
	</view:board>
	<%
	Button removeButton 	= (Button) gef.getFormButton(message.getString("portlets.admin.undeploy"), "javascript:document.undeployForm.submit();", false);

	ButtonPane buttonPane = gef.getButtonPane();
	buttonPane.addButton(removeButton);
	%>
	<br/><center><%=buttonPane.print()%></center><br/>
</c:if>