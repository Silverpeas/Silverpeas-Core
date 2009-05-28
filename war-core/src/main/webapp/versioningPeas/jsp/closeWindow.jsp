<%
	String url = (String) request.getAttribute("urlToReload");
%>

<SCRIPT LANGUAGE="JavaScript">
<% if (url != null && url.length() > 0) { %>
	self.opener.location = '<%=url%>';
<% } %>
	self.close();
</SCRIPT>