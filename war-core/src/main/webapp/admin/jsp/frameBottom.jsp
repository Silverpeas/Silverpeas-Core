<%@ page import="com.stratelia.silverpeas.peasCore.URLManager"%>
<%@ page import="com.stratelia.silverpeas.peasCore.MainSessionController"%>
<%@ page import="com.stratelia.webactiv.util.ResourceLocator"%>
<%@ page import="com.stratelia.webactiv.beans.admin.SpaceInstLight"%>
<%@ page import="java.net.URLEncoder"%>

<html>
<head>
<script language="javascript">
<!--
function MM_reloadPage(init) {  //reloads the window if Nav4 resized
  if (init==true) with (navigator) {if ((appName=="Netscape")&&(parseInt(appVersion)==4)) {
    document.MM_pgW=innerWidth; document.MM_pgH=innerHeight; onresize=MM_reloadPage; }}
  else if (innerWidth!=document.MM_pgW || innerHeight!=document.MM_pgH) location.reload();
}
MM_reloadPage(true);
//-->
</script>
</head>

  <frameset cols="180,*" border="0" framespacing="5" frameborder="NO"> 

<%
MainSessionController m_MainSessionCtrl	= (MainSessionController) session.getAttribute("SilverSessionController");

HttpSession s = request.getSession();
String strGoTo = (String)s.getValue("goto");
String strGoToNew = (String)s.getValue("gotoNew");
String spaceId =  request.getParameter("SpaceId");

String spaceIdForMain = "";
if (spaceId != null) {
	spaceIdForMain = spaceId;
}

SpaceInstLight rootSpace = m_MainSessionCtrl.getOrganizationController().getRootSpace(spaceId);
String rootSpaceId = "";
if (rootSpace != null)
{
	rootSpaceId = rootSpace.getFullId();
}
String paramsForDomainsBar = "?privateDomain="+rootSpaceId;
if (!rootSpaceId.equals(spaceId))
	paramsForDomainsBar += "&privateSubDomain="+spaceId;
%>
    <frame src="DomainsBar.jsp<%=paramsForDomainsBar%>" marginwidth="0" marginheight="10" name="SpacesBar" frameborder="NO"  scrolling="AUTO">
<%
String param = "";
if (spaceId != null && spaceId.length() >= 3){
    param = "?SpaceId=" + spaceIdForMain;
}
if (strGoTo==null)
{
    if (strGoToNew==null)
    {
%>
        <frame src="Main.jsp<%=param%>" name="MyMain" marginwidth="10" marginheight="10" frameborder="NO" scrolling="AUTO">
<%
    }
    else
    {
%>
        <frame src="<%=URLManager.getApplicationURL()+strGoToNew%>" marginwidth="10" name="MyMain" marginheight="10" frameborder="NO" scrolling="AUTO">
<%
    	s.putValue("gotoNew",null);
    }
}
else
{
%>
    <frame src="<%=strGoTo%>" marginwidth="10" name="MyMain" marginheight="10" frameborder="NO" scrolling="AUTO">
<%
	s.putValue("goto",null);
}
%>
  </frameset>
<noframes></noframes>
</html>