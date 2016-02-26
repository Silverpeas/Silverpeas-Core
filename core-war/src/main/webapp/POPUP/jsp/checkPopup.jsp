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

<%@ page import="com.stratelia.silverpeas.notificationserver.channel.popup.POPUPSessionController"%>

<%@ page import="org.silverpeas.util.viewGenerator.html.Encode"%>
<%@ page import="org.silverpeas.util.MultiSilverpeasBundle"%>
<%@ page import="java.util.*"%>
<%@ page import="org.silverpeas.util.*"%>

<%@ page import="org.silverpeas.util.viewGenerator.html.GraphicElementFactory"%>
<%@ page import="org.silverpeas.util.viewGenerator.html.buttons.*"%>
<%@ page import="org.silverpeas.util.viewGenerator.html.buttonPanes.*"%>
<%@ page import="org.silverpeas.util.viewGenerator.html.icons.*"%>
<%@ page import="org.silverpeas.util.viewGenerator.html.iconPanes.*"%>
<%@ page import="org.silverpeas.util.viewGenerator.html.arrayPanes.*"%>
<%@ page import="org.silverpeas.util.viewGenerator.html.browseBars.BrowseBar"%>
<%@ page import="org.silverpeas.util.viewGenerator.html.window.Window"%>
<%@ page import="org.silverpeas.util.viewGenerator.html.navigationList.NavigationList"%>
<%@ page import="org.silverpeas.util.viewGenerator.html.frame.Frame"%>
<%@ page import="org.silverpeas.util.viewGenerator.html.board.Board"%>
<%
      POPUPSessionController popupScc = (POPUPSessionController) request.getAttribute("POPUP");

      if (popupScc == null) {
        // No session controller in the request -> security exception
        String sessionTimeout = ResourceLocator.getGeneralSettingBundle().getString("sessionTimeout");
        getServletConfig().getServletContext().getRequestDispatcher(sessionTimeout).forward(request, response);
        return;
      }

      MultiSilverpeasBundle resource = (MultiSilverpeasBundle) request.getAttribute("resources");
      String m_context = ResourceLocator.getGeneralSettingBundle().getString("ApplicationURL");

      GraphicElementFactory gef = (GraphicElementFactory) session.getAttribute("SessionGraphicElementFactory");
      Window window = gef.getWindow();
      Frame frame = gef.getFrame();
      Board board = gef.getBoard();

%>