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
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib tagdir="/WEB-INF/tags/silverpeas/util" prefix="viewTags" %>

<fmt:setLocale value="${sessionScope['SilverSessionController'].favoriteLanguage}" />
<view:setBundle bundle="${requestScope.resources.multilangBundle}" />

<%@ include file="check.jsp" %>
<%
	Domain  domObject 		= (Domain)request.getAttribute("domainObject");
    Group 	grObject 		= (Group)request.getAttribute("groupObject");
    String 	groupsPath 		= (String)request.getAttribute("groupsPath");

    List<Group>	groups		= (List<Group>) request.getAttribute("Groups");
    List<UserDetail>	users		= (List<UserDetail>) request.getAttribute("Users");

    String thisGroupId = grObject.getId();

    browseBar.setComponentName(getDomainLabel(domObject, resource), "domainContent?Iddomain="+domObject.getId());
    if (groupsPath != null) {
      browseBar.setPath(groupsPath);
    }
%>
<html>
<head>
<view:looknfeel withFieldsetStyle="true"/>
</head>
<body>
<%
out.println(window.printBefore());

TabbedPane tabbedPane = gef.getTabbedPane();
tabbedPane.addTab(resource.getString("GML.description"), "groupContent?Idgroup="+thisGroupId, false);
tabbedPane.addTab(resource.getString("JDP.roleManager"), "groupManagersView?Id="+thisGroupId, true);
out.println(tabbedPane.print());
%>
<view:frame>
<form name="roleList" action="groupManagersUpdate" method="post">
  <fmt:message var="fieldsetLabel" key="JDP.roleManager"/>
  <viewTags:displayListOfUsersAndGroups users="<%=users%>" groups="<%=groups%>"
                                        id="roleItems" updateCallback="groupManagersChoose" label="${fieldsetLabel}"
                                        formSaveSelector="form[name=roleList]"/>
</form>
  <view:buttonPane>
    <fmt:message var="backButton" key="GML.back"/>
    <view:button label="${backButton}" action="groupContent?Idgroup=<%=grObject.getId()%>"/>
  </view:buttonPane>
</view:frame>
<%
out.println(window.printAfter());
%>
</body>
</html>