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
	// Retrieve parameters
	LinkDetail 	link			= (LinkDetail) request.getAttribute("Link");
	Boolean		isVisibleB		= (Boolean) request.getAttribute("IsVisible");

	// Variable declaration
	boolean 	isVisible		= isVisibleB.booleanValue();
	
	int 		linkId 			= 0;
	String 		name 			= "";
	String 		description 	= "";
	String 		url				= "";
	boolean 	visible 		= false;
	boolean 	popup			= false;
	String		action 			= "CreateLink";
	
	// Retrieve data if update existing link
	if (link != null)
	{
		linkId 			= link.getLinkId();
		name 			= link.getName();
		description 	= link.getDescription();
		if (description == null)
			description = "";
		url				= link.getUrl();
		visible 		= link.isVisible();
		popup			= link.isPopup();
		action 			= "UpdateLink";
	}
	// Get buttons
	Button validateButton = (Button) gef.getFormButton(resource.getString("GML.validate"), "javascript:onClick=sendData();", false);
    Button cancelButton = (Button) gef.getFormButton(resource.getString("GML.cancel"), "javascript:window.close()", false);
	
%>

<html>
<head>

<%
	out.println(gef.getLookStyleSheet());
%>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/checkForm.js"></script>		
<script language="javascript">
	
function sendData() 
{
	if (isCorrectForm()) 
	{
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
		
function isCorrectForm() 
{
   	var errorMsg = "";
   	var errorNb = 0;
	var url = stripInitialWhitespace(document.linkForm.Url.value);
						     	
   	if (url == "")
   	{
   		errorMsg+="  - '<%=resource.getString("myLinks.url")%>'  <%=resource.getString("GML.MustBeFilled")%>\n";
       	errorNb++;
   	}
   	
   	
   	switch(errorNb) 
   	{
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
<body bgcolor="#ffffff" leftmargin="5" topmargin="5" marginwidth="5" marginheight="5" onLoad="javascript:document.linkForm.Url.focus();">
<%
	if (action.equals("CreateLink"))
		browseBar.setComponentName(resource.getString("myLinks.links") + resource.getString("myLinks.createLink"));
	else
		browseBar.setComponentName(resource.getString("myLinks.links") + resource.getString("myLinks.updateLink"));
		
	Board board	= gef.getBoard();
	
	out.println(window.printBefore());
    out.println(frame.printBefore());
    out.println(board.printBefore());
%>
<table CELLPADDING=5 WIDTH="100%">
<FORM Name="linkForm" method="post" action="">
	<tr>
		<td class="txtlibform"><%=resource.getString("myLinks.url")%> :</td>
		<TD><input type="text" name="Url" size="60" maxlength="150" value="<%=url%>">
		<IMG src="<%=resource.getIcon("myLinks.obligatoire")%>" width="5" height="5" border="0"></TD>
	</tr>
	<tr>
		<td class="txtlibform"><%=resource.getString("GML.nom")%> :</td>
		<TD><input type="text" name="Name" size="60" maxlength="150" value="<%=name%>" >
			<input type="hidden" name="LinkId" value="<%=linkId%>"> </td>
	</tr>
	<tr>
		<td class="txtlibform"> <%=resource.getString("GML.description")%> :</td>
		<TD><input type="text" name="Description" size="60" maxlength="150" value="<%=description%>" ></TD>
	</tr>
	<% if (isVisible) { %>
			<tr>
				<td class="txtlibform"> <%=resource.getString("myLinks.visible")%> :</td>
				<%
					String visibleCheck = "";
					if (visible)
					{
						visibleCheck = "checked";
					}
					
				%>
			    <td><input type="checkbox" name="Visible" value="true" <%=visibleCheck%>></td>
			</tr>
	<% } else { %>
		<input type="hidden" name="Visible" value="true">
	<% } %>
	<tr>
		<td class="txtlibform"> <%=resource.getString("myLinks.popup")%> :</td>
		<%
			String popupCheck = "";
			if (popup)
			{
				popupCheck = "checked";
			}
		%>
	    <td><input type="checkbox" name="Popup" value="true" <%=popupCheck%>></td>
	</tr>
  	<tr>
  		<td colspan="2">( <img border="0" src=<%=resource.getIcon("myLinks.obligatoire")%> width="5" height="5"> : Obligatoire )</td>
  	</tr>
</form>
</table>
<% 
	out.println(board.printAfter());
	ButtonPane buttonPane = gef.getButtonPane();
    buttonPane.addButton(validateButton);
    buttonPane.addButton(cancelButton);
	out.println("<BR><center>"+buttonPane.print()+"</center><BR>");
 	out.println(frame.printAfter());
	out.println(window.printAfter());
%>
</body>
</html>