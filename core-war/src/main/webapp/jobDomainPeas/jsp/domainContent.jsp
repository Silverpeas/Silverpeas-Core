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
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="org.apache.commons.lang3.tuple.Pair"%>
<%@ page import="org.silverpeas.core.admin.quota.constant.QuotaLoad" %>
<%@ page import="org.silverpeas.core.admin.user.constant.UserState" %>
<%@ page import="org.silverpeas.kernel.logging.Level" %>
<%@ page import="java.util.Arrays" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://www.silverpeas.com/tld/silverFunctions" prefix="silfn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<%@ taglib tagdir="/WEB-INF/tags/silverpeas/util" prefix="viewTags" %>

<%@ include file="check.jsp" %>

<c:set var="userLanguage" value="${sessionScope['SilverSessionController'].favoriteLanguage}"/>
<fmt:setLocale value="${userLanguage}" />
<view:setBundle bundle="${requestScope.resources.multilangBundle}" />

<c:set var="domObject" value="${requestScope.domainObject}"/>
<jsp:useBean id="domObject" type="org.silverpeas.core.admin.domain.model.Domain"/>

<c:set var="isUserImportHandled" value="${not (isSCIMDomain(domObject) or isGoogleDomain(domObject))}"/>
<jsp:useBean id="isUserImportHandled" type="java.lang.Boolean"/>
<c:set var="isGroupHandled" value="${not (isSCIMDomain(domObject) or isGoogleDomain(domObject))}"/>
<jsp:useBean id="isGroupHandled" type="java.lang.Boolean"/>

<fmt:message var="userPanelAccessLabel" key="JDP.userPanelAccess"><fmt:param value="${not isGroupHandled ? 0 : 2}"/></fmt:message>
<jsp:useBean id="userPanelAccessLabel" type="java.lang.String"/>

<c:set var="reloadDomainNavigationFrame" value="${requestScope.reloadDomainNavigationFrame}"/>

