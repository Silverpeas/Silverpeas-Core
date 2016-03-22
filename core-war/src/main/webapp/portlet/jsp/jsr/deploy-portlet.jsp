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

<%@page import="org.silverpeas.core.web.util.viewgenerator.html.buttons.Button" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>

<%@ include file="header.jsp"%>

<!--Load the resource bundle for the page -->
<view:setBundle basename="org.silverpeas.portlets.multilang.portletsBundle" />

<script language="JavaScript">
function addRolesTextbox(checkObj)
{
  var rolesDivObj = document.getElementById("roles");
  if(checkObj.checked) {
     rolesDivObj.style.display = "block";
  } else {
     rolesDivObj.style.display = "none";
  }
}
</script>
<noscript>Enable Javascript</noscript>

<view:board>

<c:set value="${sessionScope['com.sun.portal.portletcontainer.driver.admin.deploymentFailed']}" var="msgFail" />
<c:if test="${msgFail != null}" >
  <h2 id="deploy-failed"><c:out value="${sessionScope['com.sun.portal.portletcontainer.driver.admin.deploymentFailed']}" escapeXml="false"/></h2>
</c:if>

<c:set value="${sessionScope['com.sun.portal.portletcontainer.driver.admin.deploymentSucceeded']}" var="msgSuccess" />
<c:if test="${msgSuccess != null}" >
  <h2 id="deploy-success"><c:out value="${sessionScope['com.sun.portal.portletcontainer.driver.admin.deploymentSucceeded']}" escapeXml="false"/></h2>
</c:if>

<form id="deploy-portlet" METHOD="POST" name="deployForm" enctype="multipart/form-data"  action="portletUploader" >
	<table>
	<tr><td class="txtlibform"><fmt:message key="portlets.admin.selectWar"/> :</td><td><input id="filename" type="file" name="filename" size="50" /></td></tr>
	<!-- <tr><td class="txtlibform"><fmt:message key="portlets.admin.addRoles"/> :</td>
		<td><input id="rolescheck" type="checkbox" name="addRolesCheck" onClick="addRolesTextbox(this)" onKeyPress="addRolesTextbox(this)"/>
		<div id="roles" style="display:none">
		<label for="rolefilename"><fmt:message key="portlets.admin.selectRoles"/></label>
		<input id="rolefilename" type="file" name="rolefilename"  size="50" />
    </div></td></tr> -->
	</table>
</form>

</view:board>
<%
Button validateButton 	= (Button) gef.getFormButton(message.getString("portlets.admin.deploy"), "javascript:document.deployForm.submit();", false);

ButtonPane buttonPane = gef.getButtonPane();
buttonPane.addButton(validateButton);
%>
<br/><center><%=buttonPane.print()%></center><br/>