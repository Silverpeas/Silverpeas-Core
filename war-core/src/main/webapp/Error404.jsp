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
<%@ include file="headLog.jsp" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>

<%-- Retrieve user menu display mode --%>
<c:set var="curHelper" value="${sessionScope.Silverpeas_LookHelper}"/>
<%-- Set resource bundle --%>
<fmt:setLocale value="${sessionScope['SilverSessionController'].favoriteLanguage}"/>
<view:setBundle basename="org.silverpeas.multilang.generalMultilang"/>
<view:setBundle basename="org.silverpeas.common.multilang.errors" var="fnf"/>

<% response.setStatus(HttpServletResponse.SC_NOT_FOUND); %>


<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
  <title><fmt:message key="GML.error"/>&nbsp;404</title>
  <link href="<c:url value="/style.css" />" rel="stylesheet" type="text/css"/>
  <style type="text/css">
    .titre {
      left: 490px;
    }
  </style>

</head>
<body>
<div class="page">
  <div class="titre"><fmt:message key="GML.error"/>&nbsp;404</div>
  <div id="background">
    <div class="cadre">
      <div id="header">
        <img src="<%=logo%>" class="logo" alt="logo"/>

        <p class="information"></p>
      </div>
      <div class="fnfinformation">
        <fmt:message key="error.404.description" bundle="${fnf}"></fmt:message><br/>
        <fmt:message key="error.404.solutions" bundle="${fnf}"></fmt:message><br/>
        <ul>
          <li><fmt:message key="error.404.solution.first" bundle="${fnf}"></fmt:message></li>
          <li><fmt:message key="error.404.solution.second" bundle="${fnf}"></fmt:message></li>
          <li><fmt:message key="error.404.solution.third" bundle="${fnf}"></fmt:message></li>
        </ul>
      </div>
    </div>
  </div>
</div>
</body>
</html>