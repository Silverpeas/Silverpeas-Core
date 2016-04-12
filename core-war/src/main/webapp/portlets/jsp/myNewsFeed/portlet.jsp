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

<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

<%@ include file="../portletImport.jsp" %>

<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>
<%@ taglib uri="http://www.silverpeas.com/tld/silverFunctions" prefix="silfn" %>

<fmt:setLocale value="${sessionScope['SilverSessionController'].favoriteLanguage}"/>
<view:setBundle basename="org.silverpeas.social.multilang.socialNetworkBundle"/>

<view:setConstant var="ALL_SOCIAL_INFORMATION" constant="org.silverpeas.core.socialnetwork.model.SocialInformationType.ALL"/>

<portlet:defineObjects/>

<%
  RenderRequest pReq = (RenderRequest) request.getAttribute("javax.portlet.request");
  String currentUserId = (String) pReq.getAttribute("userId");
  String notDisplayMyActivity = (String) pReq.getAttribute("notDisplayMyActivity");

  String urlServlet =
      URLUtil.getApplicationURL() + "/RnewsFeedJSONServlet?userId=" + currentUserId;
  if ("true".equals(notDisplayMyActivity)) {
    urlServlet += "&View=MyContactWall";
  } else {
    urlServlet += "&View=MyFeed";
  }
%>

<script type="text/javascript" src="${silfn:applicationURL()}/socialNetwork/jsp/js/newsfeed.js"></script>
<script type="text/javascript">
  function getApplicationContext() {
    return '${silfn:applicationURL()}';
  }

  function getFeedURL() {
    return '<%=urlServlet%>';
  }

  $(document).ready(function() {
    init('${ALL_SOCIAL_INFORMATION}');
  });
</script>

<div id="portlet-myFeed">
  <div id="newsFeed-content"></div>
  <a class="linkMore" title="<fmt:message key="newsFeed.getNext" />" href="#" onclick="getNext(); return false;"><span><fmt:message key="newsFeed.getNext"/></span></a>

  <p class="inprogress"><span><fmt:message key="newsFeed.inProgress"/></span></p>
</div>