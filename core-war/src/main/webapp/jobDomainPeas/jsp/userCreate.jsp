<%--

    Copyright (C) 2000 - 2018 Silverpeas

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    As a special exception to the terms and conditions of version 3.0 of
    the GPL, you may redistribute this Program in connection with Free/Libre
    Open Source Software ("FLOSS") applications as described in Silverpeas's
    FLOSS exception.  You should have received a copy of the text describing
    the FLOSS exception, and it is also available here:
    "https://www.silverpeas.org/legal/floss_exception.html"

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

--%>
<%@ page import="org.silverpeas.core.notification.user.client.NotificationManagerSettings" %>
<%@ page import="org.silverpeas.core.admin.user.constant.UserAccessLevel" %>
<%@ page import="org.silverpeas.core.util.WebEncodeHelper" %>
<%@ page import="org.silverpeas.core.util.CollectionUtil" %>

<%--

    Copyright (C) 2000 - 2018 Silverpeas

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

<%@ include file="check.jsp" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://www.silverpeas.com/tld/silverFunctions" prefix="silfn" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<%@ taglib tagdir="/WEB-INF/tags/silverpeas/util" prefix="viewTags" %>

<%-- Set resource bundle --%>
<fmt:setLocale value="${sessionScope['SilverSessionController'].favoriteLanguage}" />
<view:setBundle basename="org.silverpeas.jobDomainPeas.multilang.jobDomainPeasBundle"/>
<view:setBundle bundle="${requestScope.resources.iconsBundle}" var="icons" />
<view:setBundle basename="org.silverpeas.social.multilang.socialNetworkBundle" var="profile"/>

<c:set var="context" value="${pageContext.request.contextPath}"/>
<c:set var="USER_MANUAL_NOTIFICATION_MAX_RECIPIENT_LIMITATION_ENABLED" value="<%= NotificationManagerSettings.isUserManualNotificationRecipientLimitEnabled()%>"/>
<c:set var="USER_MANUAL_NOTIFICATION_MAX_RECIPIENT_LIMITATION_DEFAULT_VALUE" value="<%= NotificationManagerSettings.getUserManualNotificationRecipientLimit()%>"/>

<fmt:message key="JDP.userManualNotifReceiverLimitValue" var="userManualNotifReceiverLimitValueLabel"><fmt:param value="${USER_MANUAL_NOTIFICATION_MAX_RECIPIENT_LIMITATION_DEFAULT_VALUE}"/></fmt:message>

<c:set var="userObject" value="${requestScope.userObject}"/>
<jsp:useBean id="userObject" type="org.silverpeas.core.admin.user.model.UserFull"/>
<c:set var="currentUser" value="${requestScope.CurrentUser}"/>
<jsp:useBean id="currentUser" type="org.silverpeas.core.admin.user.model.UserDetail"/>

<%
  Domain domObject = (Domain) request.getAttribute("domainObject");
  String action = (String) request.getAttribute("action");
  String groupsPath = (String) request.getAttribute("groupsPath");
  Integer minLengthLogin = (Integer) request.getAttribute("minLengthLogin");
  List<Group> groups = (List<Group>) request.getAttribute("GroupsManagedByCurrentUser");

  boolean userCreation = "userCreate".equals(action);
  boolean extraInfosUpdatable = userCreation || "userModify".equals(action);

  browseBar.setComponentName(getDomainLabel(domObject, resource),
      "domainContent?Iddomain=" + domObject.getId());
  browseBar.setPath(groupsPath);
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<view:looknfeel withFieldsetStyle="true" withCheckFormScript="true"/>
<view:includePlugin name="password"/>
<script type="text/javascript">

  // Password
  $(document).ready(function(){
    $('#userPasswordId').password();

    <c:if test="${currentUser.accessAdmin and USER_MANUAL_NOTIFICATION_MAX_RECIPIENT_LIMITATION_ENABLED}">
    $("input[name='userAccessLevel']").on("change", function() {
      var selectedRightAccess = $("input[name='userAccessLevel']:checked").val();
      var $manualNotificationBlock = $('#identity-manual-notification');
      if(selectedRightAccess === 'USER' || selectedRightAccess === 'GUEST') {
        $manualNotificationBlock.show();
      } else {
        $manualNotificationBlock.hide();
      }
    });

    var $limitActivation = $('#userManualNotifReceiverLimitEnabled').on("change", function() {
      var $me = $(this);
      var $limitValue = $('#form-row-user-manual-notification-limitation-value');
      if($me.is(':checked')) {
        $limitValue.show();
      } else {
        $limitValue.hide();
      }
    });

    $limitActivation.trigger("change");
    $("input[name='userAccessLevel']:checked").trigger("change");
    </c:if>

    selectUnselect();

    $("#form-row-extra-message").hide();
    $('#sendEmailId').on('change', function() {
      if ($(this).is(':checked')) {
        $("#form-row-extra-message").show();
      } else {
        $("#form-row-extra-message").hide();
      }
    });
  });

