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
<%@ page import="java.util.HashMap"%>
<%@ include file="checkPersonalization.jsp" %>

<%
UserFull 	userObject 			= (UserFull) request.getAttribute("userObject");
String 		action 				= (String) request.getAttribute("action");
boolean 	updateIsAllowed		= ((Boolean) request.getAttribute("UpdateIsAllowed")).booleanValue();
Integer     minLengthPwd 		= (Integer) request.getAttribute("minLengthPwd");
Boolean		blanksAllowedInPwd 	= (Boolean) request.getAttribute("blanksAllowedInPwd");
String 		message 			= (String) request.getAttribute("Message");
	
String language = resource.getLanguage();

String fieldAttribute = " disabled ";
if (updateIsAllowed)
	fieldAttribute = "";

boolean updateFirstNameIsAllowed 	= rs.getBoolean("updateFirstName", false);
boolean updateLastNameIsAllowed 	= rs.getBoolean("updateLastName", false);
boolean updateEmailIsAllowed 		= rs.getBoolean("updateEmail", false);
boolean displayInfosLDAP			= rs.getBoolean("displayInfosLDAP", false);

%>
<HTML>
<HEAD>
<TITLE><%=resource.getString("GML.popupTitle")%></TITLE>
<%
out.println(gef.getLookStyleSheet());
%>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/checkForm.js"></script>
<script language="javascript">
	function SubmitForm()
	{
		var errorMsg = "";
		<% if (updateLastNameIsAllowed) { %>
			var namefld = document.UserForm.userLastName.value;
			if (isWhitespace(namefld))
			{
				errorMsg = "- <%=resource.getString("GML.theField")%> '<%=resource.getString("GML.lastName")%>' <%=resource.getString("GML.MustBeFilled")%>\n";
			}
		<% } %>
		if (document.UserForm.NewPassword.value != document.UserForm.NewPasswordConfirm.value)
		{
			errorMsg += "- <%=resource.getString("WrongNewPwd")%>\n";
		}
		if (<%=! blanksAllowedInPwd.booleanValue()%> && document.UserForm.NewPassword.value.indexOf(" ") != -1)
		{
			errorMsg += "- <%=resource.getString("NoSpacesInPassword")%>\n";
		}
		else if ((document.UserForm.NewPassword.value.length > 0) &&
				(document.UserForm.NewPassword.value.length < <%=minLengthPwd.intValue()%>))
			errorMsg += "- <%=resource.getString("WrongLength")%>\n";

		if (errorMsg == "")
			document.UserForm.submit();
		else
			window.alert(errorMsg);
	}
</script>
</HEAD>
<BODY MARGINHEIGHT="5" MARGINWIDTH="5" TOPMARGIN="5" LEFTMARGIN="5">
<%
	browseBar.setComponentName(resource.getString("PersonalizationTitleTab2"));
	out.println(window.printBefore());

	TabbedPane tabbedPane = gef.getTabbedPane();
    tabbedPane.addTab(resource.getString("Preferences"), "Main", false);
    tabbedPane.addTab(resource.getString("Identity"), "#", true);
    out.println(tabbedPane.print());

	out.println(frame.printBefore());
	out.println(board.printBefore());
