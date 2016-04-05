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

<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%@page import="org.silverpeas.core.socialnetwork.model.SocialInformationType"%>
<%@page import="org.silverpeas.web.socialnetwork.myprofil.servlets.MyProfileRoutes"%>
<%@page import="org.silverpeas.core.util.URLUtil"%>
<%@page import="org.silverpeas.core.util.EncodeHelper"%>
<%@page import="org.silverpeas.core.util.MultiSilverpeasBundle"%>
<%@page import="java.util.List"%>

<%
	boolean wall = view.equals(MyProfileRoutes.MyWall.toString());
	String urlServlet = URLUtil.getApplicationURL()+"/RnewsFeedJSONServlet?userId="+userFull.getId();
	if (!wall) {
	  urlServlet += "&View=MyFeed";
	}
	MultiSilverpeasBundle resource = (MultiSilverpeasBundle) request.getAttribute("resources");
%>
<script type="text/javascript" src="<%=URLUtil.getApplicationURL() %>/socialNetwork/jsp/js/newsfeed.js"></script>
<script type="text/javascript">
function getApplicationContext() {
	return '<%=URLUtil.getApplicationURL()%>';
}

function getFeedURL() {
	return '<%=urlServlet%>';
}

$(document).ready(function(){
	init('<%=SocialInformationType.ALL %>');
});
</script>

<div id="newsFeedProfil">

	<div class="sousNavBulle">
		<p><fmt:message key="newsFeed.scope.display" />
			<a class="active" href="#" onclick="changeScope('<%=SocialInformationType.ALL %>')"><fmt:message key="newsFeed.scope.all" /></a>
			<a id="scope-<%=SocialInformationType.STATUS %>" class="" href="#" onclick="changeScope('<%=SocialInformationType.STATUS %>')"><fmt:message key="newsFeed.scope.status" /></a>
			<a id="scope-<%=SocialInformationType.RELATIONSHIP %>" class="" href="#" onclick="changeScope('<%=SocialInformationType.RELATIONSHIP %>')"><fmt:message key="newsFeed.scope.relationship" /></a>
			<a id="scope-<%=SocialInformationType.PUBLICATION %>" class="" href="#" onclick="changeScope('<%=SocialInformationType.PUBLICATION %>')"><fmt:message key="newsFeed.scope.publication" /></a>
			<a id="scope-<%=SocialInformationType.MEDIA %>" class="" href="#" onclick="changeScope('<%=SocialInformationType.MEDIA %>')"><fmt:message key="newsFeed.scope.media" /></a>
			<a id="scope-<%=SocialInformationType.EVENT %>" class="" href="#" onclick="changeScope('<%=SocialInformationType.EVENT %>')"><fmt:message key="newsFeed.scope.event" /></a>
			<a id="scope-<%=SocialInformationType.COMMENT %>" class="" href="#" onclick="changeScope('<%=SocialInformationType.COMMENT %>')"><fmt:message key="newsFeed.scope.comment" /></a>
		</p>
	</div>

	<div class="newsFeed">
		<div id="newsFeed-content"></div>
	<a class="linkMore" title="<fmt:message key="newsFeed.getNext" />" href="#" onclick="getNext(); return false;"><span><fmt:message key="newsFeed.getNext" /></span></a>
	<p class="inprogress"><span><fmt:message key="newsFeed.inProgress" /></span></p>
	</div>

</div>