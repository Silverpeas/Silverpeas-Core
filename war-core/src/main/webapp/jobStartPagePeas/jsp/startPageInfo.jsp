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

<%@page import="org.silverpeas.util.UnitUtil"%>
<%@page import="org.silverpeas.quota.contant.QuotaLoad"%>
<%@page import="com.silverpeas.jobStartPagePeas.JobStartPagePeasSettings"%>

<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<view:setBundle bundle="${requestScope.resources.multilangBundle}" />
<c:set var="space" value="${requestScope.Space}" />

<%@ include file="check.jsp" %>

<%
	int	 			maintenanceState 	= (Integer) request.getAttribute("MaintenanceState");
	String	 		m_SpaceId 			= (String) request.getAttribute("currentSpaceId");
	Integer 		m_firstPageType 	= (Integer)request.getAttribute("FirstPageType");

	String 			m_SubSpace 			= (String) request.getAttribute("nameSubSpace");
	boolean			objectsSelectedInClipboard = new Boolean((String) request.getAttribute("ObjectsSelectedInClipboard")).booleanValue();
	DisplaySorted 	m_SpaceExtraInfos 	= (DisplaySorted)request.getAttribute("SpaceExtraInfos");
  boolean 		isUserAdmin 		= ((Boolean)request.getAttribute("isUserAdmin")).booleanValue();
  boolean 		isBackupEnable 		= ((Boolean)request.getAttribute("IsBackupEnable")).booleanValue();
  boolean 		isInHeritanceEnable = ((Boolean)request.getAttribute("IsInheritanceEnable")).booleanValue();

  SpaceInst 		space 				= (SpaceInst) request.getAttribute("Space");

  // Component space quota
  boolean isComponentSpaceQuotaActivated = JobStartPagePeasSettings.componentsInSpaceQuotaActivated;

  boolean isComponentSpaceQuotaFull = isComponentSpaceQuotaActivated && space.isComponentSpaceQuotaReached();
  if (isComponentSpaceQuotaActivated && QuotaLoad.UNLIMITED.equals(space.getComponentSpaceQuota().getLoad())) {
    isComponentSpaceQuotaActivated = false;
  }

  // Data storage quota
  boolean isDataStorageQuotaActivated = JobStartPagePeasSettings.dataStorageInSpaceQuotaActivated;
  boolean isDataStorageQuotaFull = isDataStorageQuotaActivated && space.isDataStorageQuotaReached();
  String dataStorageQuotaCount = "";
  String dataStorageQuotaMaxCount = "";
  if (isDataStorageQuotaActivated) {
    if (QuotaLoad.UNLIMITED.equals(space.getDataStorageQuota().getLoad())) {
      isDataStorageQuotaActivated = false;
    } else {
      dataStorageQuotaCount = UnitUtil.formatMemSize(space.getDataStorageQuota().getCount());
      dataStorageQuotaMaxCount = UnitUtil.formatMemSize(space.getDataStorageQuota().getMaxCount());
    }
  }

  String 			m_SpaceName 		= space.getName(resource.getLanguage());
  String 			m_Description 		= space.getDescription(resource.getLanguage());

  List<String>	availableLooks		= gef.getAvailableLooks();
  String		spaceLook				= space.getLook();

  if (spaceLook == null) {
  	spaceLook = "&nbsp;";
  }

  String[] pageType = {resource.getString("JSPP.main"),resource.getString("JSPP.peas"),resource.getString("JSPP.portlet"),resource.getString("JSPP.webPage")};

	TabbedPane tabbedPane = gef.getTabbedPane();

 	browseBar.setSpaceId(m_SpaceId);
	browseBar.setExtraInformation(resource.getString("GML.description"));
	browseBar.setI18N(space, resource.getLanguage());

  if (m_SpaceExtraInfos.isAdmin) {
  	operationPane.addOperation(resource.getIcon("JSPP.spaceUpdate"),resource.getString("JSPP.SpacePanelModifyTitle"),"javascript:onclick=updateSpace()");
  	operationPane.addOperation(resource.getIcon("JSPP.updateHomePage"),resource.getString("JSPP.ModifyStartPage"),"javascript:onClick=openPopup('UpdateJobStartPage', 740, 600)");
    if (isUserAdmin || m_SubSpace != null) {
      operationPane.addOperation(resource.getIcon("JSPP.SpaceOrder"),resource.getString("JSPP.SpaceOrder"),"javascript:onClick=openPopup('PlaceSpaceAfter', 750, 250)");
    }

    // This space configuration
    if (maintenanceState == JobStartPagePeasSessionController.MAINTENANCE_THISSPACE) {
      operationPane.addOperation(resource.getIcon("JSPP.spaceUnlock"),resource.getString("JSPP.maintenanceModeToOff"),"DesactivateMaintenance");
    } else if (maintenanceState == JobStartPagePeasSessionController.MAINTENANCE_OFF){
      operationPane.addOperation(resource.getIcon("JSPP.spaceLock"),resource.getString("JSPP.maintenanceModeToOn"),"ActivateMaintenance");
    }
    if (isUserAdmin || m_SubSpace != null) {
      operationPane.addOperation(resource.getIcon("JSPP.spaceDel"),resource.getString("JSPP.SpacePanelDeleteTitle"),"javascript:onClick=deleteSpace()");
      if (JobStartPagePeasSettings.recoverRightsEnable) {
      	operationPane.addOperation("useless",resource.getString("JSPP.spaceRecover"),"javascript:onClick=recoverRights()");
      }
    }

    if (isBackupEnable) {
      operationPane.addOperation(resource.getIcon("JSPP.spaceBackup"),resource.getString("JSPP.BackupSpace"),"javascript:onClick=openPopup('"+m_context+URLManager.getURL(URLManager.CMP_JOBBACKUP)+"Main?spaceToSave=" + m_SpaceId + "', 750, 550)");
    }

    if (JobStartPagePeasSettings.useComponentsCopy || objectsSelectedInClipboard) {
      operationPane.addLine();
      if (JobStartPagePeasSettings.useComponentsCopy) {
      	operationPane.addOperation(resource.getIcon("JSPP.CopyComponent"),resource.getString("JSPP.space.copy"),"javascript:onclick=clipboardCopy()");
      	if (maintenanceState >= JobStartPagePeasSessionController.MAINTENANCE_PLATFORM) {
      		operationPane.addOperation(resource.getIcon("JSPP.CopyComponent"),resource.getString("JSPP.space.cut"),"javascript:onclick=clipboardCut()");
      	}
      }
  		if (objectsSelectedInClipboard) {
  			operationPane.addOperation(resource.getIcon("JSPP.PasteComponent"),resource.getString("GML.paste"),"javascript:onclick=clipboardPaste()");
  		}
    }
    operationPane.addLine();
    operationPane.addOperationOfCreation(resource.getIcon("JSPP.subspaceAdd"),resource.getString("JSPP.SubSpacePanelCreateTitle"),"CreateSpace?SousEspace=SousEspace");
    if (!isComponentSpaceQuotaFull) {
      operationPane.addOperationOfCreation(resource.getIcon("JSPP.instanceAdd"),resource.getString("JSPP.ComponentPanelCreateTitle"),"ListComponent");
    }
  }

  tabbedPane.addTab(resource.getString("GML.description"), "#", true);
  tabbedPane.addTab(resource.getString("JSPP.SpaceAppearance"), "SpaceLook", false);
  tabbedPane.addTab(resource.getString("JSPP.Manager"), "SpaceManager", false);

  if (isInHeritanceEnable) {
      tabbedPane.addTab(resource.getString("JSPP.admin"), "SpaceManager?Role=admin", false);
      tabbedPane.addTab(resource.getString("JSPP.publisher"), "SpaceManager?Role=publisher", false);
      tabbedPane.addTab(resource.getString("JSPP.writer"), "SpaceManager?Role=writer", false);
      tabbedPane.addTab(resource.getString("JSPP.reader"), "SpaceManager?Role=reader", false);
  }
