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

<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>

<%@page import="org.silverpeas.web.socialnetwork.invitation.model.InvitationUser"%>
<%@page import="org.silverpeas.web.socialnetwork.invitation.servlets.InvitationJSONActions"%>
<%@page import="org.silverpeas.web.socialnetwork.myprofil.servlets.MyProfileRoutes"%>
<%@page import="org.silverpeas.core.util.URLUtil"%>
<%@page import="org.silverpeas.core.util.EncodeHelper"%>
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
var invitationId;
var nbInvitations = <%=invitations.size()%>;
$(function() {
	$( "#dialog-confirmCancel" ).dialog({
		autoOpen: false,
		resizable: false,
		modal: true,
		buttons: {
			"<fmt:message key="GML.yes" />": function() {
				$.getJSON("<%=m_context%>/InvitationJSON",
	                {
					IEFix: new Date().getTime(),
					Action: "<%=InvitationJSONActions.IgnoreInvitation%>",
					Id: invitationId
	                },
				function(data){
				if (data.success) {
					$("#invitation-"+invitationId).hide('slow');
					nbInvitations--;
					showEmptyListMessage();
				} else {
					alert(data.error);
				}
				});
				$( this ).dialog( "close" );
			},
			"<fmt:message key="GML.no" />": function() {
				$( this ).dialog( "close" );
			}
		}
	});

	$( "#dialog-confirmAccept" ).dialog({
		autoOpen: false,
		resizable: false,
		modal: true,
		buttons: {
			"<fmt:message key="GML.yes" />": function() {
				$.getJSON("<%=m_context%>/InvitationJSON",
	                {
					IEFix: new Date().getTime(),
					Action: "<%=InvitationJSONActions.AcceptInvitation%>",
					Id: invitationId
	                },
				function(data){
				if (data.success) {
					$("#invitation-"+invitationId).hide('slow');
					nbInvitations--;
					showEmptyListMessage();

				} else {
					alert(data.error);
				}
				});
				$( this ).dialog( "close" );
			},
			"<fmt:message key="GML.no" />": function() {
				$( this ).dialog( "close" );
			}
		}
	});

	$( "#dialog-confirmIgnore" ).dialog({
		autoOpen: false,
		resizable: false,
		modal: true,
		buttons: {
			"<fmt:message key="GML.no" />": function() {
				$( this ).dialog( "close" );
			},
			"<fmt:message key="GML.yes" />": function() {
				$.getJSON("<%=m_context%>/InvitationJSON",
	                {
					IEFix: new Date().getTime(),
					Action: "<%=InvitationJSONActions.IgnoreInvitation%>",
					Id: invitationId
	                },
				function(data){
				if (data.success) {
					$("#invitation-"+invitationId).hide('slow');
					nbInvitations--;
					showEmptyListMessage();
				} else {
					alert(data.error);
				}
				});
				$( this ).dialog( "close" );
			}
		}
	});

});

function confirmCancel(id) {
	invitationId = id;
	$( "#dialog-confirmCancel .userName" ).text($("#invitation-"+invitationId+" .txt a.name").text());
	$( "#dialog-confirmCancel" ).dialog("open");
}

function confirmAccept(id) {
	invitationId = id;
	$( "#dialog-confirmAccept .userName" ).text($("#invitation-"+invitationId+" .txt a.name").text());
	$( "#dialog-confirmAccept" ).dialog("open");
}

function confirmIgnore(id) {
	invitationId = id;
	$( "#dialog-confirmIgnore .userName" ).text($("#invitation-"+invitationId+" .txt a.name").text());
	$( "#dialog-confirmIgnore" ).dialog("open");
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
						<a class="link notification" href="#" onclick="confirmCancel(<%=id %>)"><fmt:message key="myProfile.invitations.cancel" /></a>
				<% } else { %>
								<a onclick="confirmAccept(<%=id %>)" class="link invitation" href="#"><fmt:message key="myProfile.invitations.accept" /></a>
					<a onclick="confirmIgnore(<%=id %>)" class="link notification" href="#"><fmt:message key="myProfile.invitations.ignore" /></a>
				<% } %>
				<a rel="<%=senderId %>,<%=invitation.getUserDetail().getDisplayedName()%>" class="link notification" href="#"><fmt:message key="GML.notification.send" /></a>
					</div>
					<div class="txt">
				<p>
					<view:username userId="<%=invitation.getUserDetail().getId()%>"/>
	                    </p>
	                    <p>
				<fmt:message key="myProfile.invitations.date" /> <%= resource.getOutputDateAndHour(invitation.getInvitation().getInvitationDate())%>
	                    </p>
	                    <p class="message">
	                    <%=EncodeHelper.javaStringToHtmlParagraphe(invitation.getInvitation().getMessage()) %>
				</p>
					</div>
	      </div>
	      <% } %>

	</div>

</div>

<div id="dialog-confirmCancel" title="<fmt:message key="myProfile.invitations.dialog.cancel.title" />">
	<p><fmt:message key="myProfile.invitations.dialog.cancel.message" /> <span class="userName"></span> ?</p>
</div>
<div id="dialog-confirmAccept" title="<fmt:message key="myProfile.invitations.dialog.accept.title" />">
	<p><fmt:message key="myProfile.invitations.dialog.accept.message" /> <span class="userName"></span> ?</p>
</div>
<div id="dialog-confirmIgnore" title="<fmt:message key="myProfile.invitations.dialog.ignore.title" />">
	<p><fmt:message key="myProfile.invitations.dialog.ignore.message" /> <span class="userName"></span> ?</p>
</div>
</div>
