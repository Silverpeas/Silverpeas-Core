<%--

    Copyright (C) 2000 - 2019 Silverpeas

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
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

--%>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%@ include file="../portletImport.jsp"%>
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://www.silverpeas.com/tld/silverFunctions" prefix="silfn" %>

<portlet:defineObjects/>

<fmt:setLocale value="${sessionScope[SilverSessionController].favoriteLanguage}" />
<view:setBundle basename="org.silverpeas.portlets.multilang.portletsBundle" var="portlets" />
<view:setBundle basename="org.silverpeas.social.multilang.socialNetworkBundle" />

<view:includePlugin name="userZoom"/>

<c:set var="context" value="${pageContext.request.contextPath}"/>
<c:set var="contactsConnected" value="${requestScope.ContactsConnected}" />
<c:set var="contactsNotConnected" value="${requestScope.ContactsNotConnected}"/>

<c:if test="${empty contactsConnected}">
	<c:if test="${empty contactsNotConnected}">
		<fmt:message key="portlets.portlet.myContacts.none" bundle="${portlets}" />
	</c:if>
</c:if>

	<div id="portlet-myContact">
		<ul id="listing-portlet-myContact" class="listing">

			<c:forEach var="member" items="${contactsConnected}">
				<li class="user online">
					<view:image type="avatar" css="avatar" alt="avatar" src="${member.avatar}" />

				<span class="userName">
					${member.lastName} ${member.firstName}
					<img src="${context}/util/icons/connected.png"
						alt="<fmt:message key="GML.user.online.for" /> ${member.durationOfCurrentSession}"
						title="<fmt:message key="GML.user.online.for" /> ${member.durationOfCurrentSession}"/>
				</span>

				<div class="userStatut">
					<p title="${member.status}">${silfn:escapeHtml(member.status)}</p>
				</div>

				<a href="#" title="<fmt:message key="ToContact" />" class="contact-user notification"
           onclick="sp.messager.open(null, {recipientUsers: ${member.id}, recipientEdition: false});">
					<img src="${context}/util/icons/email.gif"
						alt="<fmt:message key="ToContact" />"
						title="<fmt:message key="ToContact" />"/>
				</a>

				<a style="display: none;" href="javascript:SilverChat.gui.openChatWindow('${member.chatId}', '${member.displayedName}')" title="<fmt:message key="chat" />" class="accessTchat-user">
					<img src="${context}/util/icons/talk2user.gif"
						alt="<fmt:message key="chat" />"
						title="<fmt:message key="chat" />"/>
				</a>
			</li>
		</c:forEach>

			<c:forEach var="contact" items="${contactsNotConnected}">
				<li class="user offline">
					<view:image type="avatar" css="avatar" alt="avatar" src="${contact.avatar}" />

					<span class="userName">
					${contact.lastName} ${contact.firstName}
				</span>

				<div class="userStatut">
					<p title="${contact.status}">${contact.status}</p>
				</div>

				<a href="#" title="<fmt:message key="ToContact" />" class="contact-user notification"
           onclick="sp.messager.open(null, {recipientUsers: ${contact.id}, recipientEdition: false});">
					<img src="${context}/util/icons/email.gif"
						alt="<fmt:message key="ToContact" />"
						title="<fmt:message key="ToContact" />"/>
				</a>
			</li>
			</c:forEach>

		</ul>
		<br clear="all" />
	</div>
<script type="application/javascript">
  if (typeof window.SilverChat !== 'undefined') {
    sp.element.querySelectorAll('.accessTchat-user').forEach(function(link) {
      link.style.display = '';
    })
  }
</script>
