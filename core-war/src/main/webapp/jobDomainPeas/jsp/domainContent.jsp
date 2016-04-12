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

<%@page import="org.apache.commons.lang3.tuple.Pair"%>
<%@ page import="org.silverpeas.core.admin.user.constant.UserState" %>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.board.Board" %>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.iconpanes.IconPane" %>
<%@ page import="org.silverpeas.core.util.logging.Level" %>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>

<%@ include file="check.jsp" %>

<fmt:setLocale value="${sessionScope[sessionController].language}" />
<view:setBundle bundle="${requestScope.resources.multilangBundle}" />
<%
	Board board = gef.getBoard();

  Domain  domObject 			= (Domain)request.getAttribute("domainObject");
  UserDetail theUser 			= (UserDetail)request.getAttribute("theUser");
  boolean isDomainRW 			= (Boolean)request.getAttribute("isDomainRW");
  boolean isDomainSync 		= (Boolean)request.getAttribute("isDomainSync");
  boolean isUserRW 			= (Boolean)request.getAttribute("isUserRW");
  boolean isGroupManager		= (Boolean)request.getAttribute("isOnlyGroupManager");
  boolean isUserAddingAllowed = (Boolean)request.getAttribute("isUserAddingAllowedForGroupManager");
  Group[] subGroups = (Group[])request.getAttribute("subGroups");
  List<UserDetail> subUsers = (List<UserDetail>)request.getAttribute("subUsers");

  boolean isDomainSql = "org.silverpeas.core.admin.domain.driver.sqldriver.SQLDriver".equals(domObject.getDriverClassName());
  boolean mixedDomain = domObject.isMixedOne();

  browseBar.setComponentName(getDomainLabel(domObject, resource), "domainContent?Iddomain="+domObject.getId());

  // Initializing users in domain quota
  boolean isUserDomainQuotaFull = JobDomainSettings.usersInDomainQuotaActivated && domObject.isQuotaReached();

  // Domain operations
	operationPane.addOperation(resource.getIcon("JDP.userPanelAccess"),resource.getString("JDP.userPanelAccess"),"displaySelectUserOrGroup");
	if (theUser.isAccessAdmin())
	{
	    if (!mixedDomain) {
		    operationPane.addLine();
		if(isDomainSql) {
		        operationPane.addOperation(resource.getIcon("JDP.domainSqlUpdate"),resource.getString("JDP.domainSQLUpdate"),"displayDomainSQLModify");
		        operationPane.addOperation(resource.getIcon("JDP.domainSqlDel"),resource.getString("JDP.domainSQLDel"),"javascript:ConfirmAndSend('"+resource.getString("JDP.domainDelConfirm")+"','domainSQLDelete')");
		    } else {
		        operationPane.addOperation(resource.getIcon("JDP.domainUpdate"),resource.getString("JDP.domainUpdate"),"displayDomainModify");
		        if (!domObject.getId().equals("0"))
				operationPane.addOperation(resource.getIcon("JDP.domainDel"),resource.getString("JDP.domainDel"),"javascript:ConfirmAndSend('"+resource.getString("JDP.domainDelConfirm")+"','domainDelete')");
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
	operationPane.addOperation(resource.getIcon("JDP.domainSynchro"),resource.getString("JDP.domainSynchro"),"displayDomainSynchro");

	//User operations
          operationPane.addOperation(resource.getIcon("JDP.userImport"),resource.getString("JDP.userImport"),"displayUserImport");

          // Group operations
          operationPane.addOperation(resource.getIcon("JDP.groupImport"),resource.getString("JDP.groupImport"),"displayGroupImport");
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
	SettingBundle propDomain = ResourceLocator.getSettingBundle(domObject.getPropFileName());
	boolean synchroUser = propDomain.getBoolean("ExternalSynchro", false);
	if(synchroUser) {
	    operationPane.addLine();
	    operationPane.addOperation(resource.getIcon("JDP.domainSqlSynchro"),resource.getString("JDP.domainSynchro"),"javascript:DomainSQLSynchro()");
	}
  }
%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<view:looknfeel/>
<view:includePlugin name="popup"/>
<script type="text/javascript">
  function ConfirmAndSend(textToDisplay, targetURL) {
    jQuery.popup.confirm(textToDisplay, function() {
      jQuery('#deletionForm').attr('action', targetURL).submit();
    });
  }

function DomainSQLSynchro(){
	top.IdleFrame.SP_openWindow('<%=m_context %>/RjobDomainPeas/jsp/displayDynamicSynchroReport?IdTraceLevel=<%=Level.DEBUG%>', 'SynchroDomainReport', '750', '550', 'menubar=yes,scrollbars=yes,statusbar=yes,resizable=yes');
	window.location.href = "domainSQLSynchro";
}
</script>
</head>
<body id="domainContent">
<%
out.println(window.printBefore());
%>
<view:frame>

<% if (!mixedDomain) { %>
<div class="rightContent" id="right-content-domainContent">
	<div class="bgDegradeGris" id="link-domain-content">
		<p>
			<img alt="" src="/silverpeas/util/icons/link.gif"/> <%=resource.getString("JDP.silverpeasServerURL") %> <br/>
			<input type="text" size="40" value="<%=domObject.getSilverpeasServerURL() %>" onmouseup="return false" onfocus="select();"/>
		</p>
	</div>
</div>
<% } %>

<div class="principalContent">
	<div id="principal-content-domainContent">
		<h2 class="principal-content-title sql-domain"> <%=getDomainLabel(domObject, resource)%> </h2>
		<% if (JobDomainSettings.usersInDomainQuotaActivated && domObject.getUserDomainQuota().exists()) { %>
			<div class="tag-presentation limited-number-user">
				<div class="tag-presentation-content"><span><%=resource.getStringWithParams("JDP.quota", String.valueOf(domObject.getUserDomainQuota().getMaxCount())) %></span></div>
			</div>
		<% } %>
		<div id="number-user-group-domainContent">
			<% if (!mixedDomain) { %>
				<span id="number-user-domainContent"><%=subUsers.size() %> <%=resource.getString("GML.user_s") %></span> -
			<% } %>
			<span id="number-group-domainContent"><%=subGroups.length %> <%=resource.getString("GML.group_s") %></span>
		</div>
		<% if (StringUtil.isDefined(domObject.getDescription()) && !mixedDomain) { %>
			<p id="description-domainContent"><%=EncodeHelper.javaStringToHtmlString(domObject.getDescription())%></p>
		<% } %>
	</div>
</div>
<% if (isUserDomainQuotaFull) { %>
	<div class="inlineMessage-nok"><fmt:message key="JDP.userDomainQuotaFull" /></div>
  <br clear="all" />
<% } %>
<br/>
<view:areaOfOperationOfCreation/>
<%
  ArrayPane arrayPane = gef.getArrayPane("groupe", "domainContent.jsp", request, session);
  arrayPane.setVisibleLineNumber(JobDomainSettings.m_GroupsByPage);
  arrayPane.setTitle(resource.getString("JDP.groups"));

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
            arrayLine.addArrayCellLink(EncodeHelper.javaStringToHtmlString(group.getName()), groupCommonLinkPart + group.getId());
	          arrayLine.addArrayCellText(group.getTotalNbUsers());
	          arrayLine.addArrayCellText(EncodeHelper.javaStringToHtmlString(group.getDescription()));
	  }
      }
  }
  out.println(arrayPane.print());
%>
<br/>

<%
  if (!mixedDomain) {
	  ArrayPane arrayPaneUser = gef.getArrayPane("users", "domainContent.jsp", request, session);

	  arrayPaneUser.setVisibleLineNumber(JobDomainSettings.m_UsersByPage);
	  arrayPaneUser.setTitle(resource.getString("GML.users"));

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
            arrayLineUser.addArrayCellLink(EncodeHelper.javaStringToHtmlString(user.getLastName()), userCommonLinkPart + user.getId());
	          arrayLineUser.addArrayCellText(EncodeHelper.javaStringToHtmlString(user.getFirstName()));
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
<form id="deletionForm" action="" method="POST">
</form>
<%
	out.println(window.printAfter());
%>
</body>
</html>