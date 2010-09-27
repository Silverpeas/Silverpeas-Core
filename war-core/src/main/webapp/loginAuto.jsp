<%--

    Copyright (C) 2000 - 2009 Silverpeas

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    As a special exception to the terms and conditions of version 3.0 of
    the GPL, you may redistribute this Program in connection with Free/Libre
    Open Source Software ("FLOSS") applications as described in Silverpeas's
    FLOSS exception.  You should have received a copy of the text describing
    the FLOSS exception, and it is also available here:
    "http://repository.silverpeas.com/legal/licensing"

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

--%>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="headLog.jsp" %>

<%@ page import="java.util.*"%>
<%@ page import="com.stratelia.webactiv.beans.admin.OrganizationController"%>
<%@ page import="com.stratelia.webactiv.kmelia.control.ejb.KmeliaHelper"%>
<%@ page import="com.stratelia.webactiv.kmelia.KmeliaSecurity"%>

<%
   HttpSession httpSession = request.getSession();
    String redirection = (String) httpSession.getAttribute("gotoNew");
    ResourceLocator mesLook = new ResourceLocator(
        "com.silverpeas.lookSilverpeasV5.multilang.lookBundle", "fr");
    ResourceLocator authenticationBundle = new ResourceLocator(
        "com.silverpeas.authentication.multilang.authentication", "");

    String errorCode = request.getParameter("ErrorCode");
    if (errorCode == null || errorCode.equals("null")) {
      errorCode = "";
    }

String componentId 	= (String) httpSession.getAttribute("RedirectToComponentId");
String spaceId 		= (String) httpSession.getAttribute("RedirectToSpaceId");

boolean isAnonymousAccessAuthorized = false;
if (redirection == null && componentId == null && spaceId == null)
{
	isAnonymousAccessAuthorized = true;
}
else
{
	OrganizationController organization = new OrganizationController();
    ResourceLocator settings = new ResourceLocator(
        "com.stratelia.webactiv.util.viewGenerator.settings.SilverpeasV5", "");
    String guestId = settings.getString("guestId");

	if (guestId != null)
	{
		if (componentId != null)
		{
			if (organization.isComponentAvailable(componentId, guestId))
			{
				isAnonymousAccessAuthorized = true;
			}

			if (isAnonymousAccessAuthorized && redirection != null && componentId.startsWith("kmelia"))
			{
				String objectId = KmeliaHelper.extractObjectIdFromURL(redirection);
				String objectType = KmeliaHelper.extractObjectTypeFromURL(redirection);
				if ("Publication".equals(objectType))
				{
					KmeliaSecurity security = new KmeliaSecurity(organization);
					isAnonymousAccessAuthorized = security.isAccessAuthorized(componentId, guestId, objectId);
				}
			}
		}
		else if (spaceId != null)
		{
			if (organization.isSpaceAvailable(spaceId, guestId))
			{
				isAnonymousAccessAuthorized = true;
			}
		}
	}
}
%>

<html>
<head>

<% if (!isAnonymousAccessAuthorized) { %>
<title><%=generalMultilang.getString("GML.popupTitle")%></title>
<link REL="SHORTCUT ICON" HREF="<%=request.getContextPath()%>/util/icons/favicon.ico" />
<link type="text/css" rel="stylesheet" href="<%=styleSheet%>" />
<!--[if lt IE 8]>
<style>
input{
	background-color:#FAFAFA;
	border:1px solid #DAD9D9;
	width:448px;
	text-align:left;
    margin-left:-10px;
    height:26px;
    line-height:24px;
    padding:0px 60px;
    display:block;
    padding:0px;
}
</style>
<![endif]-->

<script type="text/javascript">
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
	var form = document.getElementById("EDform");
	<% if (authenticationSettings.getBoolean("cookieEnabled", false)) { %>
		if (GetCookie("svpPassword") != document.getElementById("EDform").Password.value)
		{
			form.cryptedPassword.value = "";
		}
		else
		{
			if (form.storePassword.checked)
				form.storePassword.click();
		}
	<% } %>
	form.action="/silverpeas/AuthenticationServlet";
	form.submit();
}

function loginQuestion() {
	var form = document.getElementById("EDform");
    if (form.elements["Login"].value.length == 0) {
        alert("<%=authenticationBundle.getString("authentication.logon.loginMissing") %>");
    } else {
    	form.action = "<%=m_context%>/CredentialsServlet/LoginQuestion";
    	form.submit();
    }
}

function resetPassword() {
	var form = document.getElementById("EDform");
    if (form.elements["Login"].value.length == 0) {
        alert("<%=authenticationBundle.getString("authentication.logon.loginMissing") %>");
    } else {
    	form.action = "<%=m_context%>/CredentialsServlet/ForgotPassword";
    	form.submit();
    }
}

