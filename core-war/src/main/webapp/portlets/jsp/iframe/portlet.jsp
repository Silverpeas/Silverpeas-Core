<%@ page import="org.silverpeas.core.util.StringUtil" %><%--

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

<%@ include file="../portletImport.jsp"%>

<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>

<portlet:defineObjects/>
<fmt:setLocale value="${sessionScope['SilverSessionController'].favoriteLanguage}" />
<view:setBundle basename="org.silverpeas.portlets.multilang.portletsBundle"/>

<%
RenderRequest pReq = (RenderRequest)request.getAttribute("javax.portlet.request");
WindowState windowState = pReq.getWindowState();

String height = "350px";
if (windowState.equals(WindowState.MAXIMIZED))
	height = "750px";

PortletPreferences pref = pReq.getPreferences();
String url = pref.getValue("url","");

if (StringUtil.isDefined(url)) { %>
	<iframe src="<%=url%>" frameborder="0" scrolling="auto" width="98%" height="<%=height%>"></iframe>
<% } else { %>
	<fmt:message key="portlets.portlet.iframe.pleaseURL"/>
<% } %>