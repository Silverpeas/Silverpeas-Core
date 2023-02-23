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
    along with this program.  If not, see <https://www.gnu.org/licenses/>.

--%>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%@ include file="importFrameSet.jsp" %>
<%@ page import="org.silverpeas.core.web.look.LookHelper"%>
<%@ page import="org.silverpeas.core.web.look.TopItem"%>
<%@ page import="org.silverpeas.core.util.StringUtil" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<%@ taglib tagdir="/WEB-INF/tags/silverpeas/util" prefix="viewTags" %>
<%-- Retrieve user menu display mode --%>
<c:set var="curHelper" value="${sessionScope.Silverpeas_LookHelper}" />

<c:url var="icon_px" value="/util/viewGenerator/icons/px.gif"/>

<%
  LookHelper 	helper 	= LookHelper.getLookHelper(session);
  SettingBundle settings 		= gef.getFavoriteLookSettings();

  String currentComponentId 	= helper.getComponentId();
  String currentSpaceId		= helper.getSpaceId();
  gef.setSpaceIdForCurrentRequest(helper.getSubSpaceId());

  boolean goToFavoriteSpaceOnHomeLink = settings.getBoolean("home.target.favoriteSpace", false);

  List<TopItem> topItems = helper.getTopItems();

  boolean isAnonymousAccess 	= helper.isAnonymousAccess();
  boolean isAccessGuest 	= helper.isAccessGuest();

  String wallPaper = helper.getSpaceWallPaper();
  if (wallPaper == null) {
    wallPaper = gef.getIcon("wallPaper");
  }
  if (wallPaper == null) {
    wallPaper = m_sContext+"/admin/jsp/icons/silverpeasV5/bandeauTop.jpg";
  }

  String helpURL = helper.getSettings("helpURL", "");

  boolean outilDisplayed = false;
%>
<c:set var="isAnonymousAccess" value="<%=helper.isAnonymousAccess()%>"/>
<c:set var="isAccessGuest" value="<%=helper.isAccessGuest()%>"/>

<c:set var="labelConnectedUser" value='<%=helper.getString("lookSilverpeasV5.connectedUser")%>'/>
<c:set var="labelConnectedUsers" value='<%=helper.getString("lookSilverpeasV5.connectedUsers")%>'/>
<c:set var="labelUserNotifications" value='<%=helper.getString("lookSilverpeasV5.userNotifications")%>'/>
<c:set var="labelUnreadUserNotification" value='<%=helper.getString("lookSilverpeasV5.unreadUserNotification")%>'/>
<c:set var="labelUnreadUserNotifications" value='<%=helper.getString("lookSilverpeasV5.unreadUserNotifications")%>'/>

<view:includePlugin name="userSession"/>
<view:includePlugin name="userNotification"/>
<view:includePlugin name="basketSelection"/>
<view:includePlugin name="ticker" />
<style type="text/css">
  #shortcuts {
    bottom: 25px;
    position: absolute;
  <% if(helper.isBackOfficeVisible()) { %>
    right: 120px;
  <% } else { %>
    right: 0px;
  <% } %>
    height: 20px;
    width: auto;
  }
  body {
    background-image: url(<%=wallPaper%>);
    background-repeat: no-repeat;
    background-position: left top;
  }
</style>
<view:loadScript src="/util/javaScript/lookV5/tools.js"/>
<view:loadScript src="/util/javaScript/lookV5/topBar.js"/>
<script type="text/javascript">
  function goToHome() {
    var params = {"FromTopBar" : '1'};
    <%if (goToFavoriteSpaceOnHomeLink) {%>
    params.SpaceId = "<%=m_MainSessionCtrl.getFavoriteSpace()%>";
    <%}%>
    spWindow.loadHomePage(params);
  }

  function getContext() {
    return "<%=m_sContext%>";
  }

  function getBannerHeight() {
    return "<%=helper.getSettings("bannerHeight", "115")%>";
  }
  function getFooterHeight() {
    return "<%=helper.getSettings("footerHeight", "26")%>";
  }

  function goToMyProfile() {
    spWindow.loadLink(webContext + '/RMyProfil/jsp/Main');
  }

  window.USERSESSION_PROMISE.then(function() {
    spUserSession.addEventListener('connectedUsersChanged', function(event) {
      var nb = event.detail.data.nb;
      var $container = jQuery("#connectedUsers");
      if (nb <= 0) {
        $container.hide();
      } else {
        var label = " ${labelConnectedUsers}";
        if (nb === 1) {
          label = " ${labelConnectedUser}";
        }
        $container.show();
        jQuery("a", $container).text(nb + label);
      }
    }, 'connectedUsersChanged@TopBar');
  });

