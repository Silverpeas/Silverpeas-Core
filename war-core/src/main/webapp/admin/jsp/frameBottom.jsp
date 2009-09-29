<%--

    Copyright (C) 2000 - 2009 Silverpeas

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    As a special exception to the terms and conditions of version 3.0 of
    the GPL, you may redistribute this Program in connection with Free/Libre
    Open Source Software ("FLOSS") applications as described in Silverpeas's
    FLOSS exception.  You should have recieved a copy of the text describing
    the FLOSS exception, and it is also available here:
    "http://repository.silverpeas.com/legal/licensing"

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

--%>
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