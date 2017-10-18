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
<%@ page import="org.silverpeas.core.util.WebEncodeHelper" %>
<%@ page import="org.silverpeas.web.jobdomain.control.JobDomainPeasSessionController" %>

<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<%@ include file="check.jsp" %>

<fmt:setLocale value="${requestScope.resources.language}"/>
<view:setBundle bundle="${requestScope.resources.multilangBundle}"/>
<view:setBundle bundle="${requestScope.resources.iconsBundle}" var="icons"/>

<c:set var="context" value="${pageContext.request.contextPath}"/>

<c:set var="reloadDomainNavigationFrame" value="${requestScope.reloadDomainNavigationFrame}"/>
<c:set var="groupData" value="${requestScope.groupObject}"/>
<jsp:useBean id="groupData" type="org.silverpeas.core.admin.user.model.Group"/>
<c:set var="manageableSpaces" value="${requestScope.ManageableSpaces}" />
<c:set var="groupProfiles" value="${requestScope.GroupProfiles}" />

<%
	  Domain  domObject 				= (Domain)request.getAttribute("domainObject");
    Group 	grObject 				= (Group)request.getAttribute("groupObject");
    String 	groupsPath 				= (String)request.getAttribute("groupsPath");
    boolean isDomainRW 				= (Boolean)request.getAttribute("isDomainRW");
    boolean isDomainSync 			= (Boolean)request.getAttribute("isDomainSync");
    boolean isGroupManagerHere		= (Boolean)request.getAttribute("isGroupManagerOnThisGroup");
    boolean isGroupManager			= (Boolean)request.getAttribute("isOnlyGroupManager");
    boolean isGroupManagerDirectly	= (Boolean)request.getAttribute("isGroupManagerDirectlyOnThisGroup");
    boolean isRightCopyReplaceEnabled = (Boolean) request.getAttribute("IsRightCopyReplaceEnabled");

    boolean showTabs		= false;

    String thisGroupId = grObject.getId();

    browseBar.setComponentName(getDomainLabel(domObject, resource), "domainContent?Iddomain="+domObject.getId());
    if (groupsPath != null)
        browseBar.setPath(groupsPath);

    if (grObject.isSynchronized())
    {
	//Group operations
        operationPane.addOperation(resource.getIcon("JDP.groupUpdate"),resource.getString("GML.modify"),"displayGroupModify?Idgroup="+thisGroupId);
        operationPane.addOperation(resource.getIcon("JDP.groupDel"),resource.getString("GML.delete"),"javascript:ConfirmAndSend('"+resource.getString("JDP.groupDelConfirm")+"','"+thisGroupId + "')");
        operationPane.addLine();
        operationPane.addOperation(resource.getIcon("JDP.groupSynchro"),resource.getString("JDP.groupSynchro"), "javascript:doSynchronization()");

        showTabs = true;
    }
    else if (isDomainRW)
    {
	if (!isGroupManager)
	{
		showTabs = true;

	        // Group operations
	        operationPane.addOperationOfCreation(resource.getIcon("JDP.groupAdd"),resource.getString("JDP.groupAdd"),"displayGroupCreate?Idgroup="+thisGroupId);
	        operationPane.addOperation(resource.getIcon("JDP.groupUpdate"),resource.getString("GML.modify"),"displayGroupModify?Idgroup="+thisGroupId);
	        operationPane.addOperation(resource.getIcon("JDP.groupDel"),resource.getString("GML.delete"),"javascript:ConfirmAndSend('"+resource.getString("JDP.groupDelConfirm")+"','"+thisGroupId + "')");
	        // User operations
          operationPane.addLine();
	        operationPane.addOperation(resource.getIcon("JDP.userManage"),resource.getString("JDP.userManage"),"displayAddRemoveUsers?Idgroup="+thisGroupId);
	}
	else if (isGroupManagerHere)
	{
		if (grObject.getSuperGroupId() == null) {
			//Group operations
	    operationPane.addOperationOfCreation(resource.getIcon("JDP.groupAdd"),resource.getString("JDP.groupAdd"),"displayGroupCreate?Idgroup="+thisGroupId);
			//User operations
			operationPane.addOperation(resource.getIcon("JDP.userManage"),resource.getString("JDP.userManage"),"displayAddRemoveUsers?Idgroup="+thisGroupId);
		}
        else {
			//Group operations
	    operationPane.addOperationOfCreation(resource.getIcon("JDP.groupAdd"),resource.getString("JDP.groupAdd"),"displayGroupCreate?Idgroup="+thisGroupId);
	    operationPane.addOperation(resource.getIcon("JDP.groupUpdate"),resource.getString("GML.modify"),"displayGroupModify?Idgroup="+thisGroupId);
	    if (!isGroupManagerDirectly) {
              operationPane.addOperation(resource.getIcon("JDP.groupDel"), resource.getString(
                  "GML.delete"), "javascript:ConfirmAndSend('" + resource.getString(
                  "JDP.groupDelConfirm") + "','" + thisGroupId + "')");
            }
            // User operations
            operationPane.addLine();
            operationPane.addOperation(resource.getIcon("JDP.userManage"), resource.getString(
                "JDP.userManage"), "displayAddRemoveUsers?Idgroup=" + thisGroupId);
		}

		showTabs = true;
	}
    }
    if (isDomainSync) {
      // Group operations
      operationPane.addLine();
      operationPane.addOperation(resource.getIcon("JDP.groupSynchro"),resource.getString("JDP.groupSynchro"),"javascript:doSynchronization()");
      operationPane.addOperation(resource.getIcon("JDP.groupUnsynchro"),resource.getString("JDP.groupUnsynchro"),"groupUnSynchro?Idgroup="+thisGroupId);
    }

  operationPane.addLine();
  operationPane.addOperation("useless", resource.getString("JDP.user.rights.action"), "groupViewRights");
  if (isRightCopyReplaceEnabled) {
    operationPane.addOperation("useless", resource.getString("JDP.rights.assign"), "javascript:assignSameRights()");
  }
