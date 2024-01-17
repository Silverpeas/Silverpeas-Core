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
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>

<%@page import="org.silverpeas.web.socialnetwork.invitation.model.InvitationUser"%>
<%@page import="org.silverpeas.web.socialnetwork.myprofil.servlets.MyProfileRoutes"%>
<%@page import="org.silverpeas.core.util.URLUtil"%>
<%@page import="org.silverpeas.core.util.WebEncodeHelper"%>
<%@page import="org.silverpeas.core.util.MultiSilverpeasBundle"%>
<%@page import="java.util.List"%>

<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>

<%
	List invitations = null;
	String receivedCssClass = "";
	String sentCssClass = "";
	boolean outbox = view.equals(MyProfileRoutes.MySentInvitations.toString());
	if (outbox) {
		invitations = (List) request.getAttribute("Outbox");
		sentCssClass = "class=\"active\"";
	} else {
		invitations = (List) request.getAttribute("Inbox");
		receivedCssClass = "class=\"active\"";
	}
	MultiSilverpeasBundle resource = (MultiSilverpeasBundle) request.getAttribute("resources");
%>
<style type="text/css">
.inlineMessage {
	<% if (invitations != null && !invitations.isEmpty()) { %>
		display: none;
	<% } %>
}
</style>
<script type="text/javascript">
var nbInvitations = <%=invitations.size()%>;
function doOnInvitation(invitation) {
  $("#invitation-"+invitation.id).remove();
  nbInvitations--;
  showEmptyListMessage();
}

function showEmptyListMessage() {
	if (nbInvitations <= 0) {
		$(".inlineMessage").show('slow');
	}
}
</script>

<div id="invitationProfil">

<div class="sousNavBulle">
	<p><fmt:message key="myProfile.tab.invitations" /> : <a <%=receivedCssClass %> href="<%=MyProfileRoutes.MyInvitations.toString() %>"><fmt:message key="myProfile.invitations.received" /></a> <a <%=sentCssClass %> href="<%=MyProfileRoutes.MySentInvitations.toString() %>"><fmt:message key="myProfile.invitations.sent" /></a></p>
</div>

<div id="ReceiveInvitation">

	<div class="inlineMessage">
		<% if (outbox) { %>
			<fmt:message key="myProfile.invitations.outbox.empty" />
		<% } else { %>
			<fmt:message key="myProfile.invitations.inbox.empty" />
		<% } %>
	</div>

	<div id="invitations-list">

		<% for (int i=0; i<invitations.size(); i++) {
			InvitationUser invitation = (InvitationUser) invitations.get(i);
			String senderId = invitation.getUserDetail().getId();
			int id = invitation.getInvitation().getId();
		%>
			<div class="a_invitation" id="invitation-<%=id%>">
	                 <div class="profilPhoto"><a href="<%=URLUtil.getApplicationURL() %>/Rprofil/jsp/Main?userId=<%=senderId%>"><view:image css="defaultAvatar" alt="" src="<%=invitation.getUserDetail().getAvatar() %>" type="avatar" /></a></div>
	                 <div class="action">
					<% if (outbox) { %>
						<a class="link cancel-invitation" href="#" rel="<%=id%>,doOnInvitation"><fmt:message key="myProfile.invitations.cancel" /></a>
				<% } else { %>
            <a class="link accept-invitation" href="#" rel="<%=id%>,doOnInvitation"><fmt:message key="myProfile.invitations.accept" /></a>
					  <a class="link cancel-invitation" href="#" rel="<%=id%>,doOnInvitation"><fmt:message key="myProfile.invitations.ignore" /></a>
				<% } %>
				<a onclick="sp.messager.open(null, {recipientUsers: <%=senderId%>, recipientEdition: false});" class="link notification" href="#"><fmt:message key="GML.notification.send" /></a>
					</div>
					<div class="txt">
				<p>
					<view:username userId="<%=invitation.getUserDetail().getId()%>"/>
	                    </p>
	                    <p>
				<fmt:message key="myProfile.invitations.date" /> <%= resource.getOutputDateAndHour(invitation.getInvitation().getInvitationDate())%>
	                    </p>
	                    <p class="message">
	                    <%=WebEncodeHelper.javaStringToHtmlParagraphe(invitation.getInvitation().getMessage()) %>
				</p>
					</div>
	      </div>
	      <% } %>

	</div>

</div>

</div>