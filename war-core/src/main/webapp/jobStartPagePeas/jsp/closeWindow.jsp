<%@ include file="check.jsp" %>
<%
	String url = (String) request.getAttribute("urlToReload");
%>

<SCRIPT LANGUAGE="JavaScript">
<% if (url != null && url.length() > 0)
{
%>
	self.opener.location = '<%=url%>';
<%
}
else if (url.equals("jobStartPageNav"))
{
%>
	self.opener.parent.startPageNavigation.location.href = '<%=url%>';
<%
}
%>
	self.close();
</SCRIPT>
