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
<%@page import="com.silverpeas.socialnetwork.myProfil.servlets.MyProfileRoutes"%>
<c:set var="browseContext" value="${requestScope.browseContext}" />
<fmt:setLocale value="${sessionScope[sessionController].language}" />
<view:setBundle bundle="${requestScope.resources.multilangBundle}" />

<%
	GraphicElementFactory gef = (GraphicElementFactory) session.getAttribute("SessionGraphicElementFactory");

    String language = request.getLocale().getLanguage();
    ResourceLocator multilang = new ResourceLocator("com.silverpeas.social.multilang.socialNetworkBundle", language);
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
<link type="text/css" href="<%=m_context%>/util/styleSheets/fieldset.css" rel="stylesheet" />
<script type="text/javascript" src="<%=m_context %>/util/javaScript/animation.js"></script>
<script type="text/javascript" src="<%=m_context %>/util/javaScript/checkForm.js"></script>
<view:includePlugin name="messageme"/>
<view:includePlugin name="password"/>
<script type="text/javascript">
function statusPublishFailed() {
	$("#statusPublishFailedDialog").dialog("open");
}

function statusPublished() {
	$("#statusPublishedDialog").dialog("open");
}
</script>

<script type="text/javascript" src="<%=m_context %>/socialNetwork/jsp/js/statusFacebook.js"></script>
<script type="text/javascript" src="<%=m_context %>/socialNetwork/jsp/js/statusLinkedIn.js"></script>

<script type="text/javascript">
function statusPublishFailed() {
	$("#statusPublishFailedDialog").dialog("open");
}

function statusPublished() {
	$("#statusPublishedDialog").dialog("open");
}

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
            width: 400,
            title: "<fmt:message key="profil.actions.changeStatus" />",
            buttons: {
				"<fmt:message key="GML.ok"/>": function() {
						var status = $("#newStatus");
						$( "#myProfileFiche .statut").html(status.val());

					    var url = "<%=m_context %>/RmyProfilJSON?Action=updateStatus";
						url+='&status='+encodeURIComponent(status.val());

						// prevents from IE amazing cache
						url+='&IEFix='+Math.round(new Date().getTime());
				        $.getJSON(url, function(data){
				        	if(data.status == "silverpeastimeout") {
				        		alert("<fmt:message key="myProfile.status.timeout" />")
				        	}
				        });
						$( this ).dialog( "close" );
				},
				"<fmt:message key="GML.cancel"/>": function() {
					$(this).dialog( "close" );
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
				"<fmt:message key="GML.ok"/>": function() {
					document.photoForm.submit();
				},
				"<fmt:message key="GML.cancel"/>": function() {
					$(this).dialog( "close" );
				}
			}
    };

    $("#avatarDialog").dialog(avatarDialogOpts);    //end dialog

    var statusPublishedDialogOpts = {
    		resizable: false,
            modal: true,
            autoOpen: false,
            height: "auto",
            width: 300,
            title: "<fmt:message key="profil.actions.changeStatus" />",
            buttons: {
				"<fmt:message key="GML.close"/>": function() {
					$(this).dialog( "close" );
				}
			}
    };
    $("#statusPublishedDialog").dialog(statusPublishedDialogOpts);    //end dialog

    var statusPublishFailedDialogOpts = {
    		resizable: false,
            modal: true,
            autoOpen: false,
            height: "auto",
            width: 300,
            title: "<fmt:message key="profil.actions.changeStatus" />",
            buttons: {
				"<fmt:message key="GML.close"/>": function() {
					$(this).dialog( "close" );
				}
			}
    };
    $("#statusPublishFailedDialog").dialog(statusPublishFailedDialogOpts);    //end dialog

});
</script>
</head>
<body id="myProfile">

