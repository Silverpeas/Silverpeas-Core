<%@ include file="check.jsp" %>
<%
    Board board = gef.getBoard();

    Group 	grObject 		= (Group)request.getAttribute("groupObject");
    String 	groupsPath 		= (String)request.getAttribute("groupsPath");
    
    Iterator	groups		= (Iterator) request.getAttribute("Groups");
    Iterator	users		= (Iterator) request.getAttribute("Users");
        
    String thisGroupId = grObject.getId();

    browseBar.setDomainName(resource.getString("JDP.jobDomain"));

    browseBar.setComponentName(Encode.javaStringToHtmlString((String)request.getAttribute("domainName")), (String)request.getAttribute("domainURL"));
    if (groupsPath != null)
        browseBar.setPath(groupsPath);
    
    operationPane.addOperation(resource.getIcon("JDP.userManage"),resource.getString("JDP.GroupManagersUpdate"),"javaScript:onClick=goToOperationInAnotherWindow(850, 800)");
    
    if (groups.hasNext() || users.hasNext()) 
		operationPane.addOperation(resource.getIcon("JDP.groupDel"),resource.getString("JDP.GroupManagersDelete"),"javaScript:onClick=deleteRoleContent()");
%>
<html>
<head>
<% out.println(gef.getLookStyleSheet()); %>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/animation.js"></script>
<script language="JavaScript">
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
<body marginheight="5" marginwidth="5" leftmargin="5" topmargin="5" bgcolor="#FFFFFF">
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
	while (users.hasNext())
	{
		user = (UserDetail) users.next();
		out.println("<TR>");
		out.println("<TD align=\"center\"><IMG SRC=\""+resource.getIcon("JDP.user")+"\"></TD>");
		out.println("<TD align=\"center\">"+user.getLastName() + " " + user.getFirstName()+"</TD>");
		out.println("</TR>");
	}
%>
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