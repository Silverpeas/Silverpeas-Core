<%--
  ~ Copyright (C) 2000 - 2020 Silverpeas
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
  ~ along with this program.  If not, see <http://www.gnu.org/licenses/>.
  --%>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"
"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<%@page import="org.silverpeas.core.admin.user.model.UserDetail" %>
<%@ include file="headLog.jsp" %>

<%
  UserDetail userDetail = (UserDetail) request.getAttribute("userDetail");
  LocalizationBundle authenticationBundle =
      ResourceLocator.getLocalizationBundle("org.silverpeas.authentication.multilang.authentication",
          userLanguage);
%>

<view:sp-page>
<view:sp-head-part minimalSilverpeasScriptEnv="true">
  <link rel="icon" href="<%=favicon%>" />
  <link type="text/css" rel="stylesheet" href="<%=styleSheet%>"/>
  <link type="text/css" rel="stylesheet" href="<%=m_context%>/util/styleSheets/silverpeas-password.css"/>
  <view:includePlugin name="virtualkeyboard"/>
  <view:includePlugin name="popup"/>
  <view:script src="/password.js"/>
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
  <div id="top"></div>
  <div class="page">
    <div class="titre"><%=authenticationBundle.getString("authentication.logon.title") %>
    </div>
    <div id="background">
      <div class="cadre">
        <div id="header">
          <img src="<%=logo%>" class="logo" alt=""/>

          <p class="information"><%=authenticationBundle
              .getString("authentication.password.init") %>
          </p>

          <div class="clear"></div>
        </div>
        <p><label><span><%=authenticationBundle.getString(
            "authentication.password.new") %> </span><input type="password" autocomplete="off" name="password" id="password"/></label>
        </p>

        <p><label><span><%=authenticationBundle.getString(
            "authentication.password.confirm") %></span><input type="password" autocomplete="off" name="confirmPassword" id="confirmPassword"/></label>
        </p>

        <div class="submit">
          <p><input type="submit" style="width:0; height:0; border:0; padding:0"/>
            <a href="#" class="submit" onclick="$('#changePwdForm').submit()"><span><span>OK</span></span></a>
          </p>

          <p>
          <span class="passwordRules"><a href="#" onclick="$('#password').focus()">
            <%=authenticationBundle.getString("authentication.password.showRules") %>
          </a></span>
          </p>
        </div>
      </div>
    </div>
    <input type="hidden" name="Login" value="<%=userDetail.getLogin()%>"/>
    <input type="hidden" name="DomainId" value="<%=userDetail.getDomainId()%>"/>
  </div>
</form>
<script type="text/javascript">
  setTimeout(function() {
    $('#password').focus();
    handlePasswordForm({
      passwordFormId : 'changePwdForm',
      passwordFormAction : '<c:url value="/CredentialsServlet/ChangePassword"/>',
      passwordInputId : 'password'
    });
  }, 0);
</script>
<view:script src="/util/javaScript/silverpeas-password.js"/>
</view:sp-body-part>
</view:sp-page>