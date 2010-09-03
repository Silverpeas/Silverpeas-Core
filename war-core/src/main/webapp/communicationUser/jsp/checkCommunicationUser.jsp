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
response.setHeader("Cache-Control","no-store"); //HTTP 1.1
response.setHeader("Pragma","no-cache");        //HTTP 1.0
response.setDateHeader ("Expires",-1);          //prevents caching at the proxy server
%>

<%@ page import="com.silverpeas.communicationUser.control.CommunicationUserSessionController"%>
<%@ page import="com.stratelia.silverpeas.peasCore.URLManager"%>
<%@ page import="com.stratelia.silverpeas.peasCore.*"%>
<%@ page import="com.stratelia.webactiv.beans.admin.OrganizationController"%>

<%@ page import="java.util.*"%>
<%@ page import="com.stratelia.webactiv.util.*"%>
<%@ page import="com.stratelia.silverpeas.util.ResourcesWrapper"%>

<%//____/ VIEW GENERATOR \_________________________________________________________________________%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.GraphicElementFactory"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.Encode"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.buttons.*"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.buttonPanes.*"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.icons.*"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.iconPanes.*"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.arrayPanes.*"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.tabs.*"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.browseBars.BrowseBar"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.window.Window"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.navigationList.NavigationList"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.frame.Frame"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.board.Board"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.operationPanes.OperationPane"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.*"%>
 
<%
	CommunicationUserSessionController communicationScc = (CommunicationUserSessionController) request.getAttribute("communicationUser");
	ResourceLocator generalMessage = GeneralPropertiesManager.getGeneralMultilang(communicationScc.getLanguage());
	ResourcesWrapper resources = (ResourcesWrapper) request.getAttribute("resources");
	ResourceLocator settings = new ResourceLocator("com.silverpeas.communicationUser.settings.communicationUserSettings", "");
	String m_context        = GeneralPropertiesManager.getGeneralResourceLocator().getString("ApplicationURL");
	String mandatoryField     = m_context + "/util/icons/mandatoryField.gif";
	GraphicElementFactory gef = (GraphicElementFactory) session.getAttribute("SessionGraphicElementFactory");
	Window window = gef.getWindow();
	Frame frame = gef.getFrame();
	Board board = gef.getBoard();
	OperationPane operationPane = window.getOperationPane();
%>
