<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>

<%@page import="com.silverpeas.socialnetwork.invitation.servlets.InvitationJSONActions"%>

<script type="text/javascript">
$(function() {
    $("#invitationDialog").dialog({
		autoOpen: false,
		resizable: false,
		modal: true,
		height: "auto",
		width: 500,
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