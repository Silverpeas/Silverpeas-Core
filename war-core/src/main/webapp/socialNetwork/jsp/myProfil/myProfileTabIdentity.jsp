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
    "http://www.silverpeas.org/legal/licensing"

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

--%>

<%@page import="com.stratelia.webactiv.beans.admin.DomainProperty"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>

<%@page import="com.stratelia.webactiv.util.ResourceLocator"%>
<%@page import="com.stratelia.silverpeas.util.ResourcesWrapper"%>
<%@page import="com.silverpeas.util.EncodeHelper"%>
<%@page import="com.stratelia.webactiv.util.viewGenerator.html.buttonPanes.ButtonPane"%>
<%@page import="com.stratelia.webactiv.util.viewGenerator.html.buttons.Button"%>
<%@page import="com.silverpeas.util.StringUtil"%>
<%@page import="com.silverpeas.socialnetwork.myProfil.servlets.MyProfileRoutes"%>

<%
ResourceLocator rs = new ResourceLocator("com.stratelia.silverpeas.personalizationPeas.settings.personalizationPeasSettings", "");
ResourcesWrapper resource = (ResourcesWrapper) request.getAttribute("resources");
ResourceLocator general = new ResourceLocator("com.stratelia.silverpeas.lookAndFeel.generalLook", "");

boolean 	updateIsAllowed				= ((Boolean) request.getAttribute("UpdateIsAllowed")).booleanValue();
boolean 	isAdmin						= ((Boolean) request.getAttribute("isAdmin")).booleanValue();
boolean 	isPasswordChangeAllowed		= ((Boolean) request.getAttribute("isPasswordChangeAllowed")).booleanValue();
Integer     minLengthPwd 				= (Integer) request.getAttribute("minLengthPwd");
Boolean		blanksAllowedInPwd 			= (Boolean) request.getAttribute("blanksAllowedInPwd");
String 		action 						= (String) request.getAttribute("Action");
String 		messageOK 					= (String) request.getAttribute("MessageOK");
String 		messageNOK 					= (String) request.getAttribute("MessageNOK");


String fieldAttribute = " disabled=\"disabled\" ";
if (updateIsAllowed) {
	fieldAttribute = "";
}

boolean updateFirstNameIsAllowed 	= rs.getBoolean("updateFirstName", false);
boolean updateLastNameIsAllowed 	= rs.getBoolean("updateLastName", false);
boolean updateEmailIsAllowed 		= rs.getBoolean("updateEmail", false);
boolean displayInfosLDAP			= rs.getBoolean("displayInfosLDAP", false);
%>

<div class="sousNavBulle">
	<p><fmt:message key="profil.subnav.display" /> : <a class="active" href="#"><fmt:message key="profil.subnav.identity" /></a> <!-- <a href="#">Personnelles</a> <a href="#">Personnelles</a> --></p>
</div>

<% if (StringUtil.isDefined(messageOK)) { %>
<div class="inlineMessage-ok">
	<%=messageOK %>
</div>
<% } else if (StringUtil.isDefined(messageNOK)) {%>
<div class="inlineMessage-nok">
	<%=messageNOK %>
</div>
<% } %>

