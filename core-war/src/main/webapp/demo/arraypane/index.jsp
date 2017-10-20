<%@ page import="java.util.List" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="org.silverpeas.core.admin.user.model.User" %>
<%@ page import="org.silverpeas.core.admin.user.model.UserDetail" %>
<%@ page import="org.silverpeas.core.admin.user.constant.UserAccessLevel" %><%--
  ~ Copyright (C) 2000 - 2017 Silverpeas
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
<%@ page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<html xmlns="http://www.w3.org/1999/xhtml">
<head>
  <title>Array panes Demo</title>
  <view:looknfeel/>
</head>
<body>
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
  List<User> data = new ArrayList<>();
  for (int i=0 ; i < 50000 ; i++) {
    data.add(createAccount("SilverAdmin_" + i, "_" + i, UserAccessLevel.ADMINISTRATOR));
    data.add(createAccount("Moquillon_", "Miguel_" + i, UserAccessLevel.USER));
    data.add(createAccount("Chastagnier_", "Yohann_" + i, UserAccessLevel.USER));
    data.add(createAccount("Guest_", "_" + i, UserAccessLevel.GUEST));
  }
  request.setAttribute("accounts", data);
%>

<%--<c:set var="accounts" value="${requestScope.accounts}"/>--%>
<%--<jsp:useBean id="accounts" type="java.util.List<org.silverpeas.core.admin.user.model.User>"/>--%>
<c:set var="accounts" value="<%=data%>"/>
<c:url var="routingAddress" value="/demo/arraypane/index.jsp"/>

<%-- Initial --%>
<view:arrayPane var="myForms" routingAddress="${routingAddress}" numberLinesPerPage="25">
  <view:arrayColumn title="Nom" sortable="true"/>
  <view:arrayColumn title="Prénom" sortable="true"/>
  <view:arrayColumn title="Type de compte" sortable="true"/>
  <c:forEach var="account" items="${accounts}">
    <view:arrayLine>
      <view:arrayCellText text="${account.lastName}"/>
      <view:arrayCellText text="${account.firstName}"/>
      <c:choose>
        <c:when test="${account.accessLevel.name() eq 'ADMINISTRATOR'}">
          <view:arrayCellText text="Administrateur"/>
        </c:when>
        <c:when test="${account.accessLevel.name() eq 'USER'}">
          <view:arrayCellText text="Utilisateur"/>
        </c:when>
        <c:otherwise>
          <view:arrayCellText text="Invité"/>
        </c:otherwise>
      </c:choose>
    </view:arrayLine>
  </c:forEach>
</view:arrayPane>

</body>
</html>
