<%--

    Copyright (C) 2000 - 2017 Silverpeas

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
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

<%@ page import="org.silverpeas.core.notification.user.client.NotificationManagerSettings" %>
<%@ page import="org.silverpeas.core.admin.user.constant.UserState" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>
<%@ taglib tagdir="/WEB-INF/tags/silverpeas/util" prefix="viewTags" %>

<%-- Set resource bundle --%>
<fmt:setLocale value="${requestScope.resources.language}"/>
<view:setBundle bundle="${requestScope.resources.multilangBundle}"/>
<view:setBundle bundle="${requestScope.resources.iconsBundle}" var="icons"/>
<view:setBundle basename="org.silverpeas.social.multilang.socialNetworkBundle" var="profile"/>

<c:set var="USER_MANUAL_NOTIFICATION_MAX_RECIPIENT_LIMITATION_ENABLED" value="<%= NotificationManagerSettings.isUserManualNotificationRecipientLimitEnabled()%>"/>
<c:set var="USER_MANUAL_NOTIFICATION_MAX_RECIPIENT_LIMITATION_DEFAULT_VALUE" value="<%= NotificationManagerSettings.getUserManualNotificationRecipientLimit()%>"/>
<c:set var="USER_STATE_BLOCKED"><%= UserState.BLOCKED %></c:set>
<c:set var="USER_STATE_DEACTIVATED"><%= UserState.DEACTIVATED %></c:set>

<fmt:message key="GML.yes" var="yesLabel"/>
<fmt:message key="GML.no" var="noLabel"/>
<fmt:message key="GML.delete" var="labelDelete"/>
<fmt:message key="JDP.userManualNotifReceiverLimitValue" var="userManualNotifReceiverLimitValueLabel"><fmt:param value="${USER_MANUAL_NOTIFICATION_MAX_RECIPIENT_LIMITATION_DEFAULT_VALUE}"/></fmt:message>
<fmt:message key="JDP.user.avatar.delete.confirm" var="labelDeleteAvatar"/>

<c:set var="userInfos" value="${requestScope.userObject}" />
<jsp:useBean id="userInfos" type="org.silverpeas.core.admin.user.model.UserFull"/>

<c:set var="lastName" value="${userInfos.lastName}" />
<c:set var="displayedLastName"><view:encodeHtml string="${lastName}" /></c:set>
<c:set var="firstName" value="${userInfos.firstName}" />
<c:set var="displayedFirstName"><view:encodeHtml string="${firstName}" /></c:set>
<c:set var="firstName" value="${userInfos.firstName}" />
<c:set var="displayedFirstName"><view:encodeHtml string="${firstName}" /></c:set>
<c:set var="email" value="${userInfos.eMail}" />
<c:set var="displayedEmail"><view:encodeHtml string="${email}" /></c:set>
<c:set var="login" value="${userInfos.login}" />
<c:set var="displayedLogin"><view:encodeHtml string="${login}" /></c:set>
<fmt:message key="GML.user.account.state.${userInfos.state.name}" var="stateLabel"/>
<fmt:message key="JDP.user.state.${userInfos.state.name}" var="stateIcon" bundle="${icons}"/>