<div id="identity">
<form name="UserForm" action="<%=MyProfileRoutes.UpdateMyInfos %>" method="post">
<table border="0" cellspacing="0" cellpadding="5" width="100%">
    <tr id="lastName">
        <td class="txtlibform"><%=resource.getString("GML.lastName")%> :</td>
        <td>
        	<%if (updateIsAllowed && updateLastNameIsAllowed) {%>
        		<input type="text" name="userLastName" size="50" maxlength="99" value="<%=userFull.getLastName()%>"/>&nbsp;<img src="<%=resource.getIcon("socialNetwork.mandatory")%>" width="5" height="5"/>
			<%} else {%>
				<%=userFull.getLastName()%>
			<%}%>
		</td>
    </tr>
    <tr id="firstName">
        <td class="txtlibform"><%=resource.getString("GML.firstName")%> :</td>
        <td>
        	<%if (updateIsAllowed && updateFirstNameIsAllowed) {%>
        		<input type="text" name="userFirstName" size="50" maxlength="99" value="<%=userFull.getFirstName()%>"/>
			<%} else {%>
				<%=userFull.getFirstName()%>
			<%}%>
		</td>
    </tr>
	<tr id="login">
        <td class="txtlibform"><%=resource.getString("GML.login")%> :</td>
		<td><%=userFull.getLogin()%></td>
    </tr>
    <tr id="email">
    	<td class="txtlibform"><%=resource.getString("GML.eMail")%> :</td>
        <td>
        	<%if (updateIsAllowed && updateEmailIsAllowed) {%>
        		<input type="text" name="userEMail" size="50" maxlength="99" value="<%=userFull.geteMail()%>"/>
			<%} else {%>
				<%=userFull.geteMail()%>
			<%}%>
		</td>
    </tr>
	<tr id="accessLevel">
		<td class="txtlibform"><%=resource.getString("myProfile.UserRights") %> :</td>
		<td>
			<input type="radio" name="userAccessLevel" value=A" <% if ((userFull.getAccessLevel() != null) && (userFull.getAccessLevel().equalsIgnoreCase("A"))) out.print("checked"); %> <% out.println("disabled"); %>/>&nbsp;<%=resource.getString("GML.admin") %><br/>
			<input type="radio" name="userAccessLevel" value=D" <% if ((userFull.getAccessLevel() != null) && (userFull.getAccessLevel().equalsIgnoreCase("D"))) out.print("checked"); %> <% out.println("disabled"); %>/>&nbsp;<%=resource.getString("GML.domainManager") %><br/>
			<input type="radio" name="userAccessLevel" value=K" <% if ((userFull.getAccessLevel() != null) && (userFull.getAccessLevel().equalsIgnoreCase("K"))) out.print("checked"); %> <% out.println("disabled"); %>/>&nbsp;<%=resource.getString("GML.kmmanager") %><br/>
			<input type="radio" name="userAccessLevel" value=U" <% if ((userFull.getAccessLevel() == null) || (userFull.getAccessLevel().equalsIgnoreCase("U"))) out.print("checked"); %> <% out.println("disabled"); %>/>&nbsp;<%=resource.getString("GML.user") %><br/>
			<input type="radio" name="userAccessLevel" value=G" <% if ((userFull.getAccessLevel() == null) || (userFull.getAccessLevel().equalsIgnoreCase("G"))) out.print("checked"); %> <% out.println("disabled"); %>/>&nbsp;<%=resource.getString("GML.guest") %>
		</td>
	</tr>
	<%if (updateIsAllowed && isPasswordChangeAllowed) {%>
		<tr id="oldPassword">
	        <td class="txtlibform"><%=resource.getString("myProfile.OldPassword")%> :</td>
	        <td><input <%=fieldAttribute%> type="password" name="OldPassword" size="50" maxlength="32"/></td>
	    </tr>
		<tr id="newPassword">
	        <td class="txtlibform"><%=resource.getString("myProfile.NewPassword")%> :</td>
	        <td><input <%=fieldAttribute%> type="password" name="NewPassword" size="50" maxlength="32"/>&nbsp;(<%=minLengthPwd.toString()%>&nbsp;<%=resource.getString("myProfile.LengthPwdLabel")%>)</td>
	    </tr>
		<tr id="newPasswordConfirmation">
	        <td class="txtlibform"><%=resource.getString("myProfile.NewPasswordConfirm")%> :</td>
	        <td><input <%=fieldAttribute%> type="password" name="NewPasswordConfirm" size="50" maxlength="32"/>&nbsp;(<%=minLengthPwd.toString()%>&nbsp;<%=resource.getString("myProfile.LengthPwdLabel")%>)</td>
	    </tr>
    <%} else { %>
	    <tr>
	        <td class="txtlibform"><%=resource.getString("myProfile.NewPassword")%> :</td>
	        <td><%=resource.getString("myProfile.ModifyPasswordNotAllowed1")+"<br/>"+resource.getString("myProfile.ModifyPasswordNotAllowed2")%></td>
	    </tr>
    <% }

    if ("personalQuestion".equals(general.getString("forgottenPwdActive"))) {
        String userLoginQuestion = userFull.getLoginQuestion();
        String userLoginAnswer = userFull.getLoginAnswer();
%>
        <tr id="question">
            <td class="txtlibform"><%=resource.getString("myProfile.LoginQuestion")%> :</td>
            <td><select name="userLoginQuestion">
                    <option value=""<%if ("".equals(userLoginQuestion)) {%> selected<%}%>></option><%

        int questionsCount = Integer.parseInt(general.getString("loginQuestion.count"));
        String question;
        for (int i = 1; i <= questionsCount; i++) {
            question = general.getString("loginQuestion." + i);
%>
                    <option value="<%=question%>"<%if (question.equals(userLoginQuestion)) {%> selected<%}%>><%=question%></option>
        <% } %>
                </select></td>
        </tr>
        <tr id="answer">
            <td class="txtlibform"><%=resource.getString("myProfile.LoginAnswer")%> :</td>
            <td><input type="text" name="userLoginAnswer" value="<%=userLoginAnswer%>" size="50" maxlength="99"/></td>
        </tr><%
    }
