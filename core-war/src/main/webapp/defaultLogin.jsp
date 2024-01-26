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
<%@ page import="org.silverpeas.kernel.bundle.LocalizationBundle" %>
<%@ page import="org.silverpeas.kernel.util.StringUtil" %>
<%@ page import="org.silverpeas.core.web.util.DomainDetector" %>
<%@ page import="org.silverpeas.kernel.bundle.ResourceLocator" %>
<%@ page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ page isELIgnored="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>
<%@ taglib uri="http://www.silverpeas.com/tld/silverFunctions" prefix="silfn" %>
<fmt:setLocale value="${pageContext.request.locale.language}"/>
<%@ include file="headLog.jsp" %>

<c:set var="computedDomainId" value="<%= DomainDetector.getDomainId(request)%>"/>

<%
  LocalizationBundle authenticationBundle = ResourceLocator.getLocalizationBundle(
      "org.silverpeas.authentication.multilang.authentication",
      SilverpeasWebUtil.get().getUserLanguage(request));

  pageContext.setAttribute("authenticationBundle", authenticationBundle);

  String domainId = null;
  if (StringUtil.isInteger(request.getParameter("DomainId"))) {
    domainId = request.getParameter("DomainId");
    request.setAttribute("Silverpeas_DomainId", domainId);
  }
%>
<view:setBundle basename="org.silverpeas.authentication.multilang.authentication"/>
<view:sp-page>
<view:sp-head-part minimalSilverpeasScriptEnv="true">
  <link rel="icon" href="<%=favicon%>" />
  <link type="text/css" rel="stylesheet" href="<%=styleSheet%>"/>
  <view:includePlugin name="virtualkeyboard"/>
  <script type="text/javascript">
    // Public domain cookie code written by:
    // Bill Dortch, hIdaho Design
    // (bdortch@netw.com)
    function getCookieVal(offset) {
      let endstr = document.cookie.indexOf(";", offset);
      if (endstr === -1) {
        endstr = document.cookie.length;
      }
      return decodeURIComponent(document.cookie.substring(offset, endstr));
    }

    function GetCookie(name) {
      let arg = name + "=";
      let alen = arg.length;
      let clen = document.cookie.length;
      let i = 0;
      while (i < clen) {
        let j = i + alen;
        if (document.cookie.substring(i, j) === arg)
          return getCookieVal(j);
        i = document.cookie.indexOf(" ", i) + 1;
        if (i === 0) break;
      }

      return null;
    }

    function checkForm() {
      $("form").submit();
    }

    function welcome() {
      location.href = '<c:url value="/" />';
    }

    function loginQuestion() {
      let form = document.getElementById("formLogin");
      if (form.elements["Login"].value.length === 0) {
        alert('<fmt:message key="authentication.logon.loginMissing" />');
      } else {
        form.action = '<c:url value="/CredentialsServlet/LoginQuestion" />';
        form.submit();
      }
    }

    function resetPassword() {
      let form = document.getElementById("formLogin");
      if (form.elements["Login"].value.length === 0) {
        alert('<fmt:message key="authentication.logon.loginMissing" />');
      } else {
        form.action = '<c:url value="/CredentialsServlet/ForgotPassword" />';
        form.submit();
      }
    }

    function changePassword() {
      let form = document.getElementById("formLogin");
      if (form.elements["Login"].value.length === 0) {
        alert('<fmt:message key="authentication.logon.loginMissing" />');
      } else {
        form.action = '<c:url value="/CredentialsServlet/ChangePasswordFromLogin" />';
        form.submit();
      }
    }

    function newRegistration() {
      let form = document.getElementById("formLogin");
      form.action = '<c:url value="/CredentialsServlet/NewRegistration" />';
      form.submit();
    }

    $(document).ready(function() {
      $("#DomainId").on("keypress", function(event) {
        if (event.keyCode === 13) {
          checkForm();
        }
      });
    });
  </script>
  <meta name="viewport" content="initial-scale=1.0"/>
