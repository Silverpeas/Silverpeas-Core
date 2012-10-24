<%--

    Copyright (C) 2000 - 2012 Silverpeas

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
<%@ page import="java.util.List"%>
<%@ page import="java.util.ArrayList"%>
<%@ page import="java.util.Vector"%>
<%@ page import="java.util.Properties"%>
<%@ page import="java.util.Iterator"%>

<%@ page import="com.silverpeas.util.EncodeHelper"%>

<%@ page import="com.stratelia.webactiv.beans.admin.SpaceInstLight"%>
<%@ page import="com.stratelia.silverpeas.peasCore.URLManager"%>
<%@ page import="com.stratelia.silverpeas.util.ResourcesWrapper"%>
<%@ page import="com.stratelia.webactiv.util.ResourceLocator"%>

<%@ page import="com.stratelia.silverpeas.personalizationPeas.control.PersonalizationSessionController"%>
<%@ page import="com.stratelia.webactiv.util.GeneralPropertiesManager"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.GraphicElementFactory"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.board.*"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.buttons.*"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.buttonPanes.*"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.operationPanes.OperationPane"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.arrayPanes.ArrayPane"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.arrayPanes.ArrayLine"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.arrayPanes.ArrayColumn"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.arrayPanes.ArrayCellText"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.arrayPanes.ArrayCellLink"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.arrayPanes.*"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.iconPanes.IconPane"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.icons.Icon"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.tabs.*"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.browseBars.BrowseBar"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.window.Window"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.frame.Frame"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.Encode"%>
<%@ page import="com.stratelia.webactiv.beans.admin.UserDetail"%>
<%@ page import="com.stratelia.webactiv.beans.admin.UserFull"%>

<%@ page errorPage="../../admin/jsp/errorpage.jsp"%>

<%
	PersonalizationSessionController personalizationScc = (PersonalizationSessionController) request.getAttribute("personalizationPeas");
	if (personalizationScc == null) {
		// No session controller in the request -> security exception
		String sessionTimeout = GeneralPropertiesManager.getGeneralResourceLocator().getString("sessionTimeout");
		getServletConfig().getServletContext().getRequestDispatcher(sessionTimeout).forward(request, response);
		return;
	}

	String m_context          = GeneralPropertiesManager.getGeneralResourceLocator().getString("ApplicationURL");

	// Icones operationBar
	String addNotif           = m_context + "/util/icons/addEvent.gif";
	String paramNotif         = m_context + "/util/icons/notification_param.gif";
	String addGuideline       = m_context + "/util/icons/notification_to_guidlines.gif";
	String addProtocol        = m_context + "/util/icons/notification_to_add.gif";

	// Icones diverses
	String delete             = m_context + "/util/icons/delete.gif";
	String modif              = m_context + "/util/icons/update.gif";
	String mandatoryField     = m_context + "/util/icons/mandatoryField.gif";
	String test               = m_context + "/util/icons/test.gif";
	String on_default         = m_context + "/util/icons/btRadio_on.gif";
	String off_default        = m_context + "/util/icons/btRadio_off.gif";

	// Pixels
	String ArrayPnoColorPix   = m_context + "/util/icons/colorPix/15px.gif";

	ResourcesWrapper 	resource 	= (ResourcesWrapper) request.getAttribute("resources");
	ResourceLocator 	rs 			= new ResourceLocator("com.stratelia.silverpeas.personalizationPeas.settings.personalizationPeasSettings", "");
	ResourceLocator general = new ResourceLocator("com.stratelia.silverpeas.lookAndFeel.generalLook", "");

	GraphicElementFactory gef = (GraphicElementFactory) session.getAttribute("SessionGraphicElementFactory");

	Window 		window 		= gef.getWindow();
	BrowseBar 	browseBar 	= window.getBrowseBar();
	Frame 		frame 		= gef.getFrame();
	Board		board		= gef.getBoard();
%>