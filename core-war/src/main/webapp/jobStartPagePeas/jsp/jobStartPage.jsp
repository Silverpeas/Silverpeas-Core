<%--

    Copyright (C) 2000 - 2017 Silverpeas

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    As a special exception to the terms and conditions of version 3.0 of
    the GPL, you may redistribute this Program in connection with Free/Libre
    Open Source Software ("FLOSS") applications as described in Silverpeas's
    FLOSS exception.  You should have received a copy of the text describing
    the FLOSS exception, and it is also available here:
    "https://www.silverpeas.org/legal/floss_exception.html"

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

--%>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%@ include file="check.jsp" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<title><%=resource.getString("GML.popupTitle")%></title>
<%
  String spaceId = request.getParameter("SpaceId");
  String navbarURL = "jobStartPageNav";
  if (StringUtil.isDefined(spaceId)) {
    navbarURL = "GoToSpace?Espace="+spaceId;
  }
%>
<script language="javascript">
<!--
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
    <frame src="<%=navbarURL%>" marginwidth="0" marginheight="10" name="startPageNavigation" frameborder="0" scrolling="AUTO">
    <frame src="welcome" name="startPageContent" marginwidth="10" marginheight="10" frameborder="0" scrolling="AUTO">
  </frameset>
<noframes></noframes>


</html>