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
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page isELIgnored="false"%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>

<%@ include file="checkVersion.jsp"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<fmt:setLocale value="${userLanguage}" />
<view:setBundle basename="com.stratelia.silverpeas.versioningPeas.multilang.versioning" var="versioningMessages"  />
<html>
<head>
<%
out.println(gef.getLookStyleSheet());
%>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title><fmt:message key="versioning.warning.checkin.locked.title" bundle="${versioningMessages}" /></title>
</head>
<body>
<fieldset>
  <legend class="txttitrecol"><fmt:message key="versioning.warning.checkin.locked.title" bundle="${versioningMessages}" /></legend>

<%
  ResourceLocator messages = new ResourceLocator("com.stratelia.silverpeas.versioningPeas.multilang.versioning", m_MainSessionCtrl.getFavoriteLanguage());
  ButtonPane warningButtonPane = gef.getButtonPane();
  warningButtonPane.addButton(gef.getFormButton(messages.getString("close"), "javascript:window.opener.parent.MyMain.location.reload(); window.close();", false));
%>
<p><fmt:message key="versioning.warning.checkin.locked" bundle="${versioningMessages}" /></p>
<p><center><%=warningButtonPane.print()%></center></p>
</fieldset>
</body>
</html>
