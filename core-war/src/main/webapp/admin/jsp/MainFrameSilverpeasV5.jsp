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

<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

<%
  response.setHeader("Expires", "Tue, 21 Dec 1993 23:59:59 GMT");
  response.setHeader("Pragma", "no-cache");
  response.setHeader("Cache-control", "no-cache");
  response.setHeader("Last-Modified", "Fri, Jan 25 2099 23:59:59 GMT");
  response.setStatus(HttpServletResponse.SC_CREATED);
%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>
<%@ include file="importFrameSet.jsp" %>
<%@ page import="org.silverpeas.core.admin.user.model.User" %>
<%@ page import="org.silverpeas.core.chat.ChatLocalizationProvider" %>
<%@ page import="org.silverpeas.core.chat.ChatUser" %>
<%@ page import="org.silverpeas.core.chat.servers.ChatServer" %>
<%@ page import="org.silverpeas.core.util.StringUtil" %>
<%@ page import="org.silverpeas.core.web.look.LookHelper" %>
<%@ page import="org.silverpeas.core.util.file.FileServerUtils" %>

<%
  String componentIdFromRedirect = (String) session.getAttribute("RedirectToComponentId");
  String spaceIdFromRedirect = (String) session.getAttribute("RedirectToSpaceId");
  if (!StringUtil.isDefined(spaceIdFromRedirect)) {
    spaceIdFromRedirect = request.getParameter("RedirectToSpaceId");
  }
  String attachmentId = (String) session.getAttribute("RedirectToAttachmentId");
  LocalizationBundle generalMessage = ResourceLocator.getGeneralLocalizationBundle(language);
  StringBuilder frameBottomParams = new StringBuilder().append("{");
  boolean login = StringUtil.getBooleanValue(request.getParameter("Login"));

  if (m_MainSessionCtrl == null) {
%>
<script type="text/javascript">
  top.location = '<c:url value="/Login"/>';
</script>
<%
} else {
  LookHelper helper = LookHelper.getLookHelper(session);
  if (helper == null) {
    helper = LookHelper.newLookHelper(session);
    helper.setMainFrame("MainFrameSilverpeasV5.jsp");
    login = true;
  }

  SettingBundle chatSettings = ChatServer.getChatSettings();
  ChatUser user = ChatUser.getCurrentRequester();
  String iceServer = chatSettings.getString("chat.servers.ice", "");

  boolean componentExists = false;
  if (StringUtil.isDefined(componentIdFromRedirect)) {
    componentExists = (organizationCtrl.getComponentInstLight(componentIdFromRedirect) != null);
  }

  if (!componentExists) {
    String spaceId = helper.getDefaultSpaceId();
    boolean spaceExists = false;
    if (StringUtil.isDefined(spaceIdFromRedirect)) {
      spaceExists = (organizationCtrl.getSpaceInstById(spaceIdFromRedirect) != null);
    }

    if (spaceExists) {
      spaceId = spaceIdFromRedirect;
    } else {
      if (helper != null && helper.getSpaceId() != null) {
        spaceId = helper.getSpaceId();
      }
    }
    helper.setSpaceIdAndSubSpaceId(spaceId);

    frameBottomParams.append("'SpaceId':'").append(spaceId).append("'");
  } else {
    helper.setComponentIdAndSpaceIds(null, null, componentIdFromRedirect);
    frameBottomParams.append("'SpaceId':''")
        .append(",'ComponentId':'")
        .append(componentIdFromRedirect)
        .append("'");
  }

  gef.setSpaceIdForCurrentRequest(helper.getSubSpaceId());

  if (login) {
    frameBottomParams.append(",'Login':'1'");
  }

  if (!"MainFrameSilverpeasV5.jsp".equalsIgnoreCase(helper.getMainFrame()) &&
      !"/admin/jsp/MainFrameSilverpeasV5.jsp".equalsIgnoreCase(helper.getMainFrame())) {
    session.setAttribute("RedirectToSpaceId", spaceIdFromRedirect);
    String topLocation = gef.getLookFrame();
    if (!topLocation.startsWith("/")) {
      topLocation = "/admin/jsp/" + topLocation;
    }
%>
<c:set var="topLocation"><%=topLocation%>
</c:set>
<script type="text/javascript">
  top.location = "<c:url value="${topLocation}" />";
</script>
<%
  }

  String bannerHeight = helper.getSettings("bannerHeight", "115") + "px";
  String footerHeight = helper.getSettings("footerHeight", "26") + "px";
  if (!helper.displayPDCFrame()) {
    footerHeight = "0px";
  }
%>

<c:set var="pdcActivated" value="<%=helper.displayPDCFrame()%>"/>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
  <title><%=generalMessage.getString("GML.popupTitle")%></title>
  <link rel="SHORTCUT ICON" href="<%=request.getContextPath()%>/util/icons/favicon.ico"/>
  <view:looknfeel/>
  <view:includePlugin name="chat"/>
  <style type="text/css">
    body {
      margin: 0;
      padding: 0;
      border: none;
      overflow: hidden;
    }

    .hidden-part {
      margin: 0;
      padding: 0;
      border: none;
      display: none;
    }

    #sp-layout-main {
      width: 100%;
      display: flex;
      flex-wrap: wrap;
      flex-direction: column;
    }

    #sp-layout-header-part, #sp-layout-body-part, #sp-layout-footer-part {
      padding: 0;
      margin: 0;
      border: none;
    }

    #sp-layout-header-part {
      width: 100%;
      height: <%=bannerHeight%>;
    }

    #sp-layout-footer-part {
      width: 100%;
      height: <%=footerHeight%>;
    }

    #sp-layout-body-part {
      width: 100%;
      display: table;
    }
  </style>
  <meta name="viewport" content="initial-scale=1.0"/>
