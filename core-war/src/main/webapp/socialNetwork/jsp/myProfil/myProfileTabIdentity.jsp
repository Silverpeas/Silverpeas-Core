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

<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>

<%@page import="org.silverpeas.web.socialnetwork.myprofil.servlets.MyProfileRoutes"%>
<%@page import="org.silverpeas.core.util.LocalizationBundle"%>
<%@page import="org.silverpeas.core.util.MultiSilverpeasBundle"%>
<%@page import="org.silverpeas.core.util.ResourceLocator"%>
<%@page import="org.silverpeas.core.util.SettingBundle"%>
<%@page import="org.silverpeas.core.web.util.viewgenerator.html.buttonpanes.ButtonPane"%>
<%@page import="org.silverpeas.core.web.util.viewgenerator.html.buttons.Button"%>

<%
  MultiSilverpeasBundle resource = (MultiSilverpeasBundle) request.getAttribute("resources");
  SettingBundle rs = ResourceLocator.getSettingBundle("org.silverpeas.personalization.settings.personalizationPeasSettings");
  LocalizationBundle authRs = ResourceLocator.getLocalizationBundle("org.silverpeas.authentication.multilang.authentication", resource.getLanguage());
  SettingBundle general = ResourceLocator.getSettingBundle("org.silverpeas.lookAndFeel.generalLook");

  boolean updateIsAllowed = (Boolean) request.getAttribute("UpdateIsAllowed");
  boolean isPasswordChangeAllowed = (Boolean) request.getAttribute("isPasswordChangeAllowed");

  String fieldAttribute = " disabled=\"disabled\" ";
  if (updateIsAllowed) {
    fieldAttribute = "";
  }

  boolean updateFirstNameIsAllowed = rs.getBoolean("updateFirstName", false);
  boolean updateLastNameIsAllowed = rs.getBoolean("updateLastName", false);
  boolean updateEmailIsAllowed = rs.getBoolean("updateEmail", false);
  boolean displayInfosLDAP = rs.getBoolean("displayInfosLDAP", false);
%>

<c:set var="messageOK" value="${requestScope.MessageOK}"/>
<c:set var="messageNOK" value="${requestScope.MessageNOK}"/>
<c:set var="displayInfosLDAP" value="<%=displayInfosLDAP%>"/>

<style type="text/css">
  .txtlibform {
    width: 230px;
  }
  .message-addon {
    float: left;
    width: 96%;
  }
</style>

<div class="sousNavBulle">
	<p><fmt:message key="profil.subnav.display" /> <a class="active" href="#"><fmt:message key="profil.subnav.identity" /></a> <!-- <a href="#">Personnelles</a> <a href="#">Personnelles</a> --></p>
</div>

<c:if test="${not empty messageOK}">
  <div class="inlineMessage-ok message-addon">${messageOK}</div>
</c:if>
<c:if test="${not empty messageNOK}">
  <div class="inlineMessage-nok message-addon">${messageNOK}</div>
</c:if>

<div id="identity">
<form name="UserForm" action="<%=MyProfileRoutes.UpdateMyInfos %>" method="post">
<fieldset id="identity-main" class="skinFieldset">
<legend><fmt:message key="myProfile.identity.fieldset.main" /></legend>
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
		<td><%=resource.getString("GML.user.type."+userFull.getAccessLevel().code()) %></td>
	</tr>
	<%if (updateIsAllowed && isPasswordChangeAllowed) {%>
		<tr id="oldPassword">
	        <td class="txtlibform"><%=resource.getString("myProfile.OldPassword")%> :</td>
	        <td><input <%=fieldAttribute%> type="password" name="OldPassword" size="50" maxlength="32"/></td>
	    </tr>
		<tr>
	        <td class="txtlibform"><%=resource.getString("myProfile.NewPassword")%> :</td>
	        <td><input <%=fieldAttribute%> type="password" id="newPassword" name="NewPassword" size="50" maxlength="32"/>&nbsp;(<a tabindex="-1" href="#" onclick="$('#newPassword').focus()"><%=authRs.getString("authentication.password.showRules") %></a>)</td>
	    </tr>
		<tr>
	        <td class="txtlibform"><%=resource.getString("myProfile.NewPasswordConfirm")%> :</td>
	        <td><input <%=fieldAttribute%> id="newPasswordConfirmation" name="NewPasswordConfirm" type="password" size="50" maxlength="32"/></td>
	    </tr>
    <%} else { %>
	    <tr>
	        <td class="txtlibform"><%=resource.getString("myProfile.NewPassword")%> :</td>
	        <td><%=resource.getString("myProfile.ModifyPasswordNotAllowed1")+"<br/>"+resource.getString("myProfile.ModifyPasswordNotAllowed2")%></td>
	    </tr>
    <% } %>
      <tr id="token">
            <td class="txtlibform"><%=resource.getString("myProfile.Token")%> :</td>
      <td><%=userFull.getToken()%></td>
      </tr>
    <%
    if ("personalQuestion".equals(general.getString("forgottenPwdActive"))) {
        String userLoginQuestion = userFull.getLoginQuestion();
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
            <td><input type="text" name="userLoginAnswer" value="" size="50" maxlength="99"/></td>
        </tr><%
    }
%>
</table>
</fieldset>
  <c:if test="${displayInfosLDAP}">
    <fieldset id="identity-extra" class="skinFieldset">
      <legend class="without-img"><fmt:message key="myProfile.identity.fieldset.extra"/></legend>
      <viewTags:displayUserExtraProperties user="<%=userFull%>" readOnly="false" includeEmail="false"/>
    </fieldset>
  </c:if>
 </form>
 </div>
 <br clear="all"/>
 <%
		ButtonPane buttonPane = gef.getButtonPane();
		if (updateIsAllowed) {
			Button validateButton = gef.getFormButton(resource.getString("GML.validate"), "javascript:onClick=submitForm();", false);
			buttonPane.addButton(validateButton);
		}
		Button cancelButton = gef.getFormButton(resource.getString("GML.cancel"), "javascript:onClick=history.back();", false);
		buttonPane.addButton(cancelButton);
		out.println(buttonPane.print());
%>

<script type="text/javascript">

  // Password
  $(document).ready(function(){
    $('#newPassword').password();
  });

	function submitForm() {
		var errorMsg = "";
		<% if (updateLastNameIsAllowed) { %>
			var namefld = document.UserForm.userLastName.value;
			if (isWhitespace(namefld))
			{
				errorMsg = "- <%=resource.getString("GML.theField")%> '<%=resource.getString("GML.lastName")%>' <%=resource.getString("GML.MustBeFilled")%>\n";
			}
		<% } %>
		<% if (isPasswordChangeAllowed) {%>
    var $pwdInput = $('#newPassword');
    if ($pwdInput.val()) {
      $pwdInput.password('verify', {onError : function() {
        errorMsg += "- <%=resource.getString("myProfile.Error_bad_credential")%>\n";
      }});
      if ($pwdInput.val() != $('#newPasswordConfirmation').val()) {
        errorMsg += "- <%=resource.getString("myProfile.WrongNewPwd")%>\n";
      }
    }
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