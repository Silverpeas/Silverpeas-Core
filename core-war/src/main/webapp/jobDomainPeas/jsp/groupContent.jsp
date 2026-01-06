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
<%@ page import="org.apache.commons.lang3.tuple.Pair" %>
<%@ page import="org.silverpeas.core.admin.user.constant.UserState" %>
<%@ page import="org.silverpeas.web.jobdomain.control.JobDomainPeasSessionController" %>
<%@ page import="java.util.Arrays" %>

<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://www.silverpeas.com/tld/silverFunctions" prefix="silfn" %>
<%@ include file="check.jsp" %>

<c:set var="userLanguage" value="${requestScope.resources.language}"/>
<fmt:setLocale value="${userLanguage}" />
<view:setBundle bundle="${requestScope.resources.multilangBundle}"/>
<view:setBundle bundle="${requestScope.resources.iconsBundle}" var="icons"/>

<c:set var="context" value="${pageContext.request.contextPath}"/>

<c:set var="reloadDomainNavigationFrame" value="${requestScope.reloadDomainNavigationFrame}"/>
<c:set var="groupData" value="${requestScope.groupObject}"/>
<jsp:useBean id="groupData" type="org.silverpeas.core.admin.user.model.Group"/>
<c:set var="manageableSpaces" value="${requestScope.ManageableSpaces}" />
<c:set var="groupProfiles" value="${requestScope.GroupProfiles}" />
<c:if test="${not empty groupProfiles}">
  <jsp:useBean id="groupProfiles" type="java.util.List<org.silverpeas.web.jobdomain.LocalizedComponentInstProfiles>"/>
</c:if>

<fmt:message key="JDP.groupRemConfirm" var="groupRemConfirmMessage"/>
<fmt:message key="JDP.groupDelConfirm" var="groupDelConfirmMessage"/>
<fmt:message key="JDP.groupDelConfirmHelp" var="groupDelConfirmMessageHelp"/>
<fmt:message key="GML.mandatory" var="mandatoryText"/>

