<%--

    Copyright (C) 2000 - 2009 Silverpeas

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    As a special exception to the terms and conditions of version 3.0 of
    the GPL, you may redistribute this Program in connection with Free/Libre
    Open Source Software ("FLOSS") applications as described in Silverpeas's
    FLOSS exception.  You should have recieved a copy of the text describing
    the FLOSS exception, and it is also available here:
    "http://repository.silverpeas.com/legal/licensing"

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

--%>
<%@ include file="check.jsp" %>
<%
    Board board = gef.getBoard();

    UserFull 	userObject 	= (UserFull)request.getAttribute("userObject");
    String 		action 		= (String)request.getAttribute("action");
    String 		groupsPath 	= (String)request.getAttribute("groupsPath");
    Boolean 	userRW 		= (Boolean) request.getAttribute("isUserRW");
    Integer 	minLengthLogin = (Integer) request.getAttribute("minLengthLogin");
    Integer     minLengthPwd = (Integer) request.getAttribute("minLengthPwd");
    Boolean		blanksAllowedInPwd = (Boolean) request.getAttribute("blanksAllowedInPwd");
    UserDetail	currentUser = (UserDetail) request.getAttribute("CurrentUser");
    
    boolean bUserRW = false;
    if (userRW != null)
    	bUserRW = userRW.booleanValue();

    browseBar.setDomainName(resource.getString("JDP.jobDomain"));
    browseBar.setComponentName(Encode.javaStringToHtmlString((String)request.getAttribute("domainName")), (String)request.getAttribute("domainURL"));
    browseBar.setPath(groupsPath);
%>
<html>
<head>
<% out.println(gef.getLookStyleSheet()); %>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/checkForm.js"></script>
<script language="JavaScript">
function SubmitWithVerif(verifParams)
{
    var namefld = stripInitialWhitespace(document.userForm.userLastName.value);
    var errorMsg = "";

    if (verifParams)
    {
         if (isWhitespace(namefld)) 
            errorMsg += "- <%=resource.getString("JDP.missingFieldStart")+resource.getString("GML.lastName")+resource.getString("JDP.missingFieldEnd")%>\n";
         
         <% if (action.equals("userCreate")) { %>
         	var loginfld = stripInitialWhitespace(document.userForm.userLogin.value);
	         if (isWhitespace(loginfld)) 
	            errorMsg += "- <%=resource.getString("JDP.missingFieldStart")+resource.getString("GML.login")+resource.getString("JDP.missingFieldEnd")%>\n";
	         else if(loginfld.length < <%=minLengthLogin.intValue()%>) 
	         	errorMsg += "- <%=resource.getString("JDP.missingFieldStart")+resource.getString("GML.login")+resource.getString("JDP.minLength")+" "+minLengthLogin.toString()+" "+resource.getString("JDP.caracteres")%>\n";
	     <% } %>
	     
	     <% if (userObject.isPasswordAvailable()) { %>
	     	var passwordfld = stripInitialWhitespace(document.userForm.userPassword.value);
	     	if (<%=! blanksAllowedInPwd.booleanValue()%> && passwordfld.indexOf(" ") != -1) 
	     		errorMsg += "- <%=resource.getString("JDP.missingFieldStart")+resource.getString("GML.password")+resource.getString("JDP.noSpaces")%>\n";
			
	        if(passwordfld.length < <%=minLengthPwd.intValue()%>) 
	         	errorMsg += "- <%=resource.getString("JDP.missingFieldStart")+resource.getString("GML.password")+resource.getString("JDP.minLength")+" "+minLengthPwd.toString()+" "+resource.getString("JDP.caracteres")%>\n";
	     <% } %>
    }
    if (errorMsg == "")
    {
        document.userForm.submit();
    }
    else
    {
        window.alert(errorMsg);
    }
}
<% if (userObject.isPasswordAvailable()) 
   {
%>
	function selectUnselect()
	{
	    var bSelected;
	
	    bSelected = document.userForm.userPasswordValid.checked;
	    if (bSelected)
	    {
	        document.userForm.userPassword.disabled = false;
	    }
	    else
	    {
	        document.userForm.userPassword.disabled = true;
	    }
	}
<%
   }
   else
   {
%>  
	function selectUnselect()
	{
	}
<%	
   }
