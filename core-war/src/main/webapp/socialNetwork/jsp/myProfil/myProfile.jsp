<%--

    Copyright (C) 2000 - 2024 Silverpeas

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    As a special exception to the terms and conditions of version 3.0 of
    the GPL, you may redistribute this Program in connection with Free/Libre
    Open Source Software ("FLOSS") applications as described in Silverpeas's
    FLOSS exception.  You should have received a copy of the text describing
    the FLOSS exception, and it is also available here:
    "https://www.silverpeas.org/legal/floss_exception.html"

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.

--%>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://www.silverpeas.com/tld/silverFunctions" prefix="silfn" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<%@ taglib tagdir="/WEB-INF/tags/silverpeas/util" prefix="viewTags" %>


<%@ page import="org.silverpeas.core.socialnetwork.model.SocialNetworkID" %>
<%@ page import="org.silverpeas.core.admin.user.model.User" %>

<c:set var="browseContext" value="${requestScope.browseContext}" />
<fmt:setLocale value="${sessionScope[sessionController].language}" />
<view:setBundle bundle="${requestScope.resources.multilangBundle}" />

<c:set var="defaultAvatar"><%= User.DEFAULT_AVATAR_PATH %></c:set>
<c:set var="nbContacts" value="${requestScope.ContactsNumber}" />
<c:set var="contacts" value="${requestScope.Contacts}" />
<c:set var="currentUser" value="${requestScope.UserFull}" />
<c:url var="currentUserAvatarUrl" value="${currentUser.avatar}"/>
<c:set var="view" value="${requestScope.view}"/>
<c:set var="userSelfDeletionAccountEnabled" value="${requestScope.UserSelfDeletionAccountEnabled}"/>
<fmt:message key="myProfile.actions.deleteAccount.confirm" var="deleteAccountConfirm"/>

<view:sp-page>
<view:sp-head-part withFieldsetStyle="true" withCheckFormScript="true">
<view:includePlugin name="password"/>
<view:includePlugin name="myprofile"/>

<script type="text/javascript" src="<c:url value='/socialNetwork/jsp/js/statusFacebook.js' />"></script>
<script type="text/javascript" src="<c:url value='/socialNetwork/jsp/js/statusLinkedIn.js' />"></script>

<script type="text/javascript">
function statusPublishFailed() {
	jQuery('#statusPublishFailedDialog').popup('information', {
		title: "<fmt:message key="profil.actions.changeStatus" />",
		callback : function() {
			return true;
		}
	});
}

function statusPublished() {
	jQuery('#statusPublishedDialog').popup('information', {
		title: "<fmt:message key="profil.actions.changeStatus" />",
		callback : function() {
			return true;
		}
	});
}

function editStatus() {
	jQuery('#statusDialog').popup('validation', {
		title: "<fmt:message key="profil.actions.changeStatus" />",
		callback: function() {
			const $status = $("#newStatus");
			const url = webContext + '/RmyProfilJSON';
			const newStatus = {
				status: $status.val()
			};
			sp.ajaxRequest(url)
					.byPostMethod()
					.sendAndPromiseJsonResponse(newStatus)
					.then(function(data) {
				if(data.status === "silverpeastimeout") {
					notyWarning("<fmt:message key="myProfile.status.timeout" />");
				} else {
					$( "#myProfileFiche .statut").html(data.status.escapeHTML());
					$("#newStatus").html(data.status.escapeHTML());
				}
			});
		}
	});
}

function updateAvatar() {
  myProfileApp.manager.openMyPhoto();
}

function deleteAccount() {
  $('#removeAccountDialog').popup('confirmation', {
    title : "${deleteAccountConfirm}", callback : function() {
      document.removeAccountForm.submit();
      return true;
    }
  });
}

whenSilverpeasReady(function() {
  window.myProfileApp = SpVue.createApp({
    data : function() {
      return {
        manager : undefined,
        profile : {
          avatarUrl : '${silfn:escapeJs(currentUserAvatarUrl)}'
        }
      }
    },
    methods : {
      myPhotoChange : function() {
        sp.navRequest('<%=MyProfileRoutes.MyInfos.toString()%>').go();
      }
    }
  }).mount('#my-profile-app');
});
</script>
</view:sp-head-part>
<view:sp-body-part id="myProfile">
  <div id="my-profile-app">
    <silverpeas-my-profile-management v-on:api="manager = $event"
                                      v-bind:profile="profile"
                                      v-on:my-photo-change="myPhotoChange">
	</silverpeas-my-profile-management>
  </div>

<c:out value="${requestScope.FB_loadSDK}" escapeXml="false"/>
<c:out value="${requestScope.LI_loadSDK}" escapeXml="false"/>

<view:window>