</view:sp-head-part>
<view:sp-body-part>
<form id="formLogin" action="<c:url value="/AuthenticationServlet" />" method="post" accept-charset="UTF-8">
  <% if (registrationPartActive || virtualKeyboardActive) { %>
  <div id="top">
    <c:if test="<%=registrationPartActive%>">
      <fmt:message key="authentication.logon.newRegistration.tease"/>
      <c:if test="<%=newRegistrationActive%>">
        <a href="javascript:newRegistration()"><fmt:message key="authentication.logon.newRegistration.create"/></a>
      </c:if>
      <c:if test="<%=facebookEnabled || linkedInEnabled%>">
        <c:if test="<%=newRegistrationActive%>">
          <fmt:message key="GML.or"/>
        </c:if>
        <fmt:message key="authentication.logon.newRegistration.connect"/>
        <c:if test="<%=linkedInEnabled%>">
          <a title="LinkedIn" href="<c:url value="/SocialNetworkLogin?networkId=LINKEDIN" />">LinkedIn</a>
        </c:if>
        <c:if test="<%=facebookEnabled%>">
          <c:if test="<%=linkedInEnabled%>">
            <fmt:message key="GML.or"/>
          </c:if>
          <a title="Facebook" href="<c:url value="/SocialNetworkLogin?networkId=FACEBOOK" />">Facebook</a>
        </c:if>
      </c:if>
    </c:if>
    <c:if test="<%=virtualKeyboardActive%>">
      <c:if test="<%=registrationPartActive%>">
        <span> | </span>
      </c:if>
      <span class="silverpeas-keyboard-activation-dock"></span>
    </c:if>
  </div>
  <% } %>
  <% if (facebookEnabled || linkedInEnabled) {%>
  <div id="login-socialnetwork">
    <% if (linkedInEnabled) { %>
    <a title="<fmt:message key='authentication.logon.with.linkedin' />" href="<c:url value="/SocialNetworkLogin?networkId=LINKEDIN" />">
      <img src="util/icons/external/btn-login-linkedin.png" alt=""/>
    </a>
    <% } %>
    <% if (facebookEnabled) { %>
    <a title="<fmt:message key='authentication.logon.with.facebook' />" href="<c:url value="/SocialNetworkLogin?networkId=FACEBOOK" />">
      <img src="util/icons/external/btn-login-facebook.png" alt=""/>
    </a>
    <% } %>
  </div>
  <% } %>
  <div class="page"> <!-- Centrage horizontal des elements (960px) -->
    <div class="titre"><fmt:message key="authentication.logon.title"/></div>
    <div id="background"> <!-- image de fond du formulaire -->
      <div class="cadre">
        <div id="header" style="display: table; width: 100%">
          <div style="display: table-cell">
            <a href="javascript:welcome()"><img src="<%=logo%>" class="logo" alt="logo"/></a>
          </div>
          <div class="information" style="display: table-cell; width: 100%; text-align: right">
            <c:set var="errorMessage"/>
            <c:if test="${silfn:isDefined(param.ErrorCode) && '4' != param.ErrorCode}">
              <fmt:message key="authentication.logon.${param.ErrorCode}" var="errorMessage"/>
            </c:if>
            <c:choose>
              <c:when test="${silfn:isDefined(errorMessage) && !fn:startsWith(errorMessage, '???')}">
                <span>${errorMessage}</span>
              </c:when>
              <c:otherwise>
                <fmt:message key="authentication.logon.subtitle"/>
              </c:otherwise>
            </c:choose>
            <c:if test="${not empty sessionScope.WarningMessage}">
              <br/><span><c:out value="${sessionScope.WarningMessage}"/></span>
            </c:if>
          </div>
          <div class="clear"></div>
        </div>

        <div id="login-extra-content">
          <view:applyTemplate locationBase="core:login" name="extraContent"/>
        </div>

        <p>
          <label><span><fmt:message key="authentication.logon.login"/></span><input type="text" name="Login" id="Login"/></label>
        </p>

        <p>
          <label><span><fmt:message key="authentication.logon.password"/></span><input type="password" name="Password" id="Password" autocomplete="off"/></label>
        </p>
        <c:choose>
          <c:when test="${!pageScope.multipleDomains}">
            <input class="noDisplay" type="hidden" name="DomainId" value="<%=listDomains.get(0).getId()%>"/>
          </c:when>
          <c:when test="${not empty computedDomainId}">
            <input class="noDisplay" type="hidden" name="DomainId" value="${computedDomainId}"/>
          </c:when>
          <c:otherwise>
            <p><label><span><fmt:message key="authentication.logon.domain"/></span>
              <select id="DomainId" name="DomainId" size="1">
                <c:forEach var="domain" items="${pageScope.listDomains}">
                  <option value="<c:out value="${domain.id}" />" <c:if test="${domain.id eq param.DomainId}">selected</c:if>><c:out value="${domain.name}"/></option>
                </c:forEach>
              </select>
            </label></p>
          </c:otherwise>
        </c:choose>
        <p>
          <input type="submit" style="width:0; height:0; border:0; padding:0"/>
          <a href="#" class="<%=submitClass%>" onclick="checkForm()"><span><span><fmt:message key="authentication.logon.login.button"/></span></span></a>
        </p>

        <% if (forgottenPwdActive || changePwdFromLoginPageActive) { %>
        <% if (forgottenPwdActive) { %>
        <p>
          <span class="forgottenPwd">
          <% if ("personalQuestion".equalsIgnoreCase(pwdResetBehavior)) { %>
            <a href="javascript:loginQuestion()"><fmt:message key="authentication.logon.passwordForgotten"/></a>
          <% } else { %>
            <a href="javascript:resetPassword()"><fmt:message key="authentication.logon.passwordReinit"/></a>
          <%} %>
          </span>
            <% } %>

            <% if (changePwdFromLoginPageActive) { %>
            <% if (forgottenPwdActive) { %>
          <span class="separator">|</span>
          <span class="changePwd">
            <% } else {%>

        <p>
          <span class="changePwd">
          <% } %>
          <a class="changePwd" href="javascript:changePassword()"><fmt:message key="authentication.logon.changePassword"/></a>
          </span>
          <% } %>

          <% if (forgottenPwdActive || changePwdFromLoginPageActive) { %>
        </p>
        <% } %>
        <% } %>
      </div>
    </div>
    <div id="copyright"><fmt:message key="GML.trademark"/></div>
  </div>
</form>
<!-- Fin class="page" -->

<script type="text/javascript">
  const domainId = <%=domainId%>;

  /* Si le domainId n'est pas dans la requete, alors recuperation depuis le cookie */
  if (GetCookie("defaultDomain")) {
    <% for (int i = 0 ; i < listDomains.size() && listDomains.size() > 1; i++) { %>
    if (GetCookie("defaultDomain").toString() === "<%=(listDomains.get(i).getId())%>") {
      document.getElementById("DomainId").options[<%=i%>].selected = true;
    }
    <% } %>
  }

  if (GetCookie("svpLogin")) {
    document.getElementById("Login").value = GetCookie("svpLogin").toString();
  }

  document.getElementById("formLogin").Password.value = '';
  document.getElementById("formLogin").Login.focus();
</script>

</view:sp-body-part>
</view:sp-page>
