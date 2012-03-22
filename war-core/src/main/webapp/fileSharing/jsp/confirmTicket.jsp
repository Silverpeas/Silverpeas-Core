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
    "http://www.silverpeas.org/legal/licensing"

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
<view:setBundle basename="com.silverpeas.external.filesharing.multilang.fileSharingBundle"/>
<view:setBundle basename="com.silverpeas.external.filesharing.settings.fileSharingIcons"
                var="icons"/>
<html>
<head>
  <view:looknfeel/>
  <script type="text/javascript" src="<%=m_context%>/util/javaScript/checkForm.js"></script>
  <script language="javascript">
  </script>
</head>
<body>
<c:set var="browseBar"><fmt:message key="fileSharing.tickets"/> > <fmt:message
    key="fileSharing.confirmTicket"/></c:set>
<fmt:message key="GML.ok" var="exitButtonMsg"/>
<view:browseBar extraInformations="${browseBar}"/>
<view:window> <view:frame>

  <view:board>
    <table CELLPADDING=5 WIDTH="100%">
      <tr>
        <td class="txtlibform" nowrap><%=resource.getString("fileSharing.url")%>
        </td>
        <td><a href="<c:url value="${requestScope.Url}" />" target="_blank"><c:url
            value="${requestScope.Url}"/></a></td>
      </tr>
    </table>
  </view:board>
  <view:buttonPane>
    <view:button label="${exitButtonMsg}" action="javascript:window.close();"/>
  </view:buttonPane>
</view:frame>
</view:window>
</body>
</html>