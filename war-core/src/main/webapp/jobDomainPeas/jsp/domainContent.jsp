<%--

    Copyright (C) 2000 - 2012 Silverpeas

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
  String[][] subUsers = (String[][])request.getAttribute("subUsers");

  boolean isDomainSql = "com.stratelia.silverpeas.domains.sqldriver.SQLDriver".equals(domObject.getDriverClassName());
  boolean mixedDomain = "-1".equals(domObject.getId());

  browseBar.setComponentName(getDomainLabel(domObject, resource), "domainContent?Iddomain="+domObject.getId());

  // Initializing users in domain quota
  boolean isUserDomainQuotaFull = JobDomainSettings.usersInDomainQuotaActivated && domObject.isQuotaReached();

  // Domain operations
	operationPane.addOperation(resource.getIcon("JDP.userPanelAccess"),resource.getString("JDP.userPanelAccess"),"displaySelectUserOrGroup");
	if (theUser.isAccessAdmin())
	{
	    if (!domObject.getId().equals("-1"))
	    {
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
  	ResourceLocator propDomain = new ResourceLocator(domObject.getPropFileName(), "");
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
<script type="text/javascript">
function ConfirmAndSend(textToDisplay,targetURL) {
    if (window.confirm(textToDisplay)) {
        window.location.href = targetURL;
    }
}

function DomainSQLSynchro(){
	top.IdleFrame.SP_openWindow('<%=m_context %>/RjobDomainPeas/jsp/displayDynamicSynchroReport?IdTraceLevel=<%=Integer.toString(SynchroReport.TRACE_LEVEL_DEBUG)%>', 'SynchroReport', '750', '550', 'menubar=yes,scrollbars=yes,statusbar=yes,resizable=yes');
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
				<div class="tag-presentation-content"><span><%=resource.getStringWithParam("JDP.quota", String.valueOf(domObject.getUserDomainQuota().getMaxCount())) %></span></div>
			</div>
		<% } %>
		<div id="number-user-group-domainContent">
			<% if (!mixedDomain) { %>
				<span id="number-user-domainContent"><%=subUsers.length %> <%=resource.getString("GML.user_s") %></span> -
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

  if (subGroups != null)
  {
	  Group group = null;
      for(int i=0; i<subGroups.length; i++){
          //cr�ation des ligne de l'arrayPane
    	  group = subGroups[i];
    	  if (group != null)
    	  {
	          ArrayLine arrayLine = arrayPane.addArrayLine();
	          IconPane iconPane1 = gef.getIconPane();
	          Icon groupIcon = iconPane1.addIcon();
	          if (group.isSynchronized())
	        	  groupIcon.setProperties(resource.getIcon("JDP.groupSynchronized"), resource.getString("GML.groupe"), "");
	          else
	        	  groupIcon.setProperties(resource.getIcon("JDP.group"), resource.getString("GML.groupe"), "");
	          arrayLine.addArrayCellIconPane(iconPane1);
	          arrayLine.addArrayCellLink(EncodeHelper.javaStringToHtmlString(group.getName()), (String)request.getAttribute("myComponentURL")+"groupContent?Idgroup="+group.getId());
	          arrayLine.addArrayCellText(group.getNbUsers());
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

	  arrayPaneUser.addArrayColumn("&nbsp;");
	  arrayPaneUser.addArrayColumn(resource.getString("GML.lastName"));
	  arrayPaneUser.addArrayColumn(resource.getString("GML.surname"));
	  arrayPaneUser.setSortable(false);

	  if (subUsers != null) {
	      for(int i=0; i<subUsers.length; i++){
	          //creation des ligne de l'arrayPane
	          ArrayLine arrayLineUser = arrayPaneUser.addArrayLine();
	          IconPane iconPane1User = gef.getIconPane();
	          Icon userIcon = iconPane1User.addIcon();
	          userIcon.setProperties(resource.getIcon("JDP.user"), resource.getString("GML.user"), "");
	          arrayLineUser.addArrayCellIconPane(iconPane1User);
	          arrayLineUser.addArrayCellLink(subUsers[i][1], (String)request.getAttribute("myComponentURL") + "userContent?Iduser=" + subUsers[i][0]);
	          arrayLineUser.addArrayCellText(subUsers[i][2]);
	        }
	  }
	  out.println(arrayPaneUser.print());
  }
%>
</view:frame>
<%
	out.println(window.printAfter());
%>
</body>
</html>