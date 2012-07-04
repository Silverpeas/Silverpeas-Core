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

<%@ page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ page isELIgnored="false"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<fmt:setLocale value="${pageContext.request.locale.language}" />
<%@ include file="headLog.jsp" %>

<%
ResourceLocator authenticationBundle = new ResourceLocator("com.silverpeas.authentication.multilang.authentication", "");
pageContext.setAttribute("authenticationBundle", authenticationBundle.getResourceBundle());
String errorCode = request.getParameter("ErrorCode");
if (!com.silverpeas.util.StringUtil.isDefined(errorCode)) {
	errorCode = "";
}

String domainId = null;
if(com.silverpeas.util.StringUtil.isInteger(request.getParameter("DomainId"))) {
  domainId = request.getParameter("DomainId");
  request.setAttribute("Silverpeas_DomainId", domainId);
}
%>
<view:setBundle basename="com.silverpeas.authentication.multilang.authentication" />
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"
        "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title><fmt:message key="GML.popupTitle" /></title>
<link rel="SHORTCUT ICON" href='<c:url value="/util/icons/favicon.ico" />'/>
<link type="text/css" rel="stylesheet" href="<%=styleSheet%>" />

<script type="text/javascript">
<!---
// Public domain cookie code written by:
// Bill Dortch, hIdaho Design
// (bdortch@netw.com)
function getCookieVal (offset) {
	var endstr = document.cookie.indexOf (";", offset);
    if (endstr == -1)
    {
    	endstr = document.cookie.length;
    }
    return unescape(document.cookie.substring(offset, endstr));
}

function GetCookie (name) {
         var arg = name + "=";
         var alen = arg.length;
         var clen = document.cookie.length;
         var i = 0;
         while (i < clen) {
         var j = i + alen;
             if (document.cookie.substring(i, j) == arg)
             return getCookieVal(j);
         i = document.cookie.indexOf(" ", i) + 1;
             if (i == 0) break;
             }

     return null;
     }

function checkForm()
{
	var form = document.getElementById("formLogin");
	<% if (authenticationSettings.getBoolean("cookieEnabled", false)) { %>
		if (GetCookie("svpPassword") != document.getElementById("formLogin").Password.value)
		{
			form.cryptedPassword.value = "";
		}
		else
		{
			if (form.storePassword.checked) {
              form.storePassword.click();
            }
		}
	<% } %>
    form.action='<c:url value="/AuthenticationServlet" />';
	form.submit();
}

function loginQuestion() {
	var form = document.getElementById("formLogin");
    if (form.elements["Login"].value.length == 0) {
      alert('<fmt:message key="authentication.logon.loginMissing" />');
    } else {
      form.action ='<c:url value="/CredentialsServlet/LoginQuestion" />';
      form.submit();
    }
}

function resetPassword() {
	var form = document.getElementById("formLogin");
    if (form.elements["Login"].value.length == 0) {
      alert('<fmt:message key="authentication.logon.loginMissing" />');
    } else {
      form.action ='<c:url value="/CredentialsServlet/ForgotPassword" />';
      form.submit();
    }
}

function newRegistration() {
	var form = document.getElementById("formLogin");
    form.action ='<c:url value="/CredentialsServlet/NewRegistration" />';
    form.submit();
}

