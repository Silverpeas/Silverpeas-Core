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

<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>

<%
response.setHeader("Cache-Control","no-store"); //HTTP 1.1
response.setHeader("Pragma","no-cache"); //HTTP 1.0
response.setDateHeader ("Expires",-1); //prevents caching at the proxy server
%>

<%@ page import="org.silverpeas.core.security.session.SessionManagement"%>
<%@ page import="org.silverpeas.core.security.session.SessionManagementProvider"%>
<%@ page import="org.silverpeas.web.clipboard.control.ClipboardSessionController"%>

<%@ page import="org.silverpeas.core.web.mvc.controller.MainSessionController"%>
<%@page import="org.silverpeas.core.util.URLUtil"%>
<%@ page import="org.silverpeas.core.util.LocalizationBundle" %>
<%@ page import="org.silverpeas.core.util.ResourceLocator" %>
<%@ page import="org.silverpeas.core.util.SettingBundle" %>
<%@ page import="org.silverpeas.core.util.StringUtil" %>
<%@ page import="java.util.Enumeration" %>
<%@ page import="org.silverpeas.core.notification.user.server.channel.popup.SilverMessageFactory" %>

<%@ page errorPage="../../admin/jsp/errorpage.jsp"%>

<%
    String m_context = URLUtil.getApplicationURL();
    MainSessionController m_MainSessionCtrl = (MainSessionController) session.getAttribute(MainSessionController.MAIN_SESSION_CONTROLLER_ATT);
    ClipboardSessionController clipboardSC = (ClipboardSessionController) request.getAttribute("clipboardScc");
    String javascripTask = "";
    if (clipboardSC != null) {
      clipboardSC.doIdle(Integer.parseInt(clipboardSC.getIntervalInSec()));
      javascripTask = clipboardSC.getHF_JavaScriptTask(request);
    }

    int nbConnectedUsers = 0;
    String language = m_MainSessionCtrl.getFavoriteLanguage();
    LocalizationBundle message = ResourceLocator.getLocalizationBundle("org.silverpeas.homePage.multilang.homePageBundle", language);
    SettingBundle homePageSettings = ResourceLocator.getSettingBundle("org.silverpeas.homePage.homePageSettings");
    String connectedUsers = message.getString("connectedUsers");
    if ("yes".equals(homePageSettings.getString("displayConnectedUsers"))) {
        SessionManagement sessionManagement = SessionManagementProvider.getSessionManagement();
        nbConnectedUsers = sessionManagement.getNbConnectedUsersList(m_MainSessionCtrl.getCurrentUserDetail()) - 1;
        if (nbConnectedUsers <= 1) {
          connectedUsers = message.getString("connectedUser");
        }
      }

%>

<html>
<HEAD>
  <%if(StringUtil.isDefined(javascripTask)){%>
  <view:includePlugin name="jquery"/>
  <view:includePlugin name="tkn"/>
  <%}%>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/silverpeas.js"></script>
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
		out.println(javascripTask);
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
			SilverMessageFactory.del(messageId);
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