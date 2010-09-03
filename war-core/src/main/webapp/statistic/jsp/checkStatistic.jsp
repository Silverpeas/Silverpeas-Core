<%--

    Copyright (C) 2000 - 2009 Silverpeas

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    As a special exception to the terms and conditions of version 3.0 of
    the GPL, you may redistribute this Program in connection with Free/Libre
    Open Source Software ("FLOSS") applications as described in Silverpeas's
    FLOSS exception.  You should have received a copy of the text describing
    the FLOSS exception, and it is also available here:
    "http://repository.silverpeas.com/legal/licensing"

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

--%>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%
response.setHeader( "Expires", "Tue, 21 Dec 1993 23:59:59 GMT" );
response.setHeader( "Pragma", "no-cache" );
response.setHeader( "Cache-control", "no-cache" );
response.setHeader( "Last-Modified", "Fri, Jan 25 2099 23:59:59 GMT" );
%>
<%@ page import="com.stratelia.silverpeas.util.ResourcesWrapper"%>
<%@ page import="com.stratelia.webactiv.util.GeneralPropertiesManager,
                 com.stratelia.silverpeas.peasCore.MainSessionController,
                 com.stratelia.webactiv.util.ResourceLocator"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.GraphicElementFactory "%>
<%@ page import="com.stratelia.silverpeas.peasCore.URLManager"%>
<%@ page import="com.stratelia.webactiv.util.DateUtil"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.arrayPanes.*"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.browseBars.BrowseBar"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.iconPanes.IconPane"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.icons.Icon"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.*"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.window.Window"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.frame.Frame"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.operationPanes.OperationPane"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.buttonPanes.ButtonPane"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.buttons.Button"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.board.Board"%>
<%@ page import="com.silverpeas.util.ForeignPK"%>
<%@ page import="java.util.Collection"%>
<%@ page import="java.util.Iterator"%>
<%@ page import="com.stratelia.webactiv.util.statistic.model.HistoryByUser"%>
<%@ page import="com.stratelia.webactiv.util.statistic.model.HistoryObjectDetail"%>
<%@ page import="com.stratelia.webactiv.util.statistic.control.StatisticBm"%>
<%@ page import="com.stratelia.webactiv.util.statistic.control.StatisticBmHome"%>
<%@ page import="com.stratelia.webactiv.util.EJBUtilitaire"%>
<%@ page import="com.stratelia.webactiv.util.JNDINames"%>
<%@ page import="com.stratelia.webactiv.util.DateUtil"%>

<%@ page import="java.net.*"%>

<%@ page errorPage="../../admin/jsp/errorpageMain.jsp"%>

<%

GraphicElementFactory gef = (GraphicElementFactory) session.getAttribute("SessionGraphicElementFactory");
String m_context = GeneralPropertiesManager.getGeneralResourceLocator().getString("ApplicationURL");

Window window = gef.getWindow();
Frame frame = gef.getFrame();
BrowseBar browseBar = window.getBrowseBar();
OperationPane operationPane = window.getOperationPane();

MainSessionController 	m_MainSessionCtrl 	= (MainSessionController) session.getAttribute("SilverSessionController");
String 					language 			= m_MainSessionCtrl.getFavoriteLanguage();
ResourceLocator 		messages 			= new ResourceLocator("com.silverpeas.statistic.multilang.statistic", language);
ResourceLocator 		generalMessage 		= GeneralPropertiesManager.getGeneralMultilang(language);
ResourcesWrapper 		resource 			= (ResourcesWrapper)request.getAttribute("resources");

%>