<%--

    Copyright (C) 2000 - 2019 Silverpeas

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
<%@page import="org.silverpeas.core.web.mvc.controller.MainSessionController"%>
<%@page import="org.silverpeas.core.web.index.components.ApplicationIndexer"%>
<%@page import="org.silverpeas.core.admin.component.model.ComponentInstLight"%>
<%@page import="org.silverpeas.core.admin.space.SpaceInstLight"%>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%
response.setHeader("Cache-Control","no-store"); //HTTP 1.1
response.setHeader("Pragma","no-cache"); //HTTP 1.0
response.setDateHeader ("Expires",-1); //prevents caching at the proxy server
%>

<%@ page import="org.silverpeas.core.admin.user.constant.UserAccessLevel"%>
<%@ page import="org.silverpeas.core.admin.service.OrganizationControllerProvider"%>

<%@ page import="org.silverpeas.core.util.ResourceLocator"%>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.GraphicElementFactory"%>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.browsebars.BrowseBar"%>

<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.frame.Frame"%>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.operationpanes.OperationPane"%>

<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.window.Window"%>
<%@ page import="org.silverpeas.core.util.LocalizationBundle" %>
<%@ page import="org.silverpeas.core.admin.service.OrganizationController" %>
<%@ page import="org.silverpeas.core.web.index.IndexationProcessExecutor" %>
<%@ page errorPage="../../admin/jsp/errorpage.jsp"%>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>

<%!
  private String printSpaceAndSubSpaces(String spaceId, int depth,
      OrganizationController m_OrganizationController,
      String m_sContext) {
    String 			compoName 	= null;
    String 			compoDesc 	= null;
    String 			compoId 	= null;
    String 			label 		= null;
    SpaceInstLight 	spaceInst 	= m_OrganizationController.getSpaceInstLightById(spaceId);
    StringBuffer	result 		= new StringBuffer();
    if (spaceInst!=null) {
        result.append("<table border=\"0\" cellspacing=\"0\" cellpadding=\"3\">\n");

        if (depth==0) result.append("<tr><td class=\"txtnote\">&nbsp;</td></tr>\n");

        result.append("<tr>\n");
        result.append("<td class=\"txttitrecol\">&#149; <A HREF=\"javaScript:index('Index','', '"+spaceInst.getLocalId()+"');\">").append(spaceInst.getName()).append("</a></td></tr>\n");

        result.append("<tr><td class=\"txtnote\">\n");

        String[] asAvailCompoForCurUser = m_OrganizationController.getAllComponentIds(Integer.toString(spaceInst.getLocalId()));
        for(int nI = 0; nI <asAvailCompoForCurUser.length; nI++) {
		ComponentInstLight compoInst = m_OrganizationController.getComponentInstLight(asAvailCompoForCurUser[nI]);
            compoName = compoInst.getName();
            compoDesc = compoInst.getDescription();
            compoId = compoInst.getId();
            label = compoInst.getLabel();
            if ((label == null) || (label.length() == 0)) {
                label = compoName;
            }

            result.append("&nbsp;<img src=").append(m_sContext).append("/util/icons/component/").append(compoName).append("Small.gif border=0 width=15 align=absmiddle>&nbsp;<A HREF=\"javaScript:index('Index','"+compoId+"','"+spaceId+"');\">").append(label).append("</A>\n");
        }

        // Get all sub spaces
        String [] subSpaceIds = m_OrganizationController.getAllSubSpaceIds(spaceId);
        for (int nI=0; nI<subSpaceIds.length; nI++) {
            result.append("<table border=\"0\" cellspacing=\"0\" cellpadding=\"0\">\n");
            result.append("<tr><td>&nbsp;&nbsp;</td>\n");
            result.append("<td class=\"txtnote\">\n");
            result.append(printSpaceAndSubSpaces(subSpaceIds[nI], depth+1, m_OrganizationController, m_sContext));
            result.append("</td></tr></table>\n");
        }

        result.append("</td>\n");
        result.append("</tr>\n");
        result.append("</table>\n");
    }
    return result.toString();
}
%>

<%
GraphicElementFactory gef = (GraphicElementFactory) session.getAttribute("SessionGraphicElementFactory");

final MainSessionController m_MainSessionCtrl = (MainSessionController) session.getAttribute(MainSessionController.MAIN_SESSION_CONTROLLER_ATT);

if (m_MainSessionCtrl == null || !UserAccessLevel.ADMINISTRATOR.equals(m_MainSessionCtrl.getUserAccessLevel())) {
    // No session controller in the request -> security exception
    String sessionTimeout = ResourceLocator.getGeneralSettingBundle().getString("sessionTimeout");
    getServletConfig().getServletContext().getRequestDispatcher(sessionTimeout).forward(request, response);
    return;
}

  OrganizationController m_OrganizationController =
      OrganizationControllerProvider.getOrganisationController();

LocalizationBundle message = ResourceLocator.getLocalizationBundle("org.silverpeas.homePage.multilang.homePageBundle", m_MainSessionCtrl.getFavoriteLanguage());

String sURI = request.getRequestURI();
String sServletPath = request.getServletPath();
String sPathInfo = request.getPathInfo();
if(sPathInfo != null)
    sURI = sURI.substring(0,sURI.lastIndexOf(sPathInfo));
