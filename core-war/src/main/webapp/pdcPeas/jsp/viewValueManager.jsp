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

<%@ include file="checkPdc.jsp" %>
<%@ page import="org.silverpeas.core.admin.user.model.Group"%>
<%@ page import="org.silverpeas.core.admin.user.model.UserDetail"%>

<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>

<%
ArrayList 		groups 			= (ArrayList) request.getAttribute("Groups");
ArrayList 		users 			= (ArrayList) request.getAttribute("Users");

ArrayList 		groupsInherited = (ArrayList) request.getAttribute("GroupsInherited");
ArrayList 		usersInherited 	= (ArrayList) request.getAttribute("UsersInherited");

AxisHeader		axis			= (AxisHeader) request.getAttribute("Axis");
Value 			value 			= (Value) request.getAttribute("Value");

String 			displayLanguage = (String) request.getAttribute("DisplayLanguage");
%>

<HTML>
<HEAD>
<TITLE><%=resource.getString("GML.popupTitle")%></TITLE>
<view:looknfeel/>
<script language="javaScript">

function goToOperationInUserPanel(action) {
	url = action;
	windowName = "userPanelWindow";
	windowParams = "directories=0,menubar=0,toolbar=0,alwaysRaised,scrollbars,resizable";
	userPanelWindow = SP_openUserPanel(url, windowName, windowParams);
}

function ConfirmAndSend(textToDisplay,targetURL)
{
    if (window.confirm(textToDisplay))
    {
        window.location.href = targetURL;
    }
}
</script>
</HEAD>
<BODY>
<%
    browseBar.setDomainName(resource.getString("pdcPeas.pdc"));
	browseBar.setComponentName(resource.getString("pdcPeas.pdcDefinition"));
	browseBar.setPath(resource.getString("pdcPeas.editRights"));

	TabbedPane tabbedPane = gef.getTabbedPane();
	if (value != null)
		tabbedPane.addTab(resource.getString("pdcPeas.valeur"), "ViewValue", false);
	else
		tabbedPane.addTab(resource.getString("pdcPeas.Axe"), "EditAxis", false);
	tabbedPane.addTab(resource.getString("pdcPeas.managers"), "ViewManager", true);

	operationPane.addOperation(m_context+"/util/icons/group_modify.gif",resource.getString("pdcPeas.updateManagers"),"EditManager");
	operationPane.addOperation(m_context+"/util/icons/userPanelPeas_to_del.gif",resource.getString("pdcPeas.deleteAllManagers"),"javascript:ConfirmAndSend('"+ resource.getString("pdcPeas.confirmDeleteAllManagers") + "','EraseManager')");

	out.println(window.printBefore());
    out.println(tabbedPane.print());
    out.println(frame.printBefore());
	out.println(board.printBefore());
%>

	<TABLE width="70%" align="center" border="0" cellPadding="0" cellSpacing="0">
		<TR>
			<TD colspan="2" align="center"><BR/></TD>
		</TR>
		<TR>
			<TD colspan="2" align="center" class="intfdcolor" height="1"><img src="<%=resource.getIcon("pdcPeas.1px")%>"></TD>
		</TR>
		<TR>
			<TD align="center" class="txttitrecol"><%=resource.getString("GML.type")%></TD>
			<TD align="center" class="txttitrecol"><%=resource.getString("pdcPeas.managers")%></TD>
		</TR>
		<TR>
			<TD colspan="2" align="center" class="intfdcolor" height="1"><img src="<%=resource.getIcon("pdcPeas.1px")%>"></TD>
		</TR>

		<%
		// La boucle sur les groupes
		int i = 0;
		Group group = null;
		while (i < groups.size())
		{
			group = (Group) groups.get(i);

			out.println("<TR>");
			if (group.isSynchronized())
				out.println("<TD align=\"center\"><IMG SRC=\""+resource.getIcon("pdcPeas.groupSynchronized")+"\"/></TD>");
			else
				out.println("<TD align=\"center\"><IMG SRC=\""+resource.getIcon("pdcPeas.group")+"\"/></TD>");
			out.println("<TD align=\"center\">"+group.getName()+"</TD>");
			out.println("</TR>");
			i++;
		}

		// La boucle sur les users
		i = 0;
		UserDetail user = null;
		while (i < users.size())
		{
			user = (UserDetail) users.get(i);
		%>
			<TR>
				<TD align="center"><IMG SRC="<%=resource.getIcon("pdcPeas.user")%>"/></TD>
				<TD align="center"><view:username userId="<%=user.getId()%>"/></TD>
			</TR>
		<%
			i++;
		}
		%>
		<TR>
			<TD colspan="2" align="center" class="intfdcolor" height="1"><img src="<%=resource.getIcon("pdcPeas.1px")%>"></TD>
		</TR>

		<% if (groupsInherited.size() > 0 || usersInherited.size() > 0 ) { %>
		<TR>
			<TD colspan="2"><BR/></TD>
		</TR>
		<TR>
			<TD colspan="2" align="center" class="intfdcolor" height="1"><img src="<%=resource.getIcon("pdcPeas.1px")%>"></TD>
		</TR>
		<TR>
			<TD align="center" class="txttitrecol"><%=resource.getString("GML.type")%></TD>
			<TD align="center" class="txttitrecol"><%=resource.getString("pdcPeas.managersInherited")%></TD>
		</TR>
		<TR>
			<TD colspan="2" align="center" class="intfdcolor" height="1"><img src="<%=resource.getIcon("pdcPeas.1px")%>"></TD>
		</TR>
		<%
		// il faut afficher les droits h�rit�s
		while (i < groupsInherited.size())
		{
			group = (Group) groupsInherited.get(i);

			out.println("<TR>");
			if (group.isSynchronized())
				out.println("<TD align=\"center\"><IMG SRC=\""+resource.getIcon("pdcPeas.groupSynchronized")+"\"/></TD>");
			else
				out.println("<TD align=\"center\"><IMG SRC=\""+resource.getIcon("pdcPeas.group")+"\"/></TD>");
			out.println("<TD align=\"center\">"+group.getName()+"</TD>");
			out.println("</TR>");
			i++;
		}

		// La boucle sur les users
		i = 0;

		while (i < usersInherited.size())
		{
			user = (UserDetail) usersInherited.get(i);
		%>
			<TR>
				<TD align="center"><IMG SRC="<%=resource.getIcon("pdcPeas.user")%>"/></TD>
				<TD align="center"><view:username userId="<%=user.getId()%>"/></TD>
			</TR>
		<%
			i++;
		}


		%>
		<TR>
			<TD colspan="2" align="center" class="intfdcolor"  height="1"><img src="<%=resource.getIcon("pdcPeas.1px")%>"></TD>
		</TR>

		<% } %>
	</TABLE>


<%
	out.println(board.printAfter());
	ButtonPane buttonPane = gef.getButtonPane();
	buttonPane.addButton((Button) gef.getFormButton(resource.getString("GML.close"), "javascript:window.close()", false));
	out.println("<br/><center>"+buttonPane.print()+"</center>");
    out.println(frame.printAfter());
    out.println(window.printAfter());
%>
</BODY>
</HTML>