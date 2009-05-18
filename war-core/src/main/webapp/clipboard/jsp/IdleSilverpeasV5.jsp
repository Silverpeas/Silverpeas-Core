<%
response.setHeader("Cache-Control","no-store"); //HTTP 1.1
response.setHeader("Pragma","no-cache"); //HTTP 1.0
response.setDateHeader ("Expires",-1); //prevents caching at the proxy server
%>

<%@ page import="java.util.*"%>
<%@ page import="javax.ejb.*,java.sql.SQLException,javax.naming.*,javax.rmi.PortableRemoteObject"%>
<%@ page import="com.stratelia.webactiv.util.*"%>
<%@ page import="com.stratelia.webactiv.beans.admin.*"%>
<%@ page import="com.stratelia.webactiv.clipboard.model.*"%>
<%@ page import="com.stratelia.webactiv.util.indexEngine.model.*"%>
<%@ page import="com.stratelia.silverpeas.clipboardPeas.control.*"%>
<%@ page import="com.stratelia.webactiv.util.GeneralPropertiesManager"%>
<%@ page import="com.stratelia.silverpeas.peasCore.SessionManager"%>
<%@ page import="com.stratelia.silverpeas.peasCore.MainSessionController"%>

<%@ page errorPage="../../admin/jsp/errorpage.jsp"%>

<%
    String m_context = GeneralPropertiesManager.getGeneralResourceLocator().getString("ApplicationURL");
    MainSessionController m_MainSessionCtrl = (MainSessionController) session.getAttribute("SilverSessionController");
    ClipboardSessionController clipboardSC = (ClipboardSessionController) request.getAttribute("clipboardScc");
    if (clipboardSC != null) clipboardSC.doIdle(Integer.parseInt(clipboardSC.getIntervalInSec()));

    int nbConnectedUsers = 0;
    String language = m_MainSessionCtrl.getFavoriteLanguage();
    ResourceLocator message = new ResourceLocator("com.stratelia.webactiv.homePage.multilang.homePageBundle", language);
    ResourceLocator homePageSettings = new ResourceLocator("com.stratelia.webactiv.homePage.homePageSettings", "");
    String connectedUsers = message.getString("connectedUsers");
    if ("yes".equals(homePageSettings.getString("displayConnectedUsers")))
    {
        nbConnectedUsers = SessionManager.getInstance().getNbConnectedUsersList() - 1;
        if (nbConnectedUsers <= 1)
            connectedUsers = message.getString("connectedUser");
    }

%>

<html>
<HEAD>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/animation.js"></script>
<Script language="JavaScript">
var counter = 0;
<%
   if (clipboardSC != null)
   {
        out.println("var interval = " + clipboardSC.getIntervalInSec() + ";");
   }
   else
   {
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
	self.location.href = "../../Rclipboard/jsp/IdleSilverpeasV5.jsp?message=IDLE";
}

<% if ("yes".equals(homePageSettings.getString("displayConnectedUsers"))) { %>
function refreshTopBar()
{
	top.topFrame.setConnectedUsers(<%=nbConnectedUsers%>);
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
Frame cachée, Time = <%if (clipboardSC != null) out.print (String.valueOf(clipboardSC.getCounter()));%> <a href="../../Rclipboard/jsp/IdleSilverpeasV5.jsp?message=IDLE">idle...</a>
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

<!-- SessionId pour sécurisation pages Web -->
<form name="ctrl">
	<input type="hidden" name="sessionId" value="<%=session.getId()%>">
</form>

</body>
</html>