<div id="myProfileFiche" >

	<div id="userinfo" class="info tableBoard">
		<h2 class="userName"><c:out value="${currentUser.firstName}" /> <br /><c:out value="${currentUser.lastName}" /></h2>
	  <p class="statut"><c:out value="${silfn:escapeHtmlWhitespaces(currentUser.status)}" escapeXml="false" /></p>
    <div class="action">
		  <button type="button" class="link updateStatus" onclick="editStatus()"><fmt:message
                  key="profil.actions.changeStatus" /></button>
      <br />
      <button type="button" class="link updateAvatar" onclick="updateAvatar()"><fmt:message
			  key="profil.actions.changePhoto" /></button>
      <br/>
			<button type="button" class="link" onclick="logIntoFB();"
				   id="FBLoginButton"><fmt:message	key="profil.actions.connectTo" />
                FACEBOOK</button>
			<button type="button" class="link" onclick="publishToFB();"
			   id="FBPublishButton"><fmt:message key="profil.actions.publishStatus"/>
                FACEBOOK</button>
      <br/>
			<button  type="button" class="link" onclick="logIntoLinkedIN();"
               id="LinkedInLoginButton"><fmt:message key="profil.actions.connectTo" />
                LINKEDIN</button>
			<button type="button" class="link" onclick="publishToLinkedIN();"
        id="LinkedInPublishButton"><fmt:message key="profil.actions.publishStatus" />
                LINKEDIN</button>
    </div>
    <div class="profilPhoto">
			<view:image src="${currentUser.avatar}" type="avatar.profil" alt="viewUser" css="avatar"/>
    </div>
    <br/>
	</div>

  <c:if test="${userSelfDeletionAccountEnabled}">
  <div id="removeMyAccount">
    <button type="button" class="sp_button" onclick="deleteAccount()"><fmt:message
            key="myProfile.actions.deleteAccount"/></button>
  </div>

  <div id="removeAccountDialog" style="display: none">
    <form name="removeAccountForm" action="DELETE_MY_ACCOUNT" method="post">
      <div>
        <div class="removeAccount-Message">
          <label for="message"><fmt:message key="myProfile.deleteAccount.message" /></label>
          <textarea name="message" id="message" cols="70" rows="5"></textarea>
        </div>
        <div class="removeAccount-forgetMe">
          <label for="forgetMe"><fmt:message key="myProfile.deleteAccount.forgetMe" />: </label>
          <input type="radio" id="forgetMe1" name="forgetMe" value="no" checked="checked">&nbsp;<label for="forgetMe1"><fmt:message key="GML.no" /></label>
          <input type="radio" id="forgetMe2" name="forgetMe" value="yes"><label for="forgetMe2">&nbsp;<fmt:message key="GML.yes" /></label>
        </div>
      </div>
    </form>
  </div>
  </c:if>

	<div id="statusDialog" style="display:none;">
		<form name="updateUserStatus" action="/RmyProfilJSON" method="POST">
			<input type="hidden" name="Action" value="updateStatus"/>
			<textarea id="newStatus" name="status" cols="49"
					  rows="4"><c:out value="${currentUser.status}" /></textarea>
		</form>
	</div>

	<div id="statusPublishedDialog" style="display:none;">
		<fmt:message key="profil.msg.statusPublished"/>
	</div>

	<div id="statusPublishFailedDialog" style="display:none;">
		<fmt:message key="profil.errors.statusPublishFailed"/>
	</div>
    <c:if test="${nbContacts > 0}">
	<h3><c:out value="${nbContacts}"/> <fmt:message key="myProfile.contacts" /></h3>
	<!-- allContact  -->
	<div id="allContact">
    <c:forEach items="${contacts}" var="contact">
	<div class="unContact">
		<div class="profilPhotoContact">
            <a href="<c:url value='/Rprofil/jsp/Main'><c:param name='userId' value='${contact.id}'/></c:url>"><view:image css="avatar" alt="viewUser" type="avatar" src="${contact.avatar}" /></a>
		</div>
              <a href="<c:url value='/Rprofil/jsp/Main'><c:param name='userId' value='${contact.id}'/></c:url>" class="contactName"><c:out value="${contact.displayedName}"/></a>
		</div> <!-- /unContact  -->
    </c:forEach>
    <c:if test="${not empty contacts}">
	     <br />
       <a href="<c:url value='/Rdirectory/jsp/Main'><c:param name='UserId' value='${currentUser.id}' /></c:url>" class="link"><fmt:message key="myProfile.contacts.all" /></a>
	     <br />
    </c:if>
	</div><!-- /allContact  -->
    </c:if>

</div>

<%
	String view = (String) request.getAttribute("View");
%>

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

</view:sp-body-part>
</view:sp-page>
