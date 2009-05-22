<%@ page import="com.stratelia.silverpeas.portlet.*"%>
<%@ page import="com.stratelia.silverpeas.peasCore.URLManager"%>

<%@ page errorPage="../../admin/jsp/errorpagePopup.jsp"%>

<HTML>
<HEAD>
<jsp:useBean id="portlet" scope="request" class="com.stratelia.silverpeas.portlet.Portlet"/>
</HEAD>

<frameset rows="20,2,1*" cols="5,2,1*,2,5" frameborder="NO" border="no" framespacing="0">
<!-- Ligne 1 = HEADER -->
  <frame scrolling="NO" src="empty.htm" noresize>
  <frame scrolling="NO" src="bordure.htm" noresize>
  <frame src="portletTitle?id=<%=portlet.getIndex()%>&spaceId=<%=request.getParameter("spaceId")%>&portletState=min" scrolling="NO"  marginheight="0" marginwidth="0" frameborder="NO" noresize>
  <frame scrolling="NO" src="bordure.htm" noresize>
  <frame scrolling="NO" src="empty.htm" noresize>

  <!-- frame src="<%= URLManager.getApplicationURL()+portlet.getRequestRooter() + portlet.getContentUrl() +
                    "?space=WA" + request.getParameter("spaceId") + "&Component=" + portlet.getComponentName() +
                    portlet.getComponentInstanceId()%>"
         scrolling="YES" marginheight="0" marginwidth="0" frameborder="NO" noresize -->
   <!-- Ligne 3 = BORDURE DU BAS -->
  <frame scrolling="NO" src="empty.htm" noresize>
  <frame scrolling="NO" src="bordure.htm" noresize>
  <frame scrolling="NO" src="bordure.htm" noresize>
  <frame scrolling="NO" src="bordure.htm" noresize>
  <frame scrolling="NO" src="empty.htm" noresize>
    <!-- Ligne 100% -->
  <frame scrolling="NO" src="empty.htm" noresize>
  <frame scrolling="NO" src="empty.htm" noresize>
  <frame scrolling="NO" src="empty.htm" noresize>
  <frame scrolling="NO" src="empty.htm" noresize>
  <frame scrolling="NO" src="empty.htm" noresize>

</frameset>
<noframes></noframes>

</HTML>