<%@ include file="check.jsp" %>
<%
  Domain domObject = (Domain) request.getAttribute("domainObject");
  UserFull userObject = (UserFull) request.getAttribute("userObject");
  String groupsPath = (String) request.getAttribute("groupsPath");
  boolean isDomainRW = (Boolean) request.getAttribute("isDomainRW");
  boolean isDomainSync = (Boolean) request.getAttribute("isDomainSync");
  boolean isUserRW = (Boolean) request.getAttribute("isUserRW");
  boolean isX509Enabled = (Boolean) request.getAttribute("isX509Enabled");
  boolean isGroupManager = (Boolean) request.getAttribute("isOnlyGroupManager");
  boolean isUserManageableByGroupManager =
      (Boolean) request.getAttribute("userManageableByGroupManager");

  String thisUserId = userObject.getId();
  boolean updatableUser = false;
  boolean avatarDefined = userObject.isAvatarDefined();

  if (domObject != null) {
    browseBar.setComponentName(getDomainLabel(domObject, resource),
        "domainContent?Iddomain=" + domObject.getId());
  }

  if (groupsPath != null && groupsPath.length() > 0) {
    browseBar.setPath(groupsPath);
  }

  if (isDomainRW && isUserRW && (!isGroupManager || isUserManageableByGroupManager)) {
    operationPane
        .addOperation(resource.getIcon("JDP.userUpdate"), resource.getString("GML.modify"),
            "displayUserModify?Iduser=" + thisUserId);
    updatableUser = true;
    if (userObject.isBlockedState()) {
      operationPane
          .addOperation(resource.getIcon("JDP.userUnblock"), resource.getString("JDP.userUnblock"),
              "userUnblock?Iduser=" + thisUserId);
    } else {
      operationPane
          .addOperation(resource.getIcon("JDP.userBlock"), resource.getString("JDP.userBlock"),
              "userBlock?Iduser=" + thisUserId);
    }
    if (userObject.isDeactivatedState()) {
      operationPane
          .addOperation(resource.getIcon("JDP.userActivate"), resource.getString("JDP.userActivate"),
              "userActivate?Iduser=" + thisUserId);
    } else {
      operationPane
          .addOperation(resource.getIcon("JDP.userDeactivate"), resource.getString("JDP.userDeactivate"),
              "userDeactivate?Iduser=" + thisUserId);
    }
    operationPane.addOperation(resource.getIcon("JDP.userDel"), resource.getString("GML.delete"),
        "javascript:deleteUser()");
  }
  if (isDomainSync && !isGroupManager) {
    operationPane
        .addOperation(resource.getIcon("JDP.userUpdate"), resource.getString("GML.modify"),
            "displayUserMS?Iduser=" + thisUserId);
    updatableUser = true;
    if (userObject.isBlockedState()) {
      operationPane
          .addOperation(resource.getIcon("JDP.userUnblock"), resource.getString("JDP.userUnblock"),
              "userUnblock?Iduser=" + thisUserId);
    } else {
      operationPane
          .addOperation(resource.getIcon("JDP.userBlock"), resource.getString("JDP.userBlock"),
              "userBlock?Iduser=" + thisUserId);
    }
    if (userObject.isDeactivatedState()) {
      operationPane
          .addOperation(resource.getIcon("JDP.userActivate"), resource.getString("JDP.userActivate"),
              "userActivate?Iduser=" + thisUserId);
    } else {
      operationPane
          .addOperation(resource.getIcon("JDP.userDeactivate"), resource.getString("JDP.userDeactivate"),
              "userDeactivate?Iduser=" + thisUserId);
    }
    operationPane
        .addOperation(resource.getIcon("JDP.userSynchro"), resource.getString("JDP.userSynchro"),
            "userSynchro?Iduser=" + thisUserId);
    operationPane.addOperation(resource.getIcon("JDP.userUnsynchro"),
        resource.getString("JDP.userUnsynchro"), "userUnSynchro?Iduser=" + thisUserId);
  }
  if (isX509Enabled && !isGroupManager) {
    operationPane.addLine();
    operationPane.addOperation(resource.getIcon("JDP.x509"), resource.getString("JDP.getX509"),
        "userGetP12?Iduser=" + thisUserId);
  }

%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
  <view:looknfeel withFieldsetStyle="true"/>
  <view:includePlugin name="popup"/>
  <script type="text/javascript">
    function deleteUser() {
      jQuery.popup.confirm("<%=resource.getString("JDP.userDelConfirm")%>", function() {
        jQuery('#deletionForm').submit();
      });
    }

    function deleteAvatar() {
      jQuery.popup.confirm("${labelDeleteAvatar}", function() {
        jQuery('#deletionForm').attr("action", "userAvatarDelete");
        jQuery('#deletionForm').submit();
      });
    }
  </script>
