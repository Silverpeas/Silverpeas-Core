<%@ page language="java" contentType="text/html; charset=ISO-8859-1"%>

<%@ include file="checkAgenda.jsp.inc" %>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title></title>
<SCRIPT language="Javascript">
	window.top.location.href = window.location.href.substring(0, window.location.href.indexOf('/Ragenda')) + '/Main/<%=graphicFactory.getLookFrame()%>';
</SCRIPT>
</head>
<body>
</body>
</html>