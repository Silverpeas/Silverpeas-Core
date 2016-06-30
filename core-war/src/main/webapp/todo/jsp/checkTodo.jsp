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

<%
response.setHeader("Cache-Control","no-store"); //HTTP 1.1
response.setHeader("Pragma","no-cache"); //HTTP 1.0
response.setDateHeader ("Expires",-1); //prevents caching at the proxy server
%>

<%@ page import="org.silverpeas.core.admin.component.model.ComponentInstLight"%>
<%@ page import="org.silverpeas.core.admin.space.SpaceInstLight"%>
<%@ page import="org.silverpeas.core.admin.user.model.UserDetail"%>
<%@ page import="org.silverpeas.core.personalorganizer.model.Attendee"%>
<%@ page import="org.silverpeas.core.personalorganizer.model.ToDoHeader"%>
<%@ page import="org.silverpeas.web.todo.control.ToDoSessionController"%>
<%@ page import="org.silverpeas.core.util.LocalizationBundle"%>
<%@ page import="org.silverpeas.core.util.MultiSilverpeasBundle"%>
<%@ page import="org.silverpeas.core.util.ResourceLocator"%>

<%@ page import="org.silverpeas.core.util.SettingBundle"%>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.GraphicElementFactory"%>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.arraypanes.ArrayCellText"%>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.arraypanes.ArrayColumn"%>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.arraypanes.ArrayLine"%>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.arraypanes.ArrayPane"%>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.browsebars.BrowseBar"%>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.frame.Frame"%>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.operationpanes.OperationPane"%>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.tabs.TabbedPane"%>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.window.Window"%>
<%@ page import="java.util.Collection"%>
<%@ page import="java.util.Date"%>
<%@ page import="java.util.Iterator"%>

<%@ page errorPage="../../admin/jsp/errorpageMain.jsp"%>

<%
	GraphicElementFactory graphicFactory 	= (GraphicElementFactory) session.getAttribute("SessionGraphicElementFactory");
	ToDoSessionController 	todo 			= (ToDoSessionController) request.getAttribute("todo");
	MultiSilverpeasBundle resources 		= (MultiSilverpeasBundle)request.getAttribute("resources");

	if (todo == null)
	{
	String sessionTimeout = ResourceLocator.getGeneralSettingBundle().getString("sessionTimeout");
	getServletConfig().getServletContext().getRequestDispatcher(sessionTimeout).forward(request, response);
	return;
	}
%>