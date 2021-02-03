<%--

    Copyright (C) 2000 - 2021 Silverpeas

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
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib tagdir="/WEB-INF/tags/silverpeas/util" prefix="viewTags" %>
<%@ include file="check.jsp" %>
<%@ page import="org.silverpeas.core.admin.service.SpaceProfile" %>

<fmt:setLocale value="${sessionScope['SilverSessionController'].favoriteLanguage}" />
<view:setBundle bundle="${requestScope.resources.multilangBundle}" />

<c:set var="m_SpaceExtraInfos" value="${requestScope['SpaceExtraInfos']}"/>

<%
	SpaceProfile spaceProfile = (SpaceProfile) request.getAttribute("SpaceProfile");
	String				role				= (String) request.getAttribute("Role");
	boolean 			isInHeritanceEnable = ((Boolean)request.getAttribute("IsInheritanceEnable")).booleanValue();

	String 				spaceId				= (String) request.getAttribute("CurrentSpaceId");
	
	String nameProfile =  resource.getString("JSPP."+role);
	
	browseBar.setSpaceId(spaceId);
	browseBar.setExtraInformation(nameProfile);

	//Onglets
  TabbedPane tabbedPane = gef.getTabbedPane();
	tabbedPane.addTab(resource.getString("GML.description"),"StartPageInfo", false);
	tabbedPane.addTab(resource.getString("JSPP.SpaceAppearance"), "SpaceLook", false);
  tabbedPane.addTab(resource.getString("JSPP.Manager"), "SpaceManager", role.equals("Manager"));
  if (isInHeritanceEnable) {
	  tabbedPane.addTab(resource.getString("JSPP.admin"), "SpaceManager?Role=admin", role.equals("admin"));
    tabbedPane.addTab(resource.getString("JSPP.publisher"), "SpaceManager?Role=publisher", role.equals("publisher"));
    tabbedPane.addTab(resource.getString("JSPP.writer"), "SpaceManager?Role=writer", role.equals("writer"));
    tabbedPane.addTab(resource.getString("JSPP.reader"), "SpaceManager?Role=reader", role.equals("reader"));
  }

  String labelInheritedRights = resource.getString("JSPP.inheritedRights");
  String labelLocalRights = resource.getString("JSPP.localRights");
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title><%=resource.getString("GML.popupTitle")%></title>
<view:looknfeel withFieldsetStyle="true"/>
</head>
<body id="admin-role" class="page_content_admin">
<%
out.println(window.printBefore());
out.println(tabbedPane.print());
%>
<view:frame>
<% if (role.equals("Manager")) { %>
	<span class="inlineMessage">
	<%=resource.getString("JSPP.Manager.help")%>
	</span>
	<br clear="all"/>
<% } %>

	<viewTags:displayListOfUsersAndGroups userIds="<%=spaceProfile.getInheritedUserIds()%>" groupIds="<%=spaceProfile.getInheritedGroupIds()%>" label="<%=labelInheritedRights%>" displayAvatar="false" hideEmptyList="true"/>

  <form name="roleList" action="EffectiveSetSpaceProfile" method="post">
    <c:set var="callback" value=""/>
    <c:if test="${m_SpaceExtraInfos.admin}">
      <c:set var="callback" value="SelectUsersGroupsSpace"/>
    </c:if>
    <viewTags:displayListOfUsersAndGroups userIds="<%=spaceProfile.getUserIds()%>" groupIds="<%=spaceProfile.getGroupIds()%>"
                                          label="<%=labelLocalRights%>" displayLabel="<%=isInHeritanceEnable%>"
                                          id="roleItems" updateCallback="${callback}" displayAvatar="false"
                                          formSaveSelector="form[name=roleList]"/>
    <input type="hidden" name="Role" value="<%=role%>"/>
  </form>

  <view:buttonPane>
    <fmt:message var="backButton" key="GML.back"/>
    <view:button label="${backButton}" action="StartPageInfo"/>
  </view:buttonPane>

</view:frame>
<%
out.println(window.printAfter());
%>
</body>
</html>