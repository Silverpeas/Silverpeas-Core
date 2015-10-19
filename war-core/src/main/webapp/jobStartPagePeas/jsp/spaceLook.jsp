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

<%@ include file="check.jsp" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>

<!-- Resource bundle configuration -->
<fmt:setLocale value="${sessionScope[sessionController].language}" />
<view:setBundle basename="org.silverpeas.jobStartPagePeas.multilang.jobStartPagePeasBundle"/>
<!-- Retrieve current space bean -->
<c:set var="curSpace" value="${requestScope.Space}" />
<c:set var="spacePositionOption" value="${requestScope.displaySpaceOption}" />

<c:set var="isAdmin" value="${requestScope.SpaceExtraInfos.admin}"/>
<c:set var="css" value="${requestScope.SpaceLookHelper.CSS}"/>
<c:set var="wallpaper" value="${requestScope.SpaceLookHelper.wallpaper}"/>

<%
SpaceInst		space				= (SpaceInst) request.getAttribute("Space");
SpaceLookHelper slh 				= (SpaceLookHelper) request.getAttribute("SpaceLookHelper");
boolean 		isInHeritanceEnable = ((Boolean)request.getAttribute("IsInheritanceEnable")).booleanValue();
DisplaySorted 	m_SpaceExtraInfos 	= (DisplaySorted)request.getAttribute("SpaceExtraInfos");
String 			spaceId				= (String) request.getAttribute("CurrentSpaceId");

List<String>	availableLooks		= gef.getAvailableLooks();
String			spaceLook			= space.getLook();

browseBar.setSpaceId(spaceId);
browseBar.setExtraInformation(resource.getString("JSPP.SpaceAppearance"));
%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title><%=resource.getString("GML.popupTitle")%></title>
<view:looknfeel/>
<script type="text/javascript">
function B_VALIDER_ONCLICK() {
	document.lookSpace.submit();
}
</script>
</head>
<body>
<%
out.println(window.printBefore());

TabbedPane tabbedPane = gef.getTabbedPane();
tabbedPane.addTab(resource.getString("GML.description"),"StartPageInfo", false);
tabbedPane.addTab(resource.getString("JSPP.SpaceAppearance"), "SpaceLook", true);

tabbedPane.addTab(resource.getString("JSPP.Manager"), "SpaceManager", false);

if (isInHeritanceEnable) {
    tabbedPane.addTab(resource.getString("JSPP.admin"), "SpaceManager?Role=admin", false);
    tabbedPane.addTab(resource.getString("JSPP.publisher"), "SpaceManager?Role=publisher", false);
    tabbedPane.addTab(resource.getString("JSPP.writer"), "SpaceManager?Role=writer", false);
    tabbedPane.addTab(resource.getString("JSPP.reader"), "SpaceManager?Role=reader", false);
}

out.println(tabbedPane.print());
%>
<view:frame>
<view:board>
	<form name="lookSpace" action="UpdateSpaceLook" method="post" enctype="multipart/form-data">
	<table border="0" cellspacing="0" cellpadding="5" width="100%">
		<% if (availableLooks.size() >= 2) { %>
		<tr>
			<td class="txtlibform"><%=resource.getString("JSPP.SpaceLook")%> :</td>
			<c:choose>
				<c:when test="${isAdmin}">
					<td>
						<select name="SelectedLook" size="1">
						<%
						if (StringUtil.isDefined(spaceLook)) {
							out.println("<option value=\"\"></option>");
						} else {
							out.println("<option value=\"\" selected></option>");
						}
				        for (String lookName : availableLooks) {
				            if (lookName.equals(spaceLook)) {
				              out.println("<option value=\""+lookName+"\" selected>"+lookName+"</option>");
				            } else {
				              out.println("<option value=\""+lookName+"\">"+lookName+"</option>");
				            }
				        } %>
				        </select>
					</td>
				</c:when>
				<c:otherwise>
					<td>
						<% if (StringUtil.isDefined(spaceLook)) { %>
								<%=spaceLook %>
						<% } %>
					</td>
				</c:otherwise>
			</c:choose>
		</tr>
		<% } %>
		<tr>
			<td class="txtlibform"><%=resource.getString("JSPP.WallPaper")%> :</td>
			<td>
				<c:if test="${wallpaper != null}">
					<a href="<c:out value="${wallpaper.URL}" />" target="_blank"><c:out value="${wallpaper.name}" /></a> / <c:out value="${wallpaper.size}" />
					<c:if test="${isAdmin}">
						<a href="RemoveFileToLook?FileName=<c:out value="${wallpaper.name}" />"><img src="<%=resource.getIcon("JSPP.delete")%>" border="0"/></a> <br/>
					</c:if>
				</c:if>
				<c:if test="${isAdmin}">
					<input type="file" name="wallPaper" size="60"/>
				</c:if>
			</td>
		</tr>
		<tr>
			<td class="txtlibform"><fmt:message key="JSPP.CSS"/> :</td>
			<td>
				<c:if test="${css != null}">
					<a href="<c:out value="${css.URL}" />" target="_blank"><c:out value="${css.name}" /></a> / <c:out value="${css.size}" />
					<c:if test="${isAdmin}">
						<a href="RemoveFileToLook?FileName=<c:out value="${css.name}" />"><img src="<%=resource.getIcon("JSPP.delete")%>" border="0"/></a> <br/>
					</c:if>
				</c:if>
				<c:if test="${isAdmin}">
					<input type="file" name="css" size="60"/>
				</c:if>
			</td>
		</tr>
  <c:if test="${spacePositionOption}">
    <tr>
      <td class="txtlibform"><fmt:message key="JSPP.SpacePosition" /> :</td>
      <td valign="top" width="100%">
	    <c:if test="${curSpace.displaySpaceFirst}">
	      <input type="radio" value="1" name="SpacePosition" checked="checked"/>&nbsp;<fmt:message key="JSPP.SpacePositionFirst" />
	      <input type="radio" value="2" name="SpacePosition" />&nbsp;<fmt:message key="JSPP.SpacePositionLast" />
	    </c:if>
	    <c:if test="${!curSpace.displaySpaceFirst}">
	      <input type="radio" value="1" name="SpacePosition" />&nbsp;<fmt:message key="JSPP.SpacePositionFirst" />
	      <input type="radio" value="2" name="SpacePosition" checked="checked"/>&nbsp;<fmt:message key="JSPP.SpacePositionLast" />
	    </c:if>
	    </td>
   </tr>
  </c:if>
</table>
</form>
</view:board>
<c:if test="${isAdmin}">
	<view:buttonPane>
		<fmt:message key="GML.validate" var="buttonLabel"/>
		<view:button label="${buttonLabel}" action="javascript:onclick=B_VALIDER_ONCLICK();"></view:button>
	</view:buttonPane>
</c:if>
</view:frame>
<%
	out.println(window.printAfter());
%>
</body>
</html>