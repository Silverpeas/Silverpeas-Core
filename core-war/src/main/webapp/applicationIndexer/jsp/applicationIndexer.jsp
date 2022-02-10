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
<%@page import="org.silverpeas.core.admin.component.model.ComponentInstLight"%>
<%@page import="org.silverpeas.core.admin.component.model.SilverpeasComponentInstance"%>
<%@page import="org.silverpeas.core.admin.service.OrganizationController"%>
<%@page import="org.silverpeas.core.admin.service.SpaceWithSubSpacesAndComponents"%>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%
response.setHeader("Cache-Control","no-store"); //HTTP 1.1
response.setHeader("Pragma","no-cache"); //HTTP 1.0
response.setDateHeader ("Expires",-1); //prevents caching at the proxy server
%>

<%@ page import="org.silverpeas.core.admin.space.SpaceInstLight"%>
<%@ page import="org.silverpeas.core.admin.user.constant.UserAccessLevel"%>

<%@ page import="org.silverpeas.core.util.LocalizationBundle"%>
<%@ page import="org.silverpeas.core.util.ResourceLocator"%>
<%@ page import="org.silverpeas.core.util.StringUtil"%>

<%@ page import="org.silverpeas.core.web.index.IndexationProcessExecutor"%>
<%@ page import="org.silverpeas.core.web.index.components.ApplicationIndexer"%>
<%@ page import="org.silverpeas.core.web.mvc.controller.MainSessionController" %>
<%@ page import="org.silverpeas.core.web.mvc.webcomponent.WebMessager" %>

<%@ page errorPage="../../admin/jsp/errorpage.jsp"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<%@ taglib prefix="fmy" uri="http://java.sun.com/jsp/jstl/fmt" %>

