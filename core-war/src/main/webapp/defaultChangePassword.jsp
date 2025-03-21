<%--
  ~ Copyright (C) 2000 - 2024 Silverpeas
  ~
  ~ This program is free software: you can redistribute it and/or modify
  ~ it under the terms of the GNU Affero General Public License as
  ~ published by the Free Software Foundation, either version 3 of the
  ~ License, or (at your option) any later version.
  ~
  ~ As a special exception to the terms and conditions of version 3.0 of
  ~ the GPL, you may redistribute this Program in connection with Free/Libre
  ~ Open Source Software ("FLOSS") applications as described in Silverpeas's
  ~ FLOSS exception.  You should have received a copy of the text describing
  ~ the FLOSS exception, and it is also available here:
  ~ "https://www.silverpeas.org/legal/floss_exception.html"
  ~
  ~ This program is distributed in the hope that it will be useful,
  ~ but WITHOUT ANY WARRANTY; without even the implied warranty of
  ~ MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  ~ GNU Affero General Public License for more details.
  ~
  ~ You should have received a copy of the GNU Affero General Public License
  ~ along with this program.  If not, see <https://www.gnu.org/licenses/>.
  --%>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>

<%@ include file="headLog.jsp" %>

<fmt:setLocale value="<%=userLanguage%>"/>
<view:setBundle basename="org.silverpeas.multilang.generalMultilang"/>
<view:setBundle basename="org.silverpeas.authentication.multilang.authentication" var="authenticationBundle"/>

<c:set var="templateLocationBase" value="core:admin"/>
<c:set var="titleTemplate" value="firstLogin_email"/>
<c:set var="isEmailAddress" value="${requestScope.isThatUserMustFillEmailAddressOnFirstLogin}"/>
<c:set var="emailAddress" value="${isEmailAddress ? requestScope.emailAddress : ''}"/>

<view:sp-page>
<view:sp-head-part minimalSilverpeasScriptEnv="true">
  <link rel="icon" href="<%=favicon%>" />
  <link type="text/css" rel="stylesheet" href="<%=styleSheet%>"/>
  <link type="text/css" rel="stylesheet" href="<%=m_context%>/util/styleSheets/silverpeas-password.css"/>
  <view:includePlugin name="virtualkeyboard"/>
  <view:includePlugin name="popup"/>
  <view:script src="/password.js"/>
  <c:if test="${isEmailAddress}">
    <view:includePlugin name="qtip"/>
  </c:if>
  <!--[if lt IE 8]>
  <style type="text/css">
    input {
      background-color: #FAFAFA;
      border: 1px solid #DAD9D9;
      width: 448px;
      text-align: left;
      margin-left: -10px;
      height: 26px;
      line-height: 24px;
      display: block;
      padding: 0;
    }
  </style>
  <![endif]-->
</view:sp-head-part>
<view:sp-body-part>
<form id="changePwdForm" action="#" method="post">
  <div class="page">
    <div class="titre">
      <fmt:message key="authentication.logon.title" bundle="${authenticationBundle}"/></div>
    <div id="backgroundBig">
      <div class="cadre">
        <div id="header" style="display: table; width: 100%">
          <div style="display: table-cell">
            <img src="<%=logo%>" class="logo" alt=""/>
          </div>
          <div class="information" style="display: table-cell; width: 100%; text-align: right">
            <fmt:message key="authentication.password.change" bundle="${authenticationBundle}"/>
            <c:if test="${isEmailAddress}">
              <fmt:message key="GML.and"/>
              <fmt:message key="authentication.email.label" var="tmp" bundle="${authenticationBundle}"/>
              ${fn:toLowerCase(tmp)}
            </c:if>
            <c:if test="${not empty requestScope.message}">
              <br/><span><c:out value="${requestScope.message}" escapeXml="false"/></span>
            </c:if>
          </div>
          <div class="clear"></div>
        </div>
        <p>
          <label><span><fmt:message key="authentication.password.old" bundle="${authenticationBundle}"/></span><input type="password" name="oldPassword" id="oldPassword" autocomplete="new-password"/></label>
        </p>

        <p>
          <label><span><fmt:message key="authentication.password.new" bundle="${authenticationBundle}"/></span><input type="password" name="newPassword" id="newPassword" autocomplete="new-password"/></label>
        </p>

        <p>
          <label><span><fmt:message key="authentication.password.confirm" bundle="${authenticationBundle}"/></span><input type="password" name="confirmPassword" id="confirmPassword" autocomplete="new-password"/></label>
        </p>
        <c:if test="${isEmailAddress}">
          <p>
            <label><span><fmt:message key="authentication.email.label" bundle="${authenticationBundle}"/></span><input type="text" name="emailAddress" id="emailAddress" value="${emailAddress}"/></label>
          </p>
        </c:if>
        <input type="hidden" name="login" value="${param.Login}"/>
        <input type="hidden" name="domainId" value="${param.DomainId}"/>

        <div class="submit">
          <p>
            <input type="submit" style="width:0; height:0; border:0; padding:0"/>
            <a href="#" style="cursor: pointer" class="<%=submitClass%>" onclick="$('#changePwdForm').submit()"><span><span>LOGIN</span></span></a>
          </p>

          <p>
          <span class="passwordRules"><a href="#" onclick="$('#newPassword').focus()">
            <fmt:message key="authentication.password.showRules" bundle="${authenticationBundle}"/>
          </a></span>
          </p>
        </div>
      </div>
    </div>
  </div>
</form>
<c:if test="${isEmailAddress}">
  <div id="emailAddressMessage" style="display: none; max-width: 400px; text-align: left;">
    <view:applyTemplate locationBase="${templateLocationBase}" name="${titleTemplate}"/></div>
</c:if>
<view:loadScript src="/util/javaScript/silverpeas-password.js" jsPromiseName="loadingPromise"/>
<script type="text/javascript">
  loadingPromise.then(function() {
    setTimeout(function() {
      var extraValidations;
      <c:if test="${isEmailAddress}">
      extraValidations = function(errorStack) {
        if (!$.trim($('#emailAddress').val())) {
          errorStack.push(
              '<fmt:message key="authentication.email.error" bundle="${authenticationBundle}"/>');
        }
        return sp.promise.resolveDirectlyWith();
      };
      var $emailMessage = $('#emailAddressMessage');
      if ($emailMessage.length > 0 && $.trim($emailMessage.html())) {
        var $emailAddress = $('#emailAddress');
        $emailAddress.qtip({
          content : $emailMessage,
          style : {
            width : "auto",
            tip : true,
            classes : "qtip-shadow qtip-cream"
          },
          position : {
            adjust : {
              method : "flip flip"
            },
            viewport : $(window),
            at : "bottom left",
            my : "top right"
          },
          show : {
            delay : 0,
            event : "displayQTip"
          },
          hide : {
            fixed : true,
            event : "hideQTip"
          }
        });
        $emailAddress.trigger("displayQTip");
      }
      </c:if>
      handlePasswordForm({
        passwordFormId : 'changePwdForm',
        passwordFormAction : '<c:url value="/CredentialsServlet/EffectiveChangePasswordFromLogin"/>',
        passwordInputId : 'newPassword',
        extraValidations : extraValidations
      });
      document.querySelector("input").focus();
    }, 0);
  });
</script>
</view:sp-body-part>
</view:sp-page>
