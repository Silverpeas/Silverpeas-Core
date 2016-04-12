<%--

    Copyright (C) 2000 - 2013 Silverpeas

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    As a special exception to the terms and conditions of version 3.0 of
    the GPL, you may redistribute this Program in connection with Free/Libre
    Open Source Software ("FLOSS") applications as described in Silverpeas's
    FLOSS exception. You should have received a copy of the text describing
    the FLOSS exception, and it is also available here:
    "http://www.silverpeas.org/docs/core/legal/floss_exception.html"

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program. If not, see <http://www.gnu.org/licenses/>.

--%>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%@ include file="check.jsp" %>

<%
   String formName = (String) request.getAttribute("formName");
   String elementId = (String) request.getAttribute("elementId");
   String elementName = (String) request.getAttribute("elementName");

   UserDetail user = (UserDetail) request.getAttribute("user");
   UserDetail[] users = (UserDetail[]) request.getAttribute("users");

   String id = "";
   String name = "";
   if (user != null)
   {
      id = user.getId();
name = user.getDisplayedName();
   }
   else if (users != null)
   {
for (int u=0; u<users.length; u++)
{
user = users[u];
id += user.getId()+",";
name += user.getDisplayedName()+"\\n";
}
   }

   Group group = (Group) request.getAttribute("group");
   Group[] groups = (Group[]) request.getAttribute("groups");
   if (group != null)
   {
      id = group.getId();
name = group.getName();
   }
   else if (groups != null)
   {
for (int u=0; u<groups.length; u++)
{
group = groups[u];
id += group.getId()+",";
name += group.getName()+"\\n";
}
   }
%>

<HTML>
<HEAD>
<TITLE></TITLE>
<SCRIPT language='Javascript'>
function resetOpener()
{
window.opener.document.getElementById('<%=elementId%>').value="<%=id%>";
window.opener.document.getElementById('<%=elementName%>').value="<%=name%>";
window.close();
}
</SCRIPT>
</HEAD>
<BODY onload="javascript:resetOpener();">
</BODY>
</HTML>