%>

	<table border="0" cellspacing="0" cellpadding="5" width="100%">
	<form name="UserForm" Action="EffectiveChangePassword" Method="POST">
    <tr>
        <td class="txtlibform" valign="baseline" width="300"><%=resource.getString("GML.lastName")%> :</td>
        <td valign="baseline">
        	<%if (updateIsAllowed && updateLastNameIsAllowed) {%>
        		<input type="text" name="userLastName" size="50" maxlength="99" VALUE="<%=Encode.javaStringToHtmlString(userObject.getLastName())%>">&nbsp;<img border="0" src="<%=resource.getIcon("PSP.mandatory")%>" width="5" height="5">
			<%} else {%>
				<%=userObject.getLastName()%>
			<%}%>
		</td>     

        </td>
    </tr>
    <tr>
        <td class="txtlibform" valign="baseline" width="300"><%=resource.getString("GML.firstName")%> :</td>
        <td valign="baseline">
        	<%if (updateIsAllowed && updateFirstNameIsAllowed) {%>
        		<input type="text" name="userFirstName" size="50" maxlength="99" VALUE="<%=Encode.javaStringToHtmlString(userObject.getFirstName())%>">
			<%} else {%>
				<%=userObject.getFirstName()%>
			<%}%>
		</td>   
    </tr>
	<tr>
        <td class="txtlibform" valign="baseline"><%=resource.getString("GML.login")%> :</td>
		<td valign="baseline"><%=userObject.getLogin()%></td>     
    </tr>
    <tr>	
    	<td class="txtlibform" valign="baseline" ><%=resource.getString("GML.eMail")%> :</td>
        <td valign="baseline">
        	<%if (updateIsAllowed && updateEmailIsAllowed) {%>
        		<input type="text" name="userEMail" size="50" maxlength="99" VALUE="<%=Encode.javaStringToHtmlString(userObject.geteMail())%>">
			<%} else {%>
				<%=userObject.geteMail()%>
			<%}%>
		</td>
    </tr>
	<tr>			
		<td class="txtlibform" valign="baseline"><%=resource.getString("UserRights") %> :</td>
		<td valign="baseline">
			<INPUT TYPE="radio" NAME="userAccessLevel" VALUE="A" <% if ((userObject.getAccessLevel() != null) && (userObject.getAccessLevel().equalsIgnoreCase("A"))) out.print("checked"); %> <% out.println("disabled"); %>>&nbsp;<%=resource.getString("GML.admin") %><br>
			<INPUT TYPE="radio" NAME="userAccessLevel" VALUE="D" <% if ((userObject.getAccessLevel() != null) && (userObject.getAccessLevel().equalsIgnoreCase("D"))) out.print("checked"); %> <% out.println("disabled"); %>>&nbsp;<%=resource.getString("GML.domainManager") %><br>
			<INPUT TYPE="radio" NAME="userAccessLevel" VALUE="K" <% if ((userObject.getAccessLevel() != null) && (userObject.getAccessLevel().equalsIgnoreCase("K"))) out.print("checked"); %> <% out.println("disabled"); %>>&nbsp;<%=resource.getString("GML.kmmanager") %><br>
			<INPUT TYPE="radio" NAME="userAccessLevel" VALUE="U" <% if ((userObject.getAccessLevel() == null) || (userObject.getAccessLevel().equalsIgnoreCase("U"))) out.print("checked"); %> <% out.println("disabled"); %>>&nbsp;<%=resource.getString("GML.user") %><br>
			<INPUT TYPE="radio" NAME="userAccessLevel" VALUE="G" <% if ((userObject.getAccessLevel() == null) || (userObject.getAccessLevel().equalsIgnoreCase("G"))) out.print("checked"); %> <% out.println("disabled"); %>>&nbsp;<%=resource.getString("GML.guest") %>
		</td>
	</tr>
	<%if (updateIsAllowed) {%>
		<tr>
	        <td valign="baseline" class="txtlibform"><%=resource.getString("OldPassword")%> :</td>
	        <td valign="baseline"><input <%=fieldAttribute%> type="password" name="OldPassword" size="50" maxlength="32"></td>
	    </tr>
		<tr>
	        <td valign="baseline" class="txtlibform"><%=resource.getString("NewPassword")%> :</td>
	        <td valign="baseline"><input <%=fieldAttribute%> type="password" name="NewPassword" size="50" maxlength="32">&nbsp;(<%=minLengthPwd.toString()%>&nbsp;<%=resource.getString("LengthPwdLabel")%>)</td>
	    </tr>
		<tr>
	        <td valign="baseline" class="txtlibform"><%=resource.getString("NewPasswordConfirm")%> :</td>
	        <td valign="baseline"><input <%=fieldAttribute%> type="password" name="NewPasswordConfirm" size="50" maxlength="32">&nbsp;(<%=minLengthPwd.toString()%>&nbsp;<%=resource.getString("LengthPwdLabel")%>)</td>
	    </tr>
    <%} else { %>
	    <tr>
	        <td valign="top" class="txtlibform"><%=resource.getString("NewPassword")%> :</td>
	        <td valign="baseline"><%=resource.getString("ModifyPasswordNotAllowed1")+"<br>"+resource.getString("ModifyPasswordNotAllowed2")%></td>
	    </tr>
    <% } 

    if ("personalQuestion".equals(general.getString("forgottenPwdActive"))) {
        String userLoginQuestion = userObject.getLoginQuestion();
        String userLoginAnswer = userObject.getLoginAnswer();
%>
        <tr>
            <td class="txtlibform" valign="baseline"><%=resource.getString("LoginQuestion")%> :</td>
            <td valign="baseline"><select name="userLoginQuestion">
                    <option value=""<%if ("".equals(userLoginQuestion)) {%> selected<%}%>></option><%

        int questionsCount = Integer.parseInt(general.getString("loginQuestion.count"));
        String question;
        for (int i = 1; i <= questionsCount; i++)
        {
            question = general.getString("loginQuestion." + i);
%>
                    <option value="<%=question%>"<%if (question.equals(userLoginQuestion)) {%> selected<%}%>><%=question%></option><%

        }
%>
                </select></td>     
        </tr>
        <tr>
            <td class="txtlibform" valign="baseline"><%=resource.getString("LoginAnswer")%> :</td>
            <td valign="baseline"><input type="text" name="userLoginAnswer" value="<%=userLoginAnswer%>" size="50" maxlength="99"/></td>
        </tr><%

    }
