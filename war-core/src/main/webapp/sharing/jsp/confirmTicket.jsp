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

<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>
<%@ include file="check.jsp" %>
<fmt:setLocale value="${requestScope.resources.language}"/>
<view:setBundle basename="com.silverpeas.sharing.multilang.fileSharingBundle"/>
<view:setBundle basename="com.silverpeas.sharing.settings.fileSharingIcons" var="icons"/>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Frameset//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-frameset.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">     
<head>
  <view:looknfeel/>
</head>
<body>
<c:set var="browseBar"><fmt:message key="sharing.tickets"/> > <fmt:message key="sharing.confirmTicket"/></c:set>
<fmt:message key="GML.ok" var="exitButtonMsg"/>
<view:browseBar extraInformations="${browseBar}"/>
<view:window>
<view:frame>
  <view:board>
    <table cellpadding="5" width="100%">
      <tr>
        <td class="txtlibform" nowrap><%=resource.getString("sharing.url")%></td>
        <td><a href="<c:url value="${requestScope.Url}" />" target="_blank"><c:url value="${requestScope.Url}"/></a></td>
      </tr>
    </table>
  </view:board>
  <center>
  <view:buttonPane>
    <view:button label="${exitButtonMsg}" action="javascript:window.close();"/>
  </view:buttonPane>
  </center>
</view:frame>
</view:window>
</body>
</html>