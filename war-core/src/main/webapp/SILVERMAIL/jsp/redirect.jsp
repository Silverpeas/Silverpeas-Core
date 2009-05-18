<%
response.setHeader("Cache-Control","no-store"); //HTTP 1.1
response.setHeader("Pragma","no-cache");        //HTTP 1.0
response.setDateHeader ("Expires",-1);          //prevents caching at the proxy server
%>

<%@ page import="com.stratelia.silverpeas.peasCore.URLManager"%>

<HTML>
<HEAD>
<TITLE>___/ Silverpeas - Corporate Portal Organizer \________________________________________________________________________</TITLE>
<script>
window.location.href = "<%=URLManager.getApplicationURL()%>/admin/jsp/Main.jsp?SpaceId=<%=request.getParameter("SpaceId")%>";
</script>
</HEAD>
<BODY marginwidth=5 marginheight=5 leftmargin=5 topmargin=5>
</BODY>
</HTML>