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
<%@ page import="org.silverpeas.web.jobdomain.control.JobDomainPeasSessionController" %>
<%@ page import="org.silverpeas.core.util.URLUtil" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>
<%@ taglib tagdir="/WEB-INF/tags/silverpeas/util" prefix="viewTags" %>

<c:set var="context" value="${pageContext.request.contextPath}"/>

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
<c:set var="listIndex" value="${requestScope.Index}" />

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

<c:set var="userGroups" value="${requestScope.UserGroups}" />
<c:set var="userManageableSpaces" value="${requestScope.UserManageableSpaces}" />
<c:set var="userManageableGroups" value="${requestScope.UserManageableGroups}" />
<c:set var="userProfiles" value="${requestScope.UserProfiles}" />

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
  boolean isRightCopyReplaceEnabled = (Boolean) request.getAttribute("IsRightCopyReplaceEnabled");

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
  operationPane.addLine();
  operationPane.addOperation("useless", resource.getString("JDP.user.rights.action"), "userViewRights");
  if (isRightCopyReplaceEnabled) {
    operationPane.addOperation("useless", resource.getString("JDP.rights.assign"), "javascript:assignSameRights()");
  }
  operationPane.addLine();
  operationPane.addOperation("useless", resource.getString("JDP.user.subscriptions"), "javascript:viewSubscriptions()");

  if (isX509Enabled && !isGroupManager) {
    operationPane.addLine();
    operationPane.addOperation(resource.getIcon("JDP.x509"), resource.getString("JDP.getX509"),
        "userGetP12?Iduser=" + thisUserId);
  }

%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
  <view:looknfeel withFieldsetStyle="true" withCheckFormScript="true"/>
  <view:includePlugin name="popup"/>
  <view:includePlugin name="qtip"/>
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

    function viewSubscriptions() {
      chemin = webContext+'<%= URLUtil.getURL(URLUtil.CMP_PDCSUBSCRIPTION)%>showUserSubscriptions.jsp?userId=${userInfos.id}';
      largeur = "600";
      hauteur = "440";
      SP_openWindow(chemin, "pdcWindow", largeur, hauteur, "resizable=yes,scrollbars=yes");
    }

    var groupWindow = window;
    function openGroup(groupId) {
      url = webContext+'/RjobDomainPeas/jsp/groupOpen?groupId=' + groupId;
      windowName = "groupWindow";
      larg = "800";
      haut = "800";
      windowParams = "directories=0,menubar=0,toolbar=0,alwaysRaised";
      if (!groupWindow.closed && groupWindow.name == "groupWindow") {
        groupWindow.close();
      }
      groupWindow = SP_openWindow(url, windowName, larg, haut, windowParams, false);
    }

    function assignSameRights() {
      $("#assignRightsDialog").dialog("open");
    }

    function ifCorrectFormExecute(callback) {
      var errorMsg = "";
      var errorNb = 0;
      var sourceRightsId = document.rightsForm.sourceRightsId.value;

      if (isWhitespace(sourceRightsId)) {
        errorMsg+=" - '<fmt:message key="JDP.rights.assign.as"/>' <fmt:message key="GML.MustBeFilled"/>\n";
        errorNb++;
      }

      switch (errorNb) {
        case 0 :
          callback.call(this);
          break;
        case 1 :
          errorMsg = "<fmt:message key="GML.ThisFormContains"/> 1 <fmt:message key="GML.error"/> : \n" + errorMsg;
          jQuery.popup.error(errorMsg);
          break;
        default :
          errorMsg = "<fmt:message key="GML.ThisFormContains"/> " + errorNb + " <fmt:message key="GML.errors"/> :\n" + errorMsg;
          jQuery.popup.error(errorMsg);
      }
    }

    $(document).ready(function() {
      // Use the each() method to gain access to each elements attributes
      $('a[rel]', $('.qTipCompliant')).each(function() {
        $(this).qtip({
          content : {
            // Set the text to an image HTML string with the correct src URL
            ajax : {
              url : webContext+$(this).attr('rel') // Use the rel attribute of each element for the url to load
            },
            text : "Loading..."
          },
          position : {
            at : "bottom center", // Position the tooltip above the link
            my : "top center",
            viewport : $(window) // Keep the tooltip on-screen at all times
          },
          style : {
            classes : "qtip-shadow qtip-green"
          }
        });
      });

      location.href = "#user-profiles";

      $("#assignRightsDialog").dialog({
        autoOpen: false,
        resizable: false,
        modal: true,
        height: "auto",
        width: 550,
        buttons: {
          "<fmt:message key="GML.ok"/>": function() {
            ifCorrectFormExecute(function() {
              $.progressMessage();
              if(!document.rightsForm.checkNodeAssignRights.checked) {
                document.rightsForm.nodeAssignRights.value = "false";
              }
              document.rightsForm.submit();
            });
          },
          "<fmt:message key="GML.cancel" />": function() {
            $(this).dialog("close");
          }
        }
      });
    });
  </script>
