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

<%@page import="org.silverpeas.core.notification.user.client.NotificationParameters"%>

<script type="text/javascript">
$(function() {
    $("#notificationDialog").dialog({
		autoOpen: false,
		resizable: false,
		modal: true,
		height: "auto",
		width: 550,
		buttons: {
			"<fmt:message key="GML.ok"/>": function() {
				var title = $("#txtTitle").val();
				var message = $("#txtMessage").val();
			    var errorMsg = "";
			    if (title.length <= 0) {
				errorMsg = "<fmt:message key="GML.thefield" />"+ " <fmt:message key="GML.notification.subject" />"+ " <fmt:message key="GML.isRequired" />";
			    }
			    if (errorMsg == "") {
					$.getJSON("<%=m_context%>/DirectoryJSON",
			{
					IEFix: new Date().getTime(),
					Action: "SendMessage",
					Title: title,
					Message: message,
					TargetUserId: notificationTargetUserId
			},
				function(data){
					if (data.success) {
						closeNotificationDialog();
					} else {
					alert(data.error);
					}
				});
			    } else {
				window.alert(errorMsg);
			    }
			},
			"<fmt:message key="GML.cancel" />": function() {
				$( this ).dialog( "close" );
			}
		}
	});
});

var notificationTargetUserId = -1;
function initNotification(userId, name){
	notificationTargetUserId = userId;
	$("#notificationDialog").dialog("option", "title", name);
	$("#notificationDialog").dialog("open");
}

function closeNotificationDialog() {
	$("#notificationDialog").dialog("close");
	$("#txtTitle").val("");
	$("#txtMessage").val("");
}
</script>

<!-- Dialog to notify a user -->
<div id="notificationDialog">
	<form>
		<table>
			<tr>
			<td class="txtlibform"><fmt:message key="GML.notification.subject" /> :</td>
			<td><input type="text" name="txtTitle" id="txtTitle" maxlength="<%=NotificationParameters.MAX_SIZE_TITLE%>" size="50" value=""/>&nbsp;<img src="<%=m_context%>/util/icons/mandatoryField.gif" width="5" height="5" alt="mandatoryField" /></td>
		</tr>
		<tr>
			<td class="txtlibform"><fmt:message key="GML.notification.message" /> :</td>
			<td><textarea name="txtMessage" id="txtMessage" cols="60" rows="8"></textarea></td>
		</tr>
		<tr>
			<td colspan="2">(<img src="<%=m_context%>/util/icons/mandatoryField.gif" width="5" height="5" alt="mandatoryField" /> : <fmt:message key="GML.requiredField"/>)</td>
			</tr>
		</table>
	</form>
</div>