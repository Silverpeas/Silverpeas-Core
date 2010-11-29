<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
    
<%@page import="com.stratelia.webactiv.util.ResourceLocator"%>
<%@page import="com.stratelia.silverpeas.util.ResourcesWrapper"%>
<%@page import="com.silverpeas.util.EncodeHelper"%>
<%@page import="com.stratelia.webactiv.util.viewGenerator.html.buttonPanes.ButtonPane"%>
<%@page import="com.stratelia.webactiv.util.viewGenerator.html.buttons.Button"%>
<%@page import="com.silverpeas.util.StringUtil"%>
    
<%
ResourceLocator rs = new ResourceLocator("com.stratelia.silverpeas.personalizationPeas.settings.personalizationPeasSettings", "");
ResourcesWrapper resource = (ResourcesWrapper) request.getAttribute("resources");
ResourceLocator general = new ResourceLocator("com.stratelia.silverpeas.lookAndFeel.generalLook", "");

boolean 	updateIsAllowed		= ((Boolean) request.getAttribute("UpdateIsAllowed")).booleanValue();
Integer     minLengthPwd 		= (Integer) request.getAttribute("minLengthPwd");
Boolean		blanksAllowedInPwd 	= (Boolean) request.getAttribute("blanksAllowedInPwd");
String 		action 				= (String) request.getAttribute("Action");
String 		message 			= (String) request.getAttribute("Message");
	
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

<% if (StringUtil.isDefined(message)) { %>
<div class="inline_message_ok">
	<%=message %>
</div>
<% } %>

<form name="UserForm" action="updateMyInfos" method="post">
<table border="0" cellspacing="0" cellpadding="5" width="100%">
    <tr>
        <td class="txtlibform"><%=resource.getString("GML.lastName")%> :</td>
        <td>
        	<%if (updateIsAllowed && updateLastNameIsAllowed) {%>
        		<input type="text" name="userLastName" size="50" maxlength="99" value="<%=userFull.getLastName()%>"/>&nbsp;<img src="<%=resource.getIcon("socialNetwork.mandatory")%>" width="5" height="5"/>
			<%} else {%>
				<%=userFull.getLastName()%>
			<%}%>
		</td>
    </tr>
    <tr>
        <td class="txtlibform"><%=resource.getString("GML.firstName")%> :</td>
        <td>
        	<%if (updateIsAllowed && updateFirstNameIsAllowed) {%>
        		<input type="text" name="userFirstName" size="50" maxlength="99" value="<%=userFull.getFirstName()%>"/>
			<%} else {%>
				<%=userFull.getFirstName()%>
			<%}%>
		</td>   
    </tr>
	<tr>
        <td class="txtlibform"><%=resource.getString("GML.login")%> :</td>
		<td><%=userFull.getLogin()%></td>     
    </tr>
    <tr>	
    	<td class="txtlibform"><%=resource.getString("GML.eMail")%> :</td>
        <td>
        	<%if (updateIsAllowed && updateEmailIsAllowed) {%>
        		<input type="text" name="userEMail" size="50" maxlength="99" value="<%=userFull.geteMail()%>"/>
			<%} else {%>
				<%=userFull.geteMail()%>
			<%}%>
		</td>
    </tr>
	<tr>			
		<td class="txtlibform"><%=resource.getString("myProfile.UserRights") %> :</td>
		<td>
			<input type="radio" name="userAccessLevel" value=A" <% if ((userFull.getAccessLevel() != null) && (userFull.getAccessLevel().equalsIgnoreCase("A"))) out.print("checked"); %> <% out.println("disabled"); %>/>&nbsp;<%=resource.getString("GML.admin") %><br/>
			<input type="radio" name="userAccessLevel" value=D" <% if ((userFull.getAccessLevel() != null) && (userFull.getAccessLevel().equalsIgnoreCase("D"))) out.print("checked"); %> <% out.println("disabled"); %>/>&nbsp;<%=resource.getString("GML.domainManager") %><br/>
			<input type="radio" name="userAccessLevel" value=K" <% if ((userFull.getAccessLevel() != null) && (userFull.getAccessLevel().equalsIgnoreCase("K"))) out.print("checked"); %> <% out.println("disabled"); %>/>&nbsp;<%=resource.getString("GML.kmmanager") %><br/>
			<input type="radio" name="userAccessLevel" value=U" <% if ((userFull.getAccessLevel() == null) || (userFull.getAccessLevel().equalsIgnoreCase("U"))) out.print("checked"); %> <% out.println("disabled"); %>/>&nbsp;<%=resource.getString("GML.user") %><br/>
			<input type="radio" name="userAccessLevel" value=G" <% if ((userFull.getAccessLevel() == null) || (userFull.getAccessLevel().equalsIgnoreCase("G"))) out.print("checked"); %> <% out.println("disabled"); %>/>&nbsp;<%=resource.getString("GML.guest") %>
		</td>
	</tr>
	<%if (updateIsAllowed) {%>
		<tr>
	        <td class="txtlibform"><%=resource.getString("myProfile.OldPassword")%> :</td>
	        <td><input <%=fieldAttribute%> type="password" name="OldPassword" size="50" maxlength="32"/></td>
	    </tr>
		<tr>
	        <td class="txtlibform"><%=resource.getString("myProfile.NewPassword")%> :</td>
	        <td><input <%=fieldAttribute%> type="password" name="NewPassword" size="50" maxlength="32"/>&nbsp;(<%=minLengthPwd.toString()%>&nbsp;<%=resource.getString("myProfile.LengthPwdLabel")%>)</td>
	    </tr>
		<tr>
	        <td class="txtlibform"><%=resource.getString("myProfile.NewPasswordConfirm")%> :</td>
	        <td><input <%=fieldAttribute%> type="password" name="NewPasswordConfirm" size="50" maxlength="32"/>&nbsp;(<%=minLengthPwd.toString()%>&nbsp;<%=resource.getString("myProfile.LengthPwdLabel")%>)</td>
	    </tr>
    <%} else { %>
	    <tr>
	        <td class="txtlibform"><%=resource.getString("myProfile.NewPassword")%> :</td>
	        <td><%=resource.getString("myProfile.ModifyPasswordNotAllowed1")+"<br>"+resource.getString("myProfile.ModifyPasswordNotAllowed2")%></td>
	    </tr>
    <% } 

    if ("personalQuestion".equals(general.getString("forgottenPwdActive"))) {
        String userLoginQuestion = userFull.getLoginQuestion();
        String userLoginAnswer = userFull.getLoginAnswer();
%>
        <tr>
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
        <tr>
            <td class="txtlibform"><%=resource.getString("myProfile.LoginAnswer")%> :</td>
            <td><input type="text" name="userLoginAnswer" value="<%=userLoginAnswer%>" size="50" maxlength="99"/></td>
        </tr><%
    }