<%!
  private String printSpaceAndSubSpaces(final SpaceWithSubSpacesAndComponents space,
      final int depth, final String appContext) {
    final SpaceInstLight spaceInst = space.getSpace();
    final StringBuilder result = new StringBuilder();
    if (spaceInst != null) {
      result.append("<table border=\"0\" cellspacing=\"0\" cellpadding=\"3\">\n");
      if (depth == 0) {
        result.append("<tr><td class=\"txtnote\">&nbsp;</td></tr>\n");
      }
      result.append("<tr>\n");
      result.append("<td class=\"txttitrecol\">&#149; <a href=\"javaScript:index('Index','', '")
          .append(spaceInst.getLocalId()).append("');\">").append(spaceInst.getName())
          .append("</a></td></tr>\n").append("<tr><td class=\"txtnote\">\n");
      for (final SilverpeasComponentInstance compoInst : space.getComponents()) {
        final String compoName = compoInst.getName();
        final String compoId = compoInst.getId();
        final String label = StringUtil.defaultStringIfNotDefined(compoInst.getLabel(), compoName);
        result.append("&nbsp;<img src=\"").append(ComponentInstLight.getIcon(compoInst, false)).append(
            "\" border=0 width=15 align=absmiddle>&nbsp;<a href=\"javaScript:index('Index','")
            .append(compoId).append("','").append(space.getSpace().getId()).append("');\">")
            .append(label).append("</a>\n");
      }
      // Get all sub spaces
      for (final SpaceWithSubSpacesAndComponents subSpace : space.getSubSpaces()) {
        result.append("<table border=\"0\" cellspacing=\"0\" cellpadding=\"0\">\n");
        result.append("<tr><td>&nbsp;&nbsp;</td>\n");
        result.append("<td class=\"txtnote\">\n");
        result.append(printSpaceAndSubSpaces(subSpace, depth + 1, appContext));
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
final MainSessionController mainSessionCtrl = (MainSessionController) session.getAttribute(MainSessionController.MAIN_SESSION_CONTROLLER_ATT);
if (mainSessionCtrl == null || !UserAccessLevel.ADMINISTRATOR.equals(mainSessionCtrl.getUserAccessLevel())) {
    // No session controller in the request -> security exception
    String sessionTimeout = ResourceLocator.getGeneralSettingBundle().getString("sessionTimeout");
    getServletConfig().getServletContext().getRequestDispatcher(sessionTimeout).forward(request, response);
    return;
}
%>

<c:set var="_userLanguage" value="<%=mainSessionCtrl.getFavoriteLanguage()%>" scope="request"/>
<jsp:useBean id="_userLanguage" type="java.lang.String" scope="request"/>
<fmt:setLocale value="${_userLanguage}"/>
<view:setBundle basename="org.silverpeas.homePage.multilang.homePageBundle"/>

<fmt:message var="reindexLabel" key="admin.reindex" />
<fmt:message var="reindexAllLabel" key="admin.reindex.op.all" />
<fmt:message var="reindexSpacesLabel" key="admin.reindex.op.spaces" />
<fmt:message var="reindexUsersLabel" key="admin.reindex.op.users" />
<fmt:message var="reindexGroupsLabel" key="admin.reindex.op.groups" />
<fmt:message var="reindexPdcLabel" key="admin.reindex.op.pdc" />
<fmt:message var="reindexTodosLabel" key="admin.reindex.op.todos" />
<fmt:message var="reindexHelpLabel" key="admin.reindex.help" />
<fmt:message var="spaceCollaborationLabel" key="SpaceCollaboration" />
<fmt:message var="prefixConfirmMessage" key="admin.reindex.js.warn" />
<fmt:message var="appConfirmMessage" key="admin.reindex.js.warn.compo" />
<fmt:message var="warnConfirmMessage" key="admin.reindex.js.warn.space" />
<fmt:message var="persoAppConfirmMessage" key="admin.reindex.js.warn.perso" />
<fmt:message var="spacesConfirmMessage" key="admin.reindex.js.warn.spaces" />
<fmt:message var="allConfirmMessage" key="admin.reindex.js.warn.all" />
<fmt:message var="pdcConfirmMessage" key="admin.reindex.js.warn.pdc" />
<fmt:message var="groupsConfirmMessage" key="admin.reindex.js.warn.groups" />
<fmt:message var="usersConfirmMessage" key="admin.reindex.js.warn.users" />
<fmt:message var="confirmMessage" key="admin.reindex.js.warn.confirm" />
<fmt:message var="inProgressMessage" key="admin.reindex.inprogress" />

<%
LocalizationBundle message = ResourceLocator.getLocalizationBundle("org.silverpeas.homePage.multilang.homePageBundle", mainSessionCtrl
    .getFavoriteLanguage());

String sURI = request.getRequestURI();
String sServletPath = request.getServletPath();
String sPathInfo = request.getPathInfo();
if(sPathInfo != null) {
  sURI = sURI.substring(0,sURI.lastIndexOf(sPathInfo));
}
String appContext = sURI.substring(0,sURI.lastIndexOf(sServletPath));

final String spaceId       = request.getParameter("SpaceId");
final String componentId     = request.getParameter("ComponentId");
final String action       = request.getParameter("Action");
final String personalCompo   = request.getParameter("PersonalCompo");
String indexMessage    = message.getString("admin.reindex.inprogress");
boolean isIndexationProcessRunning = IndexationProcessExecutor.get().isCurrentExecution();

if (action != null) {
  if (!isIndexationProcessRunning) {
    IndexationProcessExecutor.get().execute(new IndexationProcessExecutor.IndexationProcess() {
      @Override
      public void perform() {
        final ApplicationIndexer ai = ApplicationIndexer.getInstance();
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
    WebMessager.getInstance().addSuccess(indexMessage);
  } else {
    response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
    WebMessager.getInstance().addError(indexMessage);
  }
} else if (!isIndexationProcessRunning) {
  indexMessage = "";
}
%>

<c:if test="${empty param.Action}">

<c:set var="fullTreeview" value="<%=OrganizationController.get().getFullTreeview()%>"/>
<jsp:useBean id="fullTreeview" type="org.silverpeas.core.admin.service.SpaceWithSubSpacesAndComponents"/>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" lang="${_userLanguage}">
<head>
<title>Navigation</title>
<view:looknfeel/>
<script type="text/javascript">
function index(action, compo, space) {
  var message = "<p>${prefixConfirmMessage} <strong>";
  if (action === "Index") {
    if (compo.length > 1) {
      message += "${appConfirmMessage}";
    } else {
      message += "${warnConfirmMessage}";
    }
  } else if (action === "IndexPerso")  {
    message += "${persoAppConfirmMessage}";
  } else if (action === "IndexAllSpaces") {
    message += "${spacesConfirmMessage}";
  } else if (action === "IndexAll") {
    message += "${allConfirmMessage}";
  } else if (action === "IndexPdc") {
    message += "${pdcConfirmMessage}";
  } else if (action === "IndexGroups") {
    message += "${groupsConfirmMessage}";
  } else if (action === "IndexUsers") {
    message += "${usersConfirmMessage}";
  }
  message += "</strong>.</p><p>${confirmMessage}</p>";
  jQuery.popup.confirm(message, function() {
    spProgressMessage.show();
    sp.ajaxRequest('applicationIndexer.jsp')
      .withParam('Action', action)
      .withParam('PersonalCompo', compo)
      .withParam('SpaceId', space)
      .withParam('ComponentId', compo)
      .send()
      .then(function() {
        sp.navRequest('applicationIndexer.jsp').go();
      })
      ['catch'](function() {
        spProgressMessage.hide();
      });
  });
}
</script>
</head>
<body class="page_content_admin">
<view:browseBar componentId="${reindexLabel}"/>
<view:operationPane>
  <view:operation action="javaScript:index('IndexAll','','')" altText="${reindexAllLabel}"/>
  <view:operationSeparator/>
  <view:operation action="javaScript:index('IndexAllSpaces','','')" altText="${reindexSpacesLabel}"/>
  <view:operation action="javaScript:index('IndexUsers','','')" altText="${reindexUsersLabel}"/>
  <view:operation action="javaScript:index('IndexGroups','','')" altText="${reindexGroupsLabel}"/>
  <view:operation action="javaScript:index('IndexPdc','','')" altText="${reindexPdcLabel}"/>
  <view:operation action="javaScript:index('IndexPerso','Todo','')" altText="${reindexTodosLabel}"/>
</view:operationPane>
<view:window>
  <view:frame>
    <div class="inlineMessage">${reindexHelpLabel}</div>
    <br/>
    <% if (indexMessage.length() > 0) { %>
    <div class="inlineMessage-ok"><%=indexMessage%></div>
    <br/>
    <% } %>
    <table style="padding: 3px; width: 100%">
      <th id="space-list"></th>
      <tr>
        <td style="white-space: nowrap">
          <img src="../../admin/jsp/icons/accueil/esp_collabo.gif" alt=""> <span class="txtnav">${spaceCollaborationLabel}</span>
        </td>
      </tr>
      <tr>
        <td>
          <c:forEach var="space" items="${fullTreeview.subSpaces}">
            <jsp:useBean id="space" type="org.silverpeas.core.admin.service.SpaceWithSubSpacesAndComponents"/>
            <c:out value="<%=printSpaceAndSubSpaces(space, 0, appContext)%>" escapeXml="false"/>
          </c:forEach>
        </td>
      </tr>
    </table>
  </view:frame>
</view:window>
<view:progressMessage/>
</body>
</html>
</c:if>