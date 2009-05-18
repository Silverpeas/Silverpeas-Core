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