</script>
<div id="topBar">
  <div id="backHome">
    <a href="javaScript:goToHome();"><img src="${icon_px}" width="220" height="105" border="0" id="pxUrlHome" alt=""/></a></div>
  <viewTags:displayTicker/>
  <div id="outils">
    <% if (!isAnonymousAccess && !isAccessGuest) { %>
    <div class="avatarName">
      <a href="javascript:goToMyProfile()" title="<%=helper.getString("lookSilverpeasV5.userlink")%>"><view:image src="<%=helper.getUserDetail().getAvatar()%>" type="avatar" alt="avatar"/> <%=helper.getUserFullName() %></a>
    </div>
    <% } %>
    <div class="userNav">
      <c:if test="${not isAnonymousAccess && not isAccessGuest}">
        <span id="connectedUsers" style="display:none">
          <a href="#" onclick="javascript:onClick=spUserSession.viewConnectedUsers();"></a>
          <span> | </span>
        </span>
        <div id="header-user-notifications" class="silverpeas-user-notifications">
          <silverpeas-user-notifications no-unread-label="${labelUserNotifications}"
                                         one-unread-label="${labelUnreadUserNotification}"
                                         several-unread-label="${labelUnreadUserNotifications}">
            <span>
              <a href="javascript:void(0)"></a>
              <span> | </span>
            </span>
          </silverpeas-user-notifications>
        </div>
        <div id="header-basket-selection" class="silverpeas-basket-selection">
          <silverpeas-basket-selection v-on:api="setApi">
            <span>
              <a href="javascript:void(0)"></a>
              <span> | </span>
            </span>
          </silverpeas-basket-selection>
        </div>
        <script type="text/javascript">
          whenSilverpeasReady(function() {
            SpVue.createApp().mount('#header-user-notifications');
            SpVue.createApp({
              methods : {
                setApi : function(api) {
                  window.spBasketSelectionApi = api;
                }
              }
            }).mount('#header-basket-selection');
          });
        </script>
      </c:if>
      <% if (!isAnonymousAccess && !isAccessGuest && helper.getSettings("directoryVisible", true)) {
        outilDisplayed = true;
      %>
      <a href="<%=m_sContext%>/Rdirectory/jsp/Main" target="MyMain"><%=helper.getString("lookSilverpeasV5.directory")%></a>
      <% } %>
      <% if (helper.getSettings("glossaryVisible", false)) {
        outilDisplayed = true;
      %>
      <a href="javascript:onClick=openPdc()"><%=helper.getString("lookSilverpeasV5.glossaire")%></a>
      <% } %>
      <% if (helper.getSettings("mapVisible", true)) {
        if (outilDisplayed) {
          out.print(" | ");
        }
        outilDisplayed = true;
      %>
      <a href="<%=m_sContext + "/admin/jsp/Map.jsp"%>" target="MyMain"><%=helper.getString("lookSilverpeasV5.Map")%></a>
      <% } %>
      <% if (helper.getSettings("helpVisible", true) && StringUtil.isDefined(helpURL)) {
        if (outilDisplayed) {
          out.print(" | ");
        }
        outilDisplayed = true;
      %>
      <a href="<%=helpURL%>" target="_blank"><%=helper.getString("lookSilverpeasV5.Help")%></a>
      <% } %>
      <% if (!isAnonymousAccess && helper.getSettings("logVisible", true)) {
        if (outilDisplayed) {
          out.print(" | ");
        }
      %>
      <a id="logout" href="javascript:onClick=spUserSession.logout();"><%=helper.getString("lookSilverpeasV5.logout")%></a>
      <% } %>
    </div>
  </div>

  <% if (!topItems.isEmpty()) { %>
  <div id="shortcuts">
    <table border="0" cellspacing="0" cellpadding="0">
      <tr>
        <td class="gaucheShortcuts">&nbsp;</td>
        <td nowrap="nowrap" align="center"><img src="${icon_px}" width="40" height="1" border="0"/></td>
        <%
          for ( TopItem item :topItems) {
            //le composant est-il celui selectionne ?
            String cssStyle = "";
            if (item.getId().equals(currentComponentId) || item.getId().equals(currentSpaceId))
              cssStyle = "activeShortcut";
        %>
        <td nowrap="nowrap" align="center" id="item<%=item.getId()%>" class="<%=cssStyle%>"><nobr><a href="javaScript:goToItem('<%=m_sContext%><%=item.getUrl()%>', '<%=item.getId()%>');"><%=item.getLabel()%></a></nobr></td>
        <td nowrap="nowrap" align="center"><img src="${icon_px}" width="40" height="1" border="0"/></td>
        <% } %>
        <td class="droiteShortcuts">&nbsp;</td>
      </tr>
    </table>
  </div>
  <% } %>

  <% if(helper.isBackOfficeVisible()) { %>
  <div id="administration">
    <table border="0" cellspacing="0" cellpadding="0">
      <tr>
        <td>
          <a href="javascript:void(0)" onclick="spWindow.loadAdminHomePage();"><%=helper.getString("lookSilverpeasV5.backOffice")%></a>
        </td>
      </tr>
    </table>
  </div>
  <% } %>
</div>
<form name="topBarSearchForm" action="">
  <input type="hidden" name="query"/>
</form>
