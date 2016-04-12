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

<%@page import="org.silverpeas.core.admin.component.model.ComponentInstLight"%>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%@ page import="org.silverpeas.core.util.EncodeHelper" %>

<%@ include file="../portletImport.jsp"%>
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>

<portlet:defineObjects/>

<%
RenderRequest pReq = (RenderRequest)request.getAttribute("javax.portlet.request");
Collection<ComponentInstLight> applications = (Collection<ComponentInstLight>) pReq.getAttribute("Applications");

if (applications.isEmpty()) { %>
	<%=portletsBundle.getString("portlets.portlet.myLastAccessedAppli.none") %>
<% } else { %>
	<ul class="listing-portlet-lastApplication">
<%
	for (ComponentInstLight appli : applications) {
		String url = "";
		String target = "";
		if (URLUtil.displayUniversalLinks()) {
	      url = URLUtil.getSimpleURL(URLUtil.URL_COMPONENT, appli.getId());
	      target ="_top";
	    } else {
	      url = m_sContext + URLUtil.getURL(appli.getName(), "useless", appli.getId()) + "Main";
	      target ="MyMain";
	    }
%>
		<li class="lineResult">
			<div class="content">
				<div class="applicationTitle">
					<img src="<%=appli.getIcon(false)%>" class="iconComponent" />
					<a class="" href="<%=url%>" target="<%=target%>">
						<span class="" id="readSpanId_0"><%=EncodeHelper.convertHTMLEntities(appli.getLabel(language))%></span>
					</a>
				</div>
				<div class="location"><%=appli.getPath(" > ")%></div>
			</div>
		</li>
<% } %>
	</ul>
<% } %>