%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title><%=resource.getString("GML.popupTitle")%></title>
<view:looknfeel/>
<style type="text/css">
.txtlibform {
	white-space: nowrap;
}
</style>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/animation.js"></script>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/checkForm.js"></script>
<script type="text/javascript">
//<!--
var currentLanguage = "<%=space.getLanguage()%>";
<%
	for (String lang : space.getTranslations().keySet()) {
		out.println("var name_"+lang+" = \""+EncodeHelper.javaStringToJsString(space.getName(lang))+"\";\n");
		out.println("var desc_"+lang+" = \""+EncodeHelper.javaStringToJsString(space.getDescription(lang))+"\";\n");
	}
%>

function showTranslation(lang)
{
	<%=I18NHelper.updateHTMLLinks(space)%>

	document.getElementById("spaceName").innerHTML = eval("name_"+lang);
	document.getElementById("spaceDescription").innerHTML = eval("desc_"+lang);

	currentLanguage = lang;
}

function openPopup(action, larg, haut) {
	windowName = "actionWindow";
	windowParams = "directories=0,menubar=0,toolbar=0,alwaysRaised,scrollbars,resizable";
	actionWindow = SP_openWindow(action, windowName, larg, haut, windowParams, false);
}

<% if (m_SpaceExtraInfos.isAdmin) {
    if (isUserAdmin || (m_SubSpace != null)) { %>
		function deleteSpace() {
		    if (window.confirm("<%=resource.getString("JSPP.MessageSuppressionSpaceBegin")+" "+EncodeHelper.javaStringToJsString(m_SpaceName)+" "+resource.getString("JSPP.MessageSuppressionSpaceEnd")%>")) {
		    	$.progressMessage();
		    	setTimeout("location.href = \"DeleteSpace?Id=<%=space.getId()%>\";", 500);
			}
		}
	<% } %>
		function updateSpace() {
			location.href = "UpdateSpace?Translation="+currentLanguage;
		}
<% } %>

