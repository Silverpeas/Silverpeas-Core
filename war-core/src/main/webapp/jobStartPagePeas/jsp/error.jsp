<%@ include file="check.jsp" %>

<%
String when = (String) request.getAttribute("When");
String m_SpaceName = (String) request.getAttribute("currentSpaceName");
String m_SubSpace = (String) request.getAttribute("nameSubSpace");

browseBar.setDomainName(resource.getString("JSPP.manageHomePage"));
if (m_SubSpace == null) //je suis sur un espace
	browseBar.setComponentName(m_SpaceName);
else {
	browseBar.setComponentName(m_SpaceName + " > " + m_SubSpace);
}

String messageTitle = resource.getString("JSPP.ErrorComponentCreation");
String message = resource.getString("JSPP.ErrorComponentMessage");
browseBar.setPath(resource.getString("JSPP.creationInstance"));
if (when.equals("ComponentUpdate"))
{
	messageTitle = resource.getString("JSPP.ErrorComponentUpdate");
	browseBar.setPath(resource.getString("GML.modify"));
}
else if (when.equals("SpaceCreation"))
{
	messageTitle = resource.getString("JSPP.ErrorSpaceCreation");
	message = resource.getString("JSPP.ErrorSpaceMessage");
	browseBar.setPath(resource.getString("JSPP.creationSpace"));
}
else if (when.equals("SpaceUpdate"))
{
	messageTitle = resource.getString("JSPP.ErrorSpaceUpdate");
	message = resource.getString("JSPP.ErrorSpaceMessage");
	browseBar.setPath(resource.getString("JSPP.updateSpace"));
}
%>

<HTML>
<HEAD>
<TITLE><%=resource.getString("GML.popupTitle")%></TITLE>
<%
out.println(gef.getLookStyleSheet());
%>
</HEAD>
<BODY marginheight="5" marginwidth="5" leftmargin="5" topmargin="5">
<%
out.println(window.printBefore());
out.println(frame.printBefore());
out.println(board.printBefore());
%>
<center>
	<br>
	<h3><%=messageTitle%></h3><%=message%><br><br>
</center>
<%
out.println(board.printAfter());
out.println(frame.printAfter());
out.println(window.printAfter());
%>
</BODY>
</HTML>