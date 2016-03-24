<%--
  Copyright (C) 2000 - 2013 Silverpeas

  This program is free software: you can redistribute it and/or modify
  it under the terms of the GNU Affero General Public License as
  published by the Free Software Foundation, either version 3 of the
  License, or (at your option) any later version.

  As a special exception to the terms and conditions of version 3.0 of
  the GPL, you may redistribute this Program in connection with Free/Libre
  Open Source Software ("FLOSS") applications as described in Silverpeas's
  FLOSS exception.  You should have recieved a copy of the text describing
  the FLOSS exception, and it is also available here:
  "http://www.silverpeas.org/docs/core/legal/floss_exception.html"

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU Affero General Public License for more details.

  You should have received a copy of the GNU Affero General Public License
  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  --%>

<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

<%@ page isELIgnored="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>
<fmt:setLocale value="${pageContext.request.locale.language}"/>
<view:setBundle basename="org.silverpeas.authentication.multilang.authentication"/>
<%@ include file="headLog.jsp" %>

<%@ page import="org.silverpeas.core.admin.user.model.UserDetail" %>
<%@ page import="org.silverpeas.components.kmelia.KmeliaAuthorization" %>
<%@ page import="org.silverpeas.components.kmelia.service.KmeliaHelper" %>
<%@ page import="org.silverpeas.core.admin.service.OrganizationControllerProvider" %>
<%@ page import="org.silverpeas.core.util.LocalizationBundle" %>
<%@ page import="org.silverpeas.core.admin.service.OrganizationController" %>

<%
  HttpSession httpSession = request.getSession();
  String redirection = (String) httpSession.getAttribute("gotoNew");
  LocalizationBundle mesLook =
      ResourceLocator.getLocalizationBundle("org.silverpeas.lookSilverpeasV5.multilang.lookBundle",
          request.getLocale().getLanguage());
  LocalizationBundle authenticationBundle = ResourceLocator.getLocalizationBundle(
      "org.silverpeas.authentication.multilang.authentication", request.getLocale().getLanguage());

  String errorCode = request.getParameter("ErrorCode");
  if (errorCode == null || errorCode.equals("null")) {
    errorCode = "";
  }

  String componentId = (String) httpSession.getAttribute("RedirectToComponentId");
  String spaceId = (String) httpSession.getAttribute("RedirectToSpaceId");
  UserDetail anonymousUser = UserDetail.getAnonymousUser();

  boolean isAnonymousAccessAuthorized = false;
  if (redirection == null && componentId == null && spaceId == null) {
    isAnonymousAccessAuthorized = true;
  } else {
    OrganizationController organization =
        OrganizationControllerProvider.getOrganisationController();
    if (organization.isAnonymousAccessActivated()) {
      if (componentId != null) {
        if (organization.isComponentAvailable(componentId, anonymousUser.getId())) {
          isAnonymousAccessAuthorized = true;
        }

        if (isAnonymousAccessAuthorized && redirection != null &&
            componentId.startsWith("kmelia")) {
          String objectId = KmeliaHelper.extractObjectIdFromURL(redirection);
          String objectType = KmeliaHelper.extractObjectTypeFromURL(redirection);
          if ("Publication".equals(objectType)) {
            KmeliaAuthorization security = new KmeliaAuthorization(organization);
            isAnonymousAccessAuthorized =
                security.isAccessAuthorized(componentId, anonymousUser.getId(), objectId);
          }
        }
      } else if (spaceId != null) {
        if (organization.isSpaceAvailable(spaceId, anonymousUser.getId())) {
          isAnonymousAccessAuthorized = true;
        }
      }
    }
  }
%>

<html>
<head>

  <% if (!isAnonymousAccessAuthorized) { %>
  <title><%=generalMultilang.getString("GML.popupTitle")%>
  </title>
  <view:includePlugin name="jquery"/>
  <view:includePlugin name="tkn"/>
  <link rel="SHORTCUT ICON" href="<%=request.getContextPath()%>/util/icons/favicon.ico"/>
  <link type="text/css" rel="stylesheet" href="<%=styleSheet%>"/>
  <!--[if lt IE 8]>
  <style>
    input {
      background-color: #FAFAFA;
      border: 1px solid #DAD9D9;
      width: 448px;
      text-align: left;
      margin-left: -10px;
      height: 26px;
      line-height: 24px;
      padding: 0px 60px;
      display: block;
      padding: 0px;
    }
  </style>
  <![endif]-->

  <script type="text/javascript">
    function getCookieVal(offset) {
      var endstr = document.cookie.indexOf(";", offset);
      if (endstr === -1) {
        endstr = document.cookie.length;
      }
      return unescape(document.cookie.substring(offset, endstr));
    }

    function GetCookie(name) {
      var arg = name + "=";
      var alen = arg.length;
      var clen = document.cookie.length;
      var i = 0;
      while (i < clen) {
        var j = i + alen;
        if (document.cookie.substring(i, j) === arg)
          return getCookieVal(j);
        i = document.cookie.indexOf(" ", i) + 1;
        if (i === 0) break;
      }

      return null;
    }

    function checkForm() {
      var form = document.getElementById("EDform");
      <% if (authenticationSettings.getBoolean("cookieEnabled", false)) { %>
      if (GetCookie("svpPassword") !== document.getElementById("EDform").Password.value) {
        form.cryptedPassword.value = "";
      } else {
        if (form.storePassword.checked)
          form.storePassword.click();
      }
      <% } %>
      form.action = '<c:url value="/AuthenticationServlet" />';
      form.submit();
    }

    function loginQuestion() {
      var form = document.getElementById("EDform");
      if (form.elements["Login"].value.length === 0) {
        alert("<%=authenticationBundle.getString("authentication.logon.loginMissing") %>");
      } else {
        form.action = '<c:url value="/CredentialsServlet/LoginQuestion" />';
        form.submit();
      }
    }

    function resetPassword() {
      var form = document.getElementById("EDform");
      if (form.elements["Login"].value.length === 0) {
        alert("<%=authenticationBundle.getString("authentication.logon.loginMissing") %>");
      } else {
        form.action = '<c:url value="/CredentialsServlet/ForgotPassword" />';
        form.submit();
      }
    }

    function checkSubmit(ev) {
      var touche = ev.keyCode;
      if (touche === 13)
        checkForm();
    }
  </script>
  <% } %>