function ifCorrectBasicFormExecute(callback) {
  var verifyPromises = [sp.promise.resolveDirectlyWith()];
  var userLastNameInput = $("#userLastName");
  SilverpeasError.reset();

  if (userLastNameInput.length > 0 && isWhitespace(userLastNameInput.val())) {
    SilverpeasError.add("<%=resource.getString("JDP.missingFieldStart")+resource.getString("GML.lastName")+resource.getString("JDP.missingFieldEnd")%>");
  }

  <% if (userCreation) { %>
  var loginfld = stripInitialWhitespace(document.userForm.userLogin.value);
  if (isWhitespace(loginfld)) {
    SilverpeasError.add("<%=resource.getString("JDP.missingFieldStart")+resource.getString("GML.login")+resource.getString("JDP.missingFieldEnd")%>");
  } else if(loginfld.length < <%=minLengthLogin.intValue()%>) {
    SilverpeasError.add("<%=resource.getString("JDP.missingFieldStart")+resource.getString("GML.login")+resource.getString("JDP.minLength")+" "+minLengthLogin.toString()+" "+resource.getString("JDP.caracteres")%>");
  }
  <% } %>

  <% if (userObject.isPasswordAvailable()) { %>
  if ($('#userPasswordValid:checked').val()) {
    var $pwdInput = $('#userPasswordId');
    <% if (userCreation || "userModify".equals(action)) { %>
    <% if ("userModify".equals(action)) { %>
    if ($pwdInput.val()) {
      <% } %>
      var passwordDeferred = sp.promise.deferred();
      verifyPromises.push(passwordDeferred.promise);
      $pwdInput.password('verify', {
        onSuccess : function() {
          passwordDeferred.resolve();
        },
        onError : function() {
          SilverpeasError.add("<%=resource.getString("JDP.missingFieldStart")+resource.getString("GML.password")+resource.getString("JDP.pwdError")%>");
          passwordDeferred.resolve();
        }
      });
      if ($pwdInput.val() != $('#userPasswordAgainId').val()) {
        SilverpeasError.add("<fmt:message key='JDP.confirmPwdError'/>");
      }
      <% if ("userModify".equals(action)) { %>
    }
    <% } %>
    <% } %>
  }
  <% } %>

  <c:if test="${currentUser.accessAdmin and USER_MANUAL_NOTIFICATION_MAX_RECIPIENT_LIMITATION_ENABLED}">
  var rightAccess = $("input[name='userAccessLevel']:checked").val();
  var limitValue = $.trim($(document.userForm.userManualNotifReceiverLimitValue).val());
  if ((rightAccess === 'USER' || rightAccess === 'GUEST')
      && $(document.userForm.userManualNotifReceiverLimitEnabled).is(":checked")
      && (!isInteger(limitValue) || (limitValue.length > 0 && eval(limitValue) <= 0))) {
    SilverpeasError.add("<fmt:message key="GML.thefield" /> '${userManualNotifReceiverLimitValueLabel}' <fmt:message key="GML.MustContainsPositiveNumber" />");
  }
  </c:if>

  sp.promise.whenAllResolved(verifyPromises).then(function() {
    if (!SilverpeasError.show()) {
      <% if (userCreation && CollectionUtil.isNotEmpty(groups)) { %>
      var firstName = $("#userFirstName").val();
      var lastName = userLastNameInput.val();
      var email = $("#userEMail").val();
      $.post('<%=m_context%>/JobDomainPeasAJAXServlet',
        {FirstName : firstName, LastName : lastName, Email : email, Action : 'CheckUser'},
        function(data) {
          if (data === "ok") {
            callback.call(this);
          } else {
            jQuery.popup.error('<fmt:message key="JDP.userAlreadyExistingError"/>');
          }
        }, 'text');
      <% } else { %>
      callback.call(this);
      <% } %>
    }
  });
}

