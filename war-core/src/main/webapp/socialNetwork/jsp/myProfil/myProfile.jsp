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
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<%@ page import="com.stratelia.webactiv.beans.admin.UserDetail"%>
<%@ page import="com.stratelia.webactiv.beans.admin.UserFull"%>
<%@ page import="com.silverpeas.util.StringUtil"%>
<%@ page import="com.stratelia.webactiv.util.ResourceLocator"%>
<%@page import="com.silverpeas.directory.control.DirectorySessionController"%>
<%@page import="com.stratelia.webactiv.util.GeneralPropertiesManager" %>
<%@page import="com.stratelia.silverpeas.notificationManager.NotificationParameters"%>
<%@page import="com.stratelia.webactiv.util.viewGenerator.html.GraphicElementFactory"%>
<%@page import="com.stratelia.webactiv.util.viewGenerator.html.buttonPanes.*"%>
<%@page import="com.stratelia.webactiv.util.viewGenerator.html.buttons.*"%>
<%@page import="com.silverpeas.directory.model.Member"%>
<%@page import="com.silverpeas.socialNetwork.myProfil.servlets.MyProfileRoutes"%>
<c:set var="browseContext" value="${requestScope.browseContext}" />
<fmt:setLocale value="${sessionScope[sessionController].language}" />
<view:setBundle bundle="${requestScope.resources.multilangBundle}" />

<%  
	GraphicElementFactory gef = (GraphicElementFactory) session.getAttribute("SessionGraphicElementFactory");

    String language = request.getLocale().getLanguage();
    ResourceLocator multilang = new ResourceLocator("com.silverpeas.socialNetwork.multilang.socialNetworkBundle", language);
    ResourceLocator multilangG = new ResourceLocator("com.stratelia.webactiv.multilang.generalMultilang", language);
    UserFull userFull = (UserFull) request.getAttribute("UserFull");
    String view = (String) request.getAttribute("View");
    
    List contacts = (List) request.getAttribute("Contacts");
    int nbContacts = ((Integer) request.getAttribute("ContactsNumber")).intValue();
    boolean showAllContactLink = !contacts.isEmpty();

    String m_context = GeneralPropertiesManager.getGeneralResourceLocator().getString("ApplicationURL");
%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html>
<head>
<view:looknfeel />
<script type="text/javascript" src="/silverpeas/util/javaScript/animation.js"></script>
<script type="text/javascript" src="/silverpeas/util/javaScript/checkForm.js"></script>
<script type="text/javascript">
function editStatus() {
	$("#statusDialog").dialog("open");
}

function updateAvatar() {
	$("#avatarDialog").dialog("open");
}

$(document).ready(function(){
    var statusDialogOpts = {
    		resizable: false,
            modal: true,
            autoOpen: false,
            height: "auto",
            width: 300,
            title: "<fmt:message key="profil.actions.changeStatus" />",
            buttons: {
    			<fmt:message key="GML.cancel"/>: function() {
					$(this).dialog( "close" );
				},
				"<fmt:message key="GML.ok"/>": function() {
						var status = $("#newStatus");
						$( "#myProfileFiche .statut").html(status.val());

					    var url = "/silverpeas/RmyProfilJSON?Action=updateStatus";
						url+='&status='+status.val();
				        $.getJSON(url);
						$( this ).dialog( "close" );
				}
			}
    };

    $("#statusDialog").dialog(statusDialogOpts);    //end dialog

    var avatarDialogOpts = {
    		resizable: false,
            modal: true,
            autoOpen: false,
            height: "auto",
            width: 500,
            title: "<fmt:message key="profil.actions.changePhoto" />",
            buttons: {
    			<fmt:message key="GML.cancel"/>: function() {
					$(this).dialog( "close" );
				},
				"<fmt:message key="GML.ok"/>": function() {
					document.photoForm.submit();
				}
			}
    };

    $("#avatarDialog").dialog(avatarDialogOpts);    //end dialog
});
</script>
</head>
<body id="myProfile">
<view:window>

<div id="myProfileFiche" >
  
	<div class="info tableBoard">
 		<h2 class="userName"><%=userFull.getFirstName() %> <br /><%=userFull.getLastName() %></h2>
       	<p class="statut">
			<%=userFull.getStatus() %>
        </p>  
	    <div class="action">
        	<a href="#" class="link updateStatus" onclick="editStatus();"><fmt:message key="profil.actions.changeStatus" /></a>
            <br />
            <a href="#" class="link updateAvatar" onclick="updateAvatar()"><fmt:message key="profil.actions.changePhoto" /></a>
        </div>              
        <div class="profilPhoto">
			<img src="<%=m_context + userFull.getAvatar()%>" alt="viewUser" class="avatar"/>
        </div>  
        <br clear="all" />
 	</div>
 	
 	<div id="statusDialog">
		<form>
	    	<textarea id="newStatus" cols="49" rows="4"></textarea>
		</form>
	</div>

	<div id="avatarDialog">
		<form name="photoForm" action="UpdatePhoto" method="post" enctype="multipart/form-data" accept-charset="UTF-8">
	        <div>
	          <div class="txtlibform">Image :</div>
	          <div><input type="file" name="WAIMGVAR0" size="60"/></div>
	        </div>
	      </form>
	</div>
	
	<h3><%=nbContacts %> <fmt:message key="myProfile.contacts" /></h3>
	<!-- allContact  -->  
	<div id="allContact">
  	<% 
  		for (int i=0; i<contacts.size(); i++) {
  		  UserDetail contact = (UserDetail) contacts.get(i);
  	%>
		<!-- unContact  -->  
     	<div class="unContact">
        	<div class="profilPhotoContact">
        		<a href="<%=m_context %>/Rprofil/jsp/Main?userId=<%=contact.getId() %>"><img class="avatar" alt="viewUser" src="<%=m_context+contact.getAvatar() %>" /></a>
        	</div>
	        <a href="<%=m_context %>/Rprofil/jsp/Main?userId=<%=contact.getId() %>" class="contactName"><%=contact.getDisplayedName() %></a>
	   	</div> <!-- /unContact  -->
  	<% } %>
    
    <% if (showAllContactLink) { %>
	     <br clear="all" />
	     <a href="<%=m_context %>/Rdirectory/jsp/Main?UserId=<%=userFull.getId() %>" class="link"><fmt:message key="myProfile.contacts.all" /></a>
	     <br clear="all" />  
    <% } %>
	</div><!-- /allContact  -->  
      
</div>



<div id="publicProfileContenu">

	<fmt:message key="myProfile.tab.profile" var="profile" />
	<fmt:message key="myProfile.tab.invitations" var="invitations" />
	<fmt:message key="myProfile.tab.settings" var="settings" />
	<view:tabs>
    	<view:tab label="${profile}" action="<%=MyProfileRoutes.MyInfos.toString() %>" selected="<%=Boolean.toString(MyProfileRoutes.MyInfos.toString().equals(view)) %>" />
    	<view:tab label="${invitations}" action="<%=MyProfileRoutes.MyInvitations.toString() %>" selected="<%=Boolean.toString(MyProfileRoutes.MyInvitations.toString().equals(view) || MyProfileRoutes.MySentInvitations.toString().equals(view)) %>" />
    	<view:tab label="${settings}" action="<%=MyProfileRoutes.MySettings.toString() %>" selected="<%=Boolean.toString(MyProfileRoutes.MySettings.toString().equals(view)) %>" />
	</view:tabs>
	
	<% if (MyProfileRoutes.MyInfos.toString().equals(view)) { %>
		<%@include file="myProfileTabIdentity.jsp" %>
	<% } else if (MyProfileRoutes.MySettings.toString().equals(view)) { %>
		<%@include file="myProfileTabSettings.jsp" %>
	<% } else if (MyProfileRoutes.MyInvitations.toString().equals(view) || MyProfileRoutes.MySentInvitations.toString().equals(view)) { %>
		<%@include file="myProfileTabInvitations.jsp" %>
	<% } %>
              
</div>   
</view:window>
    
</body>
</html>