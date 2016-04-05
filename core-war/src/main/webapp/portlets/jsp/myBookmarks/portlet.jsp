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

<%@page import="org.silverpeas.core.mylinks.model.LinkDetail"%>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%@ page import="org.silverpeas.core.util.EncodeHelper"%>
<%@ page import="org.silverpeas.core.util.StringUtil" %>

<%@ include file="../portletImport.jsp"%>

<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>

<portlet:defineObjects/>

<%
RenderRequest 	pReq 	= (RenderRequest)request.getAttribute("javax.portlet.request");
Iterator 		links 	= (Iterator) pReq.getAttribute("Links");

if (!links.hasNext()) {
	out.println(message.getString("NoFavorites"));
} else {
    %>
    <ul>
    <%
	//affichage des liens favoris de l'utilisateur
	while (links.hasNext()) {
		LinkDetail link = (LinkDetail) links.next();
		if (link.isVisible()) {
			String lien = link.getUrl();
			String name = EncodeHelper.convertHTMLEntities(link.getName());
			if (!StringUtil.isDefined(name)) {
				name = lien;
			}

      // Add context before link if needed
			if (lien.indexOf("://") == -1 && !lien.startsWith("/website")) {
				lien = URLUtil.getApplicationURL() + lien;
			}

			String target = "_self";
			if (link.isPopup()) {
				target = "_blank";
			}
			%>
			<li><a href="<%=lien%>" target="<%=target%>"><%=name%></a></li>
			<%
		}
	}
    %>
    </ul>
    <%
}
out.flush();
%>