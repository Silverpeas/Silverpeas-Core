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
<%@ page import="org.silverpeas.web.personalization.control.PersonalizationSessionController"%>
<%@ page import="org.owasp.encoder.Encode"%>
<%@ page import="org.silverpeas.core.util.EncodeHelper"%>
<%@ page import="org.silverpeas.core.util.MultiSilverpeasBundle"%>
<%@ page import="org.silverpeas.core.util.ResourceLocator"%>

<%@ page import="org.silverpeas.core.util.SettingBundle"%>

<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.GraphicElementFactory"%>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.arraypanes.ArrayLine"%>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.arraypanes.ArrayLineTag"%>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.board.Board"%>

<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.browsebars.BrowseBar"%>

<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.buttonpanes.ButtonPane"%>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.buttons.Button"%>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.frame.Frame"%>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.iconpanes.IconPane"%>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.icons.Icon"%>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.operationpanes.OperationPane"%>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.window.Window"%>
<%@ page import="java.util.Properties"%>

<%@ page errorPage="../../admin/jsp/errorpage.jsp"%>

<%
	PersonalizationSessionController personalizationScc = (PersonalizationSessionController) request.getAttribute("personalizationPeas");
	if (personalizationScc == null) {
		// No session controller in the request -> security exception
		String sessionTimeout = ResourceLocator.getGeneralSettingBundle().getString("sessionTimeout");
		getServletConfig().getServletContext().getRequestDispatcher(sessionTimeout).forward(request, response);
		return;
	}

	String m_context          = ResourceLocator.getGeneralSettingBundle().getString("ApplicationURL");

	// Icones operationBar
	String addNotif           = m_context + "/util/icons/addEvent.gif";
	String paramNotif         = m_context + "/util/icons/notification_param.gif";
	String addGuideline       = m_context + "/util/icons/notification_to_guidlines.gif";
	String addProtocol        = m_context + "/util/icons/notification_to_add.gif";

  pageContext.setAttribute("addProtocol", addProtocol );
  pageContext.setAttribute("paramNotif", paramNotif );

	// Icones diverses
	String delete             = m_context + "/util/icons/delete.gif";
	String modif              = m_context + "/util/icons/update.gif";
	String mandatoryField     = m_context + "/util/icons/mandatoryField.gif";
	String test               = m_context + "/util/icons/test.gif";
	String on_default         = m_context + "/util/icons/btRadio_on.gif";
	String off_default        = m_context + "/util/icons/btRadio_off.gif";

	// Pixels
	String ArrayPnoColorPix   = m_context + "/util/icons/colorPix/15px.gif";

	MultiSilverpeasBundle resource 	= (MultiSilverpeasBundle) request.getAttribute("resources");
	SettingBundle rs 			= ResourceLocator.getSettingBundle("org.silverpeas.personalization.settings.personalizationPeasSettings");
	SettingBundle general = ResourceLocator.getSettingBundle("org.silverpeas.lookAndFeel.generalLook");

	GraphicElementFactory gef = (GraphicElementFactory) session.getAttribute("SessionGraphicElementFactory");

	Window 		window 		= gef.getWindow();
	BrowseBar 	browseBar 	= window.getBrowseBar();
	Frame 		frame 		= gef.getFrame();
	Board		board		= gef.getBoard();
%>