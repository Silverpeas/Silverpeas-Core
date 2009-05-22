<%
response.setHeader("Cache-Control","no-store"); //HTTP 1.1
response.setHeader("Pragma","no-cache");       //HTTP 1.0
response.setDateHeader ("Expires",-1);        //prevents caching at the proxy server
%>

<%@ page import="com.stratelia.silverpeas.portlet.*"%>

<%@ page errorPage="../../admin/jsp/errorpagePopup.jsp"%>

<HTML>

<HEAD>
  <jsp:useBean id="column" scope="request" class="com.stratelia.silverpeas.portlet.SpaceColumn"/>
</HEAD>

<frameset rows="<%=column.getRowRatios()%>" bordercolor="#FFFFFF" framespacing=5>

<% 
   String lastPortlet ;
   int rowCount = column.getPortletCount() ;
   if (rowCount == 1) {
     lastPortlet = "yes" ;
   } else {
     lastPortlet = "no" ;
   }

   for (int rowNum=0 ; rowNum < rowCount ; rowNum ++) {
     Portlet portlet = column.getPortlets(rowNum) ;
%>

  <frame name="<%=portlet.getName()%>" scrolling="NO" 
    src="adminPortlet?col=<%=request.getParameter("col")%>&id=<%=portlet.getIndex() +
                    "&row=" + rowNum +
                    "&spaceId=" + request.getParameter("spaceId") +
                    "&portletState=" + portlet.getStateAsString() +
                    "&lastPortlet=" + lastPortlet%>"
    marginheight=5 marginwidth=5>
  <% } %>
</frameset>

</HTML>