</head>


<body>
<%
  if (isAnonymousAccessAuthorized) { %>
<form id="EDform" action="<c:url value="AuthenticationServlet" />" method="post" accept-charset="UTF-8">
  <input type="hidden" name="Login" value="<%= anonymousUser.getLogin() %>"/>
  <input type="hidden" name="Password" value="<%= anonymousUser.getLogin() %>"/>
  <input type="hidden" name="DomainId" value="0"/>
</form>

<script language="javascript1.2">
  var form = document.getElementById("EDform");
  form.submit();
</script>
<% } else {
  // list of domains
  //------------------------------------------------------------------
%>
<form id="EDform" action="javascript:checkForm();" method="post" accept-charset="UTF-8">
  <div id="top"></div>
  <!-- Backgroud fonce -->
  <div class="page"> <!-- Centrage horizontal des ?l?ments (960px) -->
    <div class="titre"><%=authenticationBundle.getString("authentication.logon.title") %>
    </div>
    <div id="background"> <!-- image de fond du formulaire -->
      <div class="cadre">
        <div id="header">
          <img src="<%=logo%>" class="logo"/>

          <p class="information">
            <%=mesLook.getString("lookSilverpeasV5.anonymousUnauthorized")%>
            <% if (!errorCode.equals("") && !errorCode.equals("4")) { %>
            <br/><span><%=authenticationBundle
              .getString("authentication.logon." + errorCode)%></span>
            <% } %>
          </p>

          <div class="clear"></div>
        </div>
        <p><label><span><%=authenticationBundle.getString(
            "authentication.logon.login") %></span><input type="text" name="Login" id="Login"/><input type="hidden" class="noDisplay" name="cryptedPassword"/></label>
        </p>

        <p><label><span><%=authenticationBundle.getString("authentication.logon.password") %></span><input type="password" name="Password" id="Password" onkeydown="checkSubmit(event)"/></label>
        </p>
        <% if (!multipleDomains) { %>
        <input class="noDisplay" type="hidden" name="DomainId" value="<%=listDomains.get(0).getId()%>"/>
        <% } else { %>
        <p><label><span><fmt:message key="authentication.logon.domain"/></span>
          <select id="DomainId" name="DomainId" size="1">
            <c:forEach var="domain" items="${pageScope.listDomains}">
              <option value="<c:out value="${domain.id}" />"
                      <c:if test="${domain.id eq param.DomainId}">selected</c:if> >
                <c:out value="${domain.name}"/></option>
            </c:forEach>
          </select>
        </label></p>
        <% } %>
        <p class="button">
          <a href="#" class="<%=submitClass%>" onclick="checkForm();"><img src="<%=request.getContextPath()%>/images/bt-login.png"/></a>
        </p>
        <% if (rememberPwdActive || forgottenPwdActive) { %>
        <p>
          <% if (forgottenPwdActive) { %>
							<span class="forgottenPwd">
							<% if ("personalQuestion".equalsIgnoreCase(pwdResetBehavior)) { %>
								<a href="javascript:loginQuestion()"><%=authenticationBundle
                    .getString("authentication.logon.passwordForgotten") %>
                </a>
							<% } else { %>
								<a href="javascript:resetPassword()"><%=authenticationBundle
                     .getString("authentication.logon.passwordReinit") %>
                 </a>
							<%} %>
							</span>
          <% } %>
          <% if (rememberPwdActive) { %>
								<span class="rememberPwd">
								<% if (forgottenPwdActive) { %>
									 |
								<% } %>
								<fmt:message key="authentication.logon.passwordRemember"/> <input type="checkbox" name="storePassword" id="storePassword" value="Yes"/></span>
          <% } %>
        </p>
        <% } %>
      </div>
    </div>
    <div id="copyright"><fmt:message key="GML.trademark"/></div>
  </div>
  <!-- Fin class="page" -->
</form>
<script type="text/javascript">
  document.getElementById("EDform").Login.focus();
</script>

<% } %>
</body>

</html>