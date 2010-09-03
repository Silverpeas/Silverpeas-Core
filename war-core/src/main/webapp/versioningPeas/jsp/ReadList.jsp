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

<%@ page errorPage="../../admin/jsp/errorpage.jsp"%>
<%@ include file="checkVersion.jsp" %>
<%@ page import="com.stratelia.webactiv.beans.admin.ProfileInst"%>
<%@ page import="com.stratelia.webactiv.beans.admin.Group"%>

<%
		List groups = (List) request.getAttribute("Groups");
		List users = (List) request.getAttribute("Users");
		
    ResourceLocator messages = new ResourceLocator("com.stratelia.silverpeas.versioningPeas.multilang.versioning", m_MainSessionCtrl.getFavoriteLanguage());

    String profile = (String) request.getAttribute("Profile");
    String message = "";
    if (request.getAttribute("Message") != null)
			message = (String) request.getAttribute("Message");

%>
	<html>
	<head>
	<% out.println(gef.getLookStyleSheet()); %>
	<script type="text/javascript" src="<%=m_context%>/util/javaScript/animation.js"></script>
	<script language="Javascript">
	  function goToUserPanel()
		{
			windowName = "userPanelWindow";
			windowParams = "directories=0,menubar=0,toolbar=0,alwaysRaised,scrollbars,resizable";
			userPanelWindow = SP_openUserPanel("SelectUsersGroupsProfileInstance?Role=user", windowName, windowParams);
		}
	</script>
	</head>
	<body>  
	<%
		if ("admin".equals(profile) || "publisher".equals(profile))
		{
			operationPane.addOperation(userPanelIcon, messages.getString("readerlist.ToUserPanel"), "javaScript:onClick=goToUserPanel()");
		  operationPane.addOperation(userPanelDeleteIcon, resources.getString("GML.delete"), "DeleteReaderProfile");
		  operationPane.addOperation(saveListIcon, messages.getString("versioning.SaveList"), "SaveList?Role=user&From=ViewReadersList");
		}		
    out.println(window.printBefore());
    if (StringUtil.isDefined(message))
	    out.println(message);
	    
    TabbedPane tabbedPane = gef.getTabbedPane();
    tabbedPane.addTab(messages.getString("versions.caption") , "ViewVersions", false, true);
 
    if (versioningSC.tabWritersToDisplay())
      tabbedPane.addTab(messages.getString("writerlist.caption") , "ViewWritersList", false, true);
 
    if (versioningSC.tabReadersToDisplay())
		  tabbedPane.addTab(messages.getString("readerlist.caption") , "ViewReadersList", true, true);

    out.println(tabbedPane.print());
  	out.println(frame.printBefore());

    Board board = gef.getBoard();
    out.println(board.printBefore());

    Document document = versioningSC.getEditingDocument();
%>
		<TABLE width="70%" align="center" border="0" cellPadding="0" cellSpacing="0">
		<TR>
			<TD colspan="2" align="center">
				<BR/><BR/>
			</TD>
		</TR>
		<TR>
			<TD colspan="2" align="center" class="intfdcolor" height="1"><img src="<%=hLineSrc%>"></TD>
		</TR>
		<TR>
			<TD align="center" class="txttitrecol"><%=resources.getString("GML.type")%></TD>
			<TD align="center" class="txttitrecol"><%=resources.getString("GML.name")%></TD>
		</TR>
		<TR>
			<TD colspan="2" align="center" class="intfdcolor" height="1"><img src="<%=hLineSrc%>"></TD>
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
				out.println("<TD align=\"center\"><IMG SRC=\""+scheduledGroupSrc+"\"></TD>");
			else
				out.println("<TD align=\"center\"><IMG SRC=\""+groupSrc+"\"></TD>");
			out.println("<TD align=\"center\">"+group.getName()+"</TD>");
			out.println("</TR>");
			i++;
		}

		// La boucle sur les users
		i = 0;
		while (i < users.size()) 
		{
		%>
			<TR>
				<TD align="center"><IMG SRC="<%=userSrc%>"></TD>
				<TD align="center"><%out.println((String) users.get(i));%></TD>
			</TR>
		<%
			i++;
		}
		%>				
		<TR>
			<TD colspan="2" align="center" class="intfdcolor" height="1"><img src="<%=hLineSrc%>"></TD>
		</TR>
		<TR>
			<TD colspan="2">&nbsp;</TD>
		</TR>
	</TABLE>
	
<%
	out.println(board.printAfter());
  out.println(frame.printAfter());
  out.println(window.printAfter()); 
%>

</BODY>
</HTML>