</head>
<body class="admin-user">
<%
out.println(window.printBefore());
%>
<view:frame>
  <c:if test="${userInfos.state == USER_STATE_BLOCKED || userInfos.state == USER_STATE_DEACTIVATED}">
    <div class="inlineMessage">
      <fmt:message key="JDP.user.state.${userInfos.state.name}" />
    </div>
  </c:if>
  <viewTags:displayIndex nbItems="${listIndex.nbItems}" index="${listIndex.currentIndex}" linkSuffix="User"/>
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
          <c:if test="${not empty userInfos.lastLoginDate || not empty userInfos.creationDate}">
            <div class="lastConnection">
              <c:if test="${not empty userInfos.lastLoginDate}">
                <fmt:message key="GML.user.lastConnection"/> <view:formatDateTime value="${userInfos.lastLoginDate}"/>
              </c:if>
              <c:if test="${not empty userInfos.lastLoginDate && not empty userInfos.creationDate}">
                 -
              </c:if>
              <c:if test="${not empty userInfos.creationDate}">
                <fmt:message key="GML.creationDate"/> <view:formatDateTime value="${userInfos.creationDate}"/>
              </c:if>
            </div>
          </c:if>
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

  <fmt:message key="GML.name" var="labelGroupName"/>
  <fmt:message key="GML.users" var="labelNBUsers"/>
  <fmt:message key="GML.description" var="labelGroupDesc"/>
  <c:if test="${not empty userGroups}">
    <fieldset class="skinFieldset qTipCompliant" id="profil-groups-belong">
      <legend><fmt:message key="GML.groupes"/></legend>
      <view:arrayPane var="profile-groups" routingAddress="#" numberLinesPerPage="-1">
        <view:arrayColumn title="${labelGroupName}" sortable="false"/>
        <view:arrayColumn title="${labelNBUsers}" sortable="false"/>
        <view:arrayColumn title="${labelGroupDesc}" sortable="false"/>

        <c:forEach var="userGroup" items="${userGroups}">
          <view:arrayLine>
            <c:set var="groupLink"><a href="#" onclick="openGroup('${userGroup.id}');return false;" rel="/JobDomainPeasItemPathServlet?GroupId=${userGroup.id}">${userGroup.name}</a></c:set>
            <view:arrayCellText text="${groupLink}"/>
            <view:arrayCellText text="${userGroup.nbUsers}"/>
            <view:arrayCellText text="${userGroup.description}"/>
          </view:arrayLine>
        </c:forEach>
      </view:arrayPane>
    </fieldset>
  </c:if>

  <c:if test="${not empty userManageableGroups}">
    <fieldset class="skinFieldset qTipCompliant" id="user-manageable-groups">
      <legend><fmt:message key="JDP.user.groups.manageable"/></legend>
      <view:arrayPane var="profile-manageable-groups" routingAddress="#" numberLinesPerPage="-1">
        <view:arrayColumn title="${labelGroupName}" sortable="false"/>
        <view:arrayColumn title="${labelNBUsers}" sortable="false"/>
        <view:arrayColumn title="${labelGroupDesc}" sortable="false"/>

        <c:forEach var="userGroup" items="${userManageableGroups}">
          <view:arrayLine>
            <c:set var="groupLink"><a href="#" onclick="openGroup('${userGroup.id}');return false;" rel="/JobDomainPeasItemPathServlet?GroupId=${userGroup.id}">${userGroup.name}</a></c:set>
            <view:arrayCellText text="${groupLink}"/>
            <view:arrayCellText text="${userGroup.nbUsers}"/>
            <view:arrayCellText text="${userGroup.description}"/>
          </view:arrayLine>
        </c:forEach>
      </view:arrayPane>
    </fieldset>
  </c:if>

  <c:if test="${not empty userManageableSpaces}">
    <fieldset class="skinFieldset qTipCompliant" id="manageable-spaces">
      <legend><fmt:message key="JDP.user.spaces.manageable"/></legend>
      <view:arrayPane var="profile-spaces" routingAddress="#" numberLinesPerPage="-1">
        <fmt:message key="GML.name" var="labelSpaceName"/>
        <fmt:message key="GML.description" var="labelSpaceDesc"/>
        <view:arrayColumn title="${labelSpaceName}" sortable="false"/>
        <view:arrayColumn title="${labelSpaceDesc}" sortable="false"/>

        <c:forEach var="manageableSpace" items="${userManageableSpaces}">
          <view:arrayLine>
            <c:set var="spaceLink"><a href="#" rel="/JobDomainPeasItemPathServlet?SpaceId=${manageableSpace.id}">${manageableSpace.name}</a></c:set>
            <view:arrayCellText text="${spaceLink}"/>
            <view:arrayCellText text="${manageableSpace.description}"/>
          </view:arrayLine>
        </c:forEach>
      </view:arrayPane>
    </fieldset>
  </c:if>

  <c:if test="${not empty userProfiles}">
    <fieldset class="skinFieldset qTipCompliant" id="user-profiles">
      <legend><fmt:message key="JDP.user.rights.title"/></legend>
      <view:arrayPane var="profile-rights" routingAddress="#" numberLinesPerPage="-1">
        <fmt:message key="GML.space" var="labelSpace"/>
        <fmt:message key="GML.component" var="labelComponent"/>
        <fmt:message key="GML.type" var="labelType"/>
        <fmt:message key="JDP.user.rights" var="labelRights"/>
        <view:arrayColumn title="${labelSpace}" sortable="false"/>
        <view:arrayColumn title="${labelComponent}" sortable="false"/>
        <view:arrayColumn title="${labelType}" sortable="false"/>
        <view:arrayColumn title="${labelRights}" sortable="false"/>

        <c:forEach var="userInstanceProfiles" items="${userProfiles}">
          <view:arrayLine>
            <c:set var="spaceLink"><a href="#" rel="/JobDomainPeasItemPathServlet?SpaceId=${userInstanceProfiles.space.id}">${userInstanceProfiles.localizedSpaceLabel}</a></c:set>
            <c:set var="appLink"><a href="#" rel="/JobDomainPeasItemPathServlet?ComponentId=${userInstanceProfiles.component.id}">${userInstanceProfiles.localizedInstanceLabel}</a></c:set>
            <view:arrayCellText text="${spaceLink}"/>
            <view:arrayCellText text="${appLink}"/>
            <view:arrayCellText text="${userInstanceProfiles.localizedComponentLabel}"/>
            <view:arrayCellText text="${userInstanceProfiles.localizedProfilesName}"/>
          </view:arrayLine>
        </c:forEach>
      </view:arrayPane>
    </fieldset>
  </c:if>

  <form id="deletionForm" action="userDelete" method="post">
    <input id="Iduser" type="hidden" name="Iduser" value="${userInfos.id}"/>
  </form>
