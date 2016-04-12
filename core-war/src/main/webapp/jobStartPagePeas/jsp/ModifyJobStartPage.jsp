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

<%@ page import="org.silverpeas.web.jobstartpage.DisplaySorted" %>
<%@ page import="org.silverpeas.core.admin.space.SpaceInst" %>
<%@ page import="org.silverpeas.core.util.StringUtil" %>

<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<%@ include file="check.jsp" %>
<%
String spaceId = (String) request.getAttribute("CurrentSpaceId");

window.setPopup(true);
browseBar.setSpaceId(spaceId);
browseBar.setExtraInformation(resource.getString("JSPP.updateHomePage"));

	Integer m_firstPageType = (Integer)request.getAttribute("FirstPageType");
	String m_firstPageParam	= (String) request.getAttribute("FirstPageParam");
	if (m_firstPageParam == null || m_firstPageParam.equals("null"))
		m_firstPageParam = "";

	DisplaySorted[] m_Components = (DisplaySorted[])request.getAttribute("Peas");

	String 	defaultSP 	= "";
	String	peasSP 		= "";
	String 	portletSP 	= "";
	String	urlSP 		= "";
	switch (m_firstPageType.intValue())
	{
		case SpaceInst.FP_TYPE_STANDARD : 			defaultSP = "checked=\"checked\"";
													break;
		case SpaceInst.FP_TYPE_COMPONENT_INST : 	peasSP = "checked=\"checked\"";
													break;
		case SpaceInst.FP_TYPE_PORTLET : 			portletSP = "checked=\"checked\"";
													break;
		case SpaceInst.FP_TYPE_HTML_PAGE : 			urlSP = "checked=\"checked\"";
													break;
		default : defaultSP = "checked=\"checked\"";
	}
%>
<html>
<head>
<title><%=resource.getString("GML.popupTitle")%></title>
<view:looknfeel withCheckFormScript="true"/>
<style type="text/css">
.inlineMessage {
	text-align: left;
	margin-left: 0px;
	width: 70%;
	<% if (!StringUtil.isDefined(urlSP)) { %>
	display: none;
	<% } %>
}

.textePetitBold {
	width: 150px;
	vertical-align: top;
}

.textePetitBold input {
	vertical-align: bottom;
	border: 0px;
}
</style>
<script type="text/javascript">
function isCorrectForm() {
	var errorMsg = "";
    var errorNb = 0;
    var toCheck = document.multichoice.URL.value;
    if (isWhitespace(toCheck)) {
	errorMsg+="<%=resource.getString("GML.theField")%> '<%=resource.getString("JSPP.webPage")%>' <%=resource.getString("GML.MustBeFilled")%>\n";
	errorNb++;
    }

    switch(errorNb)
    {
	case 0 :
            result = true;
            break;
        default :
            window.alert(errorMsg);
            result = false;
            break;
     }
     return result;
}
function sendData()
{
	if ($('#url-radio').is(':checked')) {
		if (isCorrectForm()) {
			document.multichoice.submit();
		}
	} else {
		document.multichoice.submit();
	}
}

$(document).ready(function() {
	$('input:not([id="url-radio"][id="url-input"])').focus(function() {
		$('#urlhelp').hide("fast");
	});

	$('#url-radio').focus(function() {
		$('#urlhelp').show("fast");
	});
	$('#url-input').focus(function() {
		$('#urlhelp').show("fast");
	});
});


</script>
</head>
<body>
<%
out.println(window.printBefore());
out.println(frame.printBefore());
%>
<center>
<%
out.println(board.printBefore());
%>
<form name="multichoice" method="post" action="Choice">
<table cellpadding="2" cellspacing="0" border="0" width="100%">
	<tr>
		<td class="textePetitBold"><input type="radio" name="choix" value="DefaultStartPage" <%=defaultSP%>/> <%=resource.getString("JSPP.main") %></td>
		<td>&nbsp;</td>
	</tr>
	<% if (m_Components != null && m_Components.length > 0) {  %>
	<tr>
		<td class="textePetitBold"><input type="radio" name="choix" value="SelectPeas" <%=peasSP%>/> <%=resource.getString("JSPP.peas")%> :</td>
		<td>
			<select name="peas" size="1">
			<%
					String checked = "";
					for(int i = 0; i < m_Components.length; i++) {
						if (m_firstPageParam.equals(m_Components[i].id)) {
							checked = "selected=\"selected\"";
							m_firstPageParam = "";
						}
						out.println("<option value=\""+m_Components[i].id+"\" "+checked+">"+m_Components[i].name+"</option>");
						checked = "";
				    }
			%>
			</select>
		</td>
	</tr>
	<% } %>
	<tr>
		<td class="textePetitBold"><input type="radio" name="choix" value="Portlet" <%=portletSP%>/> <%=resource.getString("JSPP.portlet")%>...</td>
		<td>&nbsp;</td>
	</tr>
	<tr>
		<td class="textePetitBold"><input type="radio" name="choix" value="URL" id="url-radio" <%=urlSP%>/> <%=resource.getString("JSPP.webPage")%> :</td>
		<td>
			<input type="text" name="URL" size="60" maxlength="255" value="<%=m_firstPageParam%>" id="url-input"/>
			<div class="inlineMessage" id="urlhelp"><%=resource.getString("JSPP.SpaceHomepage.URL.help") %></div>
		</td>
	</tr>
</table>
</form>
<%
out.println(board.printAfter());
%>
<br/>
<%
 ButtonPane bouton = gef.getButtonPane();
 bouton.addButton((Button) gef.getFormButton(resource.getString("GML.validate"), "javascript:sendData();", false));
 out.println(bouton.print());
%>
</center>
<%
out.println(frame.printAfter());
out.println(window.printAfter());
%>
</body>
</html>