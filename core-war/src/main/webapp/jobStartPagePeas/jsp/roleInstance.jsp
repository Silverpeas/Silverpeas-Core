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
<%@ page import="java.util.ArrayList"%>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.tabs.TabbedPane" %>

<fmt:setLocale value="${sessionScope['SilverSessionController'].favoriteLanguage}" />
<view:setBundle bundle="${requestScope.resources.multilangBundle}" />

<%@ include file="check.jsp" %>
<%
	ComponentInst componentInst 	= (ComponentInst) request.getAttribute("ComponentInst");

	ArrayList 	m_Profiles 			= (ArrayList) request.getAttribute("Profiles");

	ProfileInst inheritedProfile 	= (ProfileInst) request.getAttribute("InheritedProfile");
	List 		inheritedGroups		= (List) request.getAttribute("listInheritedGroups"); //List of GroupDetail
	List 		inheritedUsers 		= (List) request.getAttribute("listInheritedUsers"); //List of UserDetail

	ProfileInst m_Profile 			= (ProfileInst) request.getAttribute("Profile");
	List 		m_listGroup 		= (List) request.getAttribute("listGroup"); //List of GroupDetail
	List 		m_listUser 			= (List) request.getAttribute("listUser"); //List of UserDetail
	boolean 	isInHeritanceEnable = ((Boolean)request.getAttribute("IsInheritanceEnable")).booleanValue();
	String		help				= (String) request.getAttribute("ProfileHelp");
	int			scope				= ((Integer) request.getAttribute("Scope")).intValue();

	if (scope == JobStartPagePeasSessionController.SCOPE_FRONTOFFICE) {
	  // use default breadcrumb
	  browseBar.setSpaceJavascriptCallback(null);
	  browseBar.setComponentJavascriptCallback(null);
	}

	browseBar.setComponentId(componentInst.getId());

	String profile = m_Profile.getLabel();
	browseBar.setExtraInformation(profile);

	//Onglets
    TabbedPane tabbedPane = gef.getTabbedPane();
    tabbedPane.addTab(resource.getString("GML.description"),"GoToCurrentComponent",false);
    Iterator j = m_Profiles.iterator();
	ProfileInst theProfile = null;
	String name = null;

	while (j.hasNext()) {
		theProfile = (ProfileInst) j.next();
		name = theProfile.getName();
		profile = theProfile.getLabel();
		if (name.equals(m_Profile.getName()))
			tabbedPane.addTab(profile,"RoleInstance?IdProfile="+theProfile.getId()+"&NameProfile="+name+"&LabelProfile="+theProfile.getLabel(),true);
		else
			tabbedPane.addTab(profile,"RoleInstance?IdProfile="+theProfile.getId()+"&NameProfile="+name+"&LabelProfile="+theProfile.getLabel(),false);
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
<body id="admin-role">
<%
out.println(window.printBefore());
out.println(tabbedPane.print());
%>
<view:frame>
<% if (StringUtil.isDefined(help)) { %>
	<span class="inlineMessage">
	<%= help %>
	</span>
	<br clear="all"/>
<% } %>

	<% if (inheritedProfile != null && (!inheritedGroups.isEmpty() || !inheritedUsers.isEmpty())) { %>
  <viewTags:displayListOfUsersAndGroups users="<%=inheritedUsers%>" groups="<%=inheritedGroups%>" label="<%=labelInheritedRights%>" displayAvatar="false"/>
	<br/>
	<% } %>
<form name="roleList" action="EffectiveSetInstanceProfile" method="post">
	<viewTags:displayListOfUsersAndGroups users="<%=m_listUser%>" groups="<%=m_listGroup%>"
                                        label="<%=labelLocalRights%>" displayLabel="<%=isInHeritanceEnable%>"
                                        id="roleItems" updateCallback="SelectUsersGroupsProfileInstance" displayAvatar="false"
                                        formSaveSelector="form[name=roleList]"/>
</form>
<view:buttonPane>
  <fmt:message var="backButton" key="GML.back"/>
  <view:button label="${backButton}" action="GoToCurrentComponent"/>
</view:buttonPane>
</view:frame>
<%
out.println(window.printAfter());
%>
</body>
</html>