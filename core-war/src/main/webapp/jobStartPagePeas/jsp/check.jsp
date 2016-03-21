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

<%@ page import="com.silverpeas.admin.components.LocalizedGroupOfParameters"%>
<%@ page import="com.silverpeas.admin.components.LocalizedOption"%>
<%@ page import="com.silverpeas.admin.components.LocalizedParameter"%>
<%@ page import="com.silverpeas.admin.components.WAComponent"%>
<%@ page import="com.silverpeas.admin.spaces.SpaceTemplate"%>
<%@ page import="com.silverpeas.jobStartPagePeas.AllComponentParameters"%>

<%// En fonction de ce dont vous avez besoin %>
<%@ page import="com.silverpeas.jobStartPagePeas.DisplaySorted"%>
<%@ page import="com.silverpeas.jobStartPagePeas.JobStartPagePeasSettings"%>
<%@ page import="com.silverpeas.jobStartPagePeas.SpaceLookHelper"%>
<%@ page import="com.silverpeas.jobStartPagePeas.control.JobStartPagePeasSessionController"%>
<%@ page import="com.stratelia.silverpeas.peasCore.URLManager"%>
<%@ page import="com.stratelia.webactiv.beans.admin.ComponentInst"%>
<%@ page import="com.stratelia.webactiv.beans.admin.ComponentInstLight"%>
<%@ page import="com.stratelia.webactiv.beans.admin.Group"%>
<%@ page import="com.stratelia.webactiv.beans.admin.ProfileInst"%>
<%@ page import="com.stratelia.webactiv.beans.admin.SpaceInst"%>
<%@ page import="com.stratelia.webactiv.beans.admin.SpaceInstLight"%>
<%@ page import="com.stratelia.webactiv.beans.admin.SpaceProfileInst"%>

<%@ page import="com.stratelia.webactiv.beans.admin.UserDetail"%>
<%@ page import="org.silverpeas.util.EncodeHelper"%>
<%@ page import="org.silverpeas.util.MultiSilverpeasBundle"%>
<%@ page import="org.silverpeas.util.ResourceLocator"%>
<%@ page import="org.silverpeas.util.StringUtil"%>
<%@ page import="org.silverpeas.core.web.util.viewGenerator.html.GraphicElementFactory"%>
<%@ page import="org.silverpeas.core.web.util.viewGenerator.html.arrayPanes.ArrayCellText"%>


<%@ page import="org.silverpeas.core.web.util.viewGenerator.html.arrayPanes.ArrayColumn"%>
<%@ page import="org.silverpeas.core.web.util.viewGenerator.html.arrayPanes.ArrayLine"%>
<%@ page import="org.silverpeas.core.web.util.viewGenerator.html.arrayPanes.ArrayPane"%>
<%@ page import="org.silverpeas.core.web.util.viewGenerator.html.board.Board"%>

<%@ page import="org.silverpeas.core.web.util.viewGenerator.html.browseBars.BrowseBar"%>
<%@ page import="org.silverpeas.core.web.util.viewGenerator.html.buttonPanes.ButtonPane"%>
<%@ page import="org.silverpeas.core.web.util.viewGenerator.html.buttons.Button" %>
<%@ page import="org.silverpeas.core.web.util.viewGenerator.html.frame.Frame" %>
<%@ page import="org.silverpeas.core.web.util.viewGenerator.html.iconPanes.IconPane" %>
<%@ page import="org.silverpeas.core.web.util.viewGenerator.html.icons.Icon" %>
<%@ page import="org.silverpeas.core.web.util.viewGenerator.html.operationPanes.OperationPane" %>
<%@ page import="org.silverpeas.core.web.util.viewGenerator.html.tabs.TabbedPane" %>
<%@ page import="org.silverpeas.core.web.util.viewGenerator.html.window.Window" %>
<%@ page import="java.util.Iterator" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.Set" %>

<%@ page errorPage="../../admin/jsp/errorpageMain.jsp"%>

<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>

<%
GraphicElementFactory gef = (GraphicElementFactory) session.getAttribute("SessionGraphicElementFactory");
JobStartPagePeasSessionController jobStartPageSC = (JobStartPagePeasSessionController) request.getAttribute("jobStartPageSC");

String iconsPath = ResourceLocator.getGeneralSettingBundle().getString("ApplicationURL");
String m_context = iconsPath;

Boolean haveToRefreshNavBar = (Boolean)request.getAttribute("haveToRefreshNavBar");

MultiSilverpeasBundle resource = (MultiSilverpeasBundle)request.getAttribute("resources");
Window window = gef.getWindow();
BrowseBar browseBar = window.getBrowseBar();
browseBar.setSpaceJavascriptCallback("parent.jumpToSpace");
browseBar.setComponentJavascriptCallback("parent.jumpToComponent");
OperationPane operationPane = window.getOperationPane();
Frame frame = gef.getFrame();
Board board = gef.getBoard();
%>
<%  if (haveToRefreshNavBar != null && haveToRefreshNavBar.booleanValue()) { %>
<script type="text/javascript">
if (window.name == "startPageContent") {
    window.parent.startPageNavigation.location.href="jobStartPageNav";
} else if (window.name == "IdleFrame") {
	parent.frames["bottomFrame"].frames["startPageNavigation"].location.href="<%=m_context%>/RjobStartPagePeas/jsp/jobStartPageNav";
} else {
    window.opener.parent.startPageNavigation.location.href="jobStartPageNav";
}
</script>
<% } %>