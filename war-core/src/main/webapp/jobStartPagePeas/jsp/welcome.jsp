<%--

    Copyright (C) 2000 - 2009 Silverpeas

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    As a special exception to the terms and conditions of version 3.0 of
    the GPL, you may redistribute this Program in connection with Free/Libre
    Open Source Software ("FLOSS") applications as described in Silverpeas's
    FLOSS exception.  You should have recieved a copy of the text describing
    the FLOSS exception, and it is also available here:
    "http://repository.silverpeas.com/legal/licensing"

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

--%>
<%@ include file="check.jsp" %>

<%

boolean isUserAdmin 	= ((Boolean)request.getAttribute("isUserAdmin")).booleanValue();
boolean globalMode 		= ((Boolean)request.getAttribute("globalMode")).booleanValue();
boolean isBackupEnable 	= ((Boolean)request.getAttribute("IsBackupEnable")).booleanValue();
boolean isBasketEnable 	= ((Boolean)request.getAttribute("IsBasketEnable")).booleanValue();

	// Space edition
    if (isUserAdmin)
    {   	
        operationPane.addOperation(resource.getIcon("JSPP.spaceAdd"),resource.getString("JSPP.SpacePanelCreateTitle"),"javascript:onClick=openPopup('CreateSpace', 750, 300)");
        // All Silverpeas
        if (globalMode)
        {
            operationPane.addOperation(resource.getIcon("JSPP.spaceUnlock"),resource.getString("JSPP.maintenanceModeToOff"),"DesactivateMaintenance?allIntranet=1");
        }
        else
        {
            operationPane.addOperation(resource.getIcon("JSPP.spaceLock"),resource.getString("JSPP.maintenanceModeToOn"),"ActivateMaintenance?allIntranet=1");
        }
        if (isBackupEnable)
    	{
    		operationPane.addLine();
	    	operationPane.addOperation(resource.getIcon("JSPP.silverpeasBackup"),resource.getString("JSPP.BackupUnlimited"),"javascript:onClick=openPopup('"+m_context+URLManager.getURL(URLManager.CMP_JOBBACKUP)+"Main?spaceToSave=Admin', 750, 550)");
	    }
        if (isBasketEnable)
        {
        	operationPane.addLine();
        	operationPane.addOperation(resource.getIcon("JSPP.bin"), resource.getString("JSPP.Bin"), "ViewBin");
        }
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
function openPopup(action, larg, haut) 
{
	url = action;
	windowName = "actionWindow";
	windowParams = "directories=0,menubar=0,toolbar=0,alwaysRaised,scrollbars,resizable";
	actionWindow = SP_openWindow(url, windowName, larg, haut, windowParams, false);
}
-->
</script>
</HEAD>

<BODY marginheight=5 marginwidth=5 leftmargin=5 topmargin=5>
<%
out.println(window.printBefore());
out.println(frame.printBefore());
%>
<center>
<%
out.println(board.printBefore());
%>
<div align="center" class="txtNav">
<%
    if (globalMode)
        out.print("<FONT color=#ff0000>" + resource.getString("JSPP.maintenanceTout") + "</FONT>");
    else
        out.print(resource.getString("JSPP.welcome"));
%></div>
<%
out.println(board.printAfter());
%>
</center>
<% 
out.println(frame.printAfter());
out.println(window.printAfter());
%>
</BODY>
</HTML>
