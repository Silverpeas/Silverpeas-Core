<%--

    Copyright (C) 2000 - 2022 Silverpeas

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
<%
response.setHeader("Cache-Control","no-store"); //HTTP 1.1
response.setHeader("Pragma","no-cache");        //HTTP 1.0
response.setDateHeader ("Expires",-1);          //prevents caching at the proxy server
%>
<%@ page import="org.silverpeas.web.thumbnail.ThumbnailSessionController"%>
<%@ page import="org.silverpeas.core.util.URLUtil"%>
<%@ page import="org.silverpeas.core.util.MultiSilverpeasBundle"%>

<%@ page import="org.silverpeas.core.util.ResourceLocator"%>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.GraphicElementFactory"%>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.browsebars.BrowseBar"%>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.buttonpanes.ButtonPane"%>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.frame.Frame"%>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.operationpanes.OperationPane"%>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.tabs.TabbedPane"%>
<%// En fonction de ce dont vous avez besoin %>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.window.Window"%>

<%@ page errorPage="../../admin/jsp/errorpage.jsp"%>
<%


GraphicElementFactory gef = (GraphicElementFactory) session.getAttribute(GraphicElementFactory.GE_FACTORY_SESSION_ATT);

ThumbnailSessionController scc = (ThumbnailSessionController) request.getAttribute("thumbnail");
if (scc == null)
{
    // No questionReply session controller in the request -> security exception
    String sessionTimeout = ResourceLocator.getGeneralSettingBundle().getString("sessionTimeout");
    getServletConfig().getServletContext().getRequestDispatcher(sessionTimeout).forward(request, response);
    return;
}

MultiSilverpeasBundle resource = (MultiSilverpeasBundle)request.getAttribute("resources");

String language = scc.getLanguage();

String m_context = URLUtil.getApplicationURL();

Window window = gef.getWindow();
BrowseBar browseBar = window.getBrowseBar();
OperationPane operationPane = window.getOperationPane();
TabbedPane tabbedPane = gef.getTabbedPane();
Frame frame = gef.getFrame();

String sRequestURL = request.getRequestURL().toString();
String m_sAbsolute = sRequestURL.substring(0, sRequestURL.length() - request.getRequestURI().length());
//Example: http://myserver
String httpServerBase = ResourceLocator.getGeneralSettingBundle().getString("httpServerBase",
    m_sAbsolute);
%>