<%@ page import="com.stratelia.silverpeas.portlet.*"%>

<HTML>
<HEAD>
<jsp:useBean id="column" scope="request" class="com.stratelia.silverpeas.portlet.SpaceColumn"/>
</HEAD>
<frameset rows="<%=column.getRowRatios()%>" bordercolor="#FFFFFF">
  <% Portlet portlet ;
     for (int rowNum=0 ; rowNum < column.getPortletCount() ; rowNum ++) {
       portlet = column.getPortlets(rowNum) ;
  %>
  <frame name="<%=portlet.getName()%>" scrolling="NO"
    src="portlet?id=<%=portlet.getIndex() + "&spaceId=" + request.getParameter("spaceId")%>&portletState=<%=portlet.getStateAsString()%>"
    marginheight=5 marginwidth=5>
  <% } %>
</frameset>
</HTML>