String m_sContext = sURI.substring(0,sURI.lastIndexOf(sServletPath));

String[] rootSpaceIds =  m_OrganizationController.getAllRootSpaceIds();

final String spaceId 			= request.getParameter("SpaceId");
final String componentId 		= request.getParameter("ComponentId");
final String action 			= request.getParameter("Action");
final String personalCompo 	= request.getParameter("PersonalCompo");
String indexMessage		= message.getString("admin.reindex.inprogress");
boolean isIndexationProcessRunning = IndexationProcessExecutor.get().isCurrentExecution();

if (action != null) {
  if (!isIndexationProcessRunning) {
    IndexationProcessExecutor.get().execute(new IndexationProcessExecutor.IndexationProcess() {
      @Override
      public void perform() {
        ApplicationIndexer ai = ApplicationIndexer.getInstance();
        switch (action) {
          case "Index":
            ai.index(spaceId, componentId);
            break;
          case "IndexPerso":
            ai.index(personalCompo);
            break;
          case "IndexAllSpaces":
            ai.indexAllSpaces();
            break;
          case "IndexAll":
            ai.indexAll();
            break;
          case "IndexPdc":
            ai.indexPdc();
            break;
          case "IndexGroups":
            ai.indexGroups();
            break;
          case "IndexUsers":
            ai.indexUsers();
            break;
        }
      }
    });
  }
} else if (!isIndexationProcessRunning) {
  indexMessage = "";
}

Window window = gef.getWindow();
BrowseBar browseBar = window.getBrowseBar();
browseBar.setDomainName(message.getString("admin.reindex"));

OperationPane operations = window.getOperationPane();
operations.addOperation("useless", message.getString("admin.reindex.op.all"), "javaScript:index('IndexAll','','');");
operations.addLine();
operations.addOperation("useless", message.getString("admin.reindex.op.spaces"), "javaScript:index('IndexAllSpaces','','');");
operations.addOperation("useless", message.getString("admin.reindex.op.users"), "javaScript:index('IndexUsers','','');");
operations.addOperation("useless", message.getString("admin.reindex.op.groups"), "javaScript:index('IndexGroups','','');");
operations.addOperation("useless", message.getString("admin.reindex.op.pdc"), "javaScript:index('IndexPdc','','');");
operations.addOperation("useless", message.getString("admin.reindex.op.todos"), "javaScript:index('IndexPerso','Todo','');");
operations.addOperation("useless", message.getString("admin.reindex.op.agendas"), "javaScript:index('IndexPerso','Agenda','');");

%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title>Navigation</title>
<view:looknfeel/>
<script type="text/javascript">
<% if (action != null && isIndexationProcessRunning) { %>
$(document).ready(function() {
  notyError("<%=indexMessage%>");
});
<% } %>
function index(action, compo, space) {
	var message = "<%=message.getString("admin.reindex.js.warn")%> ";
	if (action == "Index") {
		if (compo.length > 1) {
			message += "<%=message.getString("admin.reindex.js.warn.compo")%>";
		} else {
			message += "<%=message.getString("admin.reindex.js.warn.space")%>";
		}
	} else if (action == "IndexPerso")  {
		message += "<%=message.getString("admin.reindex.js.warn.perso")%>";
	} else if (action == "IndexAllSpaces") {
		message += "<%=message.getString("admin.reindex.js.warn.spaces")%>";
	} else if (action == "IndexAll") {
		message += "<%=message.getString("admin.reindex.js.warn.all")%>";
	} else if (action == "IndexPdc") {
		message += "<%=message.getString("admin.reindex.js.warn.pdc")%>";
	} else if (action == "IndexGroups") {
		message += "<%=message.getString("admin.reindex.js.warn.groups")%>";
	} else if (action == "IndexUsers") {
		message += "<%=message.getString("admin.reindex.js.warn.users")%>";
	}
	message += ". \n<%=message.getString("admin.reindex.js.warn.confirm")%>";
	if (confirm(message)) {
		$.progressMessage();
		location.href="applicationIndexer.jsp?Action="+action+"&PersonalCompo="+compo+"&SpaceId="+space+"&ComponentId="+compo;
	}
}
</script>
</head>
<body class="page_content_admin">
<%
	out.println(window.printBefore());

    Frame frame=gef.getFrame();
    out.println(frame.printBefore());
%>

	<div class="inlineMessage"><%=message.getString("admin.reindex.help")%></div>
	<br/>
	<% if (indexMessage.length() > 0) { %>
		<div class="inlineMessage-ok"><%=indexMessage%></div>
		<br/>
	<% } %>
		<table border="0" cellspacing="0" cellpadding="3" width="100%">
		<tr>
		<td nowrap><img src="../../admin/jsp/icons/accueil/esp_collabo.gif">&nbsp;&nbsp;<span class="txtnav"><%=message.getString("SpaceCollaboration")%></span></td>
            </tr>
            <tr>
		<td>
                <%
                    for(int nK = 0; nK < rootSpaceIds.length; nK++) {
			out.println(printSpaceAndSubSpaces(rootSpaceIds[nK], 0, m_OrganizationController, m_sContext));
                    }
                %>
			</td>
		</tr>
		</table>

<%
	out.println(frame.printAfter());
	out.println(window.printAfter());
%>
<view:progressMessage/>
</body>
</html>