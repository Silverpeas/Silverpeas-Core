<%
response.setHeader("Cache-Control","no-store"); //HTTP 1.1
response.setHeader("Pragma","no-cache");       //HTTP 1.0
response.setDateHeader ("Expires",-1);        //prevents caching at the proxy server
%>

<%@ page errorPage="../../admin/jsp/errorpagePopup.jsp"%>

<html>
<head>
<title>portlet Administration</title>
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
</head>

<frameset rows="38,1*" frameborder="NO" border="0" framespacing="0"> 
  <frame name="tabs" scrolling="NO" noresize src="portletAdminBarre?spaceId=<%=request.getParameter("spaceId")%>" >
  <frame name="adminMain" src="adminMain?spaceId=<%=request.getParameter("spaceId")%>">
</frameset>
<noframes><body bgcolor="#FFFFFF">

</body></noframes>
</html>