</head>
<body class="admin-users">
<%
out.println(window.printBefore());
%>
<view:frame>
  <c:if test="${userInfos.state == USER_STATE_BLOCKED || userInfos.state == USER_STATE_DEACTIVATED}">
    <div class="inlineMessage">
      <fmt:message key="JDP.user.state.${userInfos.state.name}" />
    </div>
  </c:if>
  <div class="table profile">
    <div class="cell showActionsOnMouseOver">
      <fieldset class="skinFieldset" id="identity-profil">
        <legend class="without-img">
          <div class="personna">
            <span class="surname">${displayedFirstName}</span> <span class="lastname">${displayedLastName}</span>
            <span class="access ${userInfos.state.name}"><img src="../..${stateIcon}" alt="${stateLabel}" title="${stateLabel}" /></span>
          </div>
        </legend>
        <div class="avatar"> <view:image src="${userInfos.avatar}" size="120x" alt="viewUser" css="avatar"/>
          <% if (updatableUser && avatarDefined) { %>
          <div class="operation actionShownOnMouseOver">
            <a title="${labelDelete}" href="#" onclick="deleteAvatar()"><img title="${labelDelete}" alt="${labelDelete}" src="../../util/icons/delete.gif" border="0"></a>
          </div>
          <% } %>
        </div>
        <div class="fields">
          <div class="rights">
            <c:choose>
              <c:when test="${userInfos.accessLevel.code == 'A'}">
                <fmt:message key="GML.administrateur"/>
              </c:when>
              <c:when test="${userInfos.accessLevel.code == 'G'}">
                <fmt:message key="GML.guest"/>
              </c:when>
              <c:when test="${userInfos.accessLevel.code == 'K'}">
                <fmt:message key="GML.kmmanager"/>
              </c:when>
              <c:when test="${userInfos.accessLevel.code == 'D'}">
                <fmt:message key="GML.domainManager"/>
              </c:when>
              <c:when test="${userInfos.accessLevel.code == 'U'}">
                <fmt:message key="GML.user"/>
              </c:when>
              <c:otherwise>
                <fmt:message key="GML.no"/>
              </c:otherwise>
            </c:choose>
          </div>
          <div class="lastConnection"><fmt:message key="GML.user.lastConnection"/> <view:formatDateTime value="${userInfos.lastLoginDate}"/></div>
          <div class="domain"><img class="img-label" src="../../util/icons/component/domainSmall.gif" alt="Domaine" title="Domaine"  />${userInfos.domain.name}</div>
          <div class="access"> <span class="login"><img class="img-label" src="../../util/icons/Login.gif" alt="<fmt:message key="GML.login"/>" title="<fmt:message key="GML.login"/>" />${displayedLogin}</span></div>
          <div class="email"><img  class="img-label" src="../../admin/jsp/icons/icoOutilsMail.gif" alt="Login" title="Login" /> <a href="mailto:${displayedEmail}">${displayedEmail}</a></div>
          <div class="language"><img  class="img-label" src="../../util/icons/talk2user.gif" alt="<fmt:message key="JDP.userPreferredLanguage"/>" title="<fmt:message key="JDP.userPreferredLanguage"/>" /><viewTags:userPreferredLanguageSelector user="${userInfos}" readOnly="true"/></div>
          <div class="user-zone-id"><img  class="img-label" src="../../util/icons/time-zone.png" alt="<fmt:message key="JDP.userPreferredZoneId"/>" title="<fmt:message key="JDP.userPreferredZoneId"/>" /><viewTags:userPreferredZoneIdSelector user="${userInfos}" readOnly="true"/></div>
        </div>
      </fieldset>
    </div>
    <div class="cell">
      <fieldset id="identity-extra" class="skinFieldset">
        <legend><fmt:message key="myProfile.identity.fieldset.extra" bundle="${profile}"/></legend>
        <viewTags:displayUserExtraProperties user="<%=userObject%>" readOnly="true" includeEmail="false"/>
      </fieldset>
    </div>
  </div>

  <%--User Manual Notification User Receiver Limit--%>
  <c:if test="${USER_MANUAL_NOTIFICATION_MAX_RECIPIENT_LIMITATION_ENABLED
              and (userInfos.accessUser or userInfos.accessGuest)}">
    <fieldset id="identity-manual-notification" class="skinFieldset">
      <legend class="without-img"><fmt:message key="JDP.userManualNotif"/></legend>
      <div class="fields">
        <div class="field" id="form-row-user-manual-notification-limitation-activation">
          <label class="txtlibform"><fmt:message key="JDP.userManualNotifReceiverLimitActivation"/></label>
          <div class="champs">
              ${(userInfos.userManualNotificationUserReceiverLimit)? yesLabel : noLabel}
          </div>
        </div>
        <c:if test="${userInfos.userManualNotificationUserReceiverLimit}">
          <div class="field" id="form-row-user-manual-notification-limitation-value">
            <label class="txtlibform">${userManualNotifReceiverLimitValueLabel}</label>
            <div class="champs">
                ${userInfos.notifManualReceiverLimit}
            </div>
          </div>
        </c:if>
      </div>
    </fieldset>
  </c:if>

  <view:directoryExtraForm userId="<%=thisUserId%>" />

  <form id="deletionForm" action="userDelete" method="post">
    <input id="Iduser" type="hidden" name="Iduser" value="${userInfos.id}"/>
  </form>
</view:frame>
<%
out.println(window.printAfter());
%>
</body>
</html>