%>

	<%//rajout des champs LDAP complémentaires
	 if (displayInfosLDAP || action.equals("userModify")) {
		//rajout des champs Silverpeas custom complémentaires
		String[] properties = userFull.getPropertiesNames();
		String propertyName = null;
		for (int p=0; p<properties.length; p++) {
			propertyName = (String) properties[p];
			DomainProperty property=userFull.getProperty(propertyName);

			if (!propertyName.startsWith("password")) {
			%>
			<tr id="<%=propertyName%>">
				<td class="txtlibform"><%=userFull.getSpecificLabel(resource.getLanguage(), propertyName) %> :</td>
				<% if ( (userFull.isPropertyUpdatableByUser(propertyName)) || (isAdmin && userFull.isPropertyUpdatableByAdmin(propertyName)) ) { %>
					<td><input type="text" name="prop_<%=propertyName%>" size="50" maxlength="99" value="<%=EncodeHelper.javaStringToHtmlString(userFull.getValue(propertyName))%>"></td>
				<% } else { %>
					<td><%=EncodeHelper.javaStringToHtmlString(userFull.getValue(propertyName))%></td>
				<% } %>
			</tr>
			<%
			}
		}
	}
	%>
  </table>
 </form>
 </div>
 <%
		ButtonPane buttonPane = gef.getButtonPane();
		if (updateIsAllowed) {
			Button validateButton = gef.getFormButton(resource.getString("GML.validate"), "javascript:onClick=submitForm();", false);
			buttonPane.addButton(validateButton);
		}
		Button cancelButton = gef.getFormButton(resource.getString("GML.cancel"), "javascript:onClick=history.back();", false);
		buttonPane.addButton(cancelButton);
		out.println("<br/><center>"+buttonPane.print()+"</center>");
%>

<script type="text/javascript">
	function submitForm()
	{
		var errorMsg = "";
		<% if (updateLastNameIsAllowed) { %>
			var namefld = document.UserForm.userLastName.value;
			if (isWhitespace(namefld))
			{
				errorMsg = "- <%=resource.getString("GML.theField")%> '<%=resource.getString("GML.lastName")%>' <%=resource.getString("GML.MustBeFilled")%>\n";
			}
		<% } %>
		<% if (isPasswordChangeAllowed) {%>
		if (document.UserForm.NewPassword.value != document.UserForm.NewPasswordConfirm.value)
		{
			errorMsg += "- <%=resource.getString("myProfile.WrongNewPwd")%>\n";
		}
		if (<%=! blanksAllowedInPwd.booleanValue()%> && document.UserForm.NewPassword.value.indexOf(" ") != -1)
		{
			errorMsg += "- <%=resource.getString("myProfile.NoSpacesInPassword")%>\n";
		}
		else if ((document.UserForm.NewPassword.value.length > 0) &&
				(document.UserForm.NewPassword.value.length < <%=minLengthPwd.intValue()%>))
			errorMsg += "- <%=resource.getString("myProfile.WrongLength")%>\n";
		<%
		}
		%>
		if (errorMsg == "") {
			document.UserForm.submit();
		} else {
			window.alert(errorMsg);
		}
	}
</script>