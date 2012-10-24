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
response.setHeader("Pragma","no-cache");        //HTTP 1.0
response.setDateHeader ("Expires",-1);          //prevents caching at the proxy server
%>

<%@ page import="com.stratelia.silverpeas.notificationUser.control.NotificationUserSessionController"%>
<%@ page import="com.stratelia.silverpeas.notificationManager.NotificationParameters"%>
<%@ page import="com.stratelia.silverpeas.peasCore.URLManager"%>

<%@ page import="java.util.*"%>
<%@ page import="com.stratelia.webactiv.util.*"%>

<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.GraphicElementFactory"%>
<%@ page import=" com.silverpeas.util.EncodeHelper"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.buttons.*"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.buttonPanes.*"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.frame.*"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.board.*"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.operationPanes.OperationPane"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.browseBars.BrowseBar"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.window.Window"%>

<%@ page errorPage="../../admin/jsp/errorpage.jsp"%>
 
<%
	NotificationUserSessionController notificationScc = (NotificationUserSessionController) request.getAttribute("notificationUser");

	// Ze graffik factory
	GraphicElementFactory gef = (GraphicElementFactory) session.getAttribute("SessionGraphicElementFactory");

	String m_context        = GeneralPropertiesManager.getGeneralResourceLocator().getString("ApplicationURL");
	String mandatoryField    = m_context + "/util/icons/mandatoryField.gif";

   String action = request.getParameter("Action");
	
   String notificationId = EncodeHelper.htmlStringToJavaString((String) request.getAttribute("notificationId"));
   String priorityId = (String) request.getAttribute("priorityId");
   String txtTitle = EncodeHelper.htmlStringToJavaString((String) request.getAttribute("txtTitle"));
   String txtMessage = EncodeHelper.htmlStringToJavaString((String) request.getAttribute("txtMessage"));
   String popupMode = request.getParameter("popupMode");
   String editTargets = request.getParameter("editTargets");

	String[] selectedIdUsers = (String[])request.getAttribute("SelectedIdUsers");
	String[] selectedIdGroups = (String[])request.getAttribute("SelectedIdGroups");

   if (action == null) {
       action = "NotificationView";
   }
   if (notificationId == null) {
       notificationId = "";
   }
   if (priorityId == null) {
       priorityId = "";
   }
   if (txtTitle == null) {
       txtTitle = "";
   }
   if (txtMessage == null) {
       txtMessage = "";
   }
   if (popupMode == null) {
       popupMode = "No";
   }
   if (editTargets == null) { 
       editTargets = "Yes";
   }

   if ((action.equals("sendNotif") || action.equals("emptyAll")) && popupMode.equals("Yes"))
   { 
%>
    <html>
      <body onload="javascript:window.close()"> 
      </body>
    </html> 
<% } else { %>

<html>
<head>
<%
   out.println(gef.getLookStyleSheet());
%>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/animation.js"></script>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/checkForm.js"></script>
<script type="text/javascript">
function Submit(){
	SP_openUserPanel('about:blank', 'OpenUserPanel', 'menubar=no,scrollbars=no,statusbar=no');
	document.notificationSenderForm.action = "<%=m_context+URLManager.getURL(URLManager.CMP_NOTIFICATIONUSER)%>SetTarget";
	document.notificationSenderForm.target = "OpenUserPanel";
	document.notificationSenderForm.submit();
}
function SubmitWithAction(action,verifParams)  
{
    var title = stripInitialWhitespace(document.notificationSenderForm.txtTitle.value);
    var errorMsg = "";

    if (verifParams) {
         if (isWhitespace(title)) { 
            errorMsg = "<% out.print(notificationScc.getString("missingFieldStart")+notificationScc.getString("name")+notificationScc.getString("missingFieldEnd")); %>";
         }
    }
    if (errorMsg == "") {
		document.notificationSenderForm.action = "<%=m_context+URLManager.getURL(URLManager.CMP_NOTIFICATIONUSER)%>"+ action;
		document.notificationSenderForm.submit();
    } else {
        window.alert(errorMsg);
    }
}
</script>
</head>
<body onload="document.notificationSenderForm.txtTitle.focus();">
<%
    Window window = gef.getWindow();

    BrowseBar browseBar = window.getBrowseBar(); 
    browseBar.setComponentName(notificationScc.getString("MesNotifications"));
	browseBar.setPath(notificationScc.getString("domainName"));

    if (editTargets.equals("Yes")) {
        OperationPane operationPane = window.getOperationPane();
		operationPane.addOperation(m_context + "/util/icons/notification_assign.gif", notificationScc.getString("Opane_addressees"),"javascript:Submit()");
    }

    out.println(window.printBefore());
    
    Frame frame = gef.getFrame();
    Board board = gef.getBoard();
    out.println(frame.printBefore());
%>

<center>
<form name="notificationSenderForm" action="" method="POST" accept-charset="UTF-8">
<% out.println(board.printBefore()); %>
      <table cellpadding="5" cellspacing="0" border="0" width="100%">
        <tr>
          <td class="txtlibform">
            <%=notificationScc.getString("name")%> :&nbsp;
          </td>
          <td valign="baseline">
           <input type="text" name="txtTitle" size="50" maxlength="<%=NotificationParameters.MAX_SIZE_TITLE%>" value="<%=EncodeHelper.javaStringToHtmlString(txtTitle)%>"/>
			 <img src="<%=mandatoryField%>" width="5" height="5">
          </td>
        </tr>
        <tr>
          <td class="txtlibform" valign="top"><%=notificationScc.getString("description")%> :</td>
          <td valign="top" class="txtnav">
			<textarea name="txtMessage" cols="49" rows="4"><%=EncodeHelper.javaStringToHtmlString(txtMessage)%></textarea>
          </td>
        </tr>
	    <tr>			
          <td class="txtlibform"><%=notificationScc.getString("method")%> :</td>
          <td>
            <select name="notificationId">
               <% out.println(notificationScc.buildOptions(notificationScc.getDefaultAddresses(), notificationId, notificationScc.getString("DefaultMedia"))); %>
            </select>
	      </td>
        </tr>
        <tr>			
          <td valign="top" class="txtlibform">
            <%=notificationScc.getString("users_dest")%> :&nbsp;
          </td>
          <td valign="top" class="txtnote">
               <select name="displaySelectedUsers" multiple="multiple" size="3">
                 <% out.println(notificationScc.buildOptions(notificationScc.getSelectedUsers(selectedIdUsers), "", null)); %>
               </select>
          </td>
        </tr>
        <% if (editTargets.equals("Yes") || (selectedIdGroups != null && selectedIdGroups.length > 0)) { %>
	        <tr>
	          <td valign="top" class="txtlibform">
	            <%=notificationScc.getString("groups_dest")%> :&nbsp;
	          </td>
	          <td valign="top" class="txtnote">
	               <select name="displaySelectedGroups" multiple="multiple" size="3">
	                 <% out.println(notificationScc.buildOptions(notificationScc.getSelectedGroups(selectedIdGroups), "", null)); %>
	               </select>
	          </td>
	        </tr>
        <% } %>
	<tr> 
          <td colspan="2">
	    (<img src="<%=mandatoryField%>" width="5" height="5"> : <%=notificationScc.getString("requiredFields")%>)
          </td>
        </tr>
				<% for (int i=0; i<selectedIdUsers.length; i++ ) { %>
					<input type="hidden" name="selectedUsers" value="<%=selectedIdUsers[i]%>"/>
				<% } %> 
   				<% for (int i=0; i<selectedIdGroups.length; i++) {  %>
					<input type="hidden" name="selectedGroups" value="<%=selectedIdGroups[i]%>"/>
				<% } %>
				<input type="hidden" name="popupMode" value="<%=popupMode%>"/>
				<input type="hidden" name="editTargets" value="<%=editTargets%>"/> 
      </table>
<% out.println(board.printAfter()); %>
</form>
<br />
<%
    ButtonPane buttonPane = gef.getButtonPane();
    buttonPane.addButton(gef.getFormButton(notificationScc.getString("Envoyer"), "javascript:SubmitWithAction('sendNotif',true)", false));
	buttonPane.addButton(gef.getFormButton(notificationScc.getString("Cancel"), "javascript:SubmitWithAction('emptyAll',false)", false));
		
    out.println(buttonPane.print());
%>
</center>
<%
out.println(frame.printAfter());
out.println(window.printAfter());
%>
</body>
</html>
<% } %>