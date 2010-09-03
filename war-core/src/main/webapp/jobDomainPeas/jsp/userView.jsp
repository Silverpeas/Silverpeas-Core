<%--

    Copyright (C) 2000 - 2009 Silverpeas

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    As a special exception to the terms and conditions of version 3.0 of
    the GPL, you may redistribute this Program in connection with Free/Libre
    Open Source Software ("FLOSS") applications as described in Silverpeas's
    FLOSS exception.  You should have received a copy of the text describing
    the FLOSS exception, and it is also available here:
    "http://repository.silverpeas.com/legal/licensing"

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
    Board board = gef.getBoard();

	Domain 		domObject 		= (Domain)request.getAttribute("domainObject");
    UserFull	userObject 		= (UserFull)request.getAttribute("UserFull");
      	
    browseBar.setDomainName(resource.getString("JDP.jobDomain"));
    
    if (domObject != null)
        browseBar.setComponentName(getDomainLabel(domObject, resource));
%>
<html>
<head>
<% out.println(gef.getLookStyleSheet()); %>
<title><%=userObject.getDisplayedName()%></title>
<script language="JavaScript">
function ConfirmAndSend(textToDisplay,targetURL)
{
    if (window.confirm(textToDisplay))
    {
        window.location.href = targetURL;
    }
}

function resizeMe() {
	<% 
		int height = 400;
		if (userObject!=null)
		{
			int nbProperty = userObject.getPropertiesNames().length;
			height = height+11*nbProperty;
		}
	%>
    window.resizeTo(550,<%=height%>);
}
</script>
</head>
<body marginheight="5" marginwidth="5" leftmargin="5" topmargin="5" bgcolor="#FFFFFF" onLoad="resizeMe();">
<%
out.println(window.printBefore());
out.println(frame.printBefore());
%>
<center>
<%
out.println(board.printBefore());
%>
<table CELLPADDING="5" CELLSPACING="0" BORDER="0" WIDTH="100%">
	<tr>
		<td class="textePetitBold"><%=resource.getString("GML.lastName") %> :</td>
		<td><%=EncodeHelper.javaStringToHtmlString(userObject.getLastName())%></td>
	</tr>
	<tr>
		<td class="textePetitBold"><%=resource.getString("GML.surname") %> :</td>
		<td><%=EncodeHelper.javaStringToHtmlString(userObject.getFirstName())%></td>
	</tr>
	<tr>
		<td class="textePetitBold"><%=resource.getString("GML.eMail") %> :</td>
		<td><%=EncodeHelper.javaStringToHtmlString(userObject.geteMail())%></td>
	</tr>
	<tr>
		<td class="textePetitBold"><%=resource.getString("GML.login") %> :</td>
		<td><%=EncodeHelper.javaStringToHtmlString(userObject.getLogin())%></td>
	</tr>
	
	<% 
        		String[] properties = userObject.getPropertiesNames();
				String property = null;
				for (int p=0; p<properties.length; p++)
				{
					property = (String) properties[p];
					if (!property.startsWith("password"))
					{
		%>
				<tr>			
					<td class="textePetitBold">
					<%=userObject.getSpecificLabel(resource.getLanguage(), property)%> :
					</td>
					
					<td>
					<%
				if("STRING".equals(userObject.getPropertyType(property)) ||
					"USERID".equals(userObject.getPropertyType(property))) {
					
					out.print(EncodeHelper.javaStringToHtmlString(userObject.getValue(property)));
		
				} else if("BOOLEAN".equals(userObject.getPropertyType(property))) {
					 
					 if (userObject.getValue(property) != null && "1".equals(userObject.getValue(property))) {
					 	out.print(resource.getString("GML.yes"));
					 } else if (userObject.getValue(property) == null || "".equals(userObject.getValue(property)) || "0".equals(userObject.getValue(property))) { 
					 	out.print(resource.getString("GML.no")); 
					 }
				}
		%>
					</td>
				</tr>
		<%
					}
				}
        %>
        
</table>
<%
out.println(board.printAfter());
ButtonPane bouton = gef.getButtonPane();
bouton.addButton((Button) gef.getFormButton(resource.getString("GML.close"), "javaScript:window.close();", false));
out.print("<BR/>");
out.print(bouton.print());
%>
<br/>
</center>
<%
out.println(frame.printAfter());
out.print(window.printAfter());
%>
</body>
</html>