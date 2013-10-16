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

<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>

<c:set var="currentUserId"     value="${sessionScope['SilverSessionController'].userId}"/>

<html>
<head>
  <title>Infos</title>
  <link href="<c:url value="/style.css" />" rel="stylesheet" type="text/css">
  <style type="text/css">
    .titre {
      left: 490px;
    }
  </style>
</head>
<body>
<div class="page">
  <div class="titre">Information</div>
  <div id="background">
    <div class="cadre">
      <div id="header">
        <a href="http://www.silverpeas.com"><img src="<c:url value="/images/logo.jpg" />" class="logo" alt="logo"/></a>

        <p class="information">Silverpeas version is
          <b><c:out value="${initParam.SILVERPEAS_VERSION}"/></b></p>
      </div>
      <c:if test="${currentUserId != null && currentUserId >= 0}">
      <p class="information">
        Silverpeas is running on <b><c:out value="${pageContext.servletContext.serverInfo}"/></b>
        with the version
        <b><c:out value="${pageContext.servletContext.majorVersion}"/>.<c:out value="${pageContext.servletContext.minorVersion}"/></b>
        of the Servlet API.<br/><br/>
        The server is running on <b><%=System.getProperty("os.name") %><%=System
          .getProperty("os.version")%> <%=System.getProperty("os.arch")%>
      </b> with the version <b><%=System.getProperty("java.vm.name")%> <%=System
          .getProperty("java.vm.version")%> by <%=System.getProperty("java.vm.vendor")%>
      </b><br/><br/>
        Silverpeas is running with the following configuration:<br/><i><%=System
          .getenv("JAVA_OPTS")%>
      </i></p>
      </c:if>
    </div>
  </div>
</div>
</body>
</html>