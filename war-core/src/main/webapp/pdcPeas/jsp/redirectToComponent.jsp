<%@ include file="checkPdc.jsp"%>
<%
	String				toURL				= (String) request.getAttribute("ToURL");
	String				windowLocation		= m_context+toURL;

	ContainerContext	containerContext	= (ContainerContext) request.getAttribute("ContainerContext");
	String				returnURL			= "";
	if (containerContext != null) {
		returnURL = containerContext.getReturnURL();
	}
	if (returnURL != null && returnURL.length() > 0)
		windowLocation += "&ReturnURL="+returnURL;
%>
<HTML>
<HEAD>
<TITLE><%=resource.getString("GML.popupTitle")%></TITLE>
<%
   out.println(gef.getLookStyleSheet());
%>
<script language="JavaScript">
function redirect() {
	window.location = "<%=windowLocation%>";
}
</script>
</HEAD>
<BODY onLoad=redirect()>
</BODY>
</HTML>