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

<%@page import="org.owasp.encoder.Encode"%>
<%@page import="org.silverpeas.core.web.util.viewgenerator.html.operationpanes.OperationPaneType"%>
<%@page import="org.silverpeas.core.web.util.viewgenerator.html.GraphicElementFactory"%>
<%@page import="org.silverpeas.core.util.EncodeHelper"%>
<%@page import="org.silverpeas.core.admin.user.model.UserDetail"%>
<%@page import="org.silverpeas.core.util.StringUtil"%>
<%@page import="org.silverpeas.core.admin.component.model.ComponentInstLight"%>
<%@page import="org.silverpeas.core.util.DateUtil"%>
<%@page import="org.silverpeas.core.util.URLUtil"%>
<%@page import="org.silverpeas.core.admin.space.SpaceInstLight"%>
<%@page import="org.silverpeas.core.web.look.DefaultSpaceHomePage"%>
<%@page import="org.silverpeas.core.contribution.publication.model.PublicationDetail"%>
<%@page import="java.util.List"%>
<%@page import="org.silverpeas.core.web.look.LookHelper"%>

<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>

<%
  response.setHeader("Cache-Control", "no-store"); //HTTP 1.1
  response.setHeader("Pragma", "no-cache"); //HTTP 1.0
  response.setDateHeader("Expires", -1); //prevents caching at the proxy server
%>

<view:timeout />

<%
LookHelper helper = LookHelper.getLookHelper(session);
DefaultSpaceHomePage homepage =  helper.getSpaceHomePage(request.getParameter("SpaceId"));
List<PublicationDetail> publications = homepage.getPublications();
List<PublicationDetail> news = homepage.getNews();
SpaceInstLight space = homepage.getSpace();
List<SpaceInstLight> subspaces = homepage.getSubSpaces();
List<ComponentInstLight> apps = homepage.getApps();
List<UserDetail> admins = homepage.getAdmins();

GraphicElementFactory gef =
    (GraphicElementFactory) session.getAttribute(GraphicElementFactory.GE_FACTORY_SESSION_ATT);
gef.setSpaceIdForCurrentRequest(homepage.getSpace().getId());
%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title><%=space.getName(helper.getLanguage()) %></title>
<!-- CSS SpaceHome -->
<style type="text/css">
.spaceHome #spaceEvent .portlet-content ul {
     list-style-image: none;
}

.spaceHome .spaceDescription {
     display: block;
     margin: 1em 0px;
     padding: 0px;
     text-align: justify;
}

.spaceHome .spaceNavigation .browseSpace {
     font-weight: 100;
}

.spaceHome #publicationList {
     margin: 0px 0px 0px 2em;
     padding: 10px;
}

.spaceHome .portlet {
     margin-bottom: 0.5em;
}

.spaceHome #publication {
     clear: left;
}

.spaceHome #publication li {
     margin-bottom: 1em;
}

.spaceHome #publication li a {
     display: block;
}

.spaceHome .spaceNavigation ul {
     list-style-type: none;
     margin: 0px;
     padding: 0px;
}

.spaceHome .spaceNavigation li {
     border-radius: 12px;
     float: left;
     line-height: 32px;
     margin-right: 6px;
     min-height: 32px;
     padding: 8px 12px 8px 8px;
}

.spaceHome .spaceNavigation li:hover {
     background-image: url("/silverpeas/util/icons/gradientSVG.jsp?from=e5e5e5&to=fff&vertical=0&horizontal=100");
     cursor: pointer;
}

.spaceHome .spaceNavigation li div {
     display: inline-block;
     vertical-align: middle;
}

.spaceHome .spaceNavigation li a {
     font-size: 14px;
}

.spaceHome .spaceNavigation li p {
     font-size: 90%;
     margin: -13px 0 0 0;
     padding: 0px;
}

.spaceHome .spaceNavigation li.browse-component{
     padding-left: 36px;
	 position:relative;
}

.spaceHome .spaceNavigation li img {
	left: 5px;
    position: absolute;
    top: 25%;
    width: 26px;
}

.spaceHome #spaceQuiskInfo ul.carousel {
     list-style: none outside none;
     margin: 0px;
     padding: 0px;
}

