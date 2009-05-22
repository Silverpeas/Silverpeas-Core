<%--
  CDDL HEADER START
  The contents of this file are subject to the terms
  of the Common Development and Distribution License
  (the License). You may not use this file except in
  compliance with the License.

  You can obtain a copy of the License at
  http://www.sun.com/cddl/cddl.html and legal/CDDLv1.0.txt
  See the License for the specific language governing
  permission and limitations under the License.

  When distributing Covered Code, include this CDDL
  Header Notice in each file and include the License file
  at legal/CDDLv1.0.txt.
  If applicable, add the following below the CDDL Header,
  with the fields enclosed by brackets [] replaced by
  your own identifying information:
  "Portions Copyrighted [year] [name of copyright owner]"

  Copyright 2006 Sun Microsystems Inc. All Rights Reserved
  CDDL HEADER END
--%>

<%@page contentType="text/html"%>
<%@page import="com.silverpeas.portlets.portal.DriverUtil, 
                com.sun.portal.portletcontainer.driver.admin.AdminConstants" %>
<%@page import="com.sun.portal.portletcontainer.invoker.WindowInvokerConstants"%>

<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.GraphicElementFactory "%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.buttons.Button"%>

<%@ taglib uri="/WEB-INF/c.tld" prefix="c"%>
<%@ taglib uri="/WEB-INF/fmt.tld" prefix="fmt"%>
<%@ taglib uri="/WEB-INF/viewGenerator.tld" prefix="view"%>

<%@ include file="header.jsp"%>

<!--Load the resource bundle for the page -->
<fmt:setBundle basename="com.silverpeas.portlets.multilang.portletsBundle" />

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



<form id="create-portlet" name="createForm" method="post" action="<%=DriverUtil.getAdminURL(request)%>" >
   	<c:set var="list" value="${sessionScope['com.sun.portal.portletcontainer.driver.admin.portlets']}" />
	<table cellpadding="5">
		<tr>
			<td class="txtlibform"><fmt:message key="portlets.selectBasePortlet"/> :</td>
			<td>
				<select id="portletList" name="<%=AdminConstants.PORTLET_LIST%>">
		            <c:forEach items="${list}" var="portlet">
		                <option value="<c:out value="${portlet.name}" />" >
		                        <c:out value="${portlet.label}" />
		                </option>
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
<%
Button cancelButton		= (Button) gef.getFormButton("Annuler", "javascript:window.close();", false);
Button validateButton 	= (Button) gef.getFormButton(message.getString("portlets.createPortletWindow"), "javascript:document.createForm.submit();", false);

ButtonPane buttonPane = gef.getButtonPane();
buttonPane.addButton(validateButton);
buttonPane.addButton(cancelButton);
%>
<br/><center><%=buttonPane.print()%></center><br/>