function selectUnselect() {
<% if (userObject.isPasswordAvailable()) { %>
  var bSelected = document.userForm.userPasswordValid.checked;
  if (bSelected){
      document.userForm.userPassword.disabled = false;
      $("#sendEmailTRid").show();
  } else {
      document.userForm.userPassword.disabled = true;
      $("#sendEmailTRid").hide();
  }
<% } %>
}

function saveUser() {
  if ($("#identity-template").length) {
    ifCorrectBasicFormExecute(function() {
      ifCorrectFormExecute(function() {
        document.userForm.submit();
      });
    });
  } else {
    ifCorrectBasicFormExecute(function() {
      document.userForm.submit();
    });
  }
}
</script>
</head>
<body class="page_content_admin">
<%
out.println(window.printBefore());
%>
<view:frame>
  <form name="userForm" action="<%=action%>" method="post" enctype="multipart/form-data">
    <input type="hidden" name="Iduser" value="<% if (userObject.getId() != null) {
        out.print(userObject.getId());
      } %>"/>

    <fmt:message key="JDP.mandatory" var="mandatoryIcon" bundle="${icons}" />

    <fieldset id="identity-main" class="skinFieldset">
      <legend><fmt:message key="myProfile.identity.fieldset.main" bundle="${profile}" /></legend>
      <div class="fields">
		<!--Last name-->
	<div class="field" id="form-row-name">
			<label class="txtlibform"><fmt:message key="GML.lastName"/></label>
			<div class="champs">
				<% if (action.equals("userMS")) { %>
                <%=userObject.getLastName()%>
              <% } else { %>
                <input type="text" name="userLastName" id="userLastName" size="50" maxlength="99"
			value="<%=WebEncodeHelper.javaStringToHtmlString(userObject.getLastName())%>" />
			&nbsp;<img border="0" src="${context}${mandatoryIcon}" width="5" height="5"/>
              <% } %>
			</div>
		</div>
		<!--Surname-->
	<div class="field" id="form-row-surname">
			<label class="txtlibform"><fmt:message key="GML.surname"/></label>
			<div class="champs">
				<% if (action.equals("userMS")) { %>
		<%=userObject.getFirstName()%>
		<% } else { %>
		<input type="text" name="userFirstName" id="userFirstName" size="50" maxlength="99"
			value="<%=WebEncodeHelper.javaStringToHtmlString(userObject.getFirstName())%>" />
		<% } %>
			</div>
		</div>
		<!--Login-->
	<div class="field" id="form-row-login">
			<label class="txtlibform"><fmt:message key="GML.login"/></label>
			<div class="champs">
				<% if (userCreation) { %>
                  <input type="text" name="userLogin" size="50" maxlength="50"
			value="<%=WebEncodeHelper.javaStringToHtmlString(userObject.getLogin())%>"/>
			&nbsp;<img border="0" src="${context}${mandatoryIcon}" width="5" height="5"/>
                <% } else { %>
                  <%=WebEncodeHelper.javaStringToHtmlString(userObject.getLogin())%>
                <% } %>
			</div>
		</div>
		<!--Email-->
	<div class="field" id="form-row-email">
			<label class="txtlibform"><fmt:message key="GML.eMail"/></label>
			<div class="champs">
				<% if (action.equals("userMS")) { %>
		<%=userObject.geteMail()%>
		<% } else { %>
                  <input type="text" name="userEMail" id="userEMail" size="50" maxlength="99"
			value="<%=WebEncodeHelper.javaStringToHtmlString(userObject.geteMail())%>" />
                <% } %>
			</div>
		</div>
		<!--Rights-->
	<div class="field" id="form-row-rights">
			<label class="txtlibform"><fmt:message key="JDP.userRights"/></label>
			<div class="champs">
				<% if (currentUser.isAccessAdmin()) { %>
                  <input type="radio" name="userAccessLevel" value="ADMINISTRATOR" <%
                    if (userObject.isAccessAdmin()) {
                      out.print("checked");
                    } %>/>&nbsp;<%=resource.getString("GML.administrateur") %><br/>
                  <input type="radio" name="userAccessLevel" value="PDC_MANAGER" <%
                    if (userObject.isAccessPdcManager()) {
                      out.print("checked");
                    } %>/>&nbsp;<%=resource.getString("GML.kmmanager") %><br/>
                <% } %>
                <% if (currentUser.isAccessAdmin() || currentUser.isAccessDomainManager()) { %>
                  <input type="radio" name="userAccessLevel" value="DOMAIN_ADMINISTRATOR" <%
                    if (userObject.isAccessDomainManager()) {
                      out.print("checked");
                    } %>/>&nbsp;<%=resource.getString("GML.domainManager") %><br/>
                  <input type="radio" name="userAccessLevel" value="USER" <%
                    if (userObject.isAccessUser() || UserAccessLevel.UNKNOWN.equals(userObject.getAccessLevel())) {
                      out.print("checked");
                    } %>/>&nbsp;<%=resource.getString("GML.user") %><br/>
                  <input type="radio" name="userAccessLevel" value="GUEST" <%
                    if (userObject.isAccessGuest()) {
                      out.print("checked");
                    } %>/>&nbsp;<%=resource.getString("GML.guest") %>
                <% } else { %>
			  <input type="hidden" name="userAccessLevel" value="USER"/><fmt:message key="GML.user" />
                <% } %>
			</div>
		</div>
		<% if (userObject.isPasswordAvailable()) { %>
		<!--Password Silverpeas ?-->
	<div class="field" id="form-row-passwordsp">
			<label class="txtlibform"><fmt:message key="JDP.silverPassword"/></label>
			<div class="champs">
				<input type="checkbox" name="userPasswordValid" id="userPasswordValid" value="true"
				<%
                 if (userObject.isPasswordValid() || userCreation) {
                   out.print("checked");
                 } %> onclick="selectUnselect()"/>&nbsp;<fmt:message key="GML.yes" />
			</div>
		</div>
		<!--Password-->
	<div class="field" id="form-row-password">
			<label class="txtlibform"><fmt:message key="GML.password"/></label>
			<div class="champs">
				<input type="password" autocomplete="off" name="userPassword" id="userPasswordId" size="50" maxlength="32" value=""/>
			</div>
		</div>
		<!--Password again-->
	<div class="field" id="form-row-passwordAgain">
			<label class="txtlibform"><fmt:message key="GML.passwordAgain"/></label>
			<div class="champs">
				<input type="password" autocomplete="off" name="userPasswordAgain" id="userPasswordAgainId" size="50" maxlength="32" value=""/>
			</div>
		</div>
		<!--Send Email-->
	<div class="field" id="sendEmailTRid">
			<label class="txtlibform"><fmt:message key="JDP.sendEmail"/></label>
			<div class="champs">
				<input type="checkbox" name="sendEmail" id="sendEmailId" value="true" />
				&nbsp;<fmt:message key="GML.yes" /> <br/>
			</div>
		</div>
    <div class="field" id="form-row-extra-message">
      <label class="txtlibform"><fmt:message key="JDP.sendEmail.message"/></label>
      <div class="champs">
        <fmt:message key="JDP.sendEmail.message.help" var="extraMessageHelp"/>
        <textarea rows="3" cols="50" name="extraMessage" placeholder="${extraMessageHelp}"></textarea>
      </div>
    </div>

        <% }

        //in case of group manager, the added user must be set to one group
        //if user manages only once group, user will be added to it
        //else if he manages several groups, manager chooses one group
        if (CollectionUtil.isNotEmpty(groups)) {
      %>
	<!--Group-->
	<div class="field" id="form-row-group">
			<label class="txtlibform"><fmt:message key="GML.groupe"/></label>
			<div class="champs">
				<% if (groups.size() == 1) {
			Group group = groups.get(0);
			%>
			<%=group.getName() %> <input type="hidden" name="GroupId" id="GroupId" value="<%=group.getId()%>"/>
			<% } else { %>
			<select id="GroupId" name="GroupId">
	            <% for (Group group : groups) {
	            %>
	            <option value="<%=group.getId()%>"><%=group.getName()%>
	            </option>
	            <% } %>
	          </select>&nbsp;<img border="0" src="${context}${mandatoryIcon}" width="5" height="5"/>
	          <% } %>
			</div>
		</div>
      <% } %>
        <!--User Language-->
        <div class="field" id="form-row-user-language">
          <label class="txtlibform"><fmt:message key="JDP.userPreferredLanguage"/></label>

          <div class="champs">
            <viewTags:userPreferredLanguageSelector user="${not empty userObject.id ? userObject : null}"
                                                    readOnly="${not empty userObject.id}"/>
          </div>
        </div>
        <!--User Zone Id-->
        <div class="field" id="form-row-user-zone-id">
          <label class="txtlibform"><fmt:message key="JDP.userPreferredZoneId"/></label>

          <div class="champs">
            <viewTags:userPreferredZoneIdSelector user="${not empty userObject.id ? userObject : null}"
                                                  readOnly="${not empty userObject.id}"/>
          </div>
        </div>
      </div>
    </fieldset>

      <%--User Manual Notification User Receiver Limit--%>
    <c:if test="${currentUser.accessAdmin and USER_MANUAL_NOTIFICATION_MAX_RECIPIENT_LIMITATION_ENABLED}">
      <fieldset id="identity-manual-notification" class="skinFieldset" style="display: none">
        <legend class="without-img"><fmt:message key="JDP.userManualNotif"/></legend>
        <div class="fields">
          <div class="field" id="form-row-user-manual-notification-limitation-activation">
            <label class="txtlibform"><fmt:message key="JDP.userManualNotifReceiverLimitActivation"/></label>

            <div class="champs">
              <input type="checkbox" name="userManualNotifReceiverLimitEnabled" id="userManualNotifReceiverLimitEnabled" value="true"
                     ${(userObject.userManualNotificationUserReceiverLimit)?' checked':''}/>
              &nbsp;<fmt:message key="GML.yes"/>
            </div>
          </div>
          <div class="field" id="form-row-user-manual-notification-limitation-value">
            <label class="txtlibform">${userManualNotifReceiverLimitValueLabel}</label>

            <div class="champs">
              <input type="text" name="userManualNotifReceiverLimitValue" id="userManualNotifReceiverLimitValue" size="50" maxlength="3"
                     value="${userObject.userManualNotificationUserReceiverLimit
                              and not empty userObject.notifManualReceiverLimit
                              and userObject.notifManualReceiverLimit gt 0 ? userObject.notifManualReceiverLimit : ''}"/>
            </div>
          </div>
        </div>
      </fieldset>
    </c:if>

    <c:if test="${fn:length(userObject.propertiesNames) > 0}">
      <fieldset id="identity-extra" class="skinFieldset">
        <legend class="without-img"><fmt:message key="myProfile.identity.fieldset.extra" bundle="${profile}"/></legend>
        <viewTags:displayUserExtraProperties user="${userObject}" allFieldsUpdatable="<%=extraInfosUpdatable%>" readOnly="false" includeEmail="false"/>
      </fieldset>
    </c:if>

    <view:directoryExtraForm userId="${userObject.id}" edition="true"/>

    <div class="legend">
	<img border="0" src="${context}${mandatoryIcon}" width="5" height="5"/> : <fmt:message key="GML.requiredField"/>
    </div>
  </form>
<br/>
<%
  ButtonPane bouton = gef.getButtonPane();
  bouton.addButton(gef.getFormButton(resource.getString("GML.validate"), "javascript:saveUser()", false));
  bouton.addButton(gef.getFormButton(resource.getString("GML.cancel"), "domainContent", false));
  out.println(bouton.print());
%>
</view:frame>
<%
  out.println(window.printAfter());
%>
</body>
</html>