</view:frame>
<%
out.println(window.printAfter());
%>

<!-- Dialog choice rights -->
<fmt:message key="JDP.sourceRightsUserPanel" var="sourceRightsUserPanelIcon" bundle="${icons}" />
<fmt:message key="JDP.mandatory" var="mandatoryIcon" bundle="${icons}" />
<c:set var="ASSIGNATION_MODE_ADD"><%= JobDomainPeasSessionController.ADD_RIGHTS %></c:set>
<c:set var="ASSIGNATION_MODE_REPLACE"><%= JobDomainPeasSessionController.REPLACE_RIGHTS %></c:set>

<% if (isRightCopyReplaceEnabled) { %>
  <div id="assignRightsDialog" title="<fmt:message key="JDP.rights.assign"/>">
    <form accept-charset="UTF-8" enctype="multipart/form-data;charset=utf-8" id="affected-profil"
          name="rightsForm" action="AssignSameRights" method="post">
      <label class="label-ui-dialog" for="profil-from"><fmt:message key="JDP.rights.assign.as"/></label>
      <span class="champ-ui-dialog">
		    <input type="text" id="sourceRightsName" name="sourceRightsName" value="" size="50" readonly="readonly"/>
		    <a title="<fmt:message key="JDP.rights.assign.sourceRightsUserPanel"/>" href="#" onclick="javascript:SP_openWindow('SelectRightsUserOrGroup','SelectUserGroupWindow',800,600,'');">
				<img src="${context}${sourceRightsUserPanelIcon}"
             alt="<fmt:message key="JDP.rights.assign.sourceRightsUserPanel"/>"
             title="<fmt:message key="JDP.rights.assign.sourceRightsUserPanel"/>"/>
			  </a>
        <img src="${context}${mandatoryIcon}" width="5" height="5" border="0"/>
        <input type="hidden" name="sourceRightsId" id="sourceRightsId" value=""/>
        <input type="hidden" name="sourceRightsType" id="sourceRightsType" value=""/>
		  </span>
      <label class="label-ui-dialog"><fmt:message key="JDP.rights.assign.mode"/></label>
      <span class="champ-ui-dialog">
        <input type="radio" name="choiceAssignRights" id="choiceAssignRights" value="${ASSIGNATION_MODE_ADD}" checked="checked"/>
        <strong><fmt:message key="JDP.rights.assign.mode.add"/></strong> <fmt:message key="JDP.rights.assign.actualRights"/>
        <input type="radio" name="choiceAssignRights" id="choiceAssignRights" value="${ASSIGNATION_MODE_REPLACE}"/>
        <strong><fmt:message key="JDP.rights.assign.mode.replace"/></strong> <fmt:message key="JDP.rights.assign.theActualRights"/>
	    </span>
      <label class="label-ui-dialog"></label>
      <span class="champ-ui-dialog">
        <input type="checkbox" name="checkNodeAssignRights" id="checkNodeAssignRights" checked="checked"/>
        <fmt:message key="JDP.rights.assign.nodeAssignRights"/>
        <input type="hidden" name="nodeAssignRights" id="nodeAssignRights" value="true"/>
	    </span>
      <label class="label-ui-dialog">
        <img src="${context}${mandatoryIcon}" width="5" height="5"/> : <fmt:message key="GML.requiredField"/>
      </label>
    </form>
  </div>
<% } %>

<view:progressMessage/>

</body>
</html>