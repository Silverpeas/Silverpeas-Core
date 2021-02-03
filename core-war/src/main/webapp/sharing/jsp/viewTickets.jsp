<%--
  ~ Copyright (C) 2000 - 2021 Silverpeas
  ~
  ~ This program is free software: you can redistribute it and/or modify
  ~ it under the terms of the GNU Affero General Public License as
  ~ published by the Free Software Foundation, either version 3 of the
  ~ License, or (at your option) any later version.
  ~
  ~ As a special exception to the terms and conditions of version 3.0 of
  ~ the GPL, you may redistribute this Program in connection with Free/Libre
  ~ Open Source Software ("FLOSS") applications as described in Silverpeas's
  ~ FLOSS exception.  You should have received a copy of the text describing
  ~ the FLOSS exception, and it is also available here:
  ~ "https://www.silverpeas.org/legal/floss_exception.html"
  ~
  ~ This program is distributed in the hope that it will be useful,
  ~ but WITHOUT ANY WARRANTY; without even the implied warranty of
  ~ MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  ~ GNU Affero General Public License for more details.
  ~
  ~ You should have received a copy of the GNU Affero General Public License
  ~ along with this program.  If not, see <http://www.gnu.org/licenses/>.
  --%>

<%@ page language="java" contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>
<c:set var="language" value="${requestScope.resources.language}"/>
<fmt:setLocale value="${language}"/>
<view:setBundle bundle="${requestScope.resources.multilangBundle}"/>
<view:setBundle bundle="${requestScope.resources.iconsBundle}" var="icons"/>

<c:set var="tickets" value="${requestScope.Tickets}" />
<jsp:useBean id="tickets" type="org.silverpeas.core.util.SilverpeasList<org.silverpeas.core.sharing.model.Ticket>"/>
<c:set var="ticketsPagination" value="${requestScope.TicketsPagination}" />
<jsp:useBean id="ticketsPagination" type="org.silverpeas.core.admin.PaginationPage"/>

<%@ include file="check.jsp" %>
<html>
<head>
  <view:looknfeel/>
  <script type="text/javascript">
    function editTicket(token) {
      location.href = "EditTicket?token=" + token;
    }

    function deleteTicket(token) {
      jQuery.popup.confirm("<%=resource.getString("sharing.confirmDeleteTicket")%> ?", function() {
        document.deleteForm.token.value = token;
        document.deleteForm.submit();
      });
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
      <div id="ticket-list">
        <view:arrayPane var="ticketList" routingAddress="ViewTickets" numberLinesPerPage="${ticketsPagination.pageSize}">
          <fmt:message key="GML.creationDate" var="creationLabel"/>
          <view:arrayColumn title="${creationLabel}" sortable="true"/>

          <fmt:message key="GML.nom" var="ticketName"/>
          <c:choose>
            <c:when test="${tickets.originalListSize() < 200}">
              <view:arrayColumn title="${ticketName}" compareOn="${t -> t.resource.name}"/>
            </c:when>
            <c:otherwise>
              <view:arrayColumn title="${ticketName}" sortable="false"/>
            </c:otherwise>
          </c:choose>

          <fmt:message key="sharing.ticket" var="sharingTicket"/>
          <view:arrayColumn title="${sharingTicket}" sortable="false"/>

          <fmt:message key="sharing.endDate" var="endDateTicket"/>
          <view:arrayColumn title="${endDateTicket}" sortable="true"/>

          <fmt:message key="sharing.nbAccess" var="nbAccessTicket"/>
          <view:arrayColumn title="${nbAccessTicket}" sortable="true"/>

          <fmt:message key="sharing.operation" var="operationTicket"/>
          <view:arrayColumn title="${operationTicket}" sortable="false"/>
          <view:arrayLines items="${tickets}" var="ticket">
            <c:set var="endDate" value=""/>
            <c:set var="accessCount" value="${ticket.nbAccess}"/>
            <c:set var="creationDate"><view:formatDate value="${ticket.creationDate}" language="${language}"/></c:set>
            <view:arrayLine>
              <view:arrayCellText text="${creationDate}"/>
              <c:set var="lien" value="${ticket.resource.URL}"/>
              <view:arrayCellText text="<a href=\'${lien}\' class=\'${ticket.sharedObjectType}\' target=\'_blank\'>${ticket.resource.name}</a>"/>
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
                String token = ((Ticket) pageContext.getAttribute("ticket")).getToken();
                updateIcon.setProperties(resource.getIcon("sharing.update"),
                    resource.getString("sharing.updateTicket"),
                    "javaScript:onClick=editTicket('" + token + "')");
                deleteIcon.setProperties(resource.getIcon("sharing.delete"),
                    resource.getString("sharing.deleteTicket"),
                    "javaScript:onClick=deleteTicket('" + token + "')");
                pageContext.setAttribute("ticketUpdateDeleteIcons",
                    updateIcon.print() + "&nbsp;&nbsp;&nbsp;&nbsp;" + deleteIcon.print());
              %>
              <view:arrayCellText text="${ticketUpdateDeleteIcons}"/>
            </view:arrayLine>
          </view:arrayLines>
        </view:arrayPane>
        <script type="text/javascript">
          whenSilverpeasReady(function() {
            sp.arrayPane.ajaxControls('#ticket-list');
          });
        </script>
      </div>
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