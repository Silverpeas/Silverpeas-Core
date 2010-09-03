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

<%@ page import="com.stratelia.silverpeas.notificationserver.channel.popup.POPUPSessionController"%>
<%@ page import="com.stratelia.webactiv.util.GeneralPropertiesManager"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.Encode"%>
<%@ page import="com.stratelia.silverpeas.util.ResourcesWrapper"%>
<%@ page import="java.util.*"%>
<%@ page import="com.stratelia.webactiv.util.*"%>

<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.GraphicElementFactory"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.buttons.*"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.buttonPanes.*"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.icons.*"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.iconPanes.*"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.arrayPanes.*"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.browseBars.BrowseBar"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.window.Window"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.navigationList.NavigationList"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.frame.Frame"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.board.Board"%>
<%
      POPUPSessionController popupScc = (POPUPSessionController) request.getAttribute("POPUP");

      if (popupScc == null) {
        // No session controller in the request -> security exception
        String sessionTimeout = GeneralPropertiesManager.getGeneralResourceLocator().getString("sessionTimeout");
        getServletConfig().getServletContext().getRequestDispatcher(sessionTimeout).forward(request, response);
        return;
      }

      ResourcesWrapper resource = (ResourcesWrapper) request.getAttribute("resources");
      String m_context = GeneralPropertiesManager.getGeneralResourceLocator().getString("ApplicationURL");

      GraphicElementFactory gef = (GraphicElementFactory) session.getAttribute("SessionGraphicElementFactory");
      Window window = gef.getWindow();
      Frame frame = gef.getFrame();
      Board board = gef.getBoard();

%>