<%
  UserDetail theUser 			= (UserDetail)request.getAttribute("theUser");
  boolean isDomainRW 			= (Boolean)request.getAttribute("isDomainRW");
  boolean isDomainSync 		= (Boolean)request.getAttribute("isDomainSync");
  boolean isUserRW 			= (Boolean)request.getAttribute("isUserRW");
  boolean isGroupManager		= (Boolean)request.getAttribute("isOnlyGroupManager");
  boolean onlySpaceManager		= (Boolean)request.getAttribute("isOnlySpaceManager");
  boolean isUserAddingAllowed = (Boolean)request.getAttribute("isUserAddingAllowedForGroupManager");
  boolean groupInClipboard = (Boolean) request.getAttribute("groupInClipboard");
  Group[] subGroups = (Group[])request.getAttribute("subGroups");
  Group[] appGroups = (Group[])request.getAttribute("appGroups");
    //noinspection unchecked
    List<UserDetail> subUsers = (List<UserDetail>)request.getAttribute("subUsers");

  boolean isDomainLdap = "org.silverpeas.core.admin.domain.driver.ldapdriver.LDAPDriver".equals(domObject.getDriverClassName());
  boolean isDomainSql = "org.silverpeas.core.admin.domain.driver.sqldriver.SQLDriver".equals(domObject.getDriverClassName());
  boolean isDomainScim = "org.silverpeas.core.admin.domain.driver.scimdriver.SCIMDriver".equals(domObject.getDriverClassName());
  boolean isDomainGoogle = "org.silverpeas.core.admin.domain.driver.googledriver.GoogleDriver".equals(domObject.getDriverClassName());
  boolean mixedDomain = domObject.isMixedOne();

  browseBar.setComponentName(getDomainLabel(domObject, resource), "domainContent?Iddomain="+domObject.getId());

  // Initializing users in domain quota
  boolean isUserDomainQuotaFull = JobDomainSettings.isUsersInDomainQuotaEnabled() && domObject.isQuotaReached();

  // Domain operations
	operationPane.addOperation(resource.getIcon("JDP.userPanelAccess"),userPanelAccessLabel,"displaySelectUserOrGroup");
  if (theUser.isAccessAdmin()) {
      operationPane.addOperation(resource.getIcon("JDP.removedGroupAccess"), resource.getString("JDP.removedGroupAccess"), "displayRemovedGroups");
	    if (!mixedDomain) {
	      operationPane.addOperation(resource.getIcon("JDP.removedUserAccess"), resource.getString("JDP.removedUserAccess"), "displayRemovedUsers");
	      operationPane.addOperation(resource.getIcon("JDP.deletedUserAccess"), resource.getString("JDP.deletedUserAccess"), "displayDeletedUsers");
        if (isDomainLdap) {
          operationPane.addOperation("useless", resource.getString("JDP.usersWithSensitiveDataAccess"),
                  "displayUsersWithSensitiveData");
        }
        operationPane.addLine();
		if(isDomainSql) {
		        operationPane.addOperation(resource.getIcon("JDP.domainSqlUpdate"),resource.getString("JDP.domainSQLUpdate"),"displayDomainSQLModify");
		        operationPane.addOperation(resource.getIcon("JDP.domainSqlDel"),resource.getString("JDP.domainSQLDel"),"javascript:ConfirmAndSend('"+resource.getString("JDP.domainDelConfirm")+"','domainSQLDelete')");
		    } else {
		        operationPane.addOperation(resource.getIcon("JDP.domainUpdate"),resource.getString("JDP.domainUpdate"),"displayDomainModify");
		        if (!domObject.getId().equals("0")){
          final String deleteAction = isDomainScim
              ? "domainSCIMDelete"
              : (isDomainGoogle
                  ? "domainGoogleDelete"
                  : "domainDelete");
          operationPane
              .addOperation(resource.getIcon("JDP.domainDel"), resource.getString("JDP.domainDel"),
                  "javascript:ConfirmAndSend('" + resource.getString("JDP.domainDelConfirm") +
                      "','" + deleteAction + "')");
        }
      }
    }
  }

  if (isDomainRW)
  {
    if (!isGroupManager)
    {
	operationPane.addLine();

	operationPane.addOperationOfCreation(resource.getIcon("JDP.groupAdd"),resource.getString("JDP.groupAdd"),"displayGroupCreate");
    if (groupInClipboard) {
      operationPane.addOperation(resource.getIcon("JDP.groupPaste"),
              resource.getString("JDP.groupPaste"), "javascript:onclick=clipboardPaste()");
    }
	if (isUserRW && !isUserDomainQuotaFull) {
          // User operations
          operationPane.addOperationOfCreation(resource.getIcon("JDP.userCreate"),resource.getString("JDP.userCreate"),"displayUserCreate");
          operationPane.addOperationOfCreation(resource.getIcon("JDP.importCsv"),resource.getString("JDP.csvImport"),"displayUsersCsvImport");
        }
    }
    else
    {
	if (isUserAddingAllowed && isUserRW && !isUserDomainQuotaFull) {
	  operationPane.addLine();

          //User operations
          operationPane.addOperation(resource.getIcon("JDP.userCreate"),resource.getString("JDP.userCreate"),"displayUserCreate");
          operationPane.addOperation(resource.getIcon("JDP.importCsv"),resource.getString("JDP.csvImport"),"displayUsersCsvImport");
        }
    }
  }
  if (isDomainSync)
  {
      if (!isGroupManager)
      {
	operationPane.addLine();

	// Domain operations
  if (isDomainGoogle) {
    operationPane.addOperation(resource.getIcon("JDP.domainUserFilterRuleModify"), resource.getString("JDP.domainUserFilterRuleModify"), "domainModifyUserFilter");
  }
	operationPane.addOperation(resource.getIcon("JDP.domainSynchro"),resource.getString("JDP.domainSynchro"),"displayDomainSynchro");

	//User operations
        if (isUserImportHandled) {
          operationPane.addOperation(resource.getIcon("JDP.userImport"), resource.getString("JDP.userImport"),
              "displayUserImport");
        }

          if (isGroupHandled) {
            // Group operations
            operationPane.addOperation(resource.getIcon("JDP.groupImport"), resource.getString("JDP.groupImport"), "displayGroupImport");
          }
      }
      else
      {
	if (isUserAddingAllowed)
	{
		operationPane.addLine();

		//User operations
              operationPane.addOperation(resource.getIcon("JDP.userImport"),resource.getString("JDP.userImport"),"displayUserImport");
	}
      }
  } else if(isDomainSql) {
	boolean synchroUser = domObject.getProperty("ExternalSynchro", false);
	if(synchroUser) {
	    operationPane.addLine();
	    operationPane.addOperation(resource.getIcon("JDP.domainSqlSynchro"),resource.getString("JDP.domainSynchro"),"javascript:DomainSQLSynchro()");
	}
  }

  if (onlySpaceManager) {
    // no action to space manager
    operationPane.clear();
  }