.spaceHome #spaceQuiskInfo li.slide {
     list-style: none outside none;
     padding: 0px;
     min-height: 180px;
     margin: 0px 0px 5px;
}

.spaceHome #spaceQuiskInfo li.slide > h4 {
     margin: 0 0 8px 0;
     padding: 0px;
}

.spaceHome #spaceQuiskInfo li.slide img {
    float: left;
    margin: 0.25em 0.5em 0 0;
    width: 100px;
}

.spaceHome #spaceQuiskInfo li p {
     color: #666666;
     text-align: justify;
	 padding:0;
	 margin:0;
}

.spaceHome #spaceQuiskInfo .slides-pagination {
     bottom: auto;
     left: auto;
	 position:relative;
     margin: 0px;
     padding: 0px;
	 z-index:90;
}

.spaceHome #spaceQuiskInfo .slides-pagination li {
    display: inline-block;
}

.spaceHome #spaceQuiskInfo .slides-pagination a {
     color: #FFFFFF;
     text-align: center;
	 margin-bottom: 5px;
}

.spaceHome #spaceQuiskInfo .slideshow {
     margin: 0px;
}

.spaceHome #admins #global-admins,
.spaceHome #admins #space-admins h5 {
     display: none;
}
</style>
<view:looknfeel/>
<view:includePlugin name="lightslideshow"/>
<script type="text/javascript">
<!--
function goToSpaceItem(url) {
	location.href=url;
}
$(document).ready(function() {
	// if at least one item have got a description
	if ($.trim($(".spaceNavigation li p").text()).length!==0){
		$(".spaceNavigation li").css("min-height", "43px");
		$(".spaceNavigation li").css("line-height", "40px");
    }

	// if right column is empty
	if ($.trim($(".rightContent").text()).length===0){
		$(".rightContent").css("display", "none");
		$(".principalContent").css("margin-right", "0");
	}

	var $s = $('.slideshow').slides();
 });
