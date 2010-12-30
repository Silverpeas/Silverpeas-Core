<%--

    Copyright (C) 2000 - 2009 Silverpeas

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    As a special exception to the terms and conditions of version 3.0 of
    the GPL, you may redistribute this Program in connection with Free/Libre
    Open Source Software ("FLOSS") applications as described in Silverpeas's
    FLOSS exception.  You should have received a copy of the text describing
    the FLOSS exception, and it is also available here:
    "http://repository.silverpeas.com/legal/licensing"

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

--%>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%@ include file="check.jsp" %>
<%@page import="java.net.URLEncoder"%>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>

<%	
	boolean 		mode 				= new Boolean((String) request.getAttribute("mode")).booleanValue();
	String	 		m_SpaceId 			= (String) request.getAttribute("currentSpaceId");
	Integer 		m_firstPageType 	= (Integer)request.getAttribute("FirstPageType");
	
	String 			m_SubSpace 			= (String) request.getAttribute("nameSubSpace");
	boolean			objectsSelectedInClipboard = new Boolean((String) request.getAttribute("ObjectsSelectedInClipboard")).booleanValue();
	DisplaySorted 	m_SpaceExtraInfos 	= (DisplaySorted)request.getAttribute("SpaceExtraInfos");
    boolean 		isUserAdmin 		= ((Boolean)request.getAttribute("isUserAdmin")).booleanValue();
    boolean 		isBackupEnable 		= ((Boolean)request.getAttribute("IsBackupEnable")).booleanValue();
    boolean 		isInHeritanceEnable = ((Boolean)request.getAttribute("IsInheritanceEnable")).booleanValue();
    
    SpaceInst 		space 				= (SpaceInst) request.getAttribute("Space");
       
    String 			m_SpaceName 		= space.getName(resource.getLanguage());
    String 			m_Description 		= space.getDescription(resource.getLanguage());
    
    List 		availableLooks			= gef.getAvailableLooks();
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
      	operationPane.addOperation(resource.getIcon("JSPP.spaceUpdate"),resource.getString("JSPP.SpacePanelModifyTitle"),"javascript:onClick=updateSpace(750, 350)");
      	operationPane.addOperation(resource.getIcon("JSPP.updateHomePage"),resource.getString("JSPP.ModifyStartPage"),"javascript:onClick=openPopup('UpdateJobStartPage', 740, 600)");
        if (isUserAdmin || m_SubSpace != null) {
            operationPane.addOperation(resource.getIcon("JSPP.SpaceOrder"),resource.getString("JSPP.SpaceOrder"),"javascript:onClick=openPopup('PlaceSpaceAfter', 750, 250)");
        }
        
        // This space configuration
        if (mode) {
            operationPane.addOperation(resource.getIcon("JSPP.spaceUnlock"),resource.getString("JSPP.maintenanceModeToOff"),"DesactivateMaintenance");
        } else {
            operationPane.addOperation(resource.getIcon("JSPP.spaceLock"),resource.getString("JSPP.maintenanceModeToOn"),"ActivateMaintenance");
        }
        if (isUserAdmin || m_SubSpace != null) {
            operationPane.addOperation(resource.getIcon("JSPP.spaceDel"),resource.getString("JSPP.SpacePanelDeleteTitle"),"javascript:onClick=deleteSpace()");
        }
        
        if (isBackupEnable) {
    		operationPane.addOperation(resource.getIcon("JSPP.spaceBackup"),resource.getString("JSPP.BackupSpace"),"javascript:onClick=openPopup('"+m_context+URLManager.getURL(URLManager.CMP_JOBBACKUP)+"Main?spaceToSave=" + m_SpaceId + "', 750, 550)");
        }
    	
        if (JobStartPagePeasSettings.useComponentsCopy || objectsSelectedInClipboard) { 
	        operationPane.addLine();
	        if (JobStartPagePeasSettings.useComponentsCopy) {
	        	operationPane.addOperation(resource.getIcon("JSPP.CopyComponent"),resource.getString("GML.copy"),"javascript:onClick=clipboardCopy()");
	        }
			if (objectsSelectedInClipboard) {
				operationPane.addOperation(resource.getIcon("JSPP.PasteComponent"),resource.getString("GML.paste"),"javascript:onClick=clipboardPaste()");
			}
        }
		operationPane.addLine();
        operationPane.addOperation(resource.getIcon("JSPP.subspaceAdd"),resource.getString("JSPP.SubSpacePanelCreateTitle"),"javascript:onClick=openPopup('CreateSpace?SousEspace=SousEspace', 750, 300)");
        operationPane.addOperation(resource.getIcon("JSPP.instanceAdd"),resource.getString("JSPP.ComponentPanelCreateTitle"),"javascript:onClick=openPopup('ListComponent', 750, 700)");
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

<HTML>
<HEAD>
<TITLE><%=resource.getString("GML.popupTitle")%></TITLE>
<%
out.println(gef.getLookStyleSheet());
%>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/animation.js"></script>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/checkForm.js"></script>
<script language="JavaScript">
<!--
var currentLanguage = "<%=space.getLanguage()%>";
<%
	String lang = "";
	Iterator codes = space.getTranslations().keySet().iterator();
	while (codes.hasNext())
	{
		lang = (String) codes.next();
		out.println("var name_"+lang+" = \""+Encode.javaStringToJsString(space.getName(lang))+"\";\n");
		out.println("var desc_"+lang+" = \""+Encode.javaStringToJsString(space.getDescription(lang))+"\";\n");
	}
%>

function showTranslation(lang)
{
	<%=I18NHelper.updateHTMLLinks(space)%>
	
	document.getElementById("spaceName").innerHTML = eval("name_"+lang);
	document.getElementById("spaceDescription").innerHTML = eval("desc_"+lang);
	
	currentLanguage = lang;
}