%>

<c:set var="mixedDomain" value="<%=mixedDomain%>"/>
<c:set var="groupList" value="<%=Arrays.asList(subGroups)%>"/>
<c:set var="appGroupList" value="<%=Arrays.asList(appGroups)%>"/>
<c:set var="ldapDomain" value="<%=isDomainLdap%>"/>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title></title>
<view:looknfeel withFieldsetStyle="true"/>
<view:includePlugin name="popup"/>
<script type="text/javascript">
function ConfirmAndSend(textToDisplay, targetURL) {
  jQuery.popup.confirm(textToDisplay, function() {
    jQuery('#deletionForm').attr('action', targetURL).submit();
  });
}

function DomainSQLSynchro(){
	SP_openWindow('<%=m_context %>/RjobDomainPeas/jsp/displayDynamicSynchroReport?IdTraceLevel=<%=Level.DEBUG%>', 'SynchroDomainReport', '750', '550', 'menubar=yes,scrollbars=yes,statusbar=yes,resizable=yes');
  sp.formRequest("domainSQLSynchro").submit();
}

function jumpToUser(selectionUserAPI) {
  const userIds = selectionUserAPI.getSelectedUserIds();
  const groupIds = selectionUserAPI.getSelectedGroupIds();
  if (userIds.length) {
    sp.navRequest("userContent").withParam("Iduser", userIds[0]).go();
  } else if (groupIds.length) {
    sp.navRequest("groupContent").withParam("Idgroup", groupIds[0]).go();
  }
}

function clipboardPaste() {
  document.location.href = 'groupPaste?X-ATKN=${requestScope["X-ATKN"]}';
}

