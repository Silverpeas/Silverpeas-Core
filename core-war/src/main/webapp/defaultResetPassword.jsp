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
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"
"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<%@page import="org.silverpeas.core.admin.user.model.UserDetail" %>
<%@ page import="org.silverpeas.core.util.ResourceLocator" %>
<%@ page import="org.silverpeas.core.util.LocalizationBundle" %>
<%@ page import="org.silverpeas.core.ui.DisplayI18NHelper" %>
<%@ include file="headLog.jsp" %>

<%
  LocalizationBundle authenticationBundle =
      ResourceLocator.getLocalizationBundle("org.silverpeas.authentication.multilang.authentication",
          request.getLocale().getLanguage());
  UserDetail userDetail = (UserDetail) request.getAttribute("userDetail");
%>

<html xmlns="http://www.w3.org/1999/xhtml">
<head>
  <title><%=generalMultilang.getString("GML.popupTitle")%></title>
  <link type="text/css" rel="stylesheet" href="<%=styleSheet%>"/>
  <link type="text/css" rel="stylesheet" href="<%=m_context%>/util/styleSheets/silverpeas-password.css"/>
  <view:includePlugin name="jquery"/>
  <view:includePlugin name="tkn"/>
  <script src="<%=m_context%>/password.js" type="text/javascript"></script>
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
  <script type="text/javascript">
    var webContext = '<%=m_context%>';
    $(document).ready(function() {
      $('#password').focus();
      handlePasswordForm({
        passwordFormId : 'changePwdForm',
        passwordFormAction : '<c:url value="/CredentialsServlet/ChangePassword"/>',
        passwordInputId : 'password'
      });
    });
  </script>
  <script src="<%=m_context%>/util/javaScript/silverpeas-password.js" type="text/javascript"></script>
</head>

<body>
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
            "authentication.password.new") %> </span><input type="password" name="password" id="password"/></label>
        </p>

        <p><label><span><%=authenticationBundle.getString(
            "authentication.password.confirm") %></span><input type="password" name="confirmPassword" id="confirmPassword"/></label>
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
</body>
</html>