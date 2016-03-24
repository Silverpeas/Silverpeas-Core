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

<%@page import="org.silverpeas.core.web.mvc.controller.MainSessionController" %>
<%@ page import="org.silverpeas.core.admin.space.SpaceInst"%>
<%@ page import="org.silverpeas.core.util.LocalizationBundle"%>
<%@ page import="org.silverpeas.core.util.ResourceLocator"%>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.GraphicElementFactory"%>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.browsebars.BrowseBar"%>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.buttonpanes.ButtonPane"%>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.operationpanes.OperationPane"%>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.operationpanes.OperationPaneType" %>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.window.Window"%>


<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>

<%
MainSessionController 	m_MainSessionCtrl 	= (MainSessionController) session.getAttribute(MainSessionController.MAIN_SESSION_CONTROLLER_ATT);
if (m_MainSessionCtrl == null)
{
  String sessionTimeout = ResourceLocator.getGeneralSettingBundle().getString("sessionTimeout");
  getServletConfig().getServletContext().getRequestDispatcher(sessionTimeout).forward(request, response);
  return;
}

GraphicElementFactory 	gef 				= (GraphicElementFactory) session.getAttribute("SessionGraphicElementFactory");
String 					language 			= m_MainSessionCtrl.getFavoriteLanguage();
LocalizationBundle 		message 			= ResourceLocator.getLocalizationBundle("org.silverpeas.portlets.multilang.portletsBundle", language);
String 					m_context 			= ResourceLocator.getGeneralSettingBundle().getString("ApplicationURL");
%>

<fmt:setLocale value="<%= language %>"/>
<view:setBundle basename="org.silverpeas.portlets.multilang.portletsBundle" />

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"></meta>
    <title><fmt:message key="portlets.homepage"/></title>
	<view:looknfeel />
    <c:set var="list" value="${sessionScope['com.sun.portal.portletcontainer.markupHeaders']}" />
    <c:forEach items="${list}" var="portlet">
        <c:out value="${portlet}" escapeXml="false" />
    </c:forEach>
</head>
<body id="portletPages">

<div id="portal-page">
