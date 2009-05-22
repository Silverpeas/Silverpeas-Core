<%@ page import="com.stratelia.silverpeas.portlet.*"%>
<%@ page import="com.stratelia.silverpeas.peasCore.URLManager"%>

<%@ page errorPage="../../admin/jsp/errorpagePopup.jsp"%>

<HTML>
<HEAD>
<jsp:useBean id="portlet" scope="request" class="com.stratelia.silverpeas.portlet.Portlet"/>
</HEAD>
<frameset rows="24,1*" cols="2,1*,2" frameborder="NO" border="0" framespacing="0">
<!-- Ligne 1 = HEADER -->
  <frame scrolling="NO" src="bordure.htm" noresize>
  <frame src="portletTitle?id=<%=portlet.getIndex()%>&spaceId=<%=request.getParameter("spaceId")%>" scrolling="NO"  marginheight="0" marginwidth="0" frameborder="NO" noresize>
  <frame scrolling="NO" src="bordure.htm" noresize>

  <!-- Ligne 2 = CONTENT -->
  <frame scrolling="NO" src="bordure.htm" noresize>
  <frame src="<%= URLManager.getApplicationURL()+portlet.getRequestRooter() + portlet.getContentUrl() +
                    "?space=WA" + request.getParameter("spaceId") + "&Component=" + portlet.getComponentName() +
                    portlet.getComponentInstanceId()%>"
         scrolling="AUTO" marginheight="0" marginwidth="0" frameborder="NO" noresize>
  <frame scrolling="NO" src="bordure.htm" noresize>
</frameset>
<noframes></noframes>
</HTML>
