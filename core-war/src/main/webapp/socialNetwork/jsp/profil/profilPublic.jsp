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

<%@page import="org.silverpeas.core.web.util.viewgenerator.html.buttons.Button"%>
<%@page import="org.silverpeas.core.web.util.viewgenerator.html.buttonpanes.ButtonPane"%>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<%@ taglib tagdir="/WEB-INF/tags/silverpeas/util" prefix="viewTags" %>
<%@ page import="org.silverpeas.core.admin.user.model.UserFull"%>
<%@page import="org.silverpeas.core.web.util.viewgenerator.html.GraphicElementFactory"%>
<%@page import="org.silverpeas.core.web.directory.model.Member"%>
<%@page import="org.silverpeas.core.util.MultiSilverpeasBundle"%>
<%@page import="org.silverpeas.core.util.URLUtil"%>
<fmt:setLocale value="${sessionScope[sessionController].language}" />
<view:setBundle bundle="${requestScope.resources.multilangBundle}" />

<%
	GraphicElementFactory gef = (GraphicElementFactory) session.getAttribute("SessionGraphicElementFactory");
	MultiSilverpeasBundle resource = (MultiSilverpeasBundle) request.getAttribute("resources");

    UserFull userFull = (UserFull) request.getAttribute("userFull");
    Member member = (Member) request.getAttribute("Member");

    String m_context = URLUtil.getApplicationURL();
%>

<html>
  <head>
    <view:looknfeel withFieldsetStyle="true"/>
    <view:includePlugin name="invitme"/>
    <view:includePlugin name="messageme"/>
  </head>
  <body id="publicProfile">
    <view:window>

<!-- profilPublicFiche  -->
<div id="publicProfileFiche" >

	<!-- info  -->
	<div class="info tableBoard">
	<h2 class="userName"><%=member.getFirstName() %> <br /><%=member.getLastName() %></h2>
        <p class="infoConnection">
		<% if (member.isConnected()) { %>
				<img src="<%=m_context%>/util/icons/online.gif" alt="connected"/> <fmt:message key="GML.user.online.for" /> <%=member.getDuration()%>
			<% } else { %>
		<img src="<%=m_context%>/util/icons/offline.gif" alt="deconnected"/> <fmt:message key="GML.user.offline" />
            <% } %>
        </p>

	    <!-- action  -->
        <div class="action">
		<a href="#" class="link invitation" rel="<%=member.getId() %>,<%=member.getUserDetail().getDisplayedName() %>"><fmt:message key="invitation.send" /></a>
            <br />
            <a href="#" class="link notification" rel="<%=member.getId() %>,'<%=member.getUserDetail().getDisplayedName()%>"><fmt:message key="GML.notification.send" /></a>
        </div> <!-- /action  -->

        <!-- profilPhoto  -->
		<div class="profilPhoto">
			<view:image src="<%=member.getUserDetail().getAvatar()%>" type="avatar.profil" alt="viewUser" css="avatar"/>
        </div>

        <p class="statut">

        </p>

        <br clear="all" />
	</div><!-- /info  -->

</div><!-- /profilPublicFiche  -->

<!-- profilPublicContenu  -->
<div id="publicProfileContenu">

	<!-- sousNav  -->
	<div class="sousNavBulle">
		<p><fmt:message key="profil.subnav.display" /> <a class="active" href="#"><fmt:message key="profil.subnav.identity" /></a></p>
	</div><!-- /sousNav  -->

	<div class="tab-content">
    <viewTags:displayUserExtraProperties user="<%=userFull%>" readOnly="true" includeEmail="true"/>
  </div>
	<%
		  ButtonPane buttonPane = gef.getButtonPane();
		  Button button = gef.getFormButton(resource.getString("GML.back"), "javascript:history.back()", false);
		  buttonPane.addButton(button);
		  out.print(buttonPane.print());
	%>

</div><!-- /publicProfileContenu  -->
</view:window>
</body>
</html>