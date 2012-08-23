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
    "http://www.silverpeas.org/legal/licensing"

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

--%>

<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%@ include file="checkSilverStatistics.jsp" %>

<%
// Recuperation des parametres
ArrayLine arrayLine = null;
Iterator   iter = null;
Collection cResultData = (Collection)request.getAttribute("ConnectedUsersList");
String userProfile = (String)request.getAttribute("UserProfile");

%>

<%
	TabbedPane tabbedPane = gef.getTabbedPane();
	tabbedPane.addTab(resources.getString("silverStatisticsPeas.usersWithSession"), m_context+"/RsilverStatisticsPeas/jsp/Main",true);
	tabbedPane.addTab(resources.getString("silverStatisticsPeas.connectionNumber"), m_context+"/RsilverStatisticsPeas/jsp/ViewConnections",false);
	tabbedPane.addTab(resources.getString("silverStatisticsPeas.connectionFrequence"), m_context+"/RsilverStatisticsPeas/jsp/ViewFrequence",false);
%>


<html>
<head>
<title><%=resources.getString("GML.popupTitle")%></title>
<%
   out.println(gef.getLookStyleSheet());
%>
<!--[ JAVASCRIPT ]-->
<script type="text/javascript" src="<%=m_context%>/util/javaScript/animation.js"></script>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/checkForm.js"></script>

<script language="javascript">
<!--
	// This function open a silverpeas window
	function openSPWindow(fonction,windowName){
		SP_openWindow(fonction, windowName, '750', '250','scrollbars=yes, resizable, alwaysRaised');
	}

    function ConfirmAndSend(targetURL,textToDisplay)
    {
        if (window.confirm(textToDisplay))
        {
            window.location.href = targetURL;
        }
    }

//--------------------------------------------------------------------------------------DoIdle
ID = window.setTimeout("DoIdle();", 60000);
function DoIdle()
{ self.location.href = "Main"; }

//-->
</script>

</head>
<body>

<%
	browseBar.setDomainName(resources.getString("silverStatisticsPeas.statistics"));
    browseBar.setComponentName(resources.getString("silverStatisticsPeas.Connections"));
    browseBar.setPath(resources.getString("silverStatisticsPeas.usersWithSession"));

	operationPane.addOperation(resources.getIcon("silverStatisticsPeas.icoNotifyAll"),resources.getString("silverStatisticsPeas.notifyAllUser"),"javascript:openSPWindow('DisplayNotifyAllSessions','DisplayNotifyAllSessions')");

    out.println(window.printBefore());
    if (userProfile.equals("A"))
    {
		out.println(tabbedPane.print());
    }
    out.println(frame.printBefore());
%>
<center>
<%
		  // Tableau
          ArrayPane arrayPane = gef.getArrayPane("List", "", request,session);

		  if (cResultData != null)
			  arrayPane.setTitle(cResultData.size()+" "+resources.getString("silverStatisticsPeas.usersWithSession"));

		  ArrayColumn arrayColumn1 = arrayPane.addArrayColumn("");
		  arrayColumn1.setSortable(false);

          arrayPane.addArrayColumn(resources.getString("silverStatisticsPeas.ip"));
          arrayPane.addArrayColumn(resources.getString("GML.login"));
          arrayPane.addArrayColumn(resources.getString("silverStatisticsPeas.duration"));

          arrayColumn1 = arrayPane.addArrayColumn(resources.getString("silverStatisticsPeas.Actions")+"</A>");
          arrayColumn1.setSortable(false);

        ArrayCellText cellText;

        if (cResultData != null)
        {
            long currentTime = new Date().getTime();
        	iter = cResultData.iterator();
        	while (iter.hasNext())
        	{
            	SessionInfo item = (SessionInfo) iter.next();

          		arrayLine = arrayPane.addArrayLine();

                arrayLine.addArrayCellText("<div align=\"right\"><img src=\""+resources.getIcon("silverStatisticsPeas.icoMonitor")+"\" alt=\"\"/></div>");

          		arrayLine.addArrayCellText(item.getUserHostIP());

				arrayLine.addArrayCellText(item.getLog());

				long duration = currentTime - item.getOpeningTimestamp();
				cellText = arrayLine.addArrayCellText(item.formatDuration(duration));
				cellText.setCompareOn(new Long(duration));

                arrayLine.addArrayCellText("<div align=\"left\"><a href=\"#\"><img src=\""+resources.getIcon("silverStatisticsPeas.icoNotifySession")+"\" onclick=\"javascript:openSPWindow('DisplayNotifySession?theUserId=" + item.getUserDetail().getId() + "','DisplayNotifySession')\"></a>&nbsp;<a href=\"javascript:ConfirmAndSend('KickSession?theSessionId=" + URLEncoder.encode(item.getSessionId()) + "','" + Encode.javaStringToJsString(resources.getString("silverStatisticsPeas.ConfirmKickSession") + item.getUserDetail().getLogin() + " (" + item.getUserDetail().getDisplayedName()) + ") ?')\"><img src=\""+resources.getIcon("silverStatisticsPeas.icoKillSession")+"\"/></a></div>");
            }

        	out.println(arrayPane.print());
        }
        out.println(resources.getString("silverStatisticsPeas.RefreshedEveryMinutes") + "<BR>");
%>
</center>
<%
out.println(frame.printAfter());
out.println(window.printAfter());
%>
<form name="goBack" action="Main" method="post"></form>
</body>
</html>