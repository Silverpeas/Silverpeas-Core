<%--

    Copyright (C) 2000 - 2024 Silverpeas

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
<%@ page import="org.silverpeas.kernel.util.StringUtil" %>

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
<jsp:useBean id="isAnonymousAccess" type="java.lang.Boolean"/>
<c:set var="isAccessGuest" value="<%=helper.isAccessGuest()%>"/>
<jsp:useBean id="isAccessGuest" type="java.lang.Boolean"/>
<c:set var="displayConnectedUsers" value="<%=helper.isConnectedUsersDisplayEnabled()%>"/>
<jsp:useBean id="displayConnectedUsers" type="java.lang.Boolean"/>

<c:set var="labelConnectedUser" value='<%=helper.getString("lookSilverpeasV5.connectedUser")%>'/>
<c:set var="labelConnectedUsers" value='<%=helper.getString("lookSilverpeasV5.connectedUsers")%>'/>
<c:set var="labelUserNotifications" value='<%=helper.getString("lookSilverpeasV5.userNotifications")%>'/>
<c:set var="labelUnreadUserNotification" value='<%=helper.getString("lookSilverpeasV5.unreadUserNotification")%>'/>
<c:set var="labelUnreadUserNotifications" value='<%=helper.getString("lookSilverpeasV5.unreadUserNotifications")%>'/>

<view:includePlugin name="userSession"/>
<view:includePlugin name="userNotification"/>
<view:includePlugin name="basketSelection"/>
<view:includePlugin name="ticker" />
<style>
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
    const params = {"FromTopBar": '1'};
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

  <c:if test="${displayConnectedUsers}">
  window.USERSESSION_PROMISE.then(function() {
    spUserSession.addEventListener('connectedUsersChanged', function(event) {
      const nb = event.detail.data.nb;
      const $container = jQuery("#connectedUsers");
      if (nb <= 0) {
        $container.hide();
      } else {
        let label = " ${labelConnectedUsers}";
        if (nb === 1) {
          label = " ${labelConnectedUser}";
        }
        $container.show();
        jQuery("a", $container).text(nb + label);
      }
    }, 'connectedUsersChanged@TopBar');
  });
  </c:if>
</script>
<div id="topBar">
  <div id="backHome">
    <a href="javaScript:goToHome();"><img src="${icon_px}" width="220" height="105" border="0" id="pxUrlHome" alt=""/></a></div>
  <viewTags:displayTicker/>
  <div id="outils">
    <c:if test="${not isAnonymousAccess}">
      <div class="avatarName">
        <c:if test="${isAccessGuest}">
          <view:image src="<%=helper.getUserDetail().getAvatar()%>" type="avatar" alt="avatar"/> <%=helper.getUserFullName() %>
        </c:if>
        <c:if test="${not isAccessGuest}">
          <a href="javascript:goToMyProfile()" title="<%=helper.getString("lookSilverpeasV5.userlink")%>"><view:image src="<%=helper.getUserDetail().getAvatar()%>" type="avatar" alt="avatar"/> <%=helper.getUserFullName() %></a>
        </c:if>
      </div>
    </c:if>
    <div class="userNav">
      <c:if test="${not isAnonymousAccess and not isAccessGuest}">
        <span id="connectedUsers" style="display:none">
          <button class="link"
                  onclick="spUserSession.viewConnectedUsers();"></button>
          <span> | </span>
        </span>
        <div id="header-user-notifications" class="silverpeas-user-notifications">
          <silverpeas-user-notifications no-unread-label="${labelUserNotifications}"
                                         one-unread-label="${labelUnreadUserNotification}"
                                         several-unread-label="${labelUnreadUserNotifications}">
            <span>
              <button class="link"></button>
              <span> | </span>
            </span>
          </silverpeas-user-notifications>
        </div>
        <div id="header-basket-selection" class="silverpeas-basket-selection">
          <silverpeas-basket-selection v-on:api="setApi">
            <span>
              <button class="link"></button>
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
      <% if (helper.isDirectoryDisplayEnabled()) {
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
    <table>
      <th></th>
      <tr>
        <td class="gaucheShortcuts">&nbsp;</td>
        <td style="white-space: nowrap;"><img alt="space" src="${icon_px}" width="40" height="1"/></td>
        <%
          for ( TopItem item :topItems) {
            //le composant est-il celui selectionne ?
            String cssStyle = "";
            if (item.getId().equals(currentComponentId) || item.getId().equals(currentSpaceId))
              cssStyle = "activeShortcut";
        %>
        <td style="white-space: nowrap;" id="item<%=item.getId()%>" class="<%=cssStyle%>"><span
                style="white-space: nowrap;"><a href="javaScript:goToItem('<%=m_sContext%><%=item.getUrl()%>', '<%=item.getId()%>');"><%=item.getLabel()%></a></span></td>
        <td style="white-space: nowrap;"><img alt="space" src="${icon_px}" width="40" height="1"/></td>
        <% } %>
        <td class="droiteShortcuts">&nbsp;</td>
      </tr>
    </table>
  </div>
  <% } %>

  <% if(helper.isBackOfficeVisible()) { %>
  <div id="administration">
    <table>
      <th></th>
      <tr>
        <td>
          <button class=link" type="button" onclick="spWindow.loadAdminHomePage();"><%=helper.getString("lookSilverpeasV5.backOffice")%></button>
        </td>
      </tr>
    </table>
  </div>
  <% } %>
</div>
<form name="topBarSearchForm" action="">
  <input type="hidden" name="query"/>
</form>
