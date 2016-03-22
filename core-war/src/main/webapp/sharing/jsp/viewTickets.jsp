<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.iconpanes.IconPane" %>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.icons.Icon" %><%--

    Copyright (C) 2000 - 2013 Silverpeas

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
  <script type="text/javascript">
    function editTicket(token) {
      location.href = "EditTicket?token=" + token;
    }

    function deleteTicket(token) {
      if (window.confirm("<%=resource.getString("sharing.confirmDeleteTicket")%> ?")) {
        document.deleteForm.token.value = token;
        document.deleteForm.submit();
      }
    }

    function go(url) {
	window.open(url);
    }
  </script>
</head>
<body id="sharing-overview">
<fmt:message key="sharing.tickets" var="browseBar"/>
<form name="readForm" action="" method="post">
  <input type="hidden" name="mode"/>
  <view:browseBar path="${browseBar}"/>
  <view:window>
    <view:frame>
      <view:arrayPane var="ticketList" routingAddress="ViewTickets">
        <fmt:message key="GML.nom" var="ticketName"></fmt:message>
        <view:arrayColumn title="${ticketName}"></view:arrayColumn>

        <fmt:message key="sharing.ticket" var="sharingTicket"></fmt:message>
        <view:arrayColumn title="${sharingTicket}" sortable="false"></view:arrayColumn>

        <fmt:message key="sharing.endDate" var="endDateTicket"></fmt:message>
        <view:arrayColumn title="${endDateTicket}"></view:arrayColumn>

        <fmt:message key="sharing.nbAccess" var="nbAccessTicket"></fmt:message>
        <view:arrayColumn title="${nbAccessTicket}"></view:arrayColumn>

        <fmt:message key="sharing.operation" var="operationTicket"></fmt:message>
        <view:arrayColumn title="${operationTicket}" sortable="false"></view:arrayColumn>
        <c:forEach items="${requestScope.Tickets}" var="ticket">
          <c:set var="endDate" value=""/>
          <c:set var="accessCount" value="${ticket.nbAccess}"/>
          <view:arrayLine>
            <c:choose>
              <c:when test="${ticket.resource ne null && ticket.resource.URL ne null}">
                <c:set var="lien" value="${ticket.resource.URL}"/>
              </c:when>
              <c:otherwise>
                <c:set var="lien" value="''" />
              </c:otherwise>
            </c:choose>
            <view:arrayCellText text="<a href=\'${lien}\' class=\'${ticket.sharedObjectType}\' target=\'_blank\'>${ticket.resource.name}</a>" />
            <%
              IconPane iconPane = gef.getIconPane();
              Icon keyIcon = iconPane.addIcon();
              keyIcon.setProperties(resource.getIcon("sharing.ticket"),
                  resource.getString("sharing.ticket"), "javascript:go('" + ((Ticket) pageContext.
                  getAttribute("ticket")).getUrl(request) + "');");
              pageContext.setAttribute("ticketIcon", keyIcon.print());
            %>
            <view:arrayCellText text="${ticketIcon}"/>
            <c:if test="${ticket.endDate ne null}">
              <c:set var="endDate"><view:formatDate value="${ticket.endDate}" language="${language}"/></c:set>
            </c:if>
            <c:set var="accessCount" value="n/a"/>
            <c:if test="${ticket.sharedObjectType eq 'Attachment' && ticket.nbAccessMax gt 0}">
              <c:set var="accessCount" value="${ticket.nbAccess} / ${ticket.nbAccessMax}"/>
            </c:if>
            <c:if test="${ticket.sharedObjectType eq 'Attachment' && ticket.nbAccessMax le 0}">
              <fmt:message key="sharing.access.unlimited" var="sharingUnlimited"></fmt:message>
              <c:set var="accessCount" value="${ticket.nbAccess} / ${sharingUnlimited}"/>
            </c:if>
            <view:arrayCellText text="${endDate}" />
            <view:arrayCellText text="${accessCount}" />
            <%
              iconPane = gef.getIconPane();
              Icon updateIcon = iconPane.addIcon();
              Icon deleteIcon = iconPane.addIcon();
              String token = ((Ticket)pageContext.getAttribute("ticket")).getToken();
              updateIcon.setProperties(resource.getIcon("sharing.update"),
                  resource.getString("sharing.updateTicket"),
                  "javaScript:onClick=editTicket('" + token + "')");
              deleteIcon.setProperties(resource.getIcon("sharing.delete"),
                  resource.getString("sharing.deleteTicket"),
                  "javaScript:onClick=deleteTicket('" + token + "')");
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
  <input type="hidden" name="token"/>
  <input type="hidden" name="endDate"/>
  <input type="hidden" name="nbAccessMax"/>
  <input type="hidden" name="continuous"/>
</form>

<form name="deleteForm" action="DeleteTicket" method="post">
  <input type="hidden" name="token"/>
</form>

</body>
</html>