<%
  Domain domObject = (Domain) request.getAttribute("domainObject");
  Group grObject = (Group) request.getAttribute("groupObject");
  Group[] subGroups = (Group[]) request.getAttribute("subGroups");
  List<UserDetail> subUsers = (List<UserDetail>) request.getAttribute("subUsers");
  String groupsPath = (String) request.getAttribute("groupsPath");
  boolean isDomainRW = (Boolean) request.getAttribute("isDomainRW");
  boolean isDomainSync = (Boolean) request.getAttribute("isDomainSync");
  boolean isGroupManagerHere = (Boolean) request.getAttribute("isGroupManagerOnThisGroup");
  boolean isGroupManager = (Boolean) request.getAttribute("isOnlyGroupManager");
  boolean isGroupManagerDirectly = (Boolean) request.getAttribute("isGroupManagerDirectlyOnThisGroup");
  boolean isRightCopyReplaceEnabled = (Boolean) request.getAttribute("IsRightCopyReplaceEnabled");
  boolean onlySpaceManager = (Boolean) request.getAttribute("isOnlySpaceManager");

  boolean showTabs = false;

  String thisGroupId = grObject.getId();

  browseBar.setComponentName(getDomainLabel(domObject, resource), "domainContent?Iddomain=" + domObject.getId());
  if (groupsPath != null) {
    browseBar.setPath(groupsPath);
  }
  if (isDomainRW) {
    if (!isGroupManager) {
      showTabs = true;
      // Group operations
      operationPane.addOperationOfCreation(resource.getIcon("JDP.groupAdd"), resource.getString("JDP.groupAdd"), "displayGroupCreate?Idgroup=" + thisGroupId);
      operationPane.addOperation(resource.getIcon("JDP.groupUpdate"), resource.getString("GML.modify"), "displayGroupUpdate?Idgroup=" + thisGroupId);
      operationPane.addOperation(resource.getIcon("JDP.groupDel"), resource.getString("GML.remove"), "javascript:removeGroup()");
      // User operations
      operationPane.addLine();
      if (grObject.isSynchronized()) {
        operationPane.addOperation(resource.getIcon("JDP.groupSynchro"), resource.getString("JDP.groupSynchro"), "javascript:doSynchronization()");
      } else {
        operationPane.addOperation(resource.getIcon("JDP.userManage"), resource.getString("JDP.userManage"), "displayAddRemoveUsers?Idgroup=" + thisGroupId);
      }
    } else if (isGroupManagerHere) {
      if (grObject.getSuperGroupId() == null) {
        //Group operations
        operationPane.addOperationOfCreation(resource.getIcon("JDP.groupAdd"), resource.getString("JDP.groupAdd"), "displayGroupCreate?Idgroup=" + thisGroupId);
        //User operations
        if (grObject.isSynchronized()) {
          operationPane.addOperation(resource.getIcon("JDP.groupSynchro"), resource.getString("JDP.groupSynchro"), "javascript:doSynchronization()");
        } else {
          operationPane.addOperation(resource.getIcon("JDP.userManage"), resource.getString("JDP.userManage"), "displayAddRemoveUsers?Idgroup=" + thisGroupId);
        }
      } else {
        //Group operations
        operationPane.addOperationOfCreation(resource.getIcon("JDP.groupAdd"), resource.getString("JDP.groupAdd"), "displayGroupCreate?Idgroup=" + thisGroupId);
        operationPane.addOperation(resource.getIcon("JDP.groupUpdate"), resource.getString("GML.modify"), "displayGroupUpdate?Idgroup=" + thisGroupId);
        if (!isGroupManagerDirectly) {
          operationPane.addOperation(resource.getIcon("JDP.groupDel"), resource.getString("GML.remove"), "javascript:removeGroup()");
        }
        // User operations
        operationPane.addLine();
        if (grObject.isSynchronized()) {
          operationPane.addOperation(resource.getIcon("JDP.groupSynchro"), resource.getString("JDP.groupSynchro"), "javascript:doSynchronization()");
        } else {
          operationPane.addOperation(resource.getIcon("JDP.userManage"), resource.getString("JDP.userManage"), "displayAddRemoveUsers?Idgroup=" + thisGroupId);
        }
      }
      showTabs = true;
    }
  }
  if (isDomainSync) {
    // Group operations
    operationPane.addLine();
    operationPane.addOperation(resource.getIcon("JDP.groupSynchro"), resource.getString("JDP.groupSynchro"), "javascript:doSynchronization()");
    operationPane.addOperation(resource.getIcon("JDP.groupUnsynchro"), resource.getString("JDP.groupUnsynchro"), "groupUnSynchro?Idgroup=" + thisGroupId);
  }
  operationPane.addLine();
  operationPane.addOperation("useless", resource.getString("JDP.user.rights.action"), "groupViewRights");
  if (isRightCopyReplaceEnabled) {
    operationPane.addOperation("useless", resource.getString("JDP.rights.assign"), "javascript:assignSameRights()");
  }
  if (onlySpaceManager) {
    // no action to space manager
    operationPane.clear();
    showTabs = false;
  }
%>

<c:set var="grObject" value="<%=grObject%>"/>
<c:set var="subGroupList" value="<%=Arrays.asList(subGroups)%>"/>

<view:sp-page>
<view:sp-head-part withCheckFormScript="true" withFieldsetStyle="true">
<view:includePlugin name="qtip"/>
<style>
  #deletionFormDialog .complement {
    display: flex;
    padding-top: 5px;
    vertical-align: middle;
  }
  #deletionFormDialog label {
    padding-left: 5px;
  }
  #deletionFormDialog .help {
    font-size: 0.8em;
  }
</style>
<script type="text/javascript">
  function removeGroup() {
    const $dialog = jQuery('#deletionFormDialog');
    $dialog.popup('confirmation', {
      callback : function() {
        const $deletionForm = jQuery('#deletionForm');
        if (jQuery('#definitiveDeletion')[0].checked) {
          $deletionForm.attr("action", "groupDelete");
        } else {
          $deletionForm.attr("action", "groupRemove");
        }
        $deletionForm.submit();
      }
    });
  }

  function doSynchronization() {
    $.progressMessage();
    window.location.href = "groupSynchro?Idgroup=${groupData.id}&X-ATKN=${requestScope['X-ATKN']}";
  }

  <c:if test="${reloadDomainNavigationFrame}">
  whenSilverpeasReady(function() {
    parent.refreshCurrentLevel();
  });
  </c:if>

