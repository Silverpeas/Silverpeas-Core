<%--

    Copyright (C) 2000 - 2009 Silverpeas

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    As a special exception to the terms and conditions of version 3.0 of
    the GPL, you may redistribute this Program in connection with Free/Libre
    Open Source Software ("FLOSS") applications as described in Silverpeas's
    FLOSS exception.  You should have received a copy of the text describing
    the FLOSS exception, and it is also available here:
    "http://repository.silverpeas.com/legal/licensing"

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
<view:setBundle basename="com.silverpeas.jobStartPagePeas.multilang.jobStartPagePeasBundle"/>
<!-- Retrieve current space bean -->
<c:set var="curSpace" value="${requestScope.Space}" />
<c:set var="spacePositionOption" value="${requestScope.displaySpaceOption}" />

<%
SpaceInst		space				= (SpaceInst) request.getAttribute("Space");
SpaceLookHelper slh 				= (SpaceLookHelper) request.getAttribute("SpaceLookHelper");
boolean 		isInHeritanceEnable = ((Boolean)request.getAttribute("IsInheritanceEnable")).booleanValue();
DisplaySorted 	m_SpaceExtraInfos 	= (DisplaySorted)request.getAttribute("SpaceExtraInfos");
String 			spaceId				= (String) request.getAttribute("CurrentSpaceId");

List 			availableLooks		= gef.getAvailableLooks();
String			spaceLook			= space.getLook();
SpaceLookItem 	item 				= (SpaceLookItem) slh.getItem("wallPaper");

browseBar.setSpaceId(spaceId);
browseBar.setExtraInformation(resource.getString("JSPP.SpaceAppearance"));
%>

<HTML>
<HEAD>
<TITLE><%=resource.getString("GML.popupTitle")%></TITLE>
<%
out.println(gef.getLookStyleSheet());
%>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/animation.js"></script>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/checkForm.js"></script>
<script language="JavaScript">
function B_VALIDER_ONCLICK() {
	document.lookSpace.submit();
}
</script>
</HEAD>
<BODY>
<form name="lookSpace" action="UpdateSpaceLook" method="POST" enctype="multipart/form-data">
<%
out.println(window.printBefore());

TabbedPane tabbedPane = gef.getTabbedPane();
tabbedPane.addTab(resource.getString("GML.description"),"StartPageInfo", false);
tabbedPane.addTab(resource.getString("JSPP.SpaceAppearance"), "SpaceLook", true);

tabbedPane.addTab(resource.getString("JSPP.Manager"), "SpaceManager", false);

if (isInHeritanceEnable)
{
    tabbedPane.addTab(resource.getString("JSPP.admin"), "SpaceManager?Role=admin", false);
    tabbedPane.addTab(resource.getString("JSPP.publisher"), "SpaceManager?Role=publisher", false);
    tabbedPane.addTab(resource.getString("JSPP.writer"), "SpaceManager?Role=writer", false);
    tabbedPane.addTab(resource.getString("JSPP.reader"), "SpaceManager?Role=reader", false);
}

out.println(tabbedPane.print());
out.println(frame.printBefore());
out.println(board.printBefore());
%>
	<table border="0" cellspacing="0" cellpadding="5" width="100%">
		<% if (availableLooks.size() >= 2) { %>
		<tr>
			<td class="txtlibform"><%=resource.getString("JSPP.SpaceLook")%> :</td>
			<% if (m_SpaceExtraInfos.isAdmin) { %>
					<td>
						<select name="SelectedLook" size="1">
						<%
						if (StringUtil.isDefined(spaceLook))
							out.println("<option value=\"\"></option>");
						else
							out.println("<option value=\"\" selected></option>");
				        for (int i = 0; i < availableLooks.size(); i++) {
				            String lookName = (String) availableLooks.get(i);
				            if (lookName.equals(spaceLook))
				              out.println("<option value=\""+lookName+"\" selected>"+lookName+"</option>");
				            else
				              out.println("<option value=\""+lookName+"\">"+lookName+"</option>");
				        } %>
				        </select>
					</td>
			<% } else { %>
					<td>
						<%
							if (StringUtil.isDefined(spaceLook))
								out.println(spaceLook);
						%>
					</td>
			<% } %>
		</tr>
		<% } %>
		<tr>
			<td class="txtlibform"><%=resource.getString("JSPP.WallPaper")%> :</td>
			<% if (m_SpaceExtraInfos.isAdmin) { %>
				<td>
					<% if (item != null) { %>
						<a href="<%=item.getURL()%>" target="_blank"><%=item.getName()%></a> <%=item.getSize()%> <a href="RemoveFileToLook?FileName=<%=item.getName()%>"><img src="<%=resource.getIcon("JSPP.delete")%>" border="0"></a> <BR/>
					<% } %>
					<input type="file" name="wallPaper" size="60">
				</td>
			<% } else { %>
				<td>
					<% if (item != null) { %>
						<a href="<%=item.getURL()%>" target="_blank"><%=item.getName()%></a> <%=item.getSize()%>
					<% } %>
				</td>
			<% } %>
		</tr>
  <c:if test="${spacePositionOption}">
    <tr>
      <td class="txtlibform" nowrap valign="top"><fmt:message key="JSPP.SpacePosition" /> :</td>
      <td valign="top" width="100%">
	    <c:if test="${curSpace.displaySpaceFirst}">
	      <input type="radio" value="1" name="SpacePosition" checked/>&nbsp;<fmt:message key="JSPP.SpacePositionFirst" />
	      <input type="radio" value="2" name="SpacePosition" />&nbsp;<fmt:message key="JSPP.SpacePositionLast" />
	    </c:if>
	    <c:if test="${!curSpace.displaySpaceFirst}">
	      <input type="radio" value="1" name="SpacePosition" />&nbsp;<fmt:message key="JSPP.SpacePositionFirst" />
	      <input type="radio" value="2" name="SpacePosition" checked/>&nbsp;<fmt:message key="JSPP.SpacePositionLast" />
	    </c:if>
	    </td>
   </tr>
  </c:if>

	</table>
<%
	out.println(board.printAfter());

	if (m_SpaceExtraInfos.isAdmin)
	{
		ButtonPane buttonPane = gef.getButtonPane();
		buttonPane.addButton((Button) gef.getFormButton(resource.getString("GML.validate"), "javascript:onClick=B_VALIDER_ONCLICK();", false));
		out.println("<br/><center>"+buttonPane.print()+"</center>");
	}

	out.println(frame.printAfter());
	out.println(window.printAfter());
%>
</FORM>
</BODY>
</HTML>