%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<view:looknfeel withCheckFormScript="true" withFieldsetStyle="true"/>
<view:includePlugin name="popup"/>
<view:includePlugin name="qtip"/>
<script type="text/javascript">
function ConfirmAndSend(textToDisplay,groupId)
{
  jQuery.popup.confirm(textToDisplay, function() {
    jQuery('#Idgroup').val(groupId);
    jQuery('#deletionForm').submit();
  });
}
  function doSynchronization() {
    $.progressMessage();
    window.location.href = "groupSynchro?Idgroup=${groupData.id}";
  }

  <c:if test="${reloadDomainNavigationFrame}">
  whenSilverpeasReady(function() {
    parent.domainBar.refreshCurrentLevel();
  });
  </c:if>

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
</script>
</head>
<body class="page_content_admin admin-group">
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
<table cellpadding="5" cellspacing="0" border="0" width="100%">
	<tr valign="baseline">
		<%
			String icon = resource.getIcon("JDP.group");
			if (grObject.isSynchronized())
				icon = resource.getIcon("JDP.groupSynchronized");
		%>
		<td><img src="<%=icon%>" alt="<%=resource.getString("GML.groupe") %>" title="<%=resource.getString("GML.groupe")%>" align="absmiddle"/></td>
		<td class="textePetitBold" nowrap="nowrap"><%=resource.getString("GML.name")%> :</td>
		<td align=left valign="baseline" width="100%"><%=WebEncodeHelper.javaStringToHtmlString(grObject.getName())%></td>
	</tr>
  <c:if test="${not empty groupData.description}">
	<tr>
	    <td></td>
		<td valign="baseline" align="left" class="textePetitBold" nowrap="nowrap"><%=resource.getString("GML.description") %> :</td>
		<td align=left valign="baseline" width="100%"><%=WebEncodeHelper.javaStringToHtmlString(grObject.getDescription())%></td>
	</tr>
  </c:if>
  <c:if test="${not empty groupData.rule}">
	<tr>
	<td></td>
	<td valign="baseline" align="left" class="textePetitBold" nowrap="nowrap"><%=resource.getString("JDP.synchroRule") %> :</td>
	<td align=left valign="baseline" width="100%"><%=WebEncodeHelper.javaStringToHtmlString(grObject.getRule())%></td>
    </tr>
  </c:if>
  <tr>
    <td></td>
    <td valign="baseline" align="left" class="textePetitBold" nowrap="nowrap"><%=resource.getString("GML.users") %> :</td>
    <td align=left valign="baseline" width="100%"><%=grObject.getTotalNbUsers()%></td>
  </tr>
