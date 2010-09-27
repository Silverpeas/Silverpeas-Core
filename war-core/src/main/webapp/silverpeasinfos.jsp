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
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<html>
  <head>
      <title>Infos</title>
  <view:looknfeel />
  </head>
  <body>
    Silverpeas version is <i><c:out value="${initParam.SILVERPEAS_VERSION}" /></i> <br/>
  Silverpeas is running on <c:out value="${pageContext.servletContext.serverInfo}" /><br/>
  with the version <b><c:out value="${pageContext.servletContext.majorVersion}" />.<c:out value="${pageContext.servletContext.minorVersion}" /></b> of the Servlet API.<br/>
  The server is running on <%=System.getProperty("os.name") %> with the version <%=System.getProperty("java.vm.name") %> <%=System.getProperty("java.vm.version") %> by <%=System.getProperty("java.vm.vendor") %>
  </body>
</html>