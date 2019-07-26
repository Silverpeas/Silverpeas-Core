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
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="org.apache.commons.lang3.tuple.Pair"%>
<%@ page import="org.silverpeas.core.admin.quota.constant.QuotaLoad" %>
<%@ page import="org.silverpeas.core.admin.user.constant.UserState" %>
<%@ page import="org.silverpeas.core.util.logging.Level" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<%@ taglib tagdir="/WEB-INF/tags/silverpeas/util" prefix="viewTags" %>

<%@ include file="check.jsp" %>

<fmt:setLocale value="${sessionScope['SilverSessionController'].favoriteLanguage}" />
<view:setBundle bundle="${requestScope.resources.multilangBundle}" />

<c:set var="domObject" value="${requestScope.domainObject}"/>
<jsp:useBean id="domObject" type="org.silverpeas.core.admin.domain.model.Domain"/>

<c:set var="isUserImportHandled" value="${not (isSCIMDomain(domObject) or isGoogleDomain(domObject))}"/>
<jsp:useBean id="isUserImportHandled" type="java.lang.Boolean"/>
<c:set var="isGroupHandled" value="${not (isSCIMDomain(domObject) or isGoogleDomain(domObject))}"/>
<jsp:useBean id="isGroupHandled" type="java.lang.Boolean"/>

<fmt:message var="userPanelAccessLabel" key="JDP.userPanelAccess"><fmt:param value="${not isGroupHandled ? 0 : 2}"/></fmt:message>
<jsp:useBean id="userPanelAccessLabel" type="java.lang.String"/>

