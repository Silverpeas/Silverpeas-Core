<%--

    Copyright (C) 2000 - 2024 Silverpeas

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
    along with this program.  If not, see <https://www.gnu.org/licenses/>.

--%>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%
response.setDateHeader("Expires", -1);
response.setHeader( "Pragma", "no-cache" );
response.setHeader( "Cache-control", "no-cache" );
%>
<%@ page import="org.silverpeas.core.ResourceReference"%>
<%@ page import="org.silverpeas.core.silverstatistics.access.model.HistoryByUser"%>
<%@ page import="org.silverpeas.core.silverstatistics.access.model.HistoryObjectDetail"%>
<%@ page import="org.silverpeas.core.silverstatistics.access.service.StatisticService "%>
<%@ page import="org.silverpeas.kernel.bundle.LocalizationBundle"%>
<%@ page import="org.silverpeas.core.util.MultiSilverpeasBundle"%>
<%@ page import="org.silverpeas.kernel.bundle.ResourceLocator"%>
<%@ page import="org.silverpeas.core.util.ServiceProvider"%>
<%@ page import="org.silverpeas.core.util.URLUtil"%>
<%@ page import="org.silverpeas.core.web.mvc.controller.MainSessionController"%>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.GraphicElementFactory"%>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.browsebars.BrowseBar"%>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.frame.Frame"%>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.operationpanes.OperationPane"%>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.window.Window"%>

<%@ page errorPage="../../admin/jsp/errorpageMain.jsp"%>

<%

GraphicElementFactory gef = (GraphicElementFactory) session.getAttribute(GraphicElementFactory.GE_FACTORY_SESSION_ATT);
String m_context = URLUtil.getApplicationURL();

Window window = gef.getWindow();
Frame frame = gef.getFrame();
BrowseBar browseBar = window.getBrowseBar();
OperationPane operationPane = window.getOperationPane();

MainSessionController 	m_MainSessionCtrl 	= (MainSessionController) session.getAttribute(MainSessionController.MAIN_SESSION_CONTROLLER_ATT);
String 					language 			= m_MainSessionCtrl.getFavoriteLanguage();
LocalizationBundle messages 			= ResourceLocator.getLocalizationBundle("org.silverpeas.statistic.multilang.statistic", language);
LocalizationBundle 		generalMessage 		= ResourceLocator.getGeneralLocalizationBundle(language);
MultiSilverpeasBundle resource 			= (MultiSilverpeasBundle)request.getAttribute("resources");

%>