%>

	<%//rajout des champs LDAP complémentaires
	if (displayInfosLDAP && action.equals("userMS")) {
		String[] properties = userFull.getPropertiesNames();
		String property = null;
		for (int p=0; p<properties.length; p++) {
			property = (String) properties[p];
			%>
			<tr>			
				<td class="txtlibform"><%=userFull.getSpecificLabel(language, property) %> :</td>
				<td><%=EncodeHelper.javaStringToHtmlString(userFull.getValue(property))%></td>     
			</tr>
			<%
		}
	} else if (action.equals("userModify")) { 
		//rajout des champs Silverpeas custom compl�mentaires
		String[] properties = userFull.getPropertiesNames();
		String property = null;
		for (int p=0; p<properties.length; p++) {
			property = (String) properties[p];
			if (!property.startsWith("password")) {
			%>
			<tr>			
				<td class="txtlibform"><%=userFull.getSpecificLabel(resource.getLanguage(), property) %> :</td>
				<% if (userFull.isPropertyUpdatableByUser(property)) { %>
					<td><input type="text" name="prop_<%=property%>" size="50" maxlength="99" value="<%=EncodeHelper.javaStringToHtmlString(userFull.getValue(property))%>"></td>
				<% } else { %>
					<td><%=EncodeHelper.javaStringToHtmlString(userFull.getValue(property))%></td>
				<% } %>
			</tr>
			<%
			}
		}
	}
	%>
  </table>
 </form>
 <%
		ButtonPane buttonPane = gef.getButtonPane();
		if (updateIsAllowed)
		{
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

		if (errorMsg == "") {
			document.UserForm.submit();
		} else {
			window.alert(errorMsg);
		}
	}
</script>