%>	

</script>
</head>
<body marginheight=5 marginwidth=5 leftmargin=5 topmargin=5 bgcolor="#FFFFFF" onload="javascript:selectUnselect()">

<%
out.println(window.printBefore());
out.println(frame.printBefore());
%>
<center>
<%
out.println(board.printBefore());
%>
<form name="userForm" action="<%=action%>" method="POST">
    <input type="hidden" name="Iduser" value="<% if (userObject.getId() != null) out.print(userObject.getId()); %>">
    <table CELLPADDING=5 CELLSPACING=0 BORDER=0 WIDTH="100%">
    	<tr>			
        	<td valign="baseline" align=left class="txtlibform"><%=resource.getString("GML.lastName") %> :</td>
            <td align=left valign="baseline">
            	<input type="text" name="userLastName" size="50" maxlength="99" VALUE="<%=Encode.javaStringToHtmlString(userObject.getLastName())%>" <% if (action.equals("userMS")) out.println("disabled"); %>>&nbsp;<img border="0" src="<%=resource.getIcon("JDP.mandatory")%>" width="5" height="5"> 
            </td>
        </tr>
        <tr>			
        	<td valign="baseline" align=left class="txtlibform"><%=resource.getString("GML.surname") %> :</td>
            <td align=left valign="baseline">
	        	<input type="text" name="userFirstName" size="50" maxlength="99" VALUE="<%=Encode.javaStringToHtmlString(userObject.getFirstName())%>" <% if (action.equals("userMS")) out.println("disabled"); %>> 
            </td>
        </tr>
        <tr>			
        	<td valign="baseline" align=left class="txtlibform"><%=resource.getString("GML.login") %> :</td>
            <td align=left valign="baseline">
            	<% if (action.equals("userCreate")) { %>
                	<input type="text" name="userLogin" size="50" maxlength="50" VALUE="<%=Encode.javaStringToHtmlString(userObject.getLogin())%>"> &nbsp;<img border="0" src="<%=resource.getIcon("JDP.mandatory")%>" width="5" height="5">
                <% } else { %>
                	<%=Encode.javaStringToHtmlString(userObject.getLogin())%>
                <% } %>
            </td>
        </tr>
        <tr>			
            <td valign="baseline" align=left class="txtlibform"><%=resource.getString("GML.eMail") %> :</td>
            <td align=left valign="baseline">
                <input type="text" name="userEMail" size="50" maxlength="99" VALUE="<%=Encode.javaStringToHtmlString(userObject.geteMail())%>" <% if (action.equals("userMS")) out.println("disabled"); %>> 
            </td>
        </tr>
        <tr>
            <td valign="baseline" align=left class="txtlibform"><%=resource.getString("JDP.userRights") %> :</td>
            <td align=left valign="baseline">
            	<% if (currentUser.getAccessLevel() != null && currentUser.getAccessLevel().equalsIgnoreCase("A")) { %>
                	<INPUT TYPE="radio" NAME="userAccessLevel" VALUE="A" <% if ((userObject.getAccessLevel() != null) && (userObject.getAccessLevel().equalsIgnoreCase("A"))) out.print("checked"); %>>&nbsp;<%=resource.getString("GML.administrateur") %><br>
                	<INPUT TYPE="radio" NAME="userAccessLevel" VALUE="K" <% if ((userObject.getAccessLevel() != null) && (userObject.getAccessLevel().equalsIgnoreCase("K"))) out.print("checked"); %>>&nbsp;<%=resource.getString("GML.kmmanager") %><br>
                <% } %>
                <% if (currentUser.getAccessLevel() != null && (currentUser.getAccessLevel().equalsIgnoreCase("A") || currentUser.getAccessLevel().equalsIgnoreCase("D"))) { %>
                	<INPUT TYPE="radio" NAME="userAccessLevel" VALUE="D" <% if ((userObject.getAccessLevel() != null) && (userObject.getAccessLevel().equalsIgnoreCase("D"))) out.print("checked"); %>>&nbsp;<%=resource.getString("GML.domainManager") %><br>
                	<INPUT TYPE="radio" NAME="userAccessLevel" VALUE="U" <% if ((userObject.getAccessLevel() == null) || (userObject.getAccessLevel().length() <= 0) || (userObject.getAccessLevel().equalsIgnoreCase("U"))) out.print("checked"); %>>&nbsp;<%=resource.getString("GML.user") %><br>
                	<INPUT TYPE="radio" NAME="userAccessLevel" VALUE="G" <% if ((userObject.getAccessLevel() != null) && (userObject.getAccessLevel().equalsIgnoreCase("G"))) out.print("checked"); %>>&nbsp;<%=resource.getString("GML.guest") %>
                <% } %>
            </td>
        </tr>
        <% if (userObject.isPasswordAvailable()) { %>
            <tr>			
                <td valign="baseline" align=left class="txtlibform"><%=resource.getString("JDP.silverPassword") %> :</td>
                <td align=left valign="baseline">
                    <INPUT TYPE="checkbox" NAME="userPasswordValid" VALUE="true" <% if (userObject.isPasswordValid()) out.print("checked"); %> onclick="javascript:selectUnselect()">&nbsp;<%=resource.getString("GML.yes") %><br>
                </td>
            </tr>
            <tr>			
                <td valign="baseline" align=left class="txtlibform"><%=resource.getString("GML.password") %> :</td>
                <td align=left valign="baseline">
                    <input type="password" name="userPassword" size="50" maxlength="32" VALUE="<%=Encode.javaStringToHtmlString(userObject.getPassword())%>"> 
                </td>
            </tr>
        <% } %>
        
        <% 
        	if (bUserRW) 
        	{
        		String[] properties = userObject.getPropertiesNames();
				String property = null;
				for (int p=0; p<properties.length; p++)
				{
					property = (String) properties[p];
					if (!property.startsWith("password"))
					{
		%>
				<tr>			
					<td valign="baseline" align=left class="txtlibform"><%=userObject.getSpecificLabel(resource.getLanguage(), property) %> :</td>
					
		<%
				if("STRING".equals(userObject.getPropertyType(property)) ||
					"USERID".equals(userObject.getPropertyType(property))) {
		%>
					<td align="left" valign="baseline"><input type="text" name="prop_<%=property%>" size="50" maxlength="50" value="<%=Encode.javaStringToHtmlString(userObject.getValue(property))%>"></td>
		<%
				} else if("BOOLEAN".equals(userObject.getPropertyType(property))) {
		%>
					<td align="left" valign="baseline">
						<input type="radio" name="prop_<%=property%>" value="1" <% if (userObject.getValue(property) != null && "1".equals(userObject.getValue(property))) out.print("checked"); %>><%=resource.getString("GML.yes") %>
						<input type="radio" name="prop_<%=property%>" value="0" <% if (userObject.getValue(property) == null || "".equals(userObject.getValue(property)) || "0".equals(userObject.getValue(property))) out.print("checked"); %>><%=resource.getString("GML.no") %>
					</td>
		<%
				}
		%>							
				</tr>
		<%
					}
				}
        	}
        %>
        
		<tr> 
        	<td colspan="2">(<img border="0" src="<%=resource.getIcon("JDP.mandatory")%>" width="5" height="5"> : <%=resource.getString("GML.requiredField")%>)</td>
        </tr>
    </table>
<%
out.println(board.printAfter());
%>
</form>
<br/>
		<%
		  ButtonPane bouton = gef.getButtonPane();
		  bouton.addButton((Button) gef.getFormButton(resource.getString("GML.validate"), "javascript:SubmitWithVerif(true)", false));
          bouton.addButton((Button) gef.getFormButton(resource.getString("GML.cancel"), "domainContent", false));
		  out.println(bouton.print());
		%>
</center>
<%
out.println(frame.printAfter());
out.println(window.printAfter());
%>
</body>
</html>