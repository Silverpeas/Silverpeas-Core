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

<%@ include file="check.jsp" %>

<%
	// Retrieve parameters
	LinkDetail 	link			= (LinkDetail) request.getAttribute("Link");
	boolean		isVisible		= (Boolean) request.getAttribute("IsVisible");

	int 		linkId 			= 0;
	String 		name 			= "";
	String 		description 	= "";
	String 		url				= "";
	boolean 	visible 		= false;
	boolean 	popup			= false;
	String		action 			= "CreateLink";

	// Retrieve data if update existing link
	if (link != null) {
		linkId 			= link.getLinkId();
		name 			= link.getName();
		description 	= link.getDescription();
		if (description == null) {
			description = "";
		}
		url				= link.getUrl();
		visible 		= link.isVisible();
		popup			= link.isPopup();
		action 			= "UpdateLink";
	}
	// Get buttons
	Button validateButton = gef.getFormButton(resource.getString("GML.validate"), "javascript:onclick=sendData();", false);
    Button cancelButton = gef.getFormButton(resource.getString("GML.cancel"), "javascript:window.close()", false);
%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<%
	out.println(gef.getLookStyleSheet());
%>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/checkForm.js"></script>
<script type="text/javascript">
function sendData() { 
	if (isCorrectForm()) {
   		window.opener.document.linkForm.action = "<%=action%>";
   		window.opener.document.linkForm.LinkId.value = document.linkForm.LinkId.value;
   		window.opener.document.linkForm.Name.value = document.linkForm.Name.value;
   		window.opener.document.linkForm.Description.value = document.linkForm.Description.value;
   		window.opener.document.linkForm.Url.value = document.linkForm.Url.value;
   		if (document.linkForm.Visible.checked)
   			window.opener.document.linkForm.Visible.value = document.linkForm.Visible.value;
   		if (document.linkForm.Popup.checked)
   			window.opener.document.linkForm.Popup.value = document.linkForm.Popup.value;
   		window.opener.document.linkForm.submit();
  		window.close();
	}
}

function isCorrectForm() {
   	var errorMsg = "";
   	var errorNb = 0;
	var url = stripInitialWhitespace(document.linkForm.Url.value);

   	if (url == "") {
   		errorMsg+="  - '<%=resource.getString("myLinks.url")%>'  <%=resource.getString("GML.MustBeFilled")%>\n";
       	errorNb++;
   	}

   	switch(errorNb) {
       	case 0 :
           	result = true;
           	break;
       	case 1 :
           	errorMsg = "<%=resource.getString("GML.ThisFormContains")%> 1 <%=resource.getString("GML.error")%> : \n" + errorMsg;
           	window.alert(errorMsg);
           	result = false;
           	break;
       	default :
           	errorMsg = "<%=resource.getString("GML.ThisFormContains")%> " + errorNb + " <%=resource.getString("GML.errors")%> :\n" + errorMsg;
           	window.alert(errorMsg);
           	result = false;
           	break;
    }
    return result;
}
</script>
</head>
<body onload="javascript:document.linkForm.Url.focus();">
<%
	if (action.equals("CreateLink")) {
		browseBar.setComponentName(resource.getString("myLinks.links") + resource.getString("myLinks.createLink"));
	} else {
		browseBar.setComponentName(resource.getString("myLinks.links") + resource.getString("myLinks.updateLink"));
	}

	Board board	= gef.getBoard();

	out.println(window.printBefore());
    out.println(frame.printBefore());
    out.println(board.printBefore());
%>
<form name="linkForm" method="post" action="">
<table cellpadding="5" width="100%">
	<tr>
		<td class="txtlibform"><%=resource.getString("myLinks.url")%> :</td>
		<td><input type="text" name="Url" size="60" maxlength="150" value="<%=url%>"/>
		<img src="<%=resource.getIcon("myLinks.obligatoire")%>" width="5" height="5" border="0"/></td>
	</tr>
	<tr>
		<td class="txtlibform"><%=resource.getString("GML.nom")%> :</td>
		<td><input type="text" name="Name" size="60" maxlength="150" value="<%=EncodeHelper.javaStringToHtmlString(name)%>" />
			<input type="hidden" name="LinkId" value="<%=linkId%>"/> </td>
	</tr>
	<tr>
		<td class="txtlibform"><%=resource.getString("GML.description")%> :</td>
		<td><input type="text" name="Description" size="60" maxlength="150" value="<%=EncodeHelper.javaStringToHtmlString(description)%>" /></td>
	</tr>
	<% if (isVisible) { %>
			<tr>
				<td class="txtlibform"> <%=resource.getString("myLinks.visible")%> :</td>
				<%
					String visibleCheck = "";
					if (visible) {
						visibleCheck = "checked=\"checked\"";
					}

				%>
			    <td><input type="checkbox" name="Visible" value="true" <%=visibleCheck%>/></td>
			</tr>
	<% } else { %>
		<tr><td colspan="2"><input type="hidden" name="Visible" value="true"/></td></tr>
	<% } %>
	<tr>
		<td class="txtlibform"> <%=resource.getString("myLinks.popup")%> :</td>
		<%
			String popupCheck = "";
			if (popup) {
				popupCheck = "checked=\"checked\"";
			}
		%>
	    <td><input type="checkbox" name="Popup" value="true" <%=popupCheck%>/></td>
	</tr>
  	<tr>
  		<td colspan="2"><img border="0" src=<%=resource.getIcon("myLinks.obligatoire")%> width="5" height="5"/> : <%=resource.getString("GML.mandatory") %></td>
  	</tr>
</table>
</form>
<%
	out.println(board.printAfter());
	ButtonPane buttonPane = gef.getButtonPane();
    buttonPane.addButton(validateButton);
    buttonPane.addButton(cancelButton);
	out.println("<br/>"+buttonPane.print());
 	out.println(frame.printAfter());
	out.println(window.printAfter());
%>
</body>
</html>