<%
  UserDetail theUser 			= (UserDetail)request.getAttribute("theUser");
  boolean isDomainRW 			= (Boolean)request.getAttribute("isDomainRW");
  boolean isDomainSync 		= (Boolean)request.getAttribute("isDomainSync");
  boolean isUserRW 			= (Boolean)request.getAttribute("isUserRW");
  boolean isGroupManager		= (Boolean)request.getAttribute("isOnlyGroupManager");
  boolean isUserAddingAllowed = (Boolean)request.getAttribute("isUserAddingAllowedForGroupManager");
  Group[] subGroups = (Group[])request.getAttribute("subGroups");
  List<UserDetail> subUsers = (List<UserDetail>)request.getAttribute("subUsers");

  boolean isDomainSql = "org.silverpeas.core.admin.domain.driver.sqldriver.SQLDriver".equals(domObject.getDriverClassName());
  boolean isDomainScim = "org.silverpeas.core.admin.domain.driver.scimdriver.SCIMDriver".equals(domObject.getDriverClassName());
  boolean isDomainGoogle = "org.silverpeas.core.admin.domain.driver.googledriver.GoogleDriver".equals(domObject.getDriverClassName());
  boolean mixedDomain = domObject.isMixedOne();

  browseBar.setComponentName(getDomainLabel(domObject, resource), "domainContent?Iddomain="+domObject.getId());

  // Initializing users in domain quota
  boolean isUserDomainQuotaFull = JobDomainSettings.usersInDomainQuotaActivated && domObject.isQuotaReached();

  // Domain operations
	operationPane.addOperation(resource.getIcon("JDP.userPanelAccess"),userPanelAccessLabel,"displaySelectUserOrGroup");
  if (theUser.isAccessAdmin()) {

	    if (!mixedDomain) {
	      operationPane.addOperation(resource.getIcon("JDP.removedUserAccess"), resource.getString("JDP.removedUserAccess"), "displayRemovedUsers");
	      operationPane.addOperation(resource.getIcon("JDP.deletedUserAccess"), resource.getString("JDP.deletedUserAccess"), "displayDeletedUsers");
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
%>

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
  sp.formConfig("domainSQLSynchro").submit();
}

function jumpToUser(selectionUserAPI) {
  var userIds = selectionUserAPI.getSelectedUserIds();
  var groupIds = selectionUserAPI.getSelectedGroupIds();
  if (userIds.length) {
    sp.navRequest("userContent").withParam("Iduser", userIds[0]).go();
  } else if (groupIds.length) {
    sp.navRequest("groupContent").withParam("Idgroup", groupIds[0]).go();
  }
}
</script>
</head>
<body id="domainContent" class="page_content_admin">
<%
out.println(window.printBefore());
%>
<view:frame>

  <% if (isUserDomainQuotaFull) { %>
  <div class="inlineMessage-nok"><fmt:message key="JDP.userDomainQuotaFull" /></div>
  <br clear="all" />
  <% } %>

<% if (!mixedDomain) { %>
<div class="rightContent" id="right-content-domainContent">
  <% if (JobDomainSettings.usersInDomainQuotaActivated && !QuotaLoad.UNLIMITED.equals(domObject.getUserDomainQuota().getLoad())) { %>
  <div class="tag-presentation limited-number-user">
    <div class="tag-presentation-content"><span><%=resource.getStringWithParams("JDP.quota", String.valueOf(domObject.getUserDomainQuota().getMaxCount())) %></span></div>
  </div>
  <% } %>
  <div class="bgDegradeGris" id="link-domain-content">
		<p>
			<img alt="" src="/silverpeas/util/icons/link.gif"/> <%=resource.getString("JDP.silverpeasServerURL") %> <br/>
			<input type="text" size="40" value="<%=domObject.getSilverpeasServerURL() %>" onmouseup="return false" onfocus="select();"/>
		</p>
	</div>
</div>
<% } %>

<div class="principalContent">
  <h2 class="principal-content-title sql-domain"> <%=getDomainLabel(domObject, resource)%> </h2>
  <div id="number-user-group-domainContent">
    <% if (!mixedDomain) { %>
      <span id="number-user-domainContent"><%=subUsers.size() %> <%=resource.getString("GML.user_s") %></span>
    <% } %>
    <c:if test="${isGroupHandled}">
      <span> -</span>
      <span id="number-group-domainContent"><%=subGroups.length %> <%=resource.getString("GML.group_s") %></span>
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
  <%
  ArrayPane arrayPane = gef.getArrayPane("_dc_groupe", "domainContent.jsp", request, session);
  arrayPane.setVisibleLineNumber(JobDomainSettings.m_GroupsByPage);
  arrayPane.setTitle(resource.getString("JDP.groups") + " (" +  subGroups.length + ")");

  arrayPane.addArrayColumn("&nbsp;");
  arrayPane.addArrayColumn(resource.getString("GML.name"));
  arrayPane.addArrayColumn(resource.getString("GML.users"));
  arrayPane.addArrayColumn(resource.getString("GML.description"));
  arrayPane.setSortable(false);

  if (subGroups != null) {
    final String groupCommonLinkPart = request.getAttribute("myComponentURL") + "groupContent?Idgroup=";
    IconPane iconPanelSynchronized = gef.getIconPane();
    iconPanelSynchronized.addIcon().setProperties(resource.getIcon("JDP.groupSynchronized"), resource.getString("GML.groupe"), "");
    IconPane iconPanel = gef.getIconPane();
    iconPanel.addIcon().setProperties(resource.getIcon("JDP.group"), resource.getString("GML.groupe"), "");
      for(Group group : subGroups){
	  if (group != null) {
	          ArrayLine arrayLine = arrayPane.addArrayLine();
	          if (group.isSynchronized()) {
              arrayLine.addArrayCellIconPane(iconPanelSynchronized);
	          } else {
              arrayLine.addArrayCellIconPane(iconPanel);
	          }
            arrayLine.addArrayCellLink(WebEncodeHelper.javaStringToHtmlString(group.getName()), groupCommonLinkPart + group.getId());
	          arrayLine.addArrayCellText(group.getTotalNbUsers());
	          arrayLine.addArrayCellText(WebEncodeHelper.javaStringToHtmlString(group.getDescription()));
	  }
      }
  }
  out.println(arrayPane.print());
  %>
  </c:if>
<br/>

<%
  if (!mixedDomain) {
	  ArrayPane arrayPaneUser = gef.getArrayPane("_dc_users", "domainContent.jsp", request, session);

	  arrayPaneUser.setVisibleLineNumber(JobDomainSettings.m_UsersByPage);
	  arrayPaneUser.setTitle(resource.getString("GML.users") + " (" +  subUsers.size() + ")");
    arrayPaneUser.setExportData(true);

	  arrayPaneUser.addArrayColumn(resource.getString("JDP.userState"));
	  arrayPaneUser.addArrayColumn(resource.getString("GML.lastName"));
	  arrayPaneUser.addArrayColumn(resource.getString("GML.surname"));
	  if (JobDomainSettings.lastConnectionColumnEnabled) {
		  arrayPaneUser.addArrayColumn(resource.getString("GML.user.lastConnection"));
	  }

	  if (subUsers != null) {
      Map<UserState, Pair<String, String>> bundleCache = new HashMap<UserState, Pair<String, String>>(UserState.values().length);
      for (UserState userState : UserState.values()) {
        bundleCache.put(userState, Pair.of(
            resource.getIcon("JDP.user.state."+userState.getName()),
            resource.getString("GML.user.account.state."+userState.getName())
        ));
      }
      final String userCommonLinkPart = request.getAttribute("myComponentURL") + "userContent?Iduser=";
	      for(UserDetail user : subUsers){
	          ArrayLine arrayLineUser = arrayPaneUser.addArrayLine();
            Pair<String, String> iconAndLabel = bundleCache.get(user.getState());
            String icon = iconAndLabel.getLeft();
            String iconAltText = iconAndLabel.getRight();
	          ArrayCellText cellIcon = arrayLineUser.addArrayCellText("<img src=\""+icon+"\" alt=\""+iconAltText+"\" title=\""+iconAltText+"\"/>");
	          cellIcon.setCompareOn(user.getState().name());
            arrayLineUser.addArrayCellLink(WebEncodeHelper.javaStringToHtmlString(user.getLastName()), userCommonLinkPart + user.getId());
	          arrayLineUser.addArrayCellText(WebEncodeHelper.javaStringToHtmlString(user.getFirstName()));
	          if (JobDomainSettings.lastConnectionColumnEnabled) {
		          Date lastConnection = user.getLastLoginDate();
		          ArrayCellText cell = arrayLineUser.addArrayCellText(resource.getOutputDateAndHour(lastConnection));
		          cell.setCompareOn(lastConnection);
	          }
	      }
	  }
	  out.println(arrayPaneUser.print());
  }
%>
</view:frame>
<form id="deletionForm" action="" method="post">
</form>
<%
	out.println(window.printAfter());
%>
</body>
</html>