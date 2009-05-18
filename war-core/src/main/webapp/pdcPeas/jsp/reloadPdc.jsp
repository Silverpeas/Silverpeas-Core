<%@ include file="checkPdc.jsp"%>

<HTML>
<HEAD>
<TITLE><%=resource.getString("GML.popupTitle")%></TITLE>
<%
   out.println(gef.getLookStyleSheet());
%>
<script language="JavaScript">
function refresh() {
	window.opener.document.refresh.submit();
	window.close();
}
</script>
</HEAD>
<BODY onLoad=refresh()>
</BODY>
</HTML>