<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.buttons.Button" %>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.frame.Frame" %><%--

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
<%
response.setHeader("Cache-Control","no-store"); //HTTP 1.1
response.setHeader("Pragma","no-cache"); //HTTP 1.0
response.setDateHeader ("Expires",-1); //prevents caching at the proxy server
%>
<%@ include file="import.jsp" %>
<%
//R�cup�ration des param�tres
String action = request.getParameter("action");
String messagePopup = request.getParameter("messagePopup");
String detailedMessagePopup = request.getParameter("detailedMessagePopup");
String pilePopup = request.getParameter("pilePopup");
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
   "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title><%= generalMessage.getString("GML.popupTitle")%></title>
<view:looknfeel/>
<script type="text/javascript">
function resizePopup(largeur,hauteur){
	window.resizeTo(largeur,hauteur);
}
</script>
</head>
<%
	if (action != null && action.equals("detail")) { %>
<body onload="javascript:resizePopup(650,400);">
<%
          Window window = gef.getWindow();
					out.println(window.printBefore());
					Frame frame = gef.getFrame();
					out.println(frame.printBefore());
%>
<center>
<form name="formPopup" action="popupError.jsp" method="post">
	<input type="hidden" name="action" value="minimize"/>
	<input type="hidden" name="messagePopup" value="<%=messagePopup %>"/>
	<input type="hidden" name="detailedMessagePopup" value="<%=detailedMessagePopup %>"/>
	<input type="hidden" name="pilePopup" value="<%=pilePopup %>"/>
<table cellpadding="0" cellspacing="2" border="0" width="98%" class="intfdcolor">
	<tr>
		<td class="intfdcolor4">
			<center>
				<br/>
				<span class="txtnav">
				<%=detailedMessagePopup %>
				</span>
				<br/><br/>
				<textarea rows="12" cols="90" name="pile"><%=pilePopup %></textarea>
			</center>
			<br/>
		</td>
	</tr>
</table>
</form>
<br/>
<%
	ButtonPane buttonPane = gef.getButtonPane();
	buttonPane.addButton((Button) gef.getFormButton(generalMessage.getString("GML.close"), "javascript:window.close();", false));
	buttonPane.addButton((Button) gef.getFormButton(generalMessage.getString("GML.minimize"), "javascript:document.formPopup.submit();", false));
	out.println(buttonPane.print());
%>
</center>
<%
	out.println(frame.printAfter());
	out.println(window.printAfter());
%>
</body>
<%}
// Affichage minimum
else if (action != null && action.equals("minimize")){
 %>
<body onload="javascript:resizePopup(650,180);">
<%
	Window window = gef.getWindow();
	out.println(window.printBefore());
	Frame frame = gef.getFrame();
	out.println(frame.printBefore());
%>
<center>
<form name="formPopup" action="popupError.jsp" method="post">
	<input type="hidden" name="action" value="detail"/>
	<input type="hidden" name="messagePopup" value="<%=messagePopup %>"/>
	<input type="hidden" name="detailedMessagePopup" value="<%=detailedMessagePopup %>"/>
	<input type="hidden" name="pilePopup" value="<%=pilePopup %>"/>
<table cellpadding="0" cellspacing="2" border="0" width="98%" class="intfdcolor">
	<tr>
		<td class="intfdcolor4">
			<center>
				<br/>
				<span class="txtnav">
				<%=messagePopup %>
				</span>
			</center>
			<br/>
		</td>
	</tr>
</table>
</form>
<br/>
<%
	ButtonPane buttonPane = gef.getButtonPane();
	buttonPane.addButton((Button) gef.getFormButton(generalMessage.getString("GML.close"), "javascript:window.close();", false));
	buttonPane.addButton((Button) gef.getFormButton(generalMessage.getString("GML.detail"), "javascript:document.formPopup.submit()", false));
	out.println(buttonPane.print());
%>
</center>
<%
	out.println(frame.printAfter());
	out.println(window.printAfter());
%>
</body>
<%}

// Premier affichage
else {
 %>
<body>
<%
	Window window = gef.getWindow();
	out.println(window.printBefore());
	Frame frame = gef.getFrame();
	out.println(frame.printBefore());
%>
<center>
<form name="formPopup" action="popupError.jsp" method="post">
	<input type="hidden" name="action" value="detail"/>
	<input type="hidden" name="messagePopup" value=""/>
	<input type="hidden" name="detailedMessagePopup" value="<%=detailedMessagePopup %>"/>
	<input type="hidden" name="pilePopup" value=""/>
<table cellpadding="0" cellspacing="2" border="0" width="98%" class="intfdcolor">
	<tr>
		<td class="intfdcolor4" nowrap="nowrap">
			<center>
				<br/>
				<span class="txtnav">
				<script language="JavaScript">
					window.document.write(window.opener.document.formulaire.message.value);
				</script>
				</span>
			</center>
			<br/>
		</td>
	</tr>
</table>
</form>
<br/>
<script type="text/javascript">
	window.document.formPopup.pilePopup.value = window.opener.document.formulaire.pile.value;
	window.document.formPopup.messagePopup.value = window.opener.document.formulaire.message.value;
	window.document.formPopup.detailedMessagePopup.value = window.opener.document.formulaire.detailedMessage.value;
    <% if ((action == null) || (action.equals("NOBack") == false)){  %>
	    window.opener.document.formulaire.submit();
    <%} %>
</script>
<%
		  ButtonPane buttonPane = gef.getButtonPane();
		  buttonPane.addButton((Button) gef.getFormButton(generalMessage.getString("GML.close"), "javascript:window.close()", false));
			buttonPane.addButton((Button) gef.getFormButton(generalMessage.getString("GML.detail"), "javascript:document.formPopup.submit();", false));
		  out.println(buttonPane.print());
%>
</center>
<%
	out.println(frame.printAfter());
	out.println(window.printAfter());
%>
</body>
<%
}
%>
</html>