</table>
</view:board>
<view:areaOfOperationOfCreation/>
<%
	if (!grObject.isSynchronized()) {
		ArrayPane arrayPane = gef.getArrayPane("_gc_groupe", "groupContent.jsp", request, session);
		Group[] subGroups = (Group[])request.getAttribute("subGroups");

		arrayPane.setVisibleLineNumber(JobDomainSettings.m_GroupsByPage);
		arrayPane.setTitle(resource.getString("JDP.groups") + " (" +  subGroups.length + ")");

		arrayPane.addArrayColumn("&nbsp;");
		arrayPane.addArrayColumn(resource.getString("GML.nom"));
		arrayPane.addArrayColumn(resource.getString("GML.users"));
		arrayPane.addArrayColumn(resource.getString("GML.description"));
		arrayPane.setSortable(false);

		if (subGroups != null) {
			Group group = null;
			for(int i=0; i<subGroups.length; i++){
				//creation des ligne de l'arrayPane
				group = subGroups[i];
			if (group != null) {
					ArrayLine arrayLine = arrayPane.addArrayLine();
					IconPane iconPane1 = gef.getIconPane();
					Icon groupIcon = iconPane1.addIcon();
					if (group.isSynchronized())
						groupIcon.setProperties(resource.getIcon("JDP.groupSynchronized"), resource.getString("GML.groupe"), "");
			        else
					groupIcon.setProperties(resource.getIcon("JDP.group"), resource.getString("GML.groupe"), "");
					arrayLine.addArrayCellIconPane(iconPane1);
					arrayLine.addArrayCellLink(WebEncodeHelper.javaStringToHtmlString(group.getName()), (String)request.getAttribute("myComponentURL")+"groupContent?Idgroup="+group.getId());
			        arrayLine.addArrayCellText(group.getTotalNbUsers());
			        arrayLine.addArrayCellText(WebEncodeHelper.javaStringToHtmlString(group.getDescription()));
			}
			}
		}
		out.println(arrayPane.print());
	}

	out.println("<br/>");

  ArrayPane arrayPaneUser = gef.getArrayPane("_gc_users", "groupContent.jsp", request, session);
  List<UserDetail> subUsers = (List<UserDetail>)request.getAttribute("subUsers");

  arrayPaneUser.setVisibleLineNumber(JobDomainSettings.m_UsersByPage);
  arrayPaneUser.setTitle(resource.getString("GML.users") + " (" +  subUsers.size() + ")");
  arrayPaneUser.setExportData(true);

  arrayPaneUser.addArrayColumn(resource.getString("JDP.userState"));
  arrayPaneUser.addArrayColumn(resource.getString("GML.lastName"));
  arrayPaneUser.addArrayColumn(resource.getString("GML.surname"));
  arrayPaneUser.addArrayColumn(resource.getString("GML.user.lastConnection"));

  if (subUsers != null) {
    for(UserDetail user : subUsers){
        ArrayLine arrayLineUser = arrayPaneUser.addArrayLine();
        String userIcon = resource.getIcon("JDP.user.state." + user.getState().getName());
        String iconAltText = resource.getString("GML.user.account.state."+user.getState().getName());
        ArrayCellText cellIcon = arrayLineUser.addArrayCellText("<img src=\"" + userIcon
              + "\" alt=\"" + iconAltText + "\" title=\"" + iconAltText + "\"/>");
        cellIcon.setCompareOn(user.getState().name());
        arrayLineUser.addArrayCellLink(WebEncodeHelper.javaStringToHtmlString(user.getLastName()), (String)request.getAttribute("myComponentURL") + "userContent?Iduser=" + user.getId());
        arrayLineUser.addArrayCellText(WebEncodeHelper.javaStringToHtmlString(user.getFirstName()));
        Date lastConnection = user.getLastLoginDate();
        ArrayCellText cell = arrayLineUser.addArrayCellText(resource.getOutputDateAndHour(lastConnection));
        cell.setCompareOn(lastConnection);
      }
  }
  out.println(arrayPaneUser.print());
%>

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
  <input id="Idgroup" type="hidden" name="Idgroup"/>
</form>
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