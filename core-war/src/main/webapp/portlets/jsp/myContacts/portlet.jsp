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

<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%@ include file="../portletImport.jsp"%>
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<portlet:defineObjects/>

<fmt:setLocale value="${sessionScope[SilverSessionController].favoriteLanguage}" />
<view:setBundle basename="org.silverpeas.portlets.multilang.portletsBundle" var="portlets" />
<view:setBundle basename="org.silverpeas.social.multilang.socialNetworkBundle" />

<view:includePlugin name="messageme"/>

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
					<view:image type="avatar" css="avatar" alt="avatar" src="${member.userDetail.avatar}" />

				<span class="userName">
					${member.lastName} ${member.firstName}
					<img src="${context}/util/icons/connected.png"
						alt="<fmt:message key="GML.user.online.for" /> ${member.duration}"
						title="<fmt:message key="GML.user.online.for" /> ${member.duration}"/>
				</span>

				<div class="userStatut">
					<p title="${member.status}">${member.status}</p>
				</div>

				<a href="#" title="<fmt:message key="ToContact" />" class="contact-user notification"
					rel="${member.id},${member.userDetail.displayedName}">
					<img src="${context}/util/icons/email.gif"
						alt="<fmt:message key="ToContact" />"
						title="<fmt:message key="ToContact" />"/>
				</a>

				<a href="#" title="<fmt:message key="tchat" />"  class="accessTchat-user"
					onclick="javascript:window.open('${context}/RcommunicationUser/jsp/OpenDiscussion?userId=${member.id}',
						'popupDiscussion${member.id}','menubar=no, status=no, scrollbars=no, menubar=no, width=600, height=450');return false;">
					<img src="${context}/util/icons/talk2user.gif"
						alt="<fmt:message key="tchat" />"
						title="<fmt:message key="tchat" />"/>
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
					rel="${contact.id},${contact.displayedName}">
					<img src="${context}/util/icons/email.gif"
						alt="<fmt:message key="ToContact" />"
						title="<fmt:message key="ToContact" />"/>
				</a>
			</li>
			</c:forEach>

		</ul>
		<br clear="all" />
	</div>
