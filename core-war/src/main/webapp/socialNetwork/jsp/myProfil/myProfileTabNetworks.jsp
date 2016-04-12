<%--

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

<%@page import="org.silverpeas.core.socialnetwork.model.SocialNetworkID"%>
<%@page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>

<fmt:setLocale value="${sessionScope[sessionController].language}"/>
<view:setBundle bundle="${requestScope.resources.multilangBundle}"/>

<c:set var="availableNetworks" value="<%=SocialNetworkID.values() %>"/>

<div id="myprofile-socialnetworks">
<form name="NetworksForm" action="#" method="post">
	<fmt:message key="myProfile.networks.linkToMySilverpeasAccount" var="linktoSVP" />

	<c:if test="${not empty errorMessage}">
		<div id="socialNetworkErrorMessage" class="inlineMessage-nok">
			<fmt:message key="myProfile.networks.error_${errorMessage}"/>
		</div>
	</c:if>

	<c:forEach items="${availableNetworks}" var="network">
		<c:if test="${network.enabled}">
		<div class="a-socialnetwork">
			<p>
				<img src="<c:url value="/util/icons/external/${network}.png" />" alt="" />
				<c:choose>
			<c:when test="${not empty userNetworks[network]}">
				<fmt:message key="myProfile.networks.yourAccount"/> <strong>${network}</strong> <fmt:message key="myProfile.networks.linkedToSilverpeas"/>
				<div class="bgDegradeGris switch socialnetwork"><fmt:message key="myProfile.networks.linkToMySilverpeasAccount"/> <a href="#" class="active link-socialNetwork"><fmt:message key="GML.yes"/></a><a href="javascript:confirmUnlink('${network}')" class="no-link-socialNetwork"><fmt:message key="GML.no"/></a></div>
					</c:when>
					<c:otherwise>
						<fmt:message key="myProfile.networks.yourAccount"/> <strong>${network}</strong> <fmt:message key="myProfile.networks.notLinkedToSilverpeas"/>
						<div class="bgDegradeGris switch socialnetwork"><fmt:message key="myProfile.networks.linkToMySilverpeasAccount"/> <a href="javascript:linkToSVP('${network}')" class="no-link-socialNetwork"><fmt:message key="GML.yes"/></a><a href="#" class="active link-socialNetwork"><fmt:message key="GML.no"/></a></div>
					</c:otherwise>
				</c:choose>
			</p>
			<hr class="sep" />
		</div>
		</c:if>
	</c:forEach>
</form>
<form id="unlinkForm" action="UnlinkFromSVP" method="post">
	<input type="hidden" name="networkId" id="networkId"/>
</form>
<form id="linkForm" action="LinkToSVP" method="post">
	<input type="hidden" name="networkId" id="networkIdToLink"/>
</form>
</div>

<div id="dialog-confirmUnlink" title="<fmt:message key="myProfile.networks.unlinkConfirmationTitle" />">
	<p><fmt:message key="myProfile.networks.unlinkConfirmation" /></p>
</div>

<view:progressMessage/>

<fmt:message key="myProfile.networks.unlinkConfirmation" var="confirmMsg"/>
<script type="text/javascript">

$(function() {
	$( "#dialog-confirmUnlink" ).dialog({
		autoOpen: false,
		resizable: false,
		modal: true,
		buttons: {
			"<fmt:message key="GML.yes" />": function() {
				$( this ).dialog( "close" );
				var form = document.getElementById("unlinkForm");
				$.progressMessage();
				form.submit();
			},
			"<fmt:message key="GML.no" />": function() {
				$( this ).dialog( "close" );
			}
		}
	});
});

function confirmUnlink(networkId) {
	$( '#networkId' ).val( networkId );
	$( "#dialog-confirmUnlink" ).dialog("open");
}

function linkToSVP(networkId) {
	$( '#networkIdToLink' ).val( networkId );
	var form = document.getElementById("linkForm");
	$.progressMessage();
	form.submit();
}
</script>
