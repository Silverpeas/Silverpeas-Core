<%@page contentType="text/html"%>
<%@page import="com.silverpeas.portlets.portal.DriverUtil,
                com.sun.portal.portletcontainer.driver.admin.AdminConstants" %>

<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.GraphicElementFactory "%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.buttons.Button"%>

<%@ taglib uri="/WEB-INF/c.tld" prefix="c"%>
<%@ taglib uri="/WEB-INF/fmt.tld" prefix="fmt"%>
<%@ taglib uri="/WEB-INF/viewGenerator.tld" prefix="view"%>

<%@ include file="header.jsp"%>

<!--Load the resource bundle for the page -->
<fmt:setBundle basename="com.silverpeas.portlets.multilang.portletsBundle" />

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