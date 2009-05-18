<%@ include file="checkPdc.jsp"%>

<%@page import="com.stratelia.webactiv.beans.admin.UserDetail"%>

<%
UserDetail user = (UserDetail) request.getAttribute("UserDetail");
%>

<HTML>
<HEAD>
<TITLE><%=resource.getString("GML.popupTitle")%></TITLE>
<%
   out.println(gef.getLookStyleSheet());
%>
<script language="JavaScript">

function refresh() 
{
	<% if (user != null) { %>
		/*var listUsers = window.opener.document.getElementById("listUsers");
		var option = window.opener.document.createElement("option");
		option.setAttribute("value", "<%=user.getId()%>");
		option.setAttribute("selected", "selected");
		option.innerHTML = "<%=user.getDisplayedName()%>";
		listUsers.appendChild(option);

		var callUserPanel = window.opener.document.getElementById("callUserPanel");

		var userIcon = window.opener.document.getElementById("userIcon");
		callUserPanel.removeChild(userIcon);
		
		var img = window.opener.document.createElement("img");
		img.setAttribute("src", "<%=m_context + "/util/icons/user.gif"%>");
		img.setAttribute("id", "userIcon");
		img.setAttribute("alt", "<%=resource.getString("pdcPeas.openUserPanelPeas")%>");
		img.setAttribute("align", "absmiddle");
		img.setAttribute("border", "0");
		callUserPanel.appendChild(img);*/

		var userName = window.opener.document.getElementById("userName");
		userName.innerHTML = "<%=user.getDisplayedName()%>";

		var userId = window.opener.document.getElementById("userId");
		userId.setAttribute("value", "<%=user.getId()%>");
		
		window.opener.document.getElementById("deleteURL").style.visibility = "visible";
		
	<% } %>
	
	window.close();
}
</script>
</HEAD>
<BODY onLoad="refresh()">
</BODY>
</HTML>