function checkSubmit(ev)
{
	var touche = ev.keyCode;
	if (touche == 13)
		checkForm();
}
</script>
<% } %>
</head>


<%
if (isAnonymousAccessAuthorized) { %>
<body>
	<form id="EDform" action="<%=m_context%>/AuthenticationServlet" method="post" accept-charset="UTF-8">
		<input type="hidden" name="Login" value="guest">
		<input type="hidden" name="Password" value="guest">
		<input type="hidden" name="DomainId" value="0">
	</form>

	<script language="javascript1.2">
		var form = document.getElementById("EDform");
		form.submit();
	</script>
<% } else {
	// list of domains
	java.util.List domainsIds = lpAuth.getDomainsIds();
	//------------------------------------------------------------------
%>
<body>
<form id="EDform" action="javascript:checkForm();" method="post" accept-charset="UTF-8">
<div id="top"></div> <!-- Backgroud fonce -->
        <div class="page"> <!-- Centrage horizontal des ?l?ments (960px) -->
            <div class="titre"><%=authenticationBundle.getString("authentication.logon.title") %></div>
            <div id="background"> <!-- image de fond du formulaire -->
                <div class="cadre">
                    <div id="header">
                        <img src="<%=logo%>" class="logo" />
                        <p class="information">
                        <%=mesLook.getString("lookSilverpeasV5.anonymousUnauthorized")%>
								<% if (!errorCode.equals("") && !errorCode.equals("4")) { %>
									<span><%=authenticationBundle.getString("authentication.logon."+errorCode)%></span>
								<% } else { %>
									<%=authenticationBundle.getString("authentication.logon.subtitle") %>
								<% } %>
                        </p>
                        <div class="clear"></div>
                    </div>
                    <p><label><span><%=authenticationBundle.getString("authentication.logon.login") %></span><input type="text" name="Login" id="Login"/><input type="hidden" class="noDisplay" name="cryptedPassword"/></label></p>
                    <p><label><span><%=authenticationBundle.getString("authentication.logon.password") %></span><input type="password" name="Password" id="Password" onkeydown="checkSubmit(event)"/></label></p>

					 <% if (domains != null && domains.size() == 1) { %>
                            <input class="noDisplay"type="hidden" name="DomainId" value="<%=domainIds.get(0)%>"/>
                     <%	} else { %>
                          <p><label><span><%=authenticationBundle.getString("authentication.logon.domain") %></span>
								<select id="DomainId" name="DomainId" size="1">
									<% if (domains==null ||domains.size()==0) { %>
										<option> --- </option>
									<%  } else {
										String dId 		= null;
										String dName 	= null;
										String selected	= "";

										for (Enumeration e = domains.keys() ; e.hasMoreElements() ;)
										{
											dId 		= (String) e.nextElement();
											dName 		= (String) domains.get(dId);
											selected 	= "";

											if (dId.equals(request.getAttribute("Silverpeas_DomainId")))
												selected = "selected=\"selected\"";
									%>
										<option value="<%=dId%>" <%=selected%>><%=dName%></option>
									<%  }
									}
										%>
								</select>
                          </label></p>
                     <% } %>
                     <p><a href="#" class="submit" onclick="checkForm();"><img src="<%=request.getContextPath()%>/images/bt-login.png" /></a></p>
					 <% if (rememberPwdActive || forgottenPwdActive) { %>
						 <p>
						 <% if (forgottenPwdActive) { %>
							<span class="forgottenPwd">
							<% if ("personalQuestion".equalsIgnoreCase(pwdResetBehavior)) { %>
								<a href="javascript:loginQuestion()"><%=authenticationBundle.getString("authentication.logon.passwordForgotten") %></a>
							<% } else { %>
							 	<a href="javascript:resetPassword()"><%=authenticationBundle.getString("authentication.logon.passwordReinit") %></a>
							<%} %>
							</span>
						 <% } %>
						 <% if (rememberPwdActive) { %>
								<span class="rememberPwd">
								<% if (forgottenPwdActive) { %>
									 |
								<% } %>
								<%=authenticationBundle.getString("authentication.logon.passwordRemember") %> <input type="checkbox" name="storePassword" id="storePassword" value="Yes"/></span>
						<%	} %>
						</p>
					<% } %>
                </div>
            </div>
            <div id="copyright"><%=generalMultilang.getString("GML.trademark")%></div>
        </div>
    <!-- Fin class="page" -->
</form>
		<script type="text/javascript">
			document.getElementById("EDform").Login.focus();
		</script>

<% } %>
</body>

</html>