function checkSubmit(ev)
{
	var touche = ev.keyCode;
	if (touche == 13) {
      checkForm();
    }
}
-->
</script>
</head>
<body>
      <form id="formLogin" action="javascript:checkForm();" method="post" accept-charset="UTF-8">
      	<% if (newRegistrationActive || facebookEnabled || linkedInEnabled) { %>
	        <div id="top">
				<fmt:message key="authentication.logon.newRegistration.tease" /> 
				<% if (newRegistrationActive) { %> 
					<a href="javascript:newRegistration()"><fmt:message key="authentication.logon.newRegistration.create" /></a>
				<% } %>
				<% if (facebookEnabled || linkedInEnabled) {
					if (newRegistrationActive) { %>
						<fmt:message key="GML.or" />
					<% } %>
					<fmt:message key="authentication.logon.newRegistration.connect" />
					<% if (linkedInEnabled) { %>
						<a title="LinkedIn" href="<c:url value="/SocialNetworkLogin?networkId=LINKEDIN" />">LinkedIn</a>
					<% } %>
					<% if (facebookEnabled) { %>
						<% if (linkedInEnabled) { %>
							 <fmt:message key="GML.or" /> 
						<% } %>
						<a title="Facebook" href="<c:url value="/SocialNetworkLogin?networkId=FACEBOOK" />">Facebook</a> 
					<% } %>
				<% } %>
	        </div>
        <% } %>
        <% if (facebookEnabled || linkedInEnabled) {%>
			<div id="login-socialnetwork">
			   	<% if (linkedInEnabled) { %>
					<a title="<fmt:message key='authentication.logon.with.linkedin' />" href="<c:url value="/SocialNetworkLogin?networkId=LINKEDIN" />">
						<img src="util/icons/external/btn-login-linkedin.png"/>
					</a>
				<% } %>
			   	<% if (facebookEnabled) { %>
					<a title="<fmt:message key='authentication.logon.with.facebook' />" href="<c:url value="/SocialNetworkLogin?networkId=FACEBOOK" />">
						<img src="util/icons/external/btn-login-facebook.png"/>
					</a>
				<% } %>
			</div>
		<% } %>
        <div class="page"> <!-- Centrage horizontal des elements (960px) -->
          <div class="titre"><fmt:message key="authentication.logon.title"/></div>
            <div id="background"> <!-- image de fond du formulaire -->
                <div class="cadre">
                    <div id="header">
                        <img src="<%=logo%>" class="logo" alt="logo"/>
                        <p class="information">
                          <c:choose>
                            <c:when test="${!empty param.ErrorCode && '4' != param.ErrorCode && 'null' != param.ErrorCode}">
                              <c:set var="erroMessageKey">authentication.logon.<c:out value="${param.ErrorCode}"/></c:set>
                                <span><fmt:message key="${erroMessageKey}" /></span>
                            </c:when><c:otherwise>
                               <fmt:message key="authentication.logon.subtitle" />
                            </c:otherwise>
                          </c:choose>
                        </p>
                        <div class="clear"></div>
                    </div>   
                    <p><label><span><fmt:message key="authentication.logon.login" /></span><input type="text" name="Login" id="Login"/><input type="hidden" class="noDisplay" name="cryptedPassword"/></label></p>
                    <p><label><span><fmt:message key="authentication.logon.password" /></span><input type="password" name="Password" id="Password" onkeydown="checkSubmit(event)"/></label></p>
                    <c:choose>
                      <c:when test="${!pageScope.multipleDomains}">
                        <input class="noDisplay" type="hidden" name="DomainId" value="<%=domainIds.get(0)%>"/>
                      </c:when>
                      <c:otherwise>
                      	<p><label><span><fmt:message key="authentication.logon.domain" /></span>
							<select id="DomainId" name="DomainId" size="1">
                                  <c:forEach var="domain" items="${pageScope.listDomains}">
                                      <option value="<c:out value="${domain.id}" />" <c:if test="${domain.id eq param.DomainId}">selected</c:if> ><c:out value="${domain.name}"/></option>
                                  </c:forEach>
							</select>
                          </label></p> 
                      </c:otherwise>
                    </c:choose>            
                     <p><input type="submit" style="width:0; height:0; border:0; padding:0"/><a href="#" class="<%=submitClass%>" onclick="checkForm();"><img src='<c:url value="/images/bt-login.png" />' alt="login"/></a></p>

					 <% if (rememberPwdActive || forgottenPwdActive) { %>
						 <% if (forgottenPwdActive) { %>
						 <p>
							<span class="forgottenPwd">
							<% if ("personalQuestion".equalsIgnoreCase(pwdResetBehavior)) { %>
								<a href="javascript:loginQuestion()"><fmt:message key="authentication.logon.passwordForgotten" /></a>
							<% } else { %>
							 	<a href="javascript:resetPassword()"><fmt:message key="authentication.logon.passwordReinit" /></a>
							<%} %>
							</span>
						</p>
						 <% } %>

						 <% if (rememberPwdActive) { %>
						 		<p>
								<span class="rememberPwd">
								<% if (forgottenPwdActive) { %>
									 |
								<% } %>
								<fmt:message key="authentication.logon.passwordRemember" /> <input type="checkbox" name="storePassword" id="storePassword" value="Yes"/></span>
								</p>
						<%	} %>
					<% } %>
                </div>
            </div>
            <div id="copyright"><fmt:message key="GML.trademark" /></div>
        </div>
        </form><!-- Fin class="page" -->

		<script type="text/javascript">
			nbCookiesFound=0;
			var domainId = <%=domainId%>;

			/* Si le domainId n'est pas dans la requete, alors recuperation depuis le cookie */
			if(domainId == null && GetCookie("defaultDomain") != null)
			{
				<% for (int i = 0 ; i < listDomains.size() && listDomains.size() > 1; i++) { %>
					if (GetCookie("defaultDomain").toString() == "<%=(listDomains.get(i).getId())%>")
					{
						document.getElementById("DomainId").options[<%=i%>].selected = true;
					}
				<% } %>
			}

			if(GetCookie("svpLogin") != null)
			{
				nbCookiesFound = nbCookiesFound + 1;
				document.getElementById("Login").value = GetCookie("svpLogin").toString();
			}

			<%	if (authenticationSettings.getBoolean("cookieEnabled", false)) { %>
				if(GetCookie("svpPassword") != null)
				{
					nbCookiesFound = nbCookiesFound + 1;
					document.getElementById("Password").value = GetCookie("svpPassword").toString();
				}
			<%	} %>

			if (nbCookiesFound==2)
			{
				document.getElementById("formLogin").cryptedPassword.value = "Yes";
				<% if (!com.silverpeas.util.StringUtil.isDefined(request.getParameter("logout")) && authenticationSettings.getBoolean("autoSubmit", false)) { %>
					document.getElementById("formLogin").submit();
				<% } %>
			}
			else
			{
				document.getElementById("formLogin").Password.value = '';
				document.getElementById("formLogin").Login.focus();
			}
			document.getElementById("formLogin").Login.focus();
		</script>

</body>
</html>