%>

	<%//rajout des champs LDAP complémentaires
	if (displayInfosLDAP && action.equals("userMS")) {
		String[] properties = userObject.getPropertiesNames();
		String property = null;
		for (int p=0; p<properties.length; p++)
		{
			property = (String) properties[p];
			%>
			<tr>			
				<td valign="baseline" class="txtlibform"><%=userObject.getSpecificLabel(language, property) %> :</td>
				<td valign="baseline"><%=Encode.javaStringToHtmlString(userObject.getValue(property))%></td>     
			</tr>
			<%
		}
	} else if (action.equals("userModify")) { 
		//rajout des champs Silverpeas custom complémentaires
		String[] properties = userObject.getPropertiesNames();
		String property = null;
		for (int p=0; p<properties.length; p++)
		{
			property = (String) properties[p];
			if (!property.startsWith("password"))
			{
			%>
			<tr>			
				<td valign="baseline" class="txtlibform"><%=userObject.getSpecificLabel(resource.getLanguage(), property) %> :</td>
				<% if (userObject.isPropertyUpdatableByUser(property)) { %>
					<td valign="baseline"><input type="text" name="prop_<%=property%>" size="50" maxlength="99" value="<%=Encode.javaStringToHtmlString(userObject.getValue(property))%>"></td>
				<% } else { %>
					<td valign="baseline"><%=Encode.javaStringToHtmlString(userObject.getValue(property))%></td>
				<% } %>
			</tr>
			<%
			}
		}
	}
	%>

	<% if (message != null) { %>
	<tr>
		<td align="center" valign="baseline" colspan="2">
			<%=message%>
		</td>
    </tr>
    <% } %>
    </form>
  </table>
<center>
<%
		out.println(board.printAfter());

		ButtonPane buttonPane = gef.getButtonPane();
		if (updateIsAllowed)
		{
			Button validateButton = gef.getFormButton(resource.getString("GML.validate"), "javascript:onClick=SubmitForm();", false);
			buttonPane.addButton(validateButton);
		}
		Button cancelButton = gef.getFormButton(resource.getString("GML.cancel"), "javascript:onClick=history.back();", false);
		buttonPane.addButton(cancelButton);
		out.println("<BR><center>"+buttonPane.print()+"</center>");
		out.println(frame.printAfter());
		out.println(window.printAfter());
%>
</center>
</BODY>
</HTML>