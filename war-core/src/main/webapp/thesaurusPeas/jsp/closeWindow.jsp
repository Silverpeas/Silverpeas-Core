<%@ page import="java.util.*"%>

<%@ include file="checkThesaurus.jsp" %>
<%
	String url = (String) request.getAttribute("urlToReload");
%>

<SCRIPT LANGUAGE="JavaScript">
<!--
	self.opener.location = '<%=url%>';
	self.close();
//-->
</SCRIPT>
