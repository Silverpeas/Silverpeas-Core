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
<%@ page import="org.silverpeas.web.clipboard.control.ClipboardSessionController"%>
<%@ page import="org.silverpeas.core.util.MultiSilverpeasBundle"%>
<%@ page import="org.silverpeas.core.util.ResourceLocator"%>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.GraphicElementFactory" %>
<%@ page errorPage="../../admin/jsp/errorpage.jsp"%>

<%
	GraphicElementFactory graphicFactory = (GraphicElementFactory) session.getAttribute("SessionGraphicElementFactory");
	ClipboardSessionController clipboardSC = (ClipboardSessionController) request.getAttribute("clipboardScc");

	if (clipboardSC == null)
	{
	    // No quickinfo session controller in the request -> security exception
	    String sessionTimeout = ResourceLocator.getGeneralSettingBundle().getString("sessionTimeout");
	    getServletConfig().getServletContext().getRequestDispatcher(sessionTimeout).forward(request, response);
	    return;
	}

	String m_context = ResourceLocator.getGeneralSettingBundle().getString("ApplicationURL");
	MultiSilverpeasBundle resources = (MultiSilverpeasBundle)request.getAttribute("resources");
%>