let arrayBeforeAjaxRequest = function () {
  if (${isGroupHandled ? fn:length(groupList) : 0} > 25) {
    spProgressMessage.show();
  }
}
<c:if test="${reloadDomainNavigationFrame}">
whenSilverpeasReady(function() {
  parent.refreshCurrentLevel();
});
</c:if>
</script>
</head>
<body id="domainContent" class="page_content_admin">
<%
out.println(window.printBefore());
%>
<view:frame>

  <% if (isUserDomainQuotaFull) { %>
  <div class="inlineMessage-nok"><fmt:message key="JDP.userDomainQuotaFull" /></div>
  <br />
  <% } %>

  <c:if test="${not mixedDomain}">
    <div class="rightContent" id="right-content-domainContent">
      <% if (JobDomainSettings.isUsersInDomainQuotaEnabled() &&
          !QuotaLoad.UNLIMITED.equals(domObject.getUserDomainQuota().getLoad())) { %>
      <div class="tag-presentation limited-number-user">
        <div class="tag-presentation-content"><span><%=resource.getStringWithParams("JDP.quota",
            String.valueOf(domObject.getUserDomainQuota().getMaxCount())) %></span></div>
      </div>
      <% } %>
      <div class="bgDegradeGris" id="link-domain-content">
        <p>
          <img alt="" src="/silverpeas/util/icons/link.gif"/> <%=resource
            .getString("JDP.silverpeasServerURL") %> <br/>
          <input type="text" size="40" value="<%=domObject.getSilverpeasServerURL() %>" onmouseup="return false" onfocus="select();"/>
        </p>
      </div>
    </div>
  </c:if>

  <div class="principalContent">
    <h2 class="principal-content-title sql-domain"><%=getDomainLabel(domObject, resource)%>
    </h2>
    <div id="number-user-group-domainContent">
      <c:if test="${not mixedDomain}">
      <span id="number-user-domainContent"><%=subUsers.size() %> <%=resource.getString("GML.user_s") %></span>
      <c:if test="${isGroupHandled}">
      <span> -</span>
      </c:if>
      </c:if>
      <c:if test="${isGroupHandled}">
        <span id="number-group-domainContent"><%=subGroups.length %> <%=resource.getString("GML.group_s") %></span>
        <c:if test="${fn:length(appGroupList)} > 0}">
            <span> -</span>
            <span id="number-app-group-domainContent"><%=appGroups.length%> <%=resource.getString("GML.appGroup_s") %></span>
        </c:if>
      </c:if>
    </div>
    <% if (StringUtil.isDefined(domObject.getDescription()) && !mixedDomain) { %>
    <p id="description-domainContent"><%=WebEncodeHelper.javaStringToHtmlString(domObject.getDescription())%></p>
    <% } %>
  </div>

  <br/>

  <view:areaOfOperationOfCreation/>

  <div class="tableBoard" id="domain-search">
    <div class="field">
      <label class="txtlibform">${userPanelAccessLabel}</label>
      <div class="champs">
        <viewTags:selectUsersAndGroups selectionType="${not isGroupHandled ? 'USER' : 'USER_GROUP'}"
                                       domainIdFilter="${domObject.id}"
                                       navigationalBehavior="true"
                                       onChangeJsCallback="jumpToUser"
                                       hideDeactivatedState="false"/>
      </div>
    </div>
  </div>

  <c:if test="${isGroupHandled}">
    <c:set var="groupCommonLinkPart" value="${requestScope.myComponentURL}groupContent?Idgroup="/>
    <fmt:message var="groupArrayTitle" key="JDP.groups"/>
    <fmt:message var="appGroupArrayTitle" key="JDP.communityGroups"/>
    <fmt:message var="groupLabel" key="GML.groupe"/>
    <fmt:message var="nameLabel" key="GML.name"/>
    <fmt:message var="usersLabel" key="GML.users"/>
    <fmt:message var="descriptionLabel" key="GML.description"/>
    <c:set var="iconPanelSynchronized"><view:icon iconName='<%=resource.getIcon("JDP.groupSynchronized")%>' altText="${groupLabel}"/></c:set>
    <c:set var="iconPanel"><view:icon iconName='<%=resource.getIcon("JDP.group")%>' altText="${groupLabel}"/></c:set>
    <div id="dynamic-group-container">
      <view:arrayPane var="_dc_groupe"
                      routingAddress="domainContent.jsp"
                      numberLinesPerPage="<%=JobDomainSettings.getGroupsNbPerPage()%>"
                      title="${groupArrayTitle} (${fn:length(groupList)})"
                      export="true">
        <view:arrayColumn title="" sortable="false"/>
        <view:arrayColumn title="${nameLabel}" sortable="false"/>
        <view:arrayColumn title="${usersLabel}" sortable="false"/>
        <view:arrayColumn title="${descriptionLabel}" sortable="false"/>
        <view:arrayLines var="group" items="${groupList}">
          <view:arrayLine>
            <view:arrayCellText>
              <c:choose>
                <c:when test="${group.synchronized}">${iconPanelSynchronized}</c:when>
                <c:otherwise>${iconPanel}</c:otherwise>
              </c:choose>
            </view:arrayCellText>
            <view:arrayCellText><view:a href="${groupCommonLinkPart}${group.id}">${silfn:escapeHtml(group.name)}</view:a></view:arrayCellText>
            <view:arrayCellText>${group.totalUsersCount}</view:arrayCellText>
            <view:arrayCellText text="${silfn:escapeHtml(group.description)}"/>
          </view:arrayLine>
        </view:arrayLines>
      </view:arrayPane>
      <script type="text/javascript">
        whenSilverpeasReady(function() {
          sp.arrayPane.ajaxControls('#dynamic-group-container', {
            before : arrayBeforeAjaxRequest
          });
        });
      </script>
    </div>

    <c:if test="${mixedDomain and fn:length(appGroupList) > 0 }">
        <br/>
        <div id="dynamic-app-group-container">
            <view:arrayPane var="_dc_appw_group"
                            routingAddress="domainContent.jsp"
                            numberLinesPerPage="<%=JobDomainSettings.getGroupsNbPerPage()%>"
                            title="${appGroupArrayTitle} (${fn:length(appGroupList)})"
                            export="true">
                <view:arrayColumn title="" sortable="false"/>
                <view:arrayColumn title="${nameLabel}" sortable="false"/>
                <view:arrayColumn title="${usersLabel}" sortable="false"/>
                <view:arrayColumn title="${descriptionLabel}" sortable="false"/>
                <view:arrayLines var="group" items="${appGroupList}">
                    <view:arrayLine>
                        <view:arrayCellText>${iconPanel}</view:arrayCellText>
                        <view:arrayCellText><view:a href="${groupCommonLinkPart}${group.id}">${silfn:escapeHtml(group.name)}</view:a></view:arrayCellText>
                        <view:arrayCellText>${group.totalUsersCount}</view:arrayCellText>
                        <view:arrayCellText text="${silfn:escapeHtml(group.description)}"/>
                    </view:arrayLine>
                </view:arrayLines>
            </view:arrayPane>
            <script type="text/javascript">
              whenSilverpeasReady(function() {
                sp.arrayPane.ajaxControls('#dynamic-app-group-container', {
                  before : arrayBeforeAjaxRequest
                });
              });
            </script>
        </div>
    </c:if>

  </c:if>
  <br/>
  <c:if test="${not mixedDomain}">
    <%
      Map<UserState, Pair<String, String>> bundleCache = new HashMap<>(UserState.values().length);
      for (UserState userState : UserState.values()) {
        bundleCache.put(userState,
            Pair.of(resource.getIcon("JDP.user.state." + userState.getName()),
                resource.getString("GML.user.account.state." + userState.getName())));
      }
    %>

    <fmt:message var="allUserStateLabel" key="JDP.user.state.label.ALL"/>
    <fmt:message var="validUserStateLabel" key="JDP.user.state.label.VALID"/>
    <fmt:message var="blockedUserStateLabel" key="JDP.user.state.label.BLOCKED"/>
    <fmt:message var="disabledUserStateLabel" key="JDP.user.state.label.DEACTIVATED"/>
    <view:setConstant var="BLOCKED"
                      constant="org.silverpeas.core.admin.user.constant.UserState.BLOCKED"/>
    <view:setConstant var="DEACTIVATED"
                      constant="org.silverpeas.core.admin.user.constant.UserState.DEACTIVATED"/>
    <view:setConstant var="VALID"
                      constant="org.silverpeas.core.admin.user.constant.UserState.VALID"/>
    <jsp:useBean id="userStateFilter" class="java.util.LinkedHashMap"/>
    <c:set target="${userStateFilter}" property="${allUserStateLabel}" value="${''}"/>
    <c:set target="${userStateFilter}" property="${validUserStateLabel}"
           value="${VALID.name()}"/>
    <c:set target="${userStateFilter}" property="${blockedUserStateLabel}"
           value="${BLOCKED.name()}"/>
    <c:set target="${userStateFilter}" property="${disabledUserStateLabel}"
           value="${DEACTIVATED.name()}"/>

    <c:set var="currentUserState" value="${requestScope.currentUserState}"/>
    <c:set var="currentStateQuery" value="?state=${currentUserState}"/>
    <c:if test="${empty currentUserState}">
      <c:set var="currentStateQuery" value=""/>
    </c:if>
    <c:set var="bundleCache" value="<%=bundleCache%>"/>
    <c:set var="userList" value="<%=subUsers%>"/>
    <c:set var="lastConnectionColumnEnabled" value="<%=JobDomainSettings.isLastConnectionInfoEnabled()%>"/>
    <c:set var="userCommonLinkPart" value="${requestScope.myComponentURL}userContent?Iduser="/>
    <fmt:message var="userArrayTitle"      key="GML.users"/>
    <fmt:message var="userStateLabel"      key="JDP.userState"/>
    <fmt:message var="lastNameLabel"       key="GML.lastName"/>
    <fmt:message var="surnameLabel"        key="GML.surname"/>
    <fmt:message var="privacyLabel"        key="JDP.dataSensitivity"/>
    <fmt:message var="lastConnectionLabel" key="GML.user.lastConnection"/>
    <fmt:message var="sensitivityEnabled"  key="JDP.dataSensitivityEnabled"/>
    <div id="dynamic-user-container">
      <div id="filter">
        <div id="stateFilter">
          <label for="selectedState">${userStateLabel}</label>
          <select id="selectedState">
            <c:forEach var="userState" items="${userStateFilter.entrySet()}">
              <c:set var="stateValue" value="${userState.value}"/>
              <c:set var="selected"
                     value="${stateValue eq currentUserState ? 'selected' : ''}"/>
              <option value="${stateValue}" ${selected}>${userState.key}</option>
            </c:forEach>
          </select>
        </div>
      </div>
      <view:arrayPane var="_dc_users"
                      routingAddress="domainContent.jsp${currentStateQuery}"
                      numberLinesPerPage="<%=JobDomainSettings.getUsersCountPerPage()%>"
                      title="${userArrayTitle} (${fn:length(userList)})"
                      export="true">
        <view:arrayColumn title="${userStateLabel}" compareOn="${u -> u.state.name}"/>
        <view:arrayColumn title="${lastNameLabel}" compareOn="${u ->u.lastName}"/>
        <view:arrayColumn title="${surnameLabel}" compareOn="${u ->u.firstName}"/>
        <c:if test="${ldapDomain}">
          <view:arrayColumn title="${privacyLabel}" compareOn="${u -> u.hasSensitiveData()}"/>
        </c:if>
        <c:if test="${lastConnectionColumnEnabled}">
          <view:arrayColumn title="${lastConnectionLabel}" compareOn="${u -> u.lastLoginDate}"/>
        </c:if>
        <view:arrayLines var="user" items="${userList}">
          <view:arrayLine>
            <c:set var="iconAndLabel" value="${bundleCache[user.state]}"/>
            <view:arrayCellText><view:icon iconName="${iconAndLabel.left}" altText="${iconAndLabel.right}"/></view:arrayCellText>
            <view:arrayCellText><view:a href="${userCommonLinkPart}${user.id}">${silfn:escapeHtml(user.lastName)}</view:a></view:arrayCellText>
            <view:arrayCellText text="${silfn:escapeHtml(user.firstName)}"/>
            <c:if test="${ldapDomain}">
              <view:arrayCellText text="${user.hasSensitiveData() ? sensitivityEnabled : '-'}"/>
            </c:if>
            <c:if test="${lastConnectionColumnEnabled}">
              <view:arrayCellText text="${silfn:formatDateAndHour(user.lastLoginDate, userLanguage)}"/>
            </c:if>
          </view:arrayLine>
        </view:arrayLines>
      </view:arrayPane>
      <script type="text/javascript">
        let arrayPaneAjaxControl;

        function filterByUserState() {
          const state = $("#selectedState").val();
          const ajaxRequest = sp.ajaxRequest("filterByUserState").byPostMethod();
          ajaxRequest.withParam("state", state);
          ajaxRequest.send().then(arrayPaneAjaxControl.refreshFromRequestResponse);
        }

        $("#selectedState").change(function() {
          filterByUserState(this);
        }).bind();

        whenSilverpeasReady(function() {
          arrayPaneAjaxControl = sp.arrayPane.ajaxControls('#dynamic-user-container', {
            before : arrayBeforeAjaxRequest
          });
        });
      </script>
    </div>
  </c:if>
</view:frame>
<form id="deletionForm" action="" method="post">
    <input type="hidden" name="X-ATKN" value="${requestScope['X-ATKN']}"/>
</form>
<%
	out.println(window.printAfter());
%>
<view:progressMessage/>
</body>
</html>