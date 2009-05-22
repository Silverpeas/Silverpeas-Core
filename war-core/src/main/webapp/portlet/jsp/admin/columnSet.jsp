<%
response.setHeader("Cache-Control","no-store"); //HTTP 1.1
response.setHeader("Pragma","no-cache");       //HTTP 1.0
response.setDateHeader ("Expires",-1);        //prevents caching at the proxy server
%>

<%@ page errorPage="../../admin/jsp/errorpagePopup.jsp"%>

<html>
<head>

<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
</head>

<frameset rows="25,1*" frameborder="NO" border="no" framespacing="5"> 
  <frame name="colHeader<%=request.getParameter("col")%>" src="colHeader?col=<%=request.getParameter("col")%>&spaceId=<%=request.getParameter("spaceId")%>" scrolling="NO" noresize>
  <frame name="column<%=request.getParameter("col")%>" src="adminColumn?col=<%=request.getParameter("col")%>&spaceId=<%=request.getParameter("spaceId")%>" scrolling="NO" noresize>
</frameset>
<noframes><body bgcolor="#FFFFFF">

</body></noframes>
</html>
