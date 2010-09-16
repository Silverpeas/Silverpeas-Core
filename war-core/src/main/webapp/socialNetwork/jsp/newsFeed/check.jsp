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

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>

<fmt:setLocale value="${sessionScope[sessionController].language}" />
<view:setBundle bundle="${requestScope.resources.multilangBundle}"  />
<view:setBundle basename="com.stratelia.webactiv.multilang.generalMultilang" var="GML" />


<fmt:message key="newsFeed.newsFeed" var="newsFeed" />
<fmt:message key="newsFeed.relations" var="relations" />
<fmt:message key="profil.events" var="events" />
<fmt:message key="profil.publications" var="publications" />
<fmt:message key="profil.photos"  var="photos"/>
<%--**********************Profil body******************************--%>
<fmt:message key="profil.relationShip.suffix"  var="relationShipSuffix"/>
<fmt:message key="profil.relationShip.prefix"  var="relationShipPrefix"/>
<fmt:message key="profil.status.suffix"  var="statusSuffix"/>
<fmt:message key="newsFeed.publication.apdated.true"  var="newsFeed.publication.true"/>
<fmt:message key="newsFeed.publication.apdated.false"  var="newsFeed.publication.false"/>
<fmt:message key="newsFeed.photo.apdated.true"  var="newsFeed.photo.true"/>
<fmt:message key="newsFeed.photo.apdated.false"  var="newsFeed.photo.false"/>
<fmt:message key="event.event.apdated.false"  var="newsFeed.event.false"/>
<%--**********************Url Servlet******************************--%>
<c:set var="id" value="${user.userId}"></c:set>
<c:url var="urlServlet" value="/RnewsFeedJSONServlet?userId=${id}" />
<c:url var="urlDirectory" value="/RdirectoryServlet/jsp?Action=" />
<c:url var="urlContactsDirectory" value="/RdirectoryServlet/jsp?ContactId=${id}&Action=" />
<c:url var="urlProfil" value="/Rprofil/jsp/Main?userId="/>
<c:url var="urlInvitationSent" value="/Rinvitation/jsp/InvitationsSent" />
<c:url var="urlInvitationReceived" value="/Rinvitation/jsp/Main" />
<%--************************Bar progress***********************--%>
<c:url var="progress" value="/util/icons/inProgress.gif" />






