<%@ include file="checkPdc.jsp"%>

<HTML>
<HEAD>
<TITLE><%=resource.getString("GML.popupTitle")%></TITLE>
<%
   out.println(gef.getLookStyleSheet());
%>
<script language="JavaScript">
function refresh() {
	try
	{
		window.opener.document.toComponent.submit();
	}
	catch(e)
	{
		//opening window does not contains toComponent form
	}
	window.close();
}
</script>
</HEAD>
<BODY onLoad=refresh()>
</BODY>
</HTML>