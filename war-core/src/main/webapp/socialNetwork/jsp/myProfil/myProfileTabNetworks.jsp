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

<%@page import="com.silverpeas.socialnetwork.model.SocialNetworkID"%>
<%@page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@page import="com.silverpeas.socialnetwork.myProfil.servlets.MyProfileRoutes" %>
<%@page import="com.silverpeas.socialnetwork.model.SocialNetworkID" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>

<fmt:setLocale value="${sessionScope[sessionController].language}"/>
<view:setBundle bundle="${requestScope.resources.multilangBundle}"/>

<c:set var="availableNetworks" value="<%=SocialNetworkID.values() %>"/>

<div id="socialNetworks">
<form name="NetworksForm" action="#" method="post">
	<fmt:message key="myProfile.networks.linkToMySilverpeasAccount" var="linktoSVP" />
	<fmt:message key="myProfile.networks.unlinkFromMySilverpeasAccount" var="unlinkFromSVP" />

	<c:if test="${not empty errorMessage}">
		<div id="socialNetworkErrorMessage">
			<fmt:message key="myProfile.networks.error_${errorMessage}"/>
		</div>
	</c:if>

	<c:forEach items="${availableNetworks}" var="network">
		<div id="socialNetwork">
			<div id="socialNetworkLogo">
				<img border="0" width="70" align="middle" src="/weblib/look/icons/${network}.png">
			</div>
			<div id="socialNetworkInfos">
				<span id="socialNetworkTitle">${network}</span><br/>
				<c:choose>
              		<c:when test="${not empty userNetworks[network]}">
              			<fmt:message key="myProfile.networks.linkedToSilverpeas"/><br/>
              			<view:button label="${unlinkFromSVP}" action="javascript:confirmUnlink('${network}')" />
					</c:when>
					<c:otherwise>
              			<fmt:message key="myProfile.networks.notLinkedToSilverpeas"/>
              			<view:button label="${linktoSVP}" action="javascript:linkToSVP('${network}')"/>
					</c:otherwise>
				</c:choose>
			</div>
		</div>
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