function clipboardPaste() {
	$.progressMessage();
	location.href="paste";
}

function clipboardCopy() {
	top.IdleFrame.location.href = "copy?Type=Space&Id=<%=space.getId()%>";
}

function clipboardCut() {
	top.IdleFrame.location.href = "Cut?Type=Space&Id=<%=space.getId()%>";
}

function recoverRights() {
	$.progressMessage();
	location.href = "RecoverSpaceRights?Id=<%=space.getId()%>";
}
//-->
</script>
</head>
<body>
<%
out.println(window.printBefore());
out.println(tabbedPane.print());
%>
<view:frame>
<% if (maintenanceState >= JobStartPagePeasSessionController.MAINTENANCE_PLATFORM) { %>
	<div class="inlineMessage">
		<%=resource.getString("JSPP.maintenanceStatus."+maintenanceState)%>
	</div>
	<br clear="all"/>
<% } %>
<% if (isComponentSpaceQuotaFull) { %>
  <div class="inlineMessage-nok"><%=space.getComponentSpaceQuotaReachedErrorMessage(resource.getLanguage())%></div>
  <br clear="all"/>
<% } %>
<% if (isDataStorageQuotaFull) { %>
  <div class="inlineMessage-nok"><%=space.getDataStorageQuotaReachedErrorMessage(resource.getLanguage())%></div>
  <br clear="all"/>
<% } %>
<view:areaOfOperationOfCreation/>
<view:board>
<table cellpadding="5" cellspacing="0" border="0" width="100%">
	<tr>
		<td class="txtlibform"><%=resource.getString("GML.name") %> :</td>
		<td valign="baseline" width="100%" id="spaceName"><%=EncodeHelper.javaStringToHtmlString(m_SpaceName)%></td>
	</tr>
	<tr>
		<td class="txtlibform" valign="top"><%=resource.getString("GML.description") %> :</td>
		<td valign="top" width="100%" id="spaceDescription"><%=EncodeHelper.javaStringToHtmlParagraphe(m_Description)%></td>
	</tr>
  <% if (isComponentSpaceQuotaActivated) { %>
    <tr>
      <td class="txtlibform"><%=resource.getString("JSPP.componentSpaceQuotaMaxCount")%> :</td>
      <td valign="top" width="100%" id="componentSpaceQuota"><%=space.getComponentSpaceQuota().getMaxCount()%></td>
    </tr>
    <tr>
      <td class="txtlibform"><%=resource.getString("JSPP.componentSpaceQuotaUsed")%> :</td>
      <td valign="top" width="100%" id="componentSpaceQuotaLoad">
        <fmt:message key="JSPP.componentSpaceQuotaCurrentCount"><fmt:param value="${space.componentSpaceQuota.count}"/></fmt:message>
      </td>
    </tr>
  <% } %>
  <% if (isDataStorageQuotaActivated) { %>
    <tr>
      <td class="txtlibform"><%=resource.getString("JSPP.dataStorageUsed")%> :</td>
      <td valign="top" width="100%" id="spaceDataStorageQuotaLoad"><%=dataStorageQuotaCount + " / " + dataStorageQuotaMaxCount%> (<%=space.getDataStorageQuota().getLoadPercentage().longValue()%> %)</td>
    </tr>
  <% } %>
	<% if (space.getCreateDate() != null) { %>
	<tr>
		<td class="txtlibform"><%=resource.getString("GML.creationDate") %> :</td>
		<td valign="baseline" width="100%">
			<%=resource.getOutputDateAndHour(space.getCreateDate())%>
			<% if (space.getCreator() != null) { %>
				<%=resource.getString("GML.by") %> <view:username userId="<%=space.getCreator().getId()%>" />
			<% } %>
		</td>
	</tr>
	<% } %>
	<% if (space.getUpdateDate() != null) { %>
	<tr>
		<td class="txtlibform"><%=resource.getString("GML.updateDate") %> :</td>
		<td valign="baseline" width="100%">
			<%=resource.getOutputDateAndHour(space.getUpdateDate())%>
			<% if (space.getUpdater() != null) { %>
				<%=resource.getString("GML.by") %> <view:username userId="<%=space.getUpdater().getId()%>" />
			<% } %>
		</td>
	</tr>
	<% } %>
	<% if (!space.isRoot() && isInHeritanceEnable) { %>
	<tr>
		<td class="txtlibform" valign="top"><%=resource.getString("JSPP.inheritanceBlockedComponent") %> :</td>
		<td align="left" valign="baseline" width="100%">
		<% if (space.isInheritanceBlocked()) { %>
			<input type="radio" disabled="disabled" checked="checked" /> <%=resource.getString("JSPP.inheritanceSpaceNotUsed")%><br/>
			<input type="radio" disabled="disabled" /> <%=resource.getString("JSPP.inheritanceSpaceUsed")%>
		<% } else { %>
			<input type="radio" disabled="disabled"/> <%=resource.getString("JSPP.inheritanceSpaceNotUsed")%><br/>
			<input type="radio" disabled="disabled" checked="checked" /> <%=resource.getString("JSPP.inheritanceSpaceUsed")%>
		<% } %>
		</td>
	</tr>
	<% } %>
	<tr>
		<td class="txtlibform"><%=resource.getString("JSPP.homepageType") %> :</td>
		<td valign="baseline" width="100%"><%=pageType[m_firstPageType.intValue()] %></td>
	</tr>
	<% if (availableLooks.size() >= 2) { %>
	<tr>
		<td class="txtlibform"><%=resource.getString("JSPP.SpaceLook")%> :</td>
		<td><%=spaceLook%></td>
	</tr>
	<% } %>
</table>
</view:board>
</view:frame>
<%
out.println(window.printAfter());
%>
<view:progressMessage/>
</body>
</html>