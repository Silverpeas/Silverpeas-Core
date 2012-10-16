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

<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>

<%@ include file="check.jsp" %>
<%
    Board board = gef.getBoard();

	Domain  domObject 		= (Domain)request.getAttribute("domainObject");
    Group 	grObject 		= (Group)request.getAttribute("groupObject");
    String 	groupsPath 		= (String)request.getAttribute("groupsPath");
    
    Iterator	groups		= (Iterator) request.getAttribute("Groups");
    Iterator	users		= (Iterator) request.getAttribute("Users");
        
    String thisGroupId = grObject.getId();

    browseBar.setComponentName(getDomainLabel(domObject, resource), "domainContent?Iddomain="+domObject.getId());
    if (groupsPath != null)
        browseBar.setPath(groupsPath);
    
    operationPane.addOperation(resource.getIcon("JDP.userManage"),resource.getString("JDP.GroupManagersUpdate"),"javaScript:onClick=goToOperationInAnotherWindow(850, 800)");
    
    if (groups.hasNext() || users.hasNext()) 
		operationPane.addOperation(resource.getIcon("JDP.groupDel"),resource.getString("JDP.GroupManagersDelete"),"javaScript:onClick=deleteRoleContent()");
%>
<html>
<head>
<view:looknfeel/>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/animation.js"></script>
<script type="text/javascript">
function goToOperationInAnotherWindow(larg, haut) {
	windowName = "userPanelWindow";
	windowParams = "directories=0,menubar=0,toolbar=0,alwaysRaised,scrollbars,resizable";
	userPanelWindow = SP_openWindow("groupManagersChoose", windowName, larg, haut, windowParams, false);
}    

function deleteRoleContent() {	
    if (window.confirm("<%=resource.getString("JDP.GroupManagersDeleteConfirmation")%>")) { 
    	location.href = "groupManagersDelete";
	}
}	
</script>
</head>
<body>
<%
out.println(window.printBefore());

TabbedPane tabbedPane = gef.getTabbedPane();
tabbedPane.addTab(resource.getString("GML.description"), "groupContent?Idgroup="+thisGroupId, false);
tabbedPane.addTab(resource.getString("JDP.roleManager"), "groupManagersView?Id="+thisGroupId, true);
out.println(tabbedPane.print());

out.println(frame.printBefore());
%>
<center>
<%
out.println(board.printBefore());
%>
<TABLE width="100%" align="center" border="0" cellPadding="0" cellSpacing="0">
<TR>
	<TD colspan="2" align="center" class="intfdcolor" height="1"><img src="<%=resource.getIcon("JDP.px")%>"></TD>
</TR>
<TR>
	<TD align="center" class="txttitrecol"><%=resource.getString("GML.type")%></TD>
	<TD align="center" class="txttitrecol"><%=resource.getString("GML.name")%></TD>
</TR>
<TR>
	<TD colspan="2" align="center" class="intfdcolor" height="1"><img src="<%=resource.getIcon("JDP.px")%>"></TD>
</TR>		
<%
	// La boucle sur les groupes 
	Group group = null;
	while (groups.hasNext())
	{
		group = (Group) groups.next();
		out.println("<TR>");
		if (group.isSynchronized())
			out.println("<TD align=\"center\"><IMG SRC=\""+resource.getIcon("JDP.groupSynchronized")+"\"></TD>");
		else
			out.println("<TD align=\"center\"><IMG SRC=\""+resource.getIcon("JDP.group")+"\"></TD>");
		out.println("<TD align=\"center\">"+group.getName()+"</TD>");
		out.println("</TR>");
	}
	
	//La boucle sur les users
	UserDetail user = null;
	while (users.hasNext()) {
		user = (UserDetail) users.next(); 
	%>
		<tr>
		<td align="center"><img src="<%=resource.getIcon("JDP.user") %>"></td>
		<td align="center"><view:username userId="<%=user.getId()%>"/></td>
		</tr>
	<% } %>
<TR>
	<TD colspan="2" align="center" class="intfdcolor"  height="1"><img src="<%=resource.getIcon("JDP.px")%>"></TD>
</TR>
</TABLE>
</center>
<%
out.println(board.printAfter());
out.println(frame.printAfter());
out.println(window.printAfter());
%>
</body>
</html>