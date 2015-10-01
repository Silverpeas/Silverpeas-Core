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

<%
response.setHeader("Cache-Control","no-store"); //HTTP 1.1
response.setHeader("Pragma","no-cache");        //HTTP 1.0
response.setDateHeader ("Expires",-1);          //prevents caching at the proxy server
%>
<%@ page import="javax.servlet.*"%>
<%@ page import="javax.servlet.http.*"%>
<%@ page import="javax.servlet.jsp.*"%>

<%@ page import="org.silverpeas.util.viewGenerator.html.*"%>
<%@ page import="org.silverpeas.util.viewGenerator.html.window.Window"%>
<%@ page import="org.silverpeas.util.viewGenerator.html.operationPanes.OperationPane"%>
<%@ page import="org.silverpeas.util.viewGenerator.html.browseBars.BrowseBar"%>
<%@ page import="org.silverpeas.util.viewGenerator.html.frame.Frame"%>
<%@ page import="org.silverpeas.util.MultiSilverpeasBundle"%>
<%@ page import="com.stratelia.silverpeas.peasCore.URLManager"%>
<%// En fonction de ce dont vous avez besoin %>
<%@ page import="org.silverpeas.util.viewGenerator.html.arrayPanes.ArrayPane"%>
<%@ page import="org.silverpeas.util.viewGenerator.html.arrayPanes.ArrayLine"%>
<%@ page import="org.silverpeas.util.viewGenerator.html.arrayPanes.ArrayColumn"%>
<%@ page import="org.silverpeas.util.viewGenerator.html.arrayPanes.ArrayCellText"%>
<%@ page import="org.silverpeas.util.viewGenerator.html.arrayPanes.ArrayCellLink"%>
<%@ page import="org.silverpeas.util.viewGenerator.html.arrayPanes.*"%>
<%@ page import="org.silverpeas.util.viewGenerator.html.iconPanes.IconPane"%>
<%@ page import="org.silverpeas.util.viewGenerator.html.icons.Icon"%>
<%@ page import="org.silverpeas.util.viewGenerator.html.tabs.TabbedPane"%>
<%@ page import="org.silverpeas.util.viewGenerator.html.navigationList.NavigationList"%>
<%@ page import="org.silverpeas.util.viewGenerator.html.buttonPanes.ButtonPane"%>
<%@ page import="org.silverpeas.util.viewGenerator.html.buttons.Button"%>
<%@ page import="org.silverpeas.util.*"%>
<%@ page import="org.silverpeas.util.ResourceLocator"%>
<%@ page import="org.silverpeas.util.viewGenerator.html.GraphicElementFactory"%>

<%@ page import="com.silverpeas.thumbnail.ThumbnailSessionController"%>
<%@ page import="org.silverpeas.util.GeneralPropertiesManager" %>
<%@ page errorPage="../../admin/jsp/errorpage.jsp"%>
<%


GraphicElementFactory gef = (GraphicElementFactory) session.getAttribute(GraphicElementFactory.GE_FACTORY_SESSION_ATT);

ThumbnailSessionController scc = (ThumbnailSessionController) request.getAttribute("thumbnail");
if (scc == null)
{
    // No questionReply session controller in the request -> security exception
    String sessionTimeout = GeneralPropertiesManager.getString("sessionTimeout");
    getServletConfig().getServletContext().getRequestDispatcher(sessionTimeout).forward(request, response);
    return;
}

MultiSilverpeasBundle resource = (MultiSilverpeasBundle)request.getAttribute("resources");

String language = scc.getLanguage();

String m_context = URLManager.getApplicationURL();

Window window = gef.getWindow();
BrowseBar browseBar = window.getBrowseBar();
OperationPane operationPane = window.getOperationPane();
TabbedPane tabbedPane = gef.getTabbedPane();
Frame frame = gef.getFrame();

String sRequestURL = request.getRequestURL().toString();
String m_sAbsolute = sRequestURL.substring(0, sRequestURL.length() - request.getRequestURI().length());
//Example: http://myserver
String httpServerBase = GeneralPropertiesManager.getString("httpServerBase", m_sAbsolute);
%>