-->
</script>
</head>
<body class="spaceHome <%=helper.getSubSpaceId() %>">
<view:browseBar spaceId="<%=helper.getSubSpaceId() %>"/>
<view:operationPane type="<%=OperationPaneType.space %>">
</view:operationPane>
<view:window>
  <!--INTEGRATION HOME SPACE -->
                        <div id="portletPages" class="rightContent">
                          <% if (admins != null && !admins.isEmpty()) { %>
                          <!-- gestionnaires -->
                          <div class="portlet" id="spaceManager">
                            <div class=" header">
                              <h2 class="portlet-title"><%=helper.getString("lookSilverpeasV5.homepage.space.admins") %></h2>
                            </div>
							<div class="portlet-content">
								<ul class="list-responsible-user">
									<% for (UserDetail admin : admins) { %>
									<li class="intfdcolor"><div class="content"><div class="profilPhoto"><view:image css="avatar" src="<%=admin.getAvatar()%>" type="avatar"/></div><div class="userName"><view:username userId="<%=admin.getId() %>"/></div></div></li>
									<% } %>
								</ul>
								<br clear="all" />
							</div>
                          </div>
                          <!-- /gestionnaires -->
                          <% } %>

						  <% if (news != null && !news.isEmpty()) { %>
						  <!-- QuickInfo -->
						  <div class="portlet" id="spaceQuiskInfo">
                            <div class="header">
                              <h2 class="portlet-title"><%=helper.getString("lookSilverpeasV5.homepage.space.news") %></h2>
                            </div>
                            <div class="portlet-content slideshow" data-transition="crossfade" data-loop="true" data-skip="false">
								<ul class="carousel">
									<% for (PublicationDetail aNews : news) { %>
										<li class="slide" onclick="javascript:location.href='<%=URLUtil.getSimpleURL(URLUtil.URL_PUBLI, aNews.getId())%>'">
							<h4 class="title-quickInfo"><a href="<%=URLUtil.getSimpleURL(URLUtil.URL_PUBLI, aNews.getId())%>"><%=Encode.forHtml(aNews.getName(helper.getLanguage())) %></a></h4>
							<% if (aNews.getThumbnail() != null) { %>
											<img src="<%=aNews.getThumbnail().getURL()%>" alt=""/>
										<% } %>
										<div class="content-quickInfo">
											<p><%=aNews.getDescription() %></p>
										</div>
										</li>
									<% } %>
								</ul>
							</div>
						</div>
						  <!--  /QuickInfo -->
						  <% } %>

						  <% if (StringUtil.isDefined(homepage.getNextEventsURL())) { %>
						   <!-- events -->
							 <div class="portlet" id="spaceEvent">
                            <div class=" header">
                              <h2 class="portlet-title"><%=helper.getString("lookSilverpeasV5.homepage.space.events") %></h2>
                            </div>
                            <div id="calendar" class="portlet-content">
                             <iframe src="<%=homepage.getNextEventsURL() %>" frameborder="0" height="250px"></iframe>
                            </div>
                          </div>
						  <!-- /events -->
						  <% } %>
                        </div>
                        <div class="principalContent">
                          <h1 class="spaceName"><%=Encode.forHtml(space.getName(helper.getLanguage())) %></h1>

				<% if (StringUtil.isDefined(space.getDescription(helper.getLanguage()))) { %>
                          <p class="spaceDescription"><%=EncodeHelper.convertWhiteSpacesForHTMLDisplay(Encode.forHtml(space.getDescription(helper.getLanguage()))) %></p>
                            <% } else { %>
				<p></p>
                            <% } %>

                          <% if ((apps != null && !apps.isEmpty()) || (subspaces != null && !subspaces.isEmpty())) { %>
                          <div class="spaceNavigation">
							<ul>
								<% for (SpaceInstLight subspace : subspaces) { %>
								<li class="browse-space bgDegradeGris" onclick="goToSpaceItem('<%=URLUtil.getSimpleURL(URLUtil.URL_SPACE, subspace.getId())%>')">
									<div>
										<a href="<%=URLUtil.getSimpleURL(URLUtil.URL_SPACE, subspace.getId())%>"><%=Encode.forHtml(subspace.getName(helper.getLanguage())) %></a>
										<% if (StringUtil.isDefined(subspace.getDescription(helper.getLanguage()))) { %>
											<p><%=Encode.forHtml(subspace.getDescription(helper.getLanguage())) %></p>
										<% } %>
									</div>
								</li>
								<% } %>
								<% for (ComponentInstLight app : apps) { %>
								<li class="browse-component bgDegradeGris" onclick="goToSpaceItem('<%=URLUtil.getSimpleURL(URLUtil.URL_COMPONENT, app.getId())%>')">
									<div>
										<img src="<%=app.getIcon(true) %>" />
										<a href="<%=URLUtil.getSimpleURL(URLUtil.URL_COMPONENT, app.getId())%>"><%=Encode.forHtml(app.getLabel(helper.getLanguage())) %></a>
										<% if (StringUtil.isDefined(app.getDescription(helper.getLanguage()))) { %>
											<p><%=Encode.forHtml(app.getDescription(helper.getLanguage())) %></p>
										<% } %>
									</div>
								</li>
								<% } %>
							</ul>
                          </div>
                          <% } %>

							<% if (publications != null && !publications.isEmpty()) { %>
                          <div class="bgDegradeGris portlet" id="publication">
                            <div class="bgDegradeGris header">
                              <h4 class="clean"><%=helper.getString("lookSilverpeasV5.homepage.space.publications") %></h4>
                            </div>

							<ul id="publicationList">
								<% for (PublicationDetail publication : publications) { %>
								<li>
									<a href="<%=URLUtil.getSimpleURL(URLUtil.URL_PUBLI, publication.getId())%>"><b><%=Encode.forHtml(publication.getName(helper.getLanguage())) %></b></a>
									<view:username userId="<%=publication.getUpdaterId() %>" /> - <%=DateUtil.getOutputDate(publication.getUpdateDate(), helper.getLanguage()) %> <br/>
									<%= Encode.forHtml(publication.getDescription(helper.getLanguage())) %>
								</li>
								<% } %>
                             </ul>

                          </div>
                          <% } %>
                        </div>
                        <!-- /INTEGRATION COMMUNITY -->
</view:window>
</body>
</html>