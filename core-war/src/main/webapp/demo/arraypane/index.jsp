<%@ page import="java.util.List" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="org.silverpeas.core.admin.user.model.User" %>
<%@ page import="org.silverpeas.core.admin.user.model.UserDetail" %>
<%@ page import="org.silverpeas.core.admin.user.constant.UserAccessLevel" %>
<%@ page import="org.apache.commons.lang3.time.DurationFormatUtils" %><%--
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
<%@ page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%!
  private User createAccount(final String lastName, final String firstName, final UserAccessLevel userAccessLevel) {
    UserDetail user = new UserDetail();
    user.setLastName(lastName);
    user.setFirstName(firstName);
    user.setAccessLevel(userAccessLevel);
    return user;
  }
%>
<%
  long start = System.currentTimeMillis();
  List<User> data = new ArrayList<>();
  for (int i=0 ; i < 50000 ; i++) {
    data.add(createAccount("SilverAdmin_" + i, "_" + i, UserAccessLevel.ADMINISTRATOR));
    data.add(createAccount("Moquillon_", "Miguel_" + i, UserAccessLevel.USER));
    data.add(createAccount("Chastagnier_", "Yohann_" + i, UserAccessLevel.USER));
    data.add(createAccount("Guest_", "_" + i, UserAccessLevel.GUEST));
  }
  request.setAttribute("accounts", data);
  long end = System.currentTimeMillis();
  String duration = DurationFormatUtils.formatDurationHMS(end - start);
  start = System.currentTimeMillis();
%>

<html xmlns="http://www.w3.org/1999/xhtml">
<head>
  <title>Array panes Demo</title>
  <view:looknfeel/>
</head>
<body>
<div id="container-to-refresh-after-ajax-request">

  <div>
    Data generation duration : <c:out value="<%=duration%>"/>
  </div>

  <%--<c:set var="accounts" value="${requestScope.accounts}"/>--%>
  <%--<jsp:useBean id="accounts" type="java.util.List<org.silverpeas.core.admin.user.model.User>"/>--%>
  <c:set var="accounts" value="<%=data%>"/>
  <c:url var="routingAddress" value="/demo/arraypane/index.jsp"/>

  <%--############################################################################################--%>
  <%-- Initial --%>
  <%--############################################################################################--%>
  <%--<view:arrayPane var="myForms" routingAddress="${routingAddress}" numberLinesPerPage="25">--%>
    <%--<view:arrayColumn title="Nom" sortable="true"/>--%>
    <%--<view:arrayColumn title="Prénom" sortable="true"/>--%>
    <%--<view:arrayColumn title="Type de compte" sortable="true"/>--%>
    <%--<view:arrayColumn title="Dernière connexion" sortable="true"/>--%>
    <%--<c:forEach var="account" items="${accounts}">--%>
      <%--<view:arrayLine>--%>
        <%--<view:arrayCellText text="${account.lastName}"/>--%>
        <%--<view:arrayCellText text="${account.firstName}"/>--%>
        <%--<c:choose>--%>
          <%--<c:when test="${account.accessLevel.name() eq 'ADMINISTRATOR'}">--%>
            <%--<view:arrayCellText text="Administrateur"/>--%>
          <%--</c:when>--%>
          <%--<c:when test="${account.accessLevel.name() eq 'USER'}">--%>
            <%--<view:arrayCellText text="Utilisateur"/>--%>
          <%--</c:when>--%>
          <%--<c:otherwise>--%>
            <%--<view:arrayCellText text="Invité"/>--%>
          <%--</c:otherwise>--%>
        <%--</c:choose>--%>
        <%--<view:arrayCellText text="${account.lastLoginDate}"/>--%>
      <%--</view:arrayLine>--%>
    <%--</c:forEach>--%>
  <%--</view:arrayPane>--%>

  <%--############################################################################################--%>
  <%-- <view:arrayLines compareOn="..."/> & lambda --%>
  <%--############################################################################################--%>
  <%--<c:set var="accountLabelLambda" value="${a ->--%>
                <%--(a.accessLevel.name() eq 'ADMINISTRATOR' ? 'Administrateur' :--%>
                <%--(a.accessLevel.name() eq 'USER' ? 'Utilisateur' : 'Invité'))}"/>--%>
  <%--<view:arrayPane var="myForms" routingAddress="${routingAddress}" numberLinesPerPage="25">--%>
    <%--<view:arrayColumn title="Nom" compareOn="${a -> fn:toLowerCase(a.lastName)}"/>--%>
    <%--<view:arrayColumn title="Prénom" compareOn="${a -> fn:toLowerCase(a.firstName)}"/>--%>
    <%--<view:arrayColumn title="Type de compte" compareOn="${accountLabelLambda}"/>--%>
    <%--<view:arrayColumn title="Dernière connexion" compareOn="${a -> a.lastLoginDate}"/>--%>
    <%--<view:arrayLines var="account" items="${accounts}">--%>
      <%--<view:arrayLine>--%>
        <%--<view:arrayCellText text="${account.lastName}"/>--%>
        <%--<view:arrayCellText text="${account.firstName}"/>--%>
        <%--<view:arrayCellText text="${accountLabelLambda(account)}"/>--%>
        <%--<view:arrayCellText text="${account.lastLoginDate}"/>--%>
      <%--</view:arrayLine>--%>
    <%--</view:arrayLines>--%>
  <%--</view:arrayPane>--%>

  <%--############################################################################################--%>
  <%-- AJAX CONTROLS
  <%--############################################################################################--%>
  <c:set var="accountLabelLambda" value="${a ->
                (a.accessLevel.name() eq 'ADMINISTRATOR' ? 'Administrateur' :
                (a.accessLevel.name() eq 'USER' ? 'Utilisateur' : 'Invité'))}"/>
  <view:arrayPane var="myForms" routingAddress="${routingAddress}" numberLinesPerPage="25">
    <view:arrayColumn title="Nom" compareOn="${a -> fn:toLowerCase(a.lastName)}"/>
    <view:arrayColumn title="Prénom" compareOn="${a -> fn:toLowerCase(a.firstName)}"/>
    <view:arrayColumn title="Type de compte" compareOn="${accountLabelLambda}"/>
    <view:arrayColumn title="Dernière connexion" compareOn="${a -> a.lastLoginDate}"/>
    <view:arrayLines var="account" items="${accounts}">
      <view:arrayLine>
        <view:arrayCellText text="${account.lastName}"/>
        <view:arrayCellText text="${account.firstName}"/>
        <view:arrayCellText text="${accountLabelLambda(account)}"/>
        <view:arrayCellText text="${account.lastLoginDate}"/>
      </view:arrayLine>
    </view:arrayLines>
  </view:arrayPane>
  <script type="text/javascript">
    whenSilverpeasReady(function() {
      sp.arrayPane.ajaxControls('#container-to-refresh-after-ajax-request');
    });
  </script>


  <%
    end = System.currentTimeMillis();
    String arrayPaneBuildDuration = DurationFormatUtils.formatDurationHMS(end - start);
  %>

  <div>
    Array pane build duration : <c:out value="<%=arrayPaneBuildDuration%>"/>
  </div>

</div>
</body>
</html>
