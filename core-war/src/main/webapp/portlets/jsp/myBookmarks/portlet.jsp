<%--
  ~ Copyright (C) 2000 - 2021 Silverpeas
  ~
  ~ This program is free software: you can redistribute it and/or modify
  ~ it under the terms of the GNU Affero General Public License as
  ~ published by the Free Software Foundation, either version 3 of the
  ~ License, or (at your option) any later version.
  ~
  ~ As a special exception to the terms and conditions of version 3.0 of
  ~ the GPL, you may redistribute this Program in connection with Free/Libre
  ~ Open Source Software ("FLOSS") applications as described in Silverpeas's
  ~ FLOSS exception.  You should have received a copy of the text describing
  ~ the FLOSS exception, and it is also available here:
  ~ "https://www.silverpeas.org/legal/floss_exception.html"
  ~
  ~ This program is distributed in the hope that it will be useful,
  ~ but WITHOUT ANY WARRANTY; without even the implied warranty of
  ~ MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  ~ GNU Affero General Public License for more details.
  ~
  ~ You should have received a copy of the GNU Affero General Public License
  ~ along with this program.  If not, see <http://www.gnu.org/licenses/>.
  --%>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="org.silverpeas.core.util.JSONCodec" %>
<%@ page import="org.silverpeas.core.webapi.mylinks.MyLinkEntity" %>
<%@ page import="java.util.stream.Collectors" %>

<%@ include file="../portletImport.jsp"%>

<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>

<portlet:defineObjects/>

<fmt:setLocale value="${sessionScope[SilverSessionController].favoriteLanguage}" />
<view:setBundle basename="org.silverpeas.homePage.multilang.homePageBundle"/>

<c:set var="bookmarks" value="${requestScope.Links}"/>
<jsp:useBean id="bookmarks" type="java.util.List<org.silverpeas.core.mylinks.model.LinkDetail>"/>

<c:choose>
  <c:when test="${empty bookmarks}">
    <fmt:message key="NoFavorites"/>
  </c:when>
  <c:otherwise>
    <view:script src="/myLinksPeas/jsp/javaScript/vuejs/mylinkspeas.js"/>
    <view:link href="/myLinksPeas/jsp/styleSheets/myLinksPeas.css"/>
    <div id="mylinkspeas-widget">
      <mylinkspeas-widget v-bind:links="links" v-bind:portlet="true"/>
    </div>
    <c:set var="jsonBookmark" value="<%=JSONCodec.encode(bookmarks.stream().map(b -> MyLinkEntity.fromLinkDetail(b, null)).collect(Collectors.toList()))%>"/>
    <script type="text/javascript">
      new Vue({
        el : '#mylinkspeas-widget',
        data : function() {
          return {
            links : ${jsonBookmark}
          }
        }
      });
    </script>
  </c:otherwise>
</c:choose>