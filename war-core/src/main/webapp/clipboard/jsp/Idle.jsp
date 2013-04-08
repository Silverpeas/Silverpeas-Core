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

<%
response.setHeader("Cache-Control","no-store"); //HTTP 1.1
response.setHeader("Pragma","no-cache"); //HTTP 1.0
response.setDateHeader ("Expires",-1); //prevents caching at the proxy server
%>

<%@ page import="java.util.*"%>
<%@ page import="com.stratelia.webactiv.util.*"%>
<%@ page import="com.stratelia.silverpeas.clipboardPeas.control.*"%>
<%@ page import="com.stratelia.webactiv.util.GeneralPropertiesManager"%>
<%@ page import="com.stratelia.silverpeas.peasCore.MainSessionController"%>
<%@page import="com.stratelia.silverpeas.peasCore.URLManager"%>
<%@ page import="com.silverpeas.session.SessionManagement" %>
<%@ page import="com.silverpeas.session.SessionManagementFactory" %>

<%@ page errorPage="../../admin/jsp/errorpage.jsp"%>

<%
    String m_context = URLManager.getApplicationURL();
    MainSessionController m_MainSessionCtrl = (MainSessionController) session.getAttribute(MainSessionController.MAIN_SESSION_CONTROLLER_ATT);
    ClipboardSessionController clipboardSC = (ClipboardSessionController) request.getAttribute("clipboardScc");
    if (clipboardSC != null) {
      clipboardSC.doIdle(Integer.parseInt(clipboardSC.getIntervalInSec()));
    }

    int nbConnectedUsers = 0;
    String language = m_MainSessionCtrl.getFavoriteLanguage();
    ResourceLocator message = new ResourceLocator("org.silverpeas.homePage.multilang.homePageBundle", language);
    ResourceLocator homePageSettings = new ResourceLocator("org.silverpeas.homePage.homePageSettings", "");
    String connectedUsers = message.getString("connectedUsers");
    if ("yes".equals(homePageSettings.getString("displayConnectedUsers"))) {
        SessionManagement sessionManagement = SessionManagementFactory.getFactory().getSessionManagement();
        nbConnectedUsers = sessionManagement.getNbConnectedUsersList(m_MainSessionCtrl.getCurrentUserDetail()) - 1;
        if (nbConnectedUsers <= 1) {
          connectedUsers = message.getString("connectedUser");
        }
      }

%>

<html>
<HEAD>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/animation.js"></script>
<Script language="JavaScript">
var counter = 0;
<%
   if (clipboardSC != null) {
        out.println("var interval = " + clipboardSC.getIntervalInSec() + ";");
   }
      else {
        out.println("var interval = 5;");
   }
%>

// call Update function in 1 second after first load
ID = window.setTimeout ("DoIdle(" + interval + ");", interval * 1000);

<% if ("yes".equals(homePageSettings.getString("displayConnectedUsers"))) { %>
    // call "TopBar refresh" in x second after first load
    ID = window.setTimeout ("refreshTopBar(" + interval + ");", interval * 500);
<% } %>
//--------------------------------------------------------------------------------------DoIdle
// Idle function
function DoIdle()
{
	counter ++;
	self.location.href = "../../Rclipboard/jsp/Idle.jsp?message=IDLE";
}

<% if ("yes".equals(homePageSettings.getString("displayConnectedUsers"))) { %>
function refreshTopBar()
{
		var obj = top.topFrame.document.getElementById("lineConnectedUsers");
    if (<%=nbConnectedUsers%> > 0)
    {
    				if (obj != null)
    				{
							obj.style.visibility = "visible";
						}
    }
    else
    {
    				if (obj != null)
    								obj.style.visibility = "hidden";
    }
   var objConnectedUsers = top.topFrame.document.getElementById("nbConnectedUsers");
   if (objConnectedUsers != null)
   {
   	objConnectedUsers.value = "<%=nbConnectedUsers%> <%=connectedUsers%>";
   }
}
<% } %>

//--------------------------------------------------------------------------------------DoTask
// Do taks javascript function
function DoTask() {
	<%
	if (clipboardSC != null) {
		String MessageError = clipboardSC.getMessageError();
		if (MessageError != null)
				out.println ("alert ('" + MessageError + "')");
		out.println (clipboardSC.getHF_JavaScriptTask(request));
	}
	%>
}

//--------------------------------------------------------------------------------------OpenDiscussion
function OpenDiscussion(page,nom,largeur,hauteur,options) {
	if (!top.scriptFrame.impopup || (top.scriptFrame.impopup.closed)) {
		top.scriptFrame.impopup = SP_openWindow(page,nom,largeur, hauteur,options);
	} else {
		 top.scriptFrame.impopup.focus(); 
	}

	 <%
		String messageId = (String) request.getAttribute("MessageID");

		if(messageId != null) {
			com.stratelia.silverpeas.notificationserver.channel.popup.SilverMessageFactory.del(messageId);
		}
	 %>
}

//--------------------------------------------------------------------------------------test
// Developer test
function test () {
  //window.alert ('clipboardName='+top.ClipboardWindow.name);
  status = top.ClipboardWindow.document.pasteform.compR.value;
}

</script>
</HEAD>

<body onLoad="DoTask();"><PRE>
Frame cachee, Time = <%if (clipboardSC != null) out.print (String.valueOf(clipboardSC.getCounter()));%> <a href="../../Rclipboard/jsp/Idle.jsp?message=IDLE">idle...</a>
<%
		Enumeration values = request.getParameterNames();
		String sep = "";
		while(values.hasMoreElements()) {
			String name = (String)values.nextElement();
			if (name != null) {
		      String value = request.getParameter(name);
            if(name.compareTo("submit") != 0) {
				   if (value != null)
					   out.print(sep + name + "=" + value);
				   else
					   out.print(sep + name + "=null");
				   sep = "&";
            }
			}
      }
	%>
	<a href="javascript:onClick=test()">test...</a>
	</PRE>
<%if (clipboardSC != null) out.println (clipboardSC.getHF_HTMLForm(request));%>

<!-- SessionId pour securisation pages Web -->
<form name="ctrl">
	<input type="hidden" name="sessionId" value="<%=session.getId()%>">
</form>

</body>
</html>