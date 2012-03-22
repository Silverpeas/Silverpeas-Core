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

<%@ page language="java" contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>
<c:set var="language" value="${requestScope.resources.language}"/>
<fmt:setLocale value="${language}"/>
<view:setBundle bundle="${requestScope.resources.multilangBundle}"/>
<view:setBundle bundle="${requestScope.resources.iconsBundle}" var="icons"/>

<%@ include file="check.jsp" %>
<html>
<head>
  <view:looknfeel/>
  <script type="text/javascript" src="<%=m_context%>/util/javaScript/animation.js"></script>
  <script type="text/javascript">
    var ticketWindow = window;

    function editTicket(keyFile) {
      urlWindows = "EditTicket?KeyFile=" + keyFile;
      windowName = "ticketWindow";
      larg = "700";
      haut = "400";
      windowParams = "directories=0,menubar=0,toolbar=0, alwaysRaised";
      if (!ticketWindow.closed && ticketWindow.name == "ticketWindow")
        ticketWindow.close();
      ticketWindow = SP_openWindow(urlWindows, windowName, larg, haut, windowParams);
      ticketWindow.onClose = function() {
        location.reload(true);
      }
    }

    function deleteTicket(keyFile) {
      if (window.confirm("<%=resource.getString("fileSharing.confirmDeleteTicket")%> ?")) {
        document.deleteForm.KeyFile.value = keyFile;
        document.deleteForm.submit();
      }
    }
  </script>
</head>
<body>
<fmt:message key="fileSharing.tickets" var="browseBar"/>
<form name="readForm" action="" method="post">
  <input type="hidden" name="mode"/>
  <view:browseBar path="${browseBar}"/>
  <view:window>
    <view:frame>
      <view:arrayPane var="ticketList" routingAddress="ViewTickets">
        <fmt:message key="GML.nom" var="ticketName"></fmt:message>
        <view:arrayColumn title="${ticketName}"></view:arrayColumn>

        <fmt:message key="fileSharing.ticket" var="sharingTicket"></fmt:message>
        <view:arrayColumn title="${sharingTicket}" sortable="false"></view:arrayColumn>

        <fmt:message key="fileSharing.endDate" var="endDateTicket"></fmt:message>
        <view:arrayColumn title="${endDateTicket}"></view:arrayColumn>

        <fmt:message key="fileSharing.nbAccess" var="nbAccessTicket"></fmt:message>
        <view:arrayColumn title="${nbAccessTicket}"></view:arrayColumn>

        <fmt:message key="fileSharing.operation" var="operationTicket"></fmt:message>
        <view:arrayColumn title="${operationTicket}" sortable="false"></view:arrayColumn>
        <c:forEach items="${requestScope.Tickets}" var="ticket">
          <c:set var="endDate" value=""/>
          <c:set var="accessCount" value="${ticket.nbAccess}"/>
          <view:arrayLine>
            <c:if test="${ticket.attachmentDetail != null || ticket.document != null}">
              <c:url var="lien" value="/File/${ticket.fileId}"/>
              <c:choose>
                <c:when test="${not ticket.versioned}">
                   <view:arrayCellText text="${ticket.attachmentDetail.logicalName}"/>
                </c:when>
                <c:otherwise>
                  <view:arrayCellText text="${ticket.document.name}"/>
                </c:otherwise>
              </c:choose>
              <%
               IconPane iconPane = gef.getIconPane();
              Icon keyIcon = iconPane.addIcon();
              keyIcon.setProperties(resource.getIcon("fileSharing.ticket"),
                  resource.getString("fileSharing.ticket"), ((TicketDetail)pageContext.getAttribute("ticket")).getUrl(request));
              pageContext.setAttribute("ticketIcon", keyIcon.print());
              %>
              <view:arrayCellText text="${ticketIcon}"/>
            </c:if>
            <c:if test="${ticket.endDate ne null}">
              <c:set var="endDate"><view:formatDate value="${ticket.endDate}" language="${language}"/></c:set>
            </c:if>
            <c:if test="${ticket.nbAccessMax gt 0}">
              <c:set var="accessCount" value="${ticket.nbAccess}/${ticket.nbAccessMax}"/>
            </c:if>
            <view:arrayCellText text="${endDate}" />
            <view:arrayCellText text="${accessCount}" />
            <%
              IconPane iconPane = gef.getIconPane();
              Icon updateIcon = iconPane.addIcon();
              Icon deleteIcon = iconPane.addIcon();
              String keyFile = ((TicketDetail)pageContext.getAttribute("ticket")).getKeyFile();
              updateIcon.setProperties(resource.getIcon("fileSharing.update"),
                  resource.getString("fileSharing.updateTicket"),
                  "javaScript:onClick=editTicket('" + keyFile + "')");
              deleteIcon.setProperties(resource.getIcon("fileSharing.delete"),
                  resource.getString("fileSharing.deleteTicket"),
                  "javaScript:onClick=deleteTicket('" + keyFile + "')");
              pageContext.setAttribute("ticketUpdateDeleteIcons", updateIcon.print() + "&nbsp;&nbsp;&nbsp;&nbsp;" + deleteIcon.print());
            %>
            <view:arrayCellText text="${ticketUpdateDeleteIcons}" />
          </view:arrayLine>
        </c:forEach>
      </view:arrayPane>

    </view:frame>
  </view:window>
</form>

<form name="ticketForm" action="" method="post">
  <input type="hidden" name="KeyFile"/>
  <input type="hidden" name="FileId"/>
  <input type="hidden" name="ComponentId"/>
  <input type="hidden" name="Versioning"/>
  <input type="hidden" name="EndDate"/>
  <input type="hidden" name="NbAccessMax"/>
  <input type="hidden" name="Continuous"/>
</form>

<form name="deleteForm" action="DeleteTicket" method="post">
  <input type="hidden" name="KeyFile"/>
</form>

</body>
</html>