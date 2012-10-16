<%--

    Copyright (C) 2000 - 2012 Silverpeas

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    As a special exception to the terms and conditions of version 3.0 of
    the GPL, you may redistribute this Program in connection with Free/Libre
    Open Source Software ("FLOSS") applications as described in Silverpeas's
    FLOSS exception.  You should have received a copy of the text describing
    the FLOSS exception, and it is also available here:
    "http://www.silverpeas.org/docs/core/legal/floss_exception.html"

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

--%>

<%@ page isELIgnored="false"%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>

<%@ include file="checkAttachment.jsp"%>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<fmt:setLocale value="${userLanguage}" />
<view:setBundle basename="com.stratelia.webactiv.util.attachment.multilang.attachment" var="attachmentMessages"  />
 <%
  ButtonPane warningButtonPane = gef.getButtonPane();
   if( "A".equals(m_MainSessionCtrl.getCurrentUserDetail().getAccessLevel())) {
     warningButtonPane.addButton(gef.getFormButton(messages.getString("forcer"), "javascript:checkin(" + request.getParameter("id") + ",true, true)", false));
   }
  warningButtonPane.addButton(gef.getFormButton(messages.getString("fermer"), "javascript:onClick=closeMessage()", false));
%>
<p><fmt:message key="attachment.warning.checkin.locked" bundle="${attachmentMessages}" /></p>
<p style="text-align:center;"><%=warningButtonPane.print()%></p>