function assignSameRights() {
  $("#assignRightsDialog").dialog("open");
}

function ifCorrectFormExecute(callback) {
  let errorMsg = "";
  let errorNb = 0;
  const sourceRightsId = document.rightsForm.sourceRightsId.value;

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

  location.href = "#group-profiles";

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

const arrayBeforeAjaxRequest = function () {
  if (${fn:length(subGroupList)} > 25) {
    spProgressMessage.show();
  }
}
</script>
</view:sp-head-part>
<view:sp-body-part cssClass="page_content_admin admin-group">
<%
out.println(window.printBefore());
if (showTabs) {
	TabbedPane tabbedPane = gef.getTabbedPane();
	tabbedPane.addTab(resource.getString("GML.description"), "groupContent?Idgroup="+thisGroupId, true);
	tabbedPane.addTab(resource.getString("JDP.roleManager"), "groupManagersView?Id="+thisGroupId, false);
	out.println(tabbedPane.print());
}
%>
<view:frame>
<view:board>
<table>
  <caption></caption>
  <th></th>
	<tr>
		<%
			String icon = resource.getIcon("JDP.group");
			if (grObject.isSynchronized())
				icon = resource.getIcon("JDP.groupSynchronized");
		%>
		<td><img src="<%=icon%>" alt="<%=resource.getString("GML.groupe") %>" title="<%=resource.getString("GML.groupe")%>"/></td>
		<td class="textePetitBold"><%=resource.getString("GML.name")%> :</td>
		<td><%=WebEncodeHelper.javaStringToHtmlString(grObject.getName())%></td>
	</tr>
  <c:if test="${not empty groupData.description}">
	<tr>
	    <td></td>
		<td class="textePetitBold"><%=resource.getString("GML.description") %> :</td>
		<td><%=WebEncodeHelper.javaStringToHtmlString(grObject.getDescription())%></td>
	</tr>
  </c:if>
  <c:if test="${not empty groupData.rule}">
	<tr>
	<td></td>
	<td class="textePetitBold"><%=resource.getString("JDP.synchroRule") %> :</td>
	<td><%=WebEncodeHelper.javaStringToHtmlString(grObject.getRule())%></td>
    </tr>
  </c:if>
  <tr>
    <td></td>
    <td class="textePetitBold"><%=resource.getString("GML.users") %> :</td>
    <td><%=grObject.getTotalNbUsers()%></td>
  </tr>
  <tr>
    <td></td>
    <td class="textePetitBold"><%=resource.getString("GML.Id") %> :</td>
    <td><%=grObject.getId()%></td>
  </tr>
</table>
</view:board>
<view:areaOfOperationOfCreation/>
  <c:set var="groupCommonLinkPart" value="${requestScope.myComponentURL}groupContent?Idgroup="/>
  <fmt:message var="groupArrayTitle" key="JDP.groups"/>
  <fmt:message var="groupLabel" key="GML.groupe"/>
  <fmt:message var="nameLabel" key="GML.name"/>
  <fmt:message var="usersLabel" key="GML.users"/>
  <fmt:message var="descriptionLabel" key="GML.description"/>
  <c:set var="iconPanelSynchronized"><view:icon iconName='<%=resource.getIcon("JDP.groupSynchronized")%>' altText="${groupLabel}"/></c:set>
  <c:set var="iconPanel"><view:icon iconName='<%=resource.getIcon("JDP.group")%>' altText="${groupLabel}"/></c:set>
  <div id="dynamic-group-container">
    <view:arrayPane var="_gc_groupe"
                    routingAddress="groupContent.jsp"
                    numberLinesPerPage="<%=JobDomainSettings.m_GroupsByPage%>"
                    title="${groupArrayTitle} (${fn:length(subGroupList)})"
                    export="true">
      <view:arrayColumn title="" sortable="false"/>
      <view:arrayColumn title="${nameLabel}" sortable="false"/>
      <view:arrayColumn title="${usersLabel}" sortable="false"/>
      <view:arrayColumn title="${descriptionLabel}" sortable="false"/>
      <view:arrayLines var="group" items="${subGroupList}">
        <view:arrayLine>
          <view:arrayCellText>
            <c:choose>
              <c:when test="${group.synchronized}">${iconPanelSynchronized}</c:when>
              <c:otherwise>${iconPanel}</c:otherwise>
            </c:choose>
          </view:arrayCellText>
          <view:arrayCellText><view:a href="${groupCommonLinkPart}${group.id}">${silfn:escapeHtml(group.name)}</view:a></view:arrayCellText>
          <view:arrayCellText>${group.totalNbUsers}</view:arrayCellText>
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
  <br/>
<%
  Map<UserState, Pair<String, String>> bundleCache = new HashMap<>(UserState.values().length);
  for (UserState userState : UserState.values()) {
    bundleCache.put(userState,
        Pair.of(resource.getIcon("JDP.user.state." + userState.getName()),
            resource.getString("GML.user.account.state." + userState.getName())));
  }
%>
  <c:set var="bundleCache" value="<%=bundleCache%>"/>
  <c:set var="subUserList" value="<%=subUsers%>"/>
  <c:set var="lastConnectionColumnEnabled" value="<%=JobDomainSettings.lastConnectionColumnEnabled%>"/>
  <c:set var="userCommonLinkPart" value="${requestScope.myComponentURL}userContent?Iduser="/>
  <fmt:message var="userArrayTitle" key="GML.users"/>
  <fmt:message var="userStateLabel" key="JDP.userState"/>
  <fmt:message var="lastNameLabel" key="GML.lastName"/>
  <fmt:message var="surnameLabel" key="GML.surname"/>
  <fmt:message var="lastConnectionLabel" key="GML.user.lastConnection"/>
  <div id="dynamic-user-container">
    <view:arrayPane var="_gc_users"
                    routingAddress="groupContent.jsp"
                    numberLinesPerPage="<%=JobDomainSettings.m_UsersByPage%>"
                    title="${userArrayTitle} (${fn:length(subUserList)})"
                    export="true">
      <view:arrayColumn title="${userStateLabel}" compareOn="${u -> u.state.name}"/>
      <view:arrayColumn title="${lastNameLabel}" compareOn="${u ->u.lastName}"/>
      <view:arrayColumn title="${surnameLabel}" compareOn="${u ->u.firstName}"/>
      <c:if test="${lastConnectionColumnEnabled}">
        <view:arrayColumn title="${lastConnectionLabel}" compareOn="${u -> u.lastLoginDate}"/>
      </c:if>
      <view:arrayLines var="user" items="${subUserList}">
        <view:arrayLine>
          <c:set var="iconAndLabel" value="${bundleCache[user.state]}"/>
          <view:arrayCellText><view:icon iconName="${iconAndLabel.left}" altText="${iconAndLabel.right}"/></view:arrayCellText>
          <view:arrayCellText><view:a href="${userCommonLinkPart}${user.id}">${silfn:escapeHtml(user.lastName)}</view:a></view:arrayCellText>
          <view:arrayCellText text="${silfn:escapeHtml(user.firstName)}"/>
          <c:if test="${lastConnectionColumnEnabled}">
            <view:arrayCellText text="${silfn:formatDateAndHour(user.lastLoginDate, userLanguage)}"/>
          </c:if>
        </view:arrayLine>
      </view:arrayLines>
    </view:arrayPane>
    <script type="text/javascript">
      whenSilverpeasReady(function() {
        sp.arrayPane.ajaxControls('#dynamic-user-container', {
          before : arrayBeforeAjaxRequest
        });
      });
    </script>
  </div>

  <c:if test="${not empty manageableSpaces}">
    <br/><br/>
    <fieldset class="skinFieldset qTipCompliant" id="manageable-spaces">
      <legend><fmt:message key="JDP.user.spaces.manageable"/></legend>
      <view:arrayPane var="profile-spaces" routingAddress="#" numberLinesPerPage="-1">
        <fmt:message key="GML.name" var="labelSpaceName"/>
        <fmt:message key="GML.description" var="labelSpaceDesc"/>
        <view:arrayColumn title="${labelSpaceName}" sortable="false"/>
        <view:arrayColumn title="${labelSpaceDesc}" sortable="false"/>

        <c:forEach var="manageableSpace" items="${manageableSpaces}">
          <view:arrayLine>
            <c:set var="spaceLink"><a href="#" rel="/JobDomainPeasItemPathServlet?SpaceId=${manageableSpace.id}">${manageableSpace.name}</a></c:set>
            <view:arrayCellText text="${spaceLink}"/>
            <view:arrayCellText text="${manageableSpace.description}"/>
          </view:arrayLine>
        </c:forEach>
      </view:arrayPane>
    </fieldset>
  </c:if>

  <c:if test="${not empty groupProfiles}">
    <br/>
    <fieldset class="skinFieldset qTipCompliant" id="group-profiles">
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

        <c:forEach var="userInstanceProfiles" items="${groupProfiles}">
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

</view:frame>
<form id="deletionForm" action="groupDelete" method="post">
    <input type="hidden" name="X-ATKN" value="${requestScope['X-ATKN']}"/>
  <input id="Idgroup" type="hidden" name="Idgroup" value="${groupData.id}"/>
</form>

<div id="deletionFormDialog" style="display: none;">
  ${groupRemConfirmMessage}
  <div class="complement">
    <input type="checkbox" id="definitiveDeletion">
    <label for="definitiveDeletion">${groupDelConfirmMessage}</label>
  </div>
  <div class="help">(${groupDelConfirmMessageHelp})</div>
</div>
<% out.println(window.printAfter()); %>

<!-- Dialog choice rights -->
<fmt:message key="JDP.sourceRightsUserPanel" var="sourceRightsUserPanelIcon" bundle="${icons}" />
<fmt:message key="JDP.mandatory" var="mandatoryIcon" bundle="${icons}" />
<c:set var="ASSIGNATION_MODE_ADD"><%= JobDomainPeasSessionController.ADD_RIGHTS %></c:set>
<c:set var="ASSIGNATION_MODE_REPLACE"><%= JobDomainPeasSessionController.REPLACE_RIGHTS %></c:set>

<% if (isRightCopyReplaceEnabled) { %>
<div id="assignRightsDialog" title="<fmt:message key="JDP.rights.assign"/>">
  <form accept-charset="UTF-8" enctype="multipart/form-data;charset=utf-8" id="affected-profil"
        name="rightsForm" action="AssignSameRights" method="post">
      <input type="hidden" name="X-ATKN" value="${requestScope['X-ATKN']}"/>
    <label class="label-ui-dialog" for="profil-from"><fmt:message key="JDP.rights.assign.as"/></label>
    <span class="champ-ui-dialog">
		    <input type="text" id="sourceRightsName" name="sourceRightsName" value="" size="50" readonly="readonly"/>
		    <a title="<fmt:message key="JDP.rights.assign.sourceRightsUserPanel"/>" href="#" onclick="javascript:SP_openWindow('SelectRightsUserOrGroup','SelectUserGroupWindow',800,600,'');">
				<img src="${context}${sourceRightsUserPanelIcon}"
             alt="<fmt:message key="JDP.rights.assign.sourceRightsUserPanel"/>"
             title="<fmt:message key="JDP.rights.assign.sourceRightsUserPanel"/>"/>
			  </a>
        <img src="${context}${mandatoryIcon}" width="5" height="5" alt="${mandatoryText}"/>
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
      <img src="${context}${mandatoryIcon}" width="5" height="5" alt="${mandatoryText}"/> : <fmt:message key="GML.requiredField"/>
    </label>
  </form>
</div>
<% } %>

<view:progressMessage/>
</view:sp-body-part>
</view:sp-page>