function openPopup(action, larg, haut) 
{
	windowName = "actionWindow";
	windowParams = "directories=0,menubar=0,toolbar=0,alwaysRaised,scrollbars,resizable";
	actionWindow = SP_openWindow(action, windowName, larg, haut, windowParams, false);
}

<% if (m_SpaceExtraInfos.isAdmin) {
    if (isUserAdmin || (m_SubSpace != null)) { %>
		function deleteSpace() {
		    if (window.confirm("<%=resource.getString("JSPP.MessageSuppressionSpaceBegin")+" "+Encode.javaStringToJsString(m_SpaceName)+" "+resource.getString("JSPP.MessageSuppressionSpaceEnd")%>")) {
		    	$.progressMessage();
		    	setTimeout("location.href = \"DeleteSpace?Id=<%=space.getId()%>\";", 500);
			}
		}
	<% } %>
		function updateSpace(larg, haut) 
		{
			windowName = "actionWindow";
			windowParams = "directories=0,menubar=0,toolbar=0,alwaysRaised,scrollbars,resizable";
			actionWindow = SP_openWindow("UpdateSpace?Translation="+currentLanguage, windowName, larg, haut, windowParams, false);
		}
<% } %>

function clipboardPaste() {
	$.progressMessage();
	location.href="paste";
}

function clipboardCopy() {
	top.IdleFrame.location.href = 'copy?Type=Space&Id=<%=space.getId()%>';
}
-->
</script>
</HEAD>

<BODY>
<%
out.println(window.printBefore());
out.println(tabbedPane.print());
out.println(frame.printBefore());
%>
<center>
<%
out.println(board.printBefore());
%>
<table CELLPADDING="5" CELLSPACING="0" BORDER="0" WIDTH="100%">
	<tr>
		<td class="textePetitBold" nowrap><%=resource.getString("GML.name") %> :</td>
		<td valign="baseline" width="100%" id="spaceName"><%=Encode.javaStringToHtmlString(m_SpaceName)%></td>
	</tr>
	<tr>
		<td class="textePetitBold" nowrap valign="top"><%=resource.getString("GML.description") %> :</td>
		<td valign="top" width="100%" id="spaceDescription"><%=Encode.javaStringToHtmlParagraphe(m_Description)%></td>
	</tr>
	<% if (space.getCreateDate() != null) { %>
	<tr>
		<td class="textePetitBold" nowrap><%=resource.getString("GML.creationDate") %> :</td>
		<td valign="baseline" width="100%">
			<%=resource.getOutputDateAndHour(space.getCreateDate())%>
			<% if (space.getCreator() != null) { %>  
				<%=resource.getString("GML.by") %> <%=space.getCreator().getDisplayedName() %>
			<% } %>
		</td>
	</tr>
	<% } %>
	<% if (space.getUpdateDate() != null) { %>
	<tr>
		<td class="textePetitBold" nowrap><%=resource.getString("GML.updateDate") %> :</td>
		<td valign="baseline" width="100%">
			<%=resource.getOutputDateAndHour(space.getUpdateDate())%>
			<% if (space.getUpdater() != null) { %>  
				<%=resource.getString("GML.by") %> <%=space.getUpdater().getDisplayedName() %>
			<% } %>
		</td>
	</tr>
	<% } %>
	<% if (!space.isRoot() && isInHeritanceEnable) { %>
	<tr>
		<td class="textePetitBold" nowrap valign="top"><%=resource.getString("JSPP.inheritanceBlockedComponent") %> :</td>
		<td align="left" valign="baseline" width="100%">
		<% if (space.isInheritanceBlocked()) { %>
			<input type="radio" disabled checked /> <%=resource.getString("JSPP.inheritanceSpaceNotUsed")%><br/>
			<input type="radio" disabled /> <%=resource.getString("JSPP.inheritanceSpaceUsed")%>
		<% } else { %>
			<input type="radio" disabled/> <%=resource.getString("JSPP.inheritanceSpaceNotUsed")%><br/>
			<input type="radio" disabled checked /> <%=resource.getString("JSPP.inheritanceSpaceUsed")%>
		<% } %>
		</td>
	</tr>
	<% } %>
	<tr>
		<td class="textePetitBold" nowrap><%=resource.getString("JSPP.homepageType") %> :</td>
		<td valign="baseline" width="100%"><%=pageType[m_firstPageType.intValue()] %></td>
	</tr>
	<% if (availableLooks.size() >= 2) { %>
	<tr> 
		<td class="txtlibform"><%=resource.getString("JSPP.SpaceLook")%> :</td>
		<td><%=spaceLook%></td>
	</tr>
	<% } %>
	<tr>
		<td class="textePetitBold" nowrap><%=resource.getString("JSPP.maintenance") %></td>
		<td valign="baseline" width="100%">
			<input type="radio" <% if (mode) out.print("checked");%> name="mode" value="true" disabled>&nbsp;<%=resource.getString("JSPP.maintenanceModeOn")%>
			<input type="radio" <% if (!mode) out.print("checked");%> name="mode" value="false" disabled>&nbsp;<%=resource.getString("JSPP.maintenanceModeOff")%>
		</td>
	</tr>
	<tr>
		<td></td>
		<td valign="baseline" width="100%">
		<%
			if (mode)
			{
				out.println("<b>"+resource.getString("JSPP.maintenanceStatus"));
				out.println(resource.getString("JSPP.maintenanceTxtModeOn")+"</b>");
			}
		%>
		</td>
	</tr>
</table>
<%
out.println(board.printAfter());
%>
</center>
<%
out.println(frame.printAfter());
out.println(window.printAfter());
%>
<view:progressMessage/>
</BODY>
</HTML>