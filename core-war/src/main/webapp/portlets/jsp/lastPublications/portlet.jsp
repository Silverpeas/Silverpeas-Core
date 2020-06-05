<%--

    Copyright (C) 2000 - 2020 Silverpeas

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    As a special exception to the terms and conditions of version 3.0 of
    the GPL, you may redistribute this Program in connection with Free/Libre
    Open Source Software ("FLOSS") applications as described in Silverpeas's
    FLOSS exception.  You should have received a copy of the text describing
    the FLOSS exception, and it is also available here:
    "https://www.silverpeas.org/legal/floss_exception.html"

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

--%>
<%@page import="org.silverpeas.core.admin.user.model.User"%>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%@ page import="org.silverpeas.core.admin.user.model.UserDetail" %>
<%@ page import="org.silverpeas.core.contribution.publication.model.PublicationDetail" %>
<%@ page import="org.silverpeas.core.date.TemporalFormatter" %>
<%@ page import="org.silverpeas.core.util.WebEncodeHelper" %>
<%@ page import="java.time.ZoneId" %>
<%@ page import="org.silverpeas.core.date.TemporalConverter" %>

<%@ include file="../portletImport.jsp"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<%@ taglib uri="http://www.silverpeas.com/tld/silverFunctions" prefix="silfn" %>

<portlet:defineObjects/>

<%
    RenderRequest pReq = (RenderRequest)request.getAttribute("javax.portlet.request");
    PortletPreferences pref = pReq.getPreferences();
%>
<%
List<PublicationDetail> publications = (List<PublicationDetail>) pReq.getAttribute("Publications");
ZoneId userZoneId = (ZoneId) pReq.getAttribute("ZoneId");
boolean first = true;
for (PublicationDetail pub : publications) {
  UserDetail pubUpdater = UserDetail.getById(pub.getUpdaterId());
    String url = m_sContext + URLUtil.getURL("kmelia", null, pub.getPK().getInstanceId()) + pub.getURL();
%>
<% if (!first) {%>
<br/><br/>
<% } else {
    first = false;
  }%>
  <a class="sp-link" href="<%=url %>"><strong><%=WebEncodeHelper.javaStringToHtmlString(pub.getName(language))%></strong></a>
    <% if (pubUpdater != null) { %>
      <br/><view:username userId="<%=pubUpdater.getId() %>"/> - <%=TemporalFormatter.toLocalizedDate(pub.getVisibility().getPeriod().getStartDate(), userZoneId, language)%>
    <% } %>
    <% if (pubUpdater == null && pub.getUpdateDate() != null) { %>
      <br/><%=DateUtil.getOutputDate(pub.getUpdateDate(), language) %>
    <% } %>
    <% if ("checked".equalsIgnoreCase(pref.getValue("displayDescription", "")) && StringUtil
        .isDefined(pub.getDescription(language))) { %>
      <br/><%=WebEncodeHelper.convertWhiteSpacesForHTMLDisplay(WebEncodeHelper.javaStringToHtmlString(pub.getDescription(language))) %>
    <% } %>
<% } %>
<br/>
<c:if test="${rssUrl != null}">
<a href="<c:url value="${rssUrl}" />" class="rss_link"><img src="<c:url value="/util/icons/rss.gif" />" border="0" alt="rss"/></a>
</c:if>
