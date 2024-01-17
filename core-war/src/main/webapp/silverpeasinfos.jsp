<%--

    Copyright (C) 2000 - 2024 Silverpeas

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    As a special exception to the terms and conditions of version 3.0 of
    the GPL, you may redistribute this Program in connection with Free/Libre
    Open Source Software ("FLOSS") applications as described in Silverpeas's
    FLOSS exception.  You should have received a copy of the text describing
    the FLOSS exception, and it is also available here:
    "https://www.silverpeas.org/legal/floss_exception.html"

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.

--%>
<%@ page import="org.silverpeas.core.admin.user.model.UserDetail" %><%--
  Copyright (C) 2000 - 2024 Silverpeas

  This program is free software: you can redistribute it and/or modify
  it under the terms of the GNU Affero General Public License as
  published by the Free Software Foundation, either version 3 of the
  License, or (at your option) any later version.

  As a special exception to the terms and conditions of version 3.0 of
  the GPL, you may redistribute this Program in connection with Free/Libre
  Open Source Software ("FLOSS") applications as described in Silverpeas's
  FLOSS exception.  You should have recieved a copy of the text describing
  the FLOSS exception, and it is also available here:
  "https://www.silverpeas.org/legal/floss_exception.html"

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU Affero General Public License for more details.

  You should have received a copy of the GNU Affero General Public License
  along with this program.  If not, see <https://www.gnu.org/licenses/>.
  --%>

<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://www.silverpeas.com/tld/silverFunctions" prefix="silfn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>

<c:set var="currentUser" value="${silfn:currentUser()}"/>
<c:if test="${currentUser == null or not currentUser.accessAdmin}">
  <c:redirect url="/Login"/>
</c:if>

<html>
<head>
  <title>Infos</title>
  <link href="<c:url value="/style.css" />" rel="stylesheet" type="text/css">
  <style type="text/css">
    .titre {
      left: 490px;
    }

    .java-opts {
      width: 100%;
    }

    .java-opts ul {
      font-weight: bold;
      font-size: smaller;
      display:inline;
      margin: 0;
      padding: 0;
      list-style-type: none;
    }

    .java-opts ul li {
      float: left;
    }
  </style>
</head>
<body>
<c:set var="system" value="${System.getProperty('os.name')} ${System.getProperty('os.version')} ${System.getProperty('os.arch')}"/>
<c:set var="java" value="${System.getProperty('java.vm.vendor')} ${System.getProperty('java.vm.name')} ${System.getProperty('java.vm.version')}"/>
<c:set var="javaOpts" value="${System.getenv('JAVA_OPTS')}"/>
<div class="page">
  <div class="titre"><span>Information</span></div>
  <div id="background">
    <div class="cadre">
      <div id="header">
        <a href="http://www.silverpeas.com"><img src="<c:url value="/images/logo.jpg" />" class="logo" alt="logo"/></a>
        <p class="information">Silverpeas version is
          <b><c:out value="${initParam.SILVERPEAS_VERSION}"/></b></p>
      </div>
      <div class="information">
        <p>
          Silverpeas is running on <b>${pageContext.servletContext.serverInfo}</b>
          with the Servlet API
          <b>${pageContext.servletContext.majorVersion}.${pageContext.servletContext.minorVersion}</b>
        </p>
      </div>
      <div class="information">
        <p>
          The server is running on <b>${system}</b> with <b>${java}</b>
        </p>
      </div>
      <c:if test="${javaOpts != null && fn:length(javaOpts) > 0}">
        <div class="information java-opts">
          <p>
            Silverpeas is running with the following configuration:
          </p>
          <ul>
          ${javaOpts.replaceAll('[ ]*([^ ]+)', '<li>&nbsp;$1</li>')}
          </ul>
        </div>
      </c:if>
    </div>
  </div>
</div>
</div>
</body>
</html>