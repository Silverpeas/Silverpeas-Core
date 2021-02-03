<%--
  ~ Copyright (C) 2000 - 2021 Silverpeas
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
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>

<%@ include file="headLog.jsp" %>
<%
  LocalizationBundle reinitPasswordBundle = ResourceLocator.getLocalizationBundle(
      "org.silverpeas.authentication.multilang.forgottenPasswordMail",
      userLanguage);
  LocalizationBundle authenticationBundle = ResourceLocator.getLocalizationBundle(
      "org.silverpeas.authentication.multilang.authentication",
      userLanguage);

  String action = request.getParameter("Action");
  String actionLabel = "";
  String actionTitle = reinitPasswordBundle.getString("screen.title.reinitRequested");

  if ("InvalidLogin".equalsIgnoreCase(action)) {
    actionLabel = reinitPasswordBundle.getString("screen.invalidLogin");
  } else if ("FirstMailSended".equalsIgnoreCase(action)) {
    actionLabel = reinitPasswordBundle.getString("screen.reinitRequested");
  } else if ("ChangeNotAllowed".equalsIgnoreCase(action)) {
    actionLabel = reinitPasswordBundle.getString("screen.reinitNotAllowed");
  } else if ("NewPasswordSended".equalsIgnoreCase(action)) {
    actionLabel = reinitPasswordBundle.getString("screen.reinitDone");
    actionTitle = reinitPasswordBundle.getString("screen.title.reinitDone");
  } else if ("NewPasswordError".equalsIgnoreCase(action)) {
    actionLabel = reinitPasswordBundle.getString("screen.reinitError");
  } else if ("GeneralError".equalsIgnoreCase(action)) {
    actionLabel = reinitPasswordBundle.getString("screen.generalError");
  }
%>

<view:sp-page>
<view:sp-head-part noLookAndFeel="true">
  <link rel="icon" href="<%=favicon%>" />
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
      display: block;
      padding: 0;
    }
  </style>
  <![endif]-->
</view:sp-head-part>
<view:sp-body-part>
<div id="top"></div>
<div class="page">
  <div class="titre"><%=authenticationBundle.getString("authentication.logon.title") %>
  </div>
  <div id="background">
    <div class="cadre">
      <div id="header">
        <img src="<%=logo%>" class="logo" alt=""/>

        <p class="information"><%=actionTitle %>
        </p>

        <div class="clear"></div>
      </div>
      <p><label><%=actionLabel %>
      </label></p>

      <div class="submit">
        <p>
          <a href="#" class="submit" onclick="location.href='<%=request.getContextPath()%>/Login'"><span><span>OK</span></span></a>
        </p>
      </div>
    </div>
  </div>
</div>
</view:sp-body-part>
</view:sp-page>