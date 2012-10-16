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

<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%@page import="com.stratelia.webactiv.util.ResourceLocator"%>
<%@page import="com.stratelia.silverpeas.util.ResourcesWrapper"%>
<%@page import="com.silverpeas.util.EncodeHelper"%>
<%@page import="com.silverpeas.util.StringUtil"%>
<%@page import="java.util.List"%>
<%@page import="com.stratelia.silverpeas.peasCore.URLManager"%>
<%@page import="com.silverpeas.socialnetwork.myProfil.servlets.MyProfileRoutes"%>
<%@page import="com.silverpeas.socialnetwork.model.SocialInformationType"%>

<%
	boolean wall = view.equals(MyProfileRoutes.MyWall.toString());
	String urlServlet = URLManager.getApplicationURL()+"/RnewsFeedJSONServlet?userId="+userFull.getId();
	if (!wall) {
	  urlServlet += "&View=MyFeed";
	}
	ResourcesWrapper resource = (ResourcesWrapper) request.getAttribute("resources");
%>
<script type="text/javascript" src="<%=URLManager.getApplicationURL() %>/socialNetwork/jsp/js/newsfeed.js"></script>
<script type="text/javascript">
function getApplicationContext() {
	return '<%=URLManager.getApplicationURL()%>';
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
			<a id="scope-<%=SocialInformationType.PHOTO %>" class="" href="#" onclick="changeScope('<%=SocialInformationType.PHOTO %>')"><fmt:message key="newsFeed.scope.photo" /></a>
			<a id="scope-<%=SocialInformationType.EVENT %>" class="" href="#" onclick="changeScope('<%=SocialInformationType.EVENT %>')"><fmt:message key="newsFeed.scope.event" /></a>
		</p>
	</div>

	<div class="newsFeed">
		<div id="newsFeed-content"></div>
    	<a class="linkMore" title="<fmt:message key="newsFeed.getNext" />" href="#" onclick="getNext(); return false;"><span><fmt:message key="newsFeed.getNext" /></span></a>
    	<p class="inprogress"><span><fmt:message key="newsFeed.inProgress" /></span></p>
	</div>

</div>