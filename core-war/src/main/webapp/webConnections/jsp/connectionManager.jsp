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

<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<%@ include file="check.jsp" %>

<%
	ConnectionDetail 	connection	= (ConnectionDetail) request.getAttribute("Connection");
	String 				action		= (String) request.getAttribute("Action");
	ComponentInst		inst		= (ComponentInst) request.getAttribute("ComponentInst");
	boolean				isAnonymousAccess = ((Boolean) request.getAttribute("IsAnonymousAccess")).booleanValue();

	String login = "";
	String password = "";
	String description = null;
	boolean isCreation = action.equals("CreateConnection");

	description = inst.getDescription();
	if (!isCreation) {
	String nameLogin = inst.getParameterValue("login");
		login = (String) connection.getParam().get(nameLogin);
		String namePassword = inst.getParameterValue("password");
		password = (String) connection.getParam().get(namePassword);
	}

	// d�claration des boutons
	Button validateButton;
	Button cancelButton;
	if (isCreation) {
		validateButton = (Button) gef.getFormButton(resource.getString("GML.validate"), "javascript:onClick=sendData();", false);
		cancelButton = (Button) gef.getFormButton(resource.getString("GML.cancel"), "javascript:redirect('"+connection.getComponentId()+"')", false);
	}
	else {
		validateButton = (Button) gef.getFormButton(resource.getString("GML.validate"), "javascript:onClick=updateData();", false);
		cancelButton = (Button) gef.getFormButton(resource.getString("GML.cancel"), "javascript:window.close()", false);
	}


%>

<html>
<head>
<view:looknfeel withCheckFormScript="true"/>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/dateUtils.js"></script>
<script language="javascript">

function sendData() {
	if (isCorrectForm()) {
		document.connectionForm.submit();
	}
}

function updateData() {
	if (isCorrectForm()) {
		window.opener.document.connectionForm.action = "<%=action%>";
		window.opener.document.connectionForm.Login.value = document.connectionForm.Login.value;
		window.opener.document.connectionForm.Password.value = document.connectionForm.Password.value;
		window.opener.document.connectionForm.ConnectionId.value = document.connectionForm.ConnectionId.value;
		window.opener.document.connectionForm.ComponentId.value = document.connectionForm.ComponentId.value;
		window.opener.document.connectionForm.submit();
		window.close();
	}
}

function isCorrectForm() {
     var errorMsg 			= "";
     var errorNb 			= 0;
     var login 				= document.connectionForm.Login.value;

     if (isWhitespace(login)) {
           errorMsg +="  - <%=resource.getString("GML.theField")%> '<%=resource.getString("webConnections.login")%>' <%=resource.getString("GML.MustBeFilled")%>\n";
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

function razData() {
	if (<%=isCreation%>) {
		document.connectionForm.Login.value = "";
		document.connectionForm.Password.value = "";
	}
}

function redirect() {
	document.redirectForm.action = "<%=m_context%>/RwebConnections/jsp/ExitRedirect";
	document.redirectForm.submit();
}

</script>

</head>
<body onload="javascript:razData()">
<%
if (isCreation) {
	browseBar.setComponentName(inst.getLabel() + " > " + resource.getString("webConnections.addConnection"));
}
else {
	browseBar.setComponentName(inst.getLabel() + " > " + resource.getString("webConnections.updateConnection"));
}

	Board board	= gef.getBoard();

	out.println(window.printBefore());
    out.println(frame.printBefore());
%>

	<% if (isAnonymousAccess) { %>
		<div class="inlineMessage"><%=resource.getString("webConnections.parametersWillNotBeStored")%></div><br clear="all"/>
	<% } %>

<%
    out.println(board.printBefore());
%>

<form name="connectionForm" method="post" action="<%=m_context%>/RwebConnections/jsp/<%=action%>">
<table cellpadding="5" width="100%">
	<tr>
		<td class="txtlibform"><%=resource.getString("GML.name") %> :</td>
		<td><%=inst.getLabel()%></td>
	</tr>
	<% if (StringUtil.isDefined(description)) { %>
		<tr>
		<td class="txtlibform"><%=resource.getString("GML.description") %> :</td>
		<td><%=description%></td>
	</tr>
	<% } %>
	<tr>
		<td class="txtlibform"><%=resource.getString("webConnections.login") %> :</td>
		<td><input type="text" name="Login" size="80" maxlength="100" value="<%=login%>"/>
			<img src="<%=resource.getIcon("webconnections.mandatory")%>" width="5" height="5"/>
		</td>
	</tr>
	<tr>
		<td class="txtlibform"><%=resource.getString("webConnections.password")%> :</td>
		<td><input type="password" name="Password" size="80" maxlength="100" value="<%=password%>"/>
			<input type="hidden" name="ConnectionId" value="<%=connection.getConnectionId()%>"/>
			<input type="hidden" name="ComponentId" value="<%=connection.getComponentId()%>"/>
		</td>
	</tr>
	<tr><td colspan="2">( <img src=<%=resource.getIcon("webconnections.mandatory")%> width="5" height="5"/> : <%=resource.getString("webconnections.mandatory")%> )</td></tr>
</table>
</form>

<%
	out.println(board.printAfter());
	ButtonPane buttonPane = gef.getButtonPane();
    buttonPane.addButton(validateButton);
    buttonPane.addButton(cancelButton);
	out.println("<br/><center>"+buttonPane.print()+"</center><br/>");
	out.println(frame.printAfter());
	out.println(window.printAfter());
%>
</body>

<form name="redirectForm" action="" method="post">
	<input type="hidden" name="ComponentId" value="<%=connection.getComponentId()%>"/>
</form>

</html>