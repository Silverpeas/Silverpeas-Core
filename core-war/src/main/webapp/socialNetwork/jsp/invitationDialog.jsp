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

<%@page import="org.silverpeas.web.socialnetwork.invitation.servlets.InvitationJSONActions"%>

<script type="text/javascript">
$(function() {
    $("#invitationDialog").dialog({
		autoOpen: false,
		resizable: false,
		modal: true,
		height: "auto",
		width: 550,
		buttons: {
			"<fmt:message key="GML.ok"/>": function() {
				var message = $("#invitation-message").val();
			$.getJSON("<%=m_context%>/InvitationJSON",
		{
				IEFix: new Date().getTime(),
				Action: "<%=InvitationJSONActions.SendInvitation%>",
				Message: message,
				TargetUserId: invitationTargetUserId
		},
			function(data){
				if (data.success) {
					closeInvitationDialog();
					try {
						$("#user-"+invitationTargetUserId+" .invitation").hide('slow')
					} catch (e) {
					//do nothing
					//As fragment is externalized, class invitation can be missing
					}
				} else {
				alert(data.error);
				}
			});
			},
			"<fmt:message key="GML.cancel" />": function() {
				closeInvitationDialog();
			}
		}
	});
});

var invitationTargetUserId = -1;
function initInvitation(userId, name){
	invitationTargetUserId = userId;
	$("#invitationDialog").dialog("option", "title", name);
	$("#invitationDialog").dialog("open");
}

function closeInvitationDialog() {
	$("#invitationDialog").dialog("close");
	$("#invitation-message").val("");
}
</script>

<!-- Dialog to notify a user -->
<div id="invitationDialog">
	<form>
		<table>
			<tr>
			<td class="txtlibform"><fmt:message key="GML.notification.message" /> :</td>
			<td><textarea name="txtMessage" id="invitation-message" cols="60" rows="8"></textarea></td>
		</tr>
		</table>
	</form>
</div>