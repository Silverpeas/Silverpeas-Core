<%@page contentType="text/html"%>
<%@page import="com.sun.portal.portletcontainer.driver.admin.AdminConstants" %>

<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.GraphicElementFactory "%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.buttons.Button"%>

<%@ taglib uri="/WEB-INF/c.tld" prefix="c"%>
<%@ taglib uri="/WEB-INF/fmt.tld" prefix="fmt"%>
<%@ taglib uri="/WEB-INF/viewGenerator.tld" prefix="view"%>

<%@ include file="header.jsp"%>

<!--Load the resource bundle for the page -->
<fmt:setBundle basename="com.silverpeas.portlets.multilang.portletsBundle" />

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