<%--

    Copyright (C) 2000 - 2013 Silverpeas

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    As a special exception to the terms and conditions of version 3.0 of
    the GPL, you may redistribute this Program in connection with Free/Libre
    Open Source Software ("FLOSS") applications as described in Silverpeas's
    FLOSS exception.  You should have received a copy of the text describing
    the FLOSS exception, and it is also available here:
    "http://www.silverpeas.org/docs/core/legal/floss_exception.html"

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

--%>

<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%@ include file="check.jsp" %>
<html>
<head>
<TITLE><%=resource.getString("GML.popupTitle")%></TITLE>
<script language="javascript">
<!--
function MM_reloadPage(init) {  //reloads the window if Nav4 resized
  if (init==true) with (navigator) {if ((appName=="Netscape")&&(parseInt(appVersion)==4)) {
    document.MM_pgW=innerWidth; document.MM_pgH=innerHeight; onresize=MM_reloadPage; }}
  else if (innerWidth!=document.MM_pgW || innerHeight!=document.MM_pgH) location.reload();
}
MM_reloadPage(true);
function jumpToSpace(spaceId)
{
	window.startPageNavigation.location.href="GoToSubSpace?SubSpace="+spaceId;
}
function jumpToComponent(componentId)
{
	window.startPageContent.location.href="GoToComponent?ComponentId="+componentId;
}
//-->
</script>

</head>
  <frameset cols="200,*" border="0" framespacing="5" frameborder="NO">
    <frame src="jobStartPageNav" marginwidth="0" marginheight="10" name="startPageNavigation" frameborder="0" scrolling="AUTO">
    <frame src="welcome" name="startPageContent" marginwidth="10" marginheight="10" frameborder="0" scrolling="AUTO">
  </frameset>
<noframes></noframes>


</html>