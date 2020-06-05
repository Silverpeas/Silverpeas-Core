<%--

    Copyright (C) 2000 - 2020 Silverpeas

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
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

--%>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<%@ include file="check.jsp" %>

<%
	ConnectionDetail 	connection	= (ConnectionDetail) request.getAttribute("Connection");
	String 				action		= (String) request.getAttribute("Action");
	ComponentInst		inst		= (ComponentInst) request.getAttribute("ComponentInst");
	boolean				isAnonymousAccess = (Boolean) request.getAttribute("IsAnonymousAccess");

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

	Button validateButton;
	Button cancelButton = null;
  boolean isPopup = false;
  if (isCreation) {
		validateButton = gef.getFormButton(resource.getString("GML.validate"), "javascript:onClick=sendData();", false);
	} else {
    isPopup = true;
		validateButton = gef.getFormButton(resource.getString("GML.validate"), "javascript:onClick=updateData();", false);
		cancelButton = gef.getFormButton(resource.getString("GML.cancel"), "javascript:window.close()", false);
	}
  window.setPopup(isPopup);
%>

<html>
<head>
<view:looknfeel withCheckFormScript="true" withFieldsetStyle="true"/>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/dateUtils.js"></script>
<script language="javascript">

function sendData() {
	ifCorrectFormExecute(function() {
		document.connectionForm.submit();
  });
}

function updateData() {
  ifCorrectFormExecute(function() {
		window.opener.document.connectionForm.action = "<%=action%>";
		window.opener.document.connectionForm.Login.value = document.connectionForm.Login.value;
		window.opener.document.connectionForm.Password.value = document.connectionForm.Password.value;
		window.opener.document.connectionForm.ConnectionId.value = document.connectionForm.ConnectionId.value;
		window.opener.document.connectionForm.ComponentId.value = document.connectionForm.ComponentId.value;
		window.opener.document.connectionForm.submit();
		window.close();
	});
}

function ifCorrectFormExecute(callback) {
  var errorMsg 			= "";
  var errorNb 			= 0;
  var login 				= document.connectionForm.Login.value;

  if (isWhitespace(login)) {
    errorMsg +="  - <%=resource.getString("GML.theField")%> '<%=resource.getString("webConnections.login")%>' <%=resource.getString("GML.MustBeFilled")%>\n";
    errorNb++;
  }
  switch(errorNb) {
    case 0 :
      callback.call(this);
      break;
    case 1 :
      errorMsg = "<%=resource.getString("GML.ThisFormContains")%> 1 <%=resource.getString("GML.error")%> : \n" + errorMsg;
      jQuery.popup.error(errorMsg);
      break;
    default :
      errorMsg = "<%=resource.getString("GML.ThisFormContains")%> " + errorNb + " <%=resource.getString("GML.errors")%> :\n" + errorMsg;
      jQuery.popup.error(errorMsg);
  }
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

	out.println(window.printBefore());
  out.println(frame.printBefore());
%>

	<% if (isAnonymousAccess) { %>
		<div class="inlineMessage"><%=resource.getString("webConnections.parametersWillNotBeStored")%></div><br clear="all"/>
	<% } %>

<% if (isCreation) { %>
  <div class="inlineMessage"><%=resource.getString("webConnections.addConnection.explanation")%></div><br/>
<% } %>

<form name="connectionForm" method="post" action="<%=m_context%>/RwebConnections/jsp/<%=action%>">
  <fieldset class="skinFieldset">
    <legend><%=inst.getLabel()%></legend>
    <div class="oneFieldPerLine">
    <% if (StringUtil.isDefined(description)) { %>
      <div class="field">
        <label class="txtlibform"><%=resource.getString("GML.description") %></label>
        <div class="champs">
          <%=description%>
        </div>
      </div>
    <% } %>
    <div class="field">
      <label class="txtlibform"><%=resource.getString("webConnections.login") %></label>
      <div class="champs">
        <input type="text" name="Login" maxlength="100" value="<%=login%>"/>
        <img src="<%=resource.getIcon("webconnections.mandatory")%>" width="5" height="5"/>
      </div>
    </div>
    <div class="field">
      <label class="txtlibform"><%=resource.getString("webConnections.password") %></label>
      <div class="champs">
        <input type="password" autocomplete="off" name="Password" maxlength="100" value="<%=password%>"/>
        <input type="hidden" name="ConnectionId" value="<%=connection.getConnectionId()%>"/>
        <input type="hidden" name="ComponentId" value="<%=connection.getComponentId()%>"/>
      </div>
    </div>
    </div>
  </fieldset>
  <div class="legend">
    <img src="<%=resource.getIcon("webconnections.mandatory")%>" width="5" height="5"/> : <%=resource.getString("GML.mandatory")%>
  </div>
</form>

<%
	ButtonPane buttonPane = gef.getButtonPane();
  buttonPane.addButton(validateButton);
  if (cancelButton != null) {
    buttonPane.addButton(cancelButton);
  }
	out.println(buttonPane.print());
	out.println(frame.printAfter());
	out.println(window.printAfter());
%>

<form name="redirectForm" action="" method="post">
  <input type="hidden" name="ComponentId" value="<%=connection.getComponentId()%>"/>
</form>

</body>
</html>