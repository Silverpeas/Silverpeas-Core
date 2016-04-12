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
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<%@ include file="check.jsp" %>

<%
String when = (String) request.getAttribute("When");
String spaceId = (String) request.getAttribute("CurrentSpaceId");

browseBar.setSpaceId(spaceId);

String messageTitle = resource.getString("JSPP.ErrorComponentCreation");
String message = (String) request.getAttribute("ErrorMessage");
if (!StringUtil.isDefined(message)) {
  message = resource.getString("JSPP.ErrorComponentMessage");
}
browseBar.setPath(resource.getString("JSPP.creationInstance"));
if (when.equals("ComponentUpdate")) {
  messageTitle = resource.getString("JSPP.ErrorComponentUpdate");
  browseBar.setPath(resource.getString("GML.modify"));
} else if (when.equals("SpaceCreation")) {
  messageTitle = resource.getString("JSPP.ErrorSpaceCreation");
  message = resource.getString("JSPP.ErrorSpaceMessage");
  browseBar.setPath(resource.getString("JSPP.creationSpace"));
} else if (when.equals("SpaceUpdate")) {
  messageTitle = resource.getString("JSPP.ErrorSpaceUpdate");
  message = resource.getString("JSPP.ErrorSpaceMessage");
  browseBar.setPath(resource.getString("JSPP.updateSpace"));
}
%>

<HTML>
<HEAD>
<TITLE><%=resource.getString("GML.popupTitle")%></TITLE>
<view:looknfeel/>
</HEAD>
<BODY marginheight="5" marginwidth="5" leftmargin="5" topmargin="5">
<%
out.println(window.printBefore());
out.println(frame.printBefore());
out.println(board.printBefore());
%>
<center>
	<br>
	<h3><%=messageTitle%></h3><%=message%><br><br>
</center>
<%
out.println(board.printAfter());
out.println(frame.printAfter());
out.println(window.printAfter());
%>
</BODY>
</HTML>