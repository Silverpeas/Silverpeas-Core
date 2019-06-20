<%--

    Copyright (C) 2000 - 2019 Silverpeas

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
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

<%@ page import="org.apache.commons.lang3.tuple.Pair" %>
<%@ page import="org.silverpeas.core.admin.PaginationPage" %>
<%@ page import="org.silverpeas.core.cache.model.SimpleCache" %>
<%@ page import="org.silverpeas.core.cache.service.CacheServiceProvider" %>
<%@ page import="org.silverpeas.core.silverstatistics.access.model.HistoryCriteria.QUERY_ORDER_BY" %>
<%@ page import="org.silverpeas.core.util.SilverpeasList" %>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.arraypanes.ArrayPane" %>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.pagination.Pagination" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="java.util.Map" %>
<%@ page import="static org.silverpeas.core.silverstatistics.access.model.HistoryCriteria.QUERY_ORDER_BY.ACCESS_DATE_ASC" %>
<%@ page import="static org.silverpeas.core.silverstatistics.access.model.HistoryCriteria.QUERY_ORDER_BY.ACCESS_DATE_DESC" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.silverpeas.com/tld/silverFunctions" prefix="silfn" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>

<%-- Set resource bundle --%>
<c:set var="currentUserLanguage" value="${sessionScope['SilverSessionController'].favoriteLanguage}"/>
<fmt:setLocale value="${currentUserLanguage}"/>
<view:setBundle basename="org.silverpeas.statistic.multilang.statistic"/>

<%@ include file="checkStatistic.jsp" %>

<fmt:message var="popupLabel" key="GML.popupTitle"/>
<fmt:message var="userLabel" key="GML.user"/>
<fmt:message var="closeLabel" key="GML.close"/>
<fmt:message var="detailLabel" key="statistic.detail"/>

<c:set var="currentUserId" value="${sessionScope['SilverSessionController'].userId}"/>
<c:set var="id" value="${param.id}"/>
<jsp:useBean id="id" type="java.lang.String"/>
<c:set var="componentId" value="${param.componentId}"/>
<jsp:useBean id="componentId" type="java.lang.String"/>
<c:set var="userId" value="${param.userId}"/>
<jsp:useBean id="userId" type="java.lang.String"/>
<c:set var="userName" value="${param.userName}"/>
<c:set var="objectType" value="${param.objectType}"/>
<jsp:useBean id="objectType" type="java.lang.String"/>
<c:set var="routingAddress" value="detailByUser.jsp?id=${id}&componentId=${componentId}&objectType=${objectType}&userId=${userId}&userName=${userName}"/>

<%
  final SimpleCache sessionCache = CacheServiceProvider.getSessionCacheService().getCache();
  final Map<Integer, Pair<QUERY_ORDER_BY, QUERY_ORDER_BY>> ORDER_BIES = sessionCache
      .computeIfAbsent("statistic_readingControl_byUser_orderBies", Map.class, () -> {
        Map<Integer, Pair<QUERY_ORDER_BY, QUERY_ORDER_BY>> mapping = new HashMap<>();
        mapping.put(1, Pair.of(ACCESS_DATE_ASC, ACCESS_DATE_DESC));
        return mapping;
      });

  final String orderByCacheKey = "statistic_readingControl_byUser_orderBies_choice";
  QUERY_ORDER_BY orderBy = sessionCache.computeIfAbsent(orderByCacheKey, QUERY_ORDER_BY.class, () -> QUERY_ORDER_BY.ACCESS_DATE_DESC);
  final QUERY_ORDER_BY requestOrderBy = ArrayPane.getOrderByFrom(request, ORDER_BIES);
  if (requestOrderBy != null) {
    orderBy = requestOrderBy;
    sessionCache.put(orderByCacheKey, orderBy);
  }

  final String paginationCacheKey = "statistic_readingControl_byUser_pagination";
  final PaginationPage currentPagination = sessionCache.get(paginationCacheKey, PaginationPage.class);
  final PaginationPage newPagination = Pagination.getPaginationPageFrom(request, currentPagination);
  sessionCache.put(paginationCacheKey, newPagination);

  final StatisticService statisticService = ServiceProvider.getService(StatisticService.class);
  final ResourceReference resourceReference = new ResourceReference(id, componentId);
  final SilverpeasList<HistoryObjectDetail> readingState = statisticService
      .getHistoryByObjectAndUser(resourceReference, 1, objectType, userId, newPagination, orderBy);
%>
<c:set var="readingState" value="<%=readingState%>"/>

<html>
<head>
  <title>${popupLabel}</title>
  <view:looknfeel/>
  <style type="text/css">
    .hautFrame {
      padding-top: 0;
    }
    .cellBrowseBar,.cellOperation {
      display: none;
    }
    #header {
      white-space: nowrap;
      padding-bottom: 3px;
    }
  </style>
</head>
<body>
<view:window popup="true">
  <view:frame>
    <div id="header">
      <span class="txtlibform">${userLabel} :</span><span>${userName}</span>
    </div>
    <div id="dynamic-container">
      <view:arrayPane var="detailByUser" routingAddress="${routingAddress}" numberLinesPerPage="<%=newPagination.getPageSize()%>">
        <view:arrayColumn title="${detailLabel}" sortable="true"/>
        <view:arrayLines var="entity" items="${readingState}">
          <view:arrayLine>
            <view:arrayCellText text="${silfn:formatDateAndHour(entity.date, currentUserLanguage)}"/>
          </view:arrayLine>
        </view:arrayLines>
      </view:arrayPane>
      <script type="text/javascript">
        whenSilverpeasReady(function() {
          sp.arrayPane.ajaxControls('#dynamic-container');
        });
      </script>
    </div>
  </view:frame>
  <view:buttonPane>
    <view:button label="${closeLabel}" action="javascript:window.close()"/>
  </view:buttonPane>
</view:window>
</body>
</html>
