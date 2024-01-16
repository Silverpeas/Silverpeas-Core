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
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>

<%@ include file="headLog.jsp" %>

<%
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
  <div class="page">
    <div class="titre"><%=authenticationBundle.getString("authentication.logon.title") %>
    </div>
    <div id="backgroundBig">
      <div class="cadre">
        <div id="header" style="display: table; width: 100%">
          <div style="display: table-cell">
            <img src="<%=logo%>" class="logo" alt="logo"/>
          </div>
          <div class="information" style="display: table-cell; width: 100%; text-align: right">
            <%=authenticationBundle.getString("authentication.password.init")%>
            <c:if test="${not empty requestScope.message}">
              <br/><span><c:out value="${requestScope.message}" escapeXml="false"/></span>
            </c:if>
          </div>
          <div class="clear"></div>
        </div>
        <p><label><span><%=authenticationBundle.getString(
            "authentication.password.old") %></span><input type="password" autocomplete="new-password" name="oldPassword" id="oldPassword"/></label>
        </p>

        <p><label><span><%=authenticationBundle.getString(
            "authentication.password.new") %> </span><input type="password" autocomplete="new-password" name="newPassword" id="newPassword"/></label>
        </p>

        <p><label><span><%=authenticationBundle.getString(
            "authentication.password.confirm") %></span><input type="password" autocomplete="new-password" name="confirmPassword" id="confirmPassword"/></label>
        </p>

        <div class="submit">
          <p><input type="submit" style="width:0; height:0; border:0; padding:0"/>
            <a href="#" class="submit" onclick="$('#changePwdForm').submit()"><span><span>OK</span></span></a>
          </p>

          <p>
          <span class="passwordRules"><a href="#" onclick="$('#newPassword').focus()">
            <%=authenticationBundle.getString("authentication.password.showRules") %>
          </a></span>
          </p>
        </div>
      </div>
    </div>
  </div>
</form>
<view:loadScript src="/util/javaScript/silverpeas-password.js" jsPromiseName="loadingPromise"/>
<script type="text/javascript">
  loadingPromise.then(function() {
    setTimeout(function() {
      $('#oldPassword').focus();
      handlePasswordForm({
        passwordFormId : 'changePwdForm',
        passwordFormAction : '<c:url value="/CredentialsServlet/EffectiveChangePassword"/>',
        passwordInputId : 'newPassword'
      });
    }, 0);
  });
</script>
</view:sp-body-part>
</view:sp-page>
