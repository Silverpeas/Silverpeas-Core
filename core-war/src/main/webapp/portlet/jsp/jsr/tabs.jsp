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

<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%@page import="org.silverpeas.web.portlets.portal.DriverUtil" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>

<!--Load the resource bundle for the page -->
<fmt:setLocale value="${sessionScope['SilverSessionController'].favoriteLanguage}" />
<view:setBundle basename="DesktopMessages" />
<%
   java.util.ArrayList tabs = new java.util.ArrayList();

   java.util.HashMap tab = new java.util.HashMap();
   tab.put("id", "dt");
   tab.put("url", DriverUtil.getPortletsURL(request));
%>
    <c:set var="portletsTitle">
        <fmt:message key="portlets"/>
    </c:set>
<%
   tab.put("title",pageContext.getAttribute("portletsTitle"));
   tabs.add(tab);

   tab = new java.util.HashMap();
   tab.put("id", "portletAdmin");
   tab.put("url",DriverUtil.getAdminURL(request));
%>
    <c:set var="adminTitle">
        <fmt:message key="admin"/>
    </c:set>
<%
   tab.put("title",pageContext.getAttribute("adminTitle"));
   tabs.add(tab);

   if(DriverUtil.isWSRPAvailable()){
       tab = new java.util.HashMap();
       tab.put("id", "wsrp");
       tab.put("url",DriverUtil.getWSRPURL(request));
       tab.put("title", DriverUtil.getWSRPTabName());
       tabs.add(tab);
   }

   pageContext.setAttribute("tabs", tabs);
%>

<c:if test="${selectedTab==null}">
  <c:set var="selectedTab" value="dt" scope="session" />
</c:if>
<c:if test="${param.selectedTab!=null}">
  <c:set var="selectedTab" value="${param.selectedTab}" scope="session" />
</c:if>

<div id="portal-tabs">
<ul id="portal-tablist">
<c:forEach var='tab' items='${tabs}' varStatus="status">
  <c:set var="id" value='${tab["id"]}' />
  <li><a href="<c:out value='${tab["url"]}'/>" <c:if test="${id==selectedTab}">id="selected"</c:if>><c:out value='${tab["title"]}'/></a></li>
</c:forEach>
</ul>
</div> <!-- closes portal-tabs -->