</head>
<body>
<% if (attachmentId != null) {
  session.setAttribute("RedirectToAttachmentId", null);
  String mapping = (String) session.getAttribute("RedirectToMapping");
%>
<script type="text/javascript">
  SP_openWindow('<%=m_sContext%>/<%=mapping%>/<%=attachmentId%>', 'Fichier', '800', '600',
      'directories=0,menubar=1,toolbar=1,scrollbars=1,location=1,alwaysRaised');
</script>
<% } %>

<div id="sp-layout-main">
  <div id="sp-layout-header-part"></div>
  <div id="sp-layout-body-part"></div>
  <div id="sp-layout-footer-part" style="display: none"></div>
</div>
<div class="hidden-part" style="height: 0">
  <iframe src="../../clipboard/jsp/Idle.jsp" name="IdleFrame" marginwidth="0" marginheight="0" scrolling="no" frameborder="0"></iframe>
  <iframe src="<c:url value='/Ragenda/jsp/importCalendar'/>" name="importFrame" marginwidth="0" marginheight="0" scrolling="no" frameborder="0"></iframe>
</div>

<view:progressMessage/>
<script type="text/javascript">
  (function() {
    whenSilverpeasReady(function() {
      if (!top.window.mainFrameOnLoad) {
        top.window.mainFrameOnLoad = function(event) {
          sp.log.debug(
              "This is just a demonstration: it is possible to listen to events ('load', 'show', 'hide') dispatched from each part of the layout");
          sp.log.debug(
              "On footer part could also be listen to events: 'pdcload', 'pdcshow' and 'pdchide'");
          sp.log.debug(
              "The condition here (please consult the code if you are reading from the browser console!) is to ensure that the listener will not be declared several times.");
          sp.log.debug(
              "Indeed, because of ajax reloading and according to the location of the event listener attachment, same treatment could be performed several times");
          // notySuccess("Body content event well performed!");
        };
      }
      initializeSilverpeasLayout(<%=frameBottomParams.append('}')%>);
      spLayout.getBody().ready(function() {
        spLayout.getBody().getContent().addEventListener('load', top.window.mainFrameOnLoad);
      });
    });
    <c:choose>
    <c:when test="${sessionScope.get('Silverpeas.Chat')}">
    SilverChat.init({
      url : '<%= chatSettings.getString("chat.xmpp.httpBindUrl") %>',
      id : '<%= user.getChatLogin() %>',
      password : '<%= user.getChatPassword() %>',
      domain : '<%= user.getChatDomain() %>',
      <% if (!iceServer.isEmpty()) { %>
      ice : {
        server: '<%= iceServer %>',
        auth: true
      },
      <% } %>
      language : '<%= user.getUserPreferences().getLanguage() %>',
      avatar: webContext + '/display/avatar/60x/',
      debug: true,
      selectUser: function(openChatWith) {
        $('#userId').off('change').on('change', function() {
          var id = $(this).val();
          if (id && id !== '<%= user.getId() %>') {
            User.get(id).then(function(user) {
              if (user) {
                openChatWith(user.chatId, user.fullName);
              }
            });
          }
        });
        SP_openUserPanel(webContext + '/selectChatUser', '', 'menubar=no,scrollbars=no,statusbar=no');
      }
    }).start();
    </c:when>
    <c:otherwise>
    <%
    LocalizationBundle chatBundle = ChatLocalizationProvider.getLocalizationBundle(
        User.getCurrentRequester().getUserPreferences().getLanguage());
    String errorMessage = chatBundle.getString("chat.server.notAvailable");
    %>
    sp.log.error("<%= errorMessage %>");
    </c:otherwise>
    </c:choose>
  })();
</script>
<form id="chat_selected_user">
  <input type="hidden" name="userId" id="userId"/>
  <input type="hidden" name="userName" id="userName"/>
</form>

</body>
</html>
<% } %>
