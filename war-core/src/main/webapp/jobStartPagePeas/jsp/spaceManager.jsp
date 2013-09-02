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
<%
	SpaceProfileInst 	m_Profile 			= (SpaceProfileInst) request.getAttribute("Profile");
	List<Group>			m_listGroup 		= (List<Group>) request.getAttribute("listGroupSpace");
	List<UserDetail> 	m_listUser 			= (List<UserDetail>) request.getAttribute("listUserSpace");
	Boolean 			m_ProfileEditable 	= (Boolean) request.getAttribute("ProfileEditable");
	String				role				= (String) request.getAttribute("Role");
	DisplaySorted 		m_SpaceExtraInfos 	= (DisplaySorted)request.getAttribute("SpaceExtraInfos");
	boolean 			isInHeritanceEnable = ((Boolean)request.getAttribute("IsInheritanceEnable")).booleanValue();
	
	List<Group>			inheritedGroups		= (List<Group>) request.getAttribute("listInheritedGroups"); //List of GroupDetail
	List<UserDetail>	inheritedUsers 		= (List<UserDetail>) request.getAttribute("listInheritedUsers"); //List of UserDetail
	String 				spaceId				= (String) request.getAttribute("CurrentSpaceId");
	
	String nameProfile = null;
	if (m_Profile == null) {
		nameProfile = resource.getString("JSPP."+role);
	} else {
		nameProfile = m_Profile.getLabel();
		if (!StringUtil.isDefined(nameProfile)) {
			nameProfile = resource.getString("JSPP."+role);
		}
	}
	
	browseBar.setSpaceId(spaceId);
	browseBar.setExtraInformation(nameProfile);
		
	//Onglets
    TabbedPane tabbedPane = gef.getTabbedPane();
	tabbedPane.addTab(resource.getString("GML.description"),"StartPageInfo", false);
	
	tabbedPane.addTab(resource.getString("JSPP.SpaceAppearance"), "SpaceLook", false);
	
    tabbedPane.addTab(resource.getString("JSPP.Manager"), "SpaceManager", role.equals("Manager"));
    
    if (isInHeritanceEnable) {
	    tabbedPane.addTab(resource.getString("JSPP.admin"), "SpaceManager?Role=admin", role.equals("admin"));
	    tabbedPane.addTab(resource.getString("JSPP.publisher"), "SpaceManager?Role=publisher", role.equals("publisher"));
	    tabbedPane.addTab(resource.getString("JSPP.writer"), "SpaceManager?Role=writer", role.equals("writer"));
	    tabbedPane.addTab(resource.getString("JSPP.reader"), "SpaceManager?Role=reader", role.equals("reader"));
    }
%>
<html>
<head>
<title><%=resource.getString("GML.popupTitle")%></title>
<%
out.println(gef.getLookStyleSheet());
%>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/animation.js"></script>
<script type="text/javascript">
function goToOperationInAnotherWindow(larg, haut) {
	windowName = "userPanelWindow";
	windowParams = "directories=0,menubar=0,toolbar=0,alwaysRaised,scrollbars,resizable";
	userPanelWindow = SP_openWindow("SelectUsersGroupsSpace?Role=<%=role%>", windowName, larg, haut, windowParams, false);
}    

function deleteRoleContent() {	
    if (window.confirm("<%=resource.getString("JSPP.MessageSuppressionSpaceManager")%>")) { 
    	location.href = "DeleteSpaceManager?Role=<%=role%>";
	}
}	
</script>
</head>
<body id="admin-role">
<%
	if (m_SpaceExtraInfos.isAdmin) {
		// Space edition
		if (m_Profile == null) { //creation
			operationPane.addOperation(resource.getIcon("JSPP.userManage"),resource.getString("JSPP.SpaceProfilePanelCreateTitle"),"javaScript:onClick=goToOperationInAnotherWindow(850, 800)");
		} else {
		  	//update
			operationPane.addOperation(resource.getIcon("JSPP.userManage"),resource.getString("JSPP.SpaceProfilePanelModifyTitle"),"javaScript:onClick=goToOperationInAnotherWindow(850, 800)");
			
			if (m_ProfileEditable.equals(Boolean.TRUE)) {
				operationPane.addOperation(resource.getIcon("JSPP.spaceManagerDescription"),resource.getString("JSPP.ProfilePanelModifyTitle"),"javaScript:onClick=goToOperationInAnotherWindow('SpaceManagerDescription', 750, 250)");
			}
				
			if (!m_listGroup.isEmpty() || !m_listUser.isEmpty()) { 
				operationPane.addOperation(resource.getIcon("JSPP.usersGroupsDelete"),resource.getString("JSPP.SpaceProfilePanelDeleteTitle"),"javaScript:onClick=deleteRoleContent()");
			}
		}
	}
	
out.println(window.printBefore());
out.println(tabbedPane.print());
out.println(frame.printBefore());
%>
<% if (role.equals("Manager")) { %>
	<span class="inlineMessage">
	<%=resource.getString("JSPP.Manager.help")%>
	</span>
	<br clear="all"/>
<% } %>
<%
out.println(board.printBefore());
%>
<center>
<br/><br/>
	<% if ((inheritedGroups != null && !inheritedGroups.isEmpty()) || (inheritedUsers != null && !inheritedUsers.isEmpty())) { %>
	<table width="70%" align="center" border="0" cellPadding="0" cellSpacing="0">
		<tr>
			<td colspan="2" class="txttitrecol"><%=resource.getString("JSPP.inheritedRights")%></td>
		</tr>
		<tr>
			<td colspan="2" align="center" class="intfdcolor" height="1"><img src="<%=resource.getIcon("JSPP.px")%>"/></td>
		</tr>
		<tr>
			<td align="center" class="txttitrecol"><%=resource.getString("GML.type")%></td>
			<td align="center" class="txttitrecol"><%=resource.getString("GML.name")%></td>
		</tr>
		<tr>
			<td colspan="2" align="center" class="intfdcolor" height="1"><img src="<%=resource.getIcon("JSPP.px")%>"/></td>
		</tr>
		<% if (inheritedGroups != null) { %>
		<% for (Group group : inheritedGroups) { %>
			<tr>
			<% if (group.isSynchronized()) { %>
				<td align="center"><img src="<%=resource.getIcon("JSPP.scheduledGroup") %>" class="group-icon"/></td>
			<% } else { %>
				<td align="center"><img src="<%=resource.getIcon("JSPP.group")%>" class="group-icon"/></td>
			<% } %>
			<td align="center"><%=group.getName() %></td>
			</tr>
		<% } %>
		<% } %>
		
		<% if (inheritedUsers != null) { %>
		<% for (UserDetail user : inheritedUsers) { %>
			<tr>
				<td align="center"><img src="<%=resource.getIcon("JSPP.user") %>" class="user-icon"/></td>
				<td align="center"><view:username userId="<%=user.getId()%>" /></td>
			</tr>
		<% } %>
		<% } %>
		<tr>
			<td colspan="2" align="center" class="intfdcolor"  height="1"><img src="<%=resource.getIcon("JSPP.px")%>"/></td>
		</tr>
	</table>
	<br/><br/>
	<% } %>

	<table width="70%" align="center" border="0" cellPadding="0" cellSpacing="0">
		<% if (isInHeritanceEnable) { %>
		<tr>
			<td colspan="2" class="txttitrecol"><%=resource.getString("JSPP.localRights")%></td>
		</tr>
		<% } %>
		<tr>
			<td colspan="2" align="center" class="intfdcolor" height="1"><img src="<%=resource.getIcon("JSPP.px")%>"/></td>
		</tr>
		<tr>
			<td align="center" class="txttitrecol"><%=resource.getString("GML.type")%></td>
			<td align="center" class="txttitrecol"><%=resource.getString("GML.name")%></td>
		</tr>
		<tr>
			<td colspan="2" align="center" class="intfdcolor" height="1"><img src="<%=resource.getIcon("JSPP.px")%>"/></td>
		</tr>		
		<% for (Group group : m_listGroup) { %>
		  	<tr>
				<% if (group.isSynchronized()) { %>
					<td align="center"><img src="<%=resource.getIcon("JSPP.scheduledGroup") %>" class="group-icon"/></td>
				<% } else { %>
					<td align="center"><img src="<%=resource.getIcon("JSPP.group")%>" class="group-icon"/></td>
				<% } %>
				<td align="center"><%=group.getName() %></td>
			</tr>
		<% } %>
		
		<% for (UserDetail user : m_listUser) { %>
			<tr>
				<td align="center"><img src="<%=resource.getIcon("JSPP.user") %>" class="user-icon"/></td>
				<td align="center"><view:username userId="<%=user.getId()%>" /></td>
			</tr>
		<% } %>
		<tr>
			<td colspan="2" align="center" class="intfdcolor" height="1"><img src="<%=resource.getIcon("JSPP.px")%>"/></td>
		</tr>
	</table>
<br/><br/>
</center>
<%
out.println(board.printAfter());
out.println(frame.printAfter());
out.println(window.printAfter());
%>
</body>
</html>