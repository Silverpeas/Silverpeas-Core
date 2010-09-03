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

<%@ include file="checkCommunicationUser.jsp" %>

<%
	ArrayLine arrayLine = null;
	Iterator   iter = null;
	Collection cResultData = (Collection) request.getAttribute("ConnectedUsersList");
	OrganizationController orgaController = new OrganizationController();
%>

<HTML>
<HEAD>
<TITLE><%=resources.getString("GML.popupTitle")%></TITLE>
<%
   out.println(gef.getLookStyleSheet());
%>
<!--[ JAVASCRIPT ]-->
<script type="text/javascript" src="<%=m_context%>/util/javaScript/animation.js"></script>

<SCRIPT LANGUAGE="JAVASCRIPT">
<!--
	// This function open a silverpeas window
	function openSPWindow(fonction,windowName){
		SP_openWindow(fonction, windowName, '500', '250','scrollbars=yes, resizable, alwaysRaised');
	}

  function ConfirmAndSend(targetURL,textToDisplay)
  {
      if (window.confirm(textToDisplay))
      {
          window.location.href = targetURL;
      }
  }

//--------------------------------------------------------------------------------------DoIdle
ID = window.setTimeout("DoIdle();", <%=settings.getString("refreshList")%>*1000);
function DoIdle()
{ self.location.href = "Main"; }


//CBO : ADD
function manageWindow(page,nom,largeur,hauteur,options)
{
	var top=(screen.height-hauteur)/2;
	var left=(screen.width-largeur)/2;
	window.open(page,nom,"top="+top+",left="+left+",width="+largeur+",height="+hauteur+","+options);
}

function enterPopup(userId)
{
	manageWindow('OpenDiscussion?userId='+userId,'popupDiscussion'+userId,'650', '400', 'menubar=no,scrollbars=no,statusbar=no');
}
//CBO : FIN ADD

//-->
</SCRIPT>

</HEAD>
<BODY marginheight=5 marginwidth=5 leftmargin=5 topmargin=5>
<%
    out.println(window.printBefore());
    out.println(frame.printBefore());
%>
<CENTER>
<%

	String icoMonitor = m_context + "/util/icons/monitor.gif";
	String icoNotify = m_context + "/util/icons/talk2user.gif";

	ArrayPane arrayPane = gef.getArrayPane("List", "", request,session);
	ArrayColumn arrayColumn1 = arrayPane.addArrayColumn("");
	arrayColumn1.setSortable(false);
	arrayPane.addArrayColumn(resources.getString("user"));
    if (settings.getBoolean("displayColumnLanguage",false))
    {
		arrayColumn1 = arrayPane.addArrayColumn(resources.getString("language"));
		arrayColumn1.setSortable(true);
    }
	arrayColumn1 = arrayPane.addArrayColumn("");
    ArrayCellText cellText;         

    if (cResultData != null)
    {
		long currentTime = new Date().getTime();        
		iter = cResultData.iterator();
		while (iter.hasNext())
		{
			SessionInfo item = (SessionInfo) iter.next();
			if (!item.m_User.getId().equals(communicationScc.getUserId()))
			{
				List userList = new ArrayList();
				userList.add(item.m_User.getId());
				Hashtable usersLanguages = orgaController.getUsersLanguage(userList);

				arrayLine = arrayPane.addArrayLine();
				arrayLine.addArrayCellText("<div align=right><img src=\""+icoMonitor+"\" border=0></div>");
				arrayLine.addArrayCellText(item.m_User.getDisplayedName());
				if (settings.getBoolean("displayColumnLanguage",false))
					arrayLine.addArrayCellText(usersLanguages.get(item.m_User.getId()).toString());

				//CBO : UPDATE
				//arrayLine.addArrayCellText("<div align=left><a href=#><img alt=\"" + resources.getString("notifyUser") + "\" src=\""+icoNotify+"\" border=0 onclick=\"javascript:openSPWindow('NotifyUser?theUserId=" + item.m_User.getId() + "','DisplayNotifySession')\"></A></div>");				
				arrayLine.addArrayCellText("<div align=left><a href=#><img alt=\"" + resources.getString("notifyUser") + "\" src=\""+icoNotify+"\" border=0 onclick=\"javascript:enterPopup('" + item.m_User.getId() + "')\"></A></div>");
			}
		}
		out.println(arrayPane.print());
    }
    out.println(resources.getString("refreshedTime") + "&nbsp;" + settings.getString("refreshList")+ "&nbsp;" + resources.getString("seconds") + "<BR>");
%>
</CENTER>
<%       
out.println(frame.printAfter());
out.println(window.printAfter());
%>
<!-- CBO : REMOVE -->
<!--<form name="userConnected" action="Main" method="post"></form>-->

</BODY>
</HTML>