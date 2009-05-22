<%
response.setHeader("Cache-Control","no-store"); //HTTP 1.1
response.setHeader("Pragma","no-cache");       //HTTP 1.0
response.setDateHeader ("Expires",-1);        //prevents caching at the proxy server
%>

<%@ page errorPage="../../admin/jsp/errorpagePopup.jsp"%>

<HTML>
<HEAD>
<jsp:useBean id="spaceModel" scope="request" class="com.stratelia.silverpeas.portlet.SpaceModel">
  <jsp:setProperty name="spaceModel" property="*" />
</jsp:useBean>

<%@ page import="com.stratelia.silverpeas.portlet.*"%>
<%
  SpaceColumn column ;
%>

<TITLE>
  <jsp:getProperty name="spaceModel" property="name"/>
</TITLE>
</HEAD>
<frameset cols="<%=spaceModel.getColumnsRatios()%>" bordercolor="#FFFFFF" marginheight=5 marginwidth=5>
  <% for (int colNum=0 ; colNum <spaceModel.getcolumnsCount() ; colNum++ ) {
       column = spaceModel.getColumn(colNum) ;
  %>
       <frame name="column<%=column.getColumnNumber()%>" scrolling="NO" src="column?col=<%=colNum%>&spaceId=<%=request.getParameter("spaceId")%>" marginheight=5 marginwidth=5>  <% } %>
</frameset>
<noframes>
</noframes>
</HTML>
