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
response.setHeader("Pragma","no-cache");        //HTTP 1.0
response.setDateHeader ("Expires",-1);          //prevents caching at the proxy server
%>
<%@ page import="javax.servlet.*"%>
<%@ page import="javax.servlet.http.*"%>
<%@ page import="javax.servlet.jsp.*"%>
<%@ page import="java.io.PrintWriter"%>
<%@ page import="java.io.IOException"%>
<%@ page import="java.io.FileInputStream"%>
<%@ page import="java.io.ObjectInputStream"%>
<%@ page import="java.util.Collection"%>
<%@ page import="java.util.Iterator"%>
<%@ page import="java.util.ArrayList"%>
<%@ page import="java.util.Hashtable"%>
<%@ page import="java.beans.*"%>

<%@ page import="com.stratelia.silverpeas.pdc.model.Value"%>
<%@ page import="com.stratelia.silverpeas.pdc.model.Axis"%>
<%@ page import="com.stratelia.silverpeas.pdc.model.AxisHeader"%>

<%@ page import="java.text.SimpleDateFormat"%>
<%@ page import="org.silverpeas.util.viewGenerator.html.*"%>
<%@ page import="org.silverpeas.util.viewGenerator.html.window.Window"%>
<%@ page import="org.silverpeas.util.viewGenerator.html.operationPanes.OperationPane"%>
<%@ page import="org.silverpeas.util.viewGenerator.html.browseBars.BrowseBar"%>
<%@ page import="org.silverpeas.util.viewGenerator.html.frame.Frame"%>
<%@ page import="org.silverpeas.util.MultiSilverpeasBundle"%>

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
<%@ page import="com.stratelia.silverpeas.peasCore.MainSessionController"%>

<%@ page import="org.silverpeas.util.viewGenerator.html.Encode"%>
<%@ page import="com.silverpeas.thesaurusPeas.control.*"%>
<%@ page import="com.silverpeas.thesaurus.model.*"%>
<%@ page import="com.stratelia.silverpeas.pdc.model.*"%>

<%@ page errorPage="../../admin/jsp/errorpage.jsp"%>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<%


GraphicElementFactory gef = (GraphicElementFactory) session.getAttribute("SessionGraphicElementFactory");

ThesaurusSessionController scc = (ThesaurusSessionController) request.getAttribute("thesaurusPeas");
if (scc == null)
{
    // No questionReply session controller in the request -> security exception
    String sessionTimeout = ResourceLocator.getGeneralSettingBundle().getString("sessionTimeout");
    getServletConfig().getServletContext().getRequestDispatcher(sessionTimeout).forward(request, response);
    return;
}
	

MultiSilverpeasBundle resource = (MultiSilverpeasBundle)request.getAttribute("resources");


String spaceLabel = "";
String componentLabel = resource.getString("thesaurus.componentName");

String language = scc.getLanguage();

String m_context = ResourceLocator.getGeneralSettingBundle().getString("ApplicationURL");

Window window = gef.getWindow();
BrowseBar browseBar = window.getBrowseBar();
OperationPane operationPane = window.getOperationPane();
TabbedPane tabbedPane = gef.getTabbedPane();
Frame frame = gef.getFrame();

int maxSyn = 5; //nombre max de synonymes

String boardStart = "<table cellpadding=5 cellspacing=2 border=0 width=\"98%\" class=intfdcolor><tr><td bgcolor=FFFFFF align=left>";
String boardEnd   = "</td></tr></table>";
String openBorder = "<table cellpadding=\"0\" cellspacing=\"0\" border=\"0\" width=\"50%\" class=\"line\"><tr><td><table cellpadding=\"2\" cellspacing=\"1\" border=\"0\" width=\"100%\" ><tr><td class=\"intfdcolor\" align=\"center\" nowrap width=\"100%\" height=\"22\">";
String closeBorder = "</td></tr></table></td></tr></table>";
%>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/dateUtils.js"></script>
<view:script src="/util/javaScript/checkForm.js"/>