<c:out value="${FB_loadSDK}" escapeXml="false"/>
<c:out value="${LI_loadSDK}" escapeXml="false"/>

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
            <br/>
			<a href="#" class="link" onclick="logIntoFB();" id="FBLoginButton"><fmt:message key="profil.actions.connectTo" /> FACEBOOK</a>
			<a href="#" class="link" onclick="publishToFB();" id="FBPublishButton"><fmt:message key="profil.actions.publishStatus"/> FACEBOOK</a>
            <br/>
			<a href="#" class="link" onclick="logIntoLinkedIN();" id="LinkedInLoginButton"><fmt:message key="profil.actions.connectTo" /> LINKEDIN</a>
			<a href="#" class="link" onclick="publishToLinkedIN();" id="LinkedInPublishButton"><fmt:message key="profil.actions.publishStatus" /> LINKEDIN</a>
        </div>
        <div class="profilPhoto">
			<img src="<%=m_context + userFull.getAvatar()%>" alt="viewUser" class="avatar"/>
        </div>
        <br clear="all" />
 	</div>

 	<div id="statusDialog">
		<form>
	    	<textarea id="newStatus" cols="49" rows="4"></textarea><br/>
		</form>
	</div>

	<div id="avatarDialog">
		<form name="photoForm" action="UpdatePhoto" method="post" enctype="multipart/form-data" accept-charset="UTF-8">
	        <div>
	          <div class="txtlibform"><fmt:message key="profil.image" /> :</div>
	          <div><input type="file" name="WAIMGVAR0" size="60"/></div>
	        </div>
	        <span id="avatar-policy"><fmt:message key="profil.descriptionImage" /></span>
	      </form>
	</div>

 	<div id="statusPublishedDialog">
 		<fmt:message key="profil.msg.statusPublished"/>
	</div>

 	<div id="statusPublishFailedDialog">
 		<fmt:message key="profil.errors.statusPublishFailed"/>
	</div>
	<% if (nbContacts > 0) { %>
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
	<% } %>

</div>



<div id="publicProfileContenu">

	<fmt:message key="myProfile.tab.feed" var="feed" />
	<fmt:message key="myProfile.tab.wall" var="wall" />
	<fmt:message key="myProfile.tab.profile" var="profile" />
	<fmt:message key="myProfile.tab.networks" var="networks" />
	<fmt:message key="myProfile.tab.invitations" var="invitations" />
	<fmt:message key="myProfile.tab.settings" var="settings" />
	<view:tabs>
		<view:tab label="${feed}" action="<%=MyProfileRoutes.MyFeed.toString() %>" selected="<%=Boolean.toString(MyProfileRoutes.MyFeed.toString().equals(view)) %>" />
		<view:tab label="${wall}" action="<%=MyProfileRoutes.MyWall.toString() %>" selected="<%=Boolean.toString(MyProfileRoutes.MyWall.toString().equals(view)) %>" />
    	<view:tab label="${profile}" action="<%=MyProfileRoutes.MyInfos.toString() %>" selected="<%=Boolean.toString(MyProfileRoutes.MyInfos.toString().equals(view)) %>" />
		<% if (SocialNetworkID.oneIsEnable()) {%>
	    	<view:tab label="${networks}" action="<%=MyProfileRoutes.MyNetworks.toString() %>" selected="<%=Boolean.toString(MyProfileRoutes.MyNetworks.toString().equals(view)) %>" />
		<%}%>
    	<view:tab label="${invitations}" action="<%=MyProfileRoutes.MyInvitations.toString() %>" selected="<%=Boolean.toString(MyProfileRoutes.MyInvitations.toString().equals(view) || MyProfileRoutes.MySentInvitations.toString().equals(view)) %>" />
    	<view:tab label="${settings}" action="<%=MyProfileRoutes.MySettings.toString() %>" selected="<%=Boolean.toString(MyProfileRoutes.MySettings.toString().equals(view)) %>" />
	</view:tabs>

	<% if (MyProfileRoutes.MyInfos.toString().equals(view)) { %>
		<%@include file="myProfileTabIdentity.jsp" %>
	<% } else if (MyProfileRoutes.MySettings.toString().equals(view)) { %>
		<%@include file="myProfileTabSettings.jsp" %>
	<% } else if (MyProfileRoutes.MyNetworks.toString().equals(view)) { %>
		<%@include file="myProfileTabNetworks.jsp" %>
	<% } else if (MyProfileRoutes.MyInvitations.toString().equals(view) || MyProfileRoutes.MySentInvitations.toString().equals(view)) { %>
		<%@include file="myProfileTabInvitations.jsp" %>
	<% } else if (MyProfileRoutes.MyWall.toString().equals(view) || MyProfileRoutes.MyFeed.toString().equals(view)) { %>
		<%@include file="myProfileTabWall.jsp" %>
	<% } %>

</div>
</view:window>

</body>
</html>
