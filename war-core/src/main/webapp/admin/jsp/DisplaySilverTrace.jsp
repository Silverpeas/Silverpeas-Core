<%
response.setHeader("Cache-Control","no-store"); //HTTP 1.1
response.setHeader("Pragma","no-cache"); //HTTP 1.0
response.setDateHeader ("Expires",-1); //prevents caching at the proxy server
response.setContentType("text/plain");
%>
<%@ page import="java.util.*"%> 
<%@ page import="com.stratelia.silverpeas.silvertrace.*"%>
<%
    String[] lines = SilverTrace.getEndFileTrace(request.getParameter("NbLinesToDisplay"));
    for (int i = 0; i < lines.length; i++)
        out.println(lines[i]);
%>