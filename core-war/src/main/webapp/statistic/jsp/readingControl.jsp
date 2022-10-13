<%--
  ~ Copyright (C) 2000 - 2022 Silverpeas
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
  ~ along with this program.  If not, see <https://www.gnu.org/licenses/>.
  --%>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.arraypanes.ArrayPane" %>
<%@ page import="java.util.List" %>

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

<fmt:message var="userLabel" key="GML.user"/>
<fmt:message var="lastAccessLabel" key="statistic.lastAccess"/>
<fmt:message var="nbAccessLabel" key="statistic.nbAccess"/>
<fmt:message var="detailLabel" key="statistic.detail"/>
<c:url var="infoIconUrl" value="/util/icons/info.gif"/>

<c:set var="currentUserId" value="${sessionScope['SilverSessionController'].userId}"/>
<c:set var="id" value="${param.id}"/>
<jsp:useBean id="id" type="java.lang.String"/>
<c:set var="componentId" value="${param.componentId}"/>
<jsp:useBean id="componentId" type="java.lang.String"/>
<c:set var="objectType" value="${param.objectType}"/>
<jsp:useBean id="objectType" type="java.lang.String"/>

<%
  final ResourceReference resourceReference = new ResourceReference(id, componentId);
  final String resourceType = objectType;
  final List<HistoryByUser> readingState = ArrayPane
    .computeDataUserSessionIfAbsent(request, "statistic_readingControl_list", () -> {
      final StatisticService statisticService = StatisticService.get();
      final List<String> userIds = (List) request.getAttribute("UserIds");
      return statisticService.getHistoryByObject(resourceReference, 1, resourceType, userIds);
    });
%>
<c:set var="readingState" value="<%=readingState%>"/>

<script language="javascript">
  function editDetail(userId, actorName) {
    SP_openWindow(
        "<%=m_context%>/statistic/jsp/detailByUser.jsp?id=${id}&userId=" + userId + "&userName=" +
        actorName + "&componentId=${componentId}&objectType=${objectType}", "blank", "280", "330",
        "scrollbars=no, resizable, alwaysRaised");
  }
</script>
<div id="dynamic-container">
  <view:arrayPane var="readingControl" routingAddress="ReadingControl?fromArrayPane=true" numberLinesPerPage="25">
    <view:arrayColumn title="${userLabel}" compareOn="${h -> h.user}"/>
    <view:arrayColumn title="${lastAccessLabel}" compareOn="${h ->h.lastAccess}"/>
    <view:arrayColumn title="${nbAccessLabel}" compareOn="${h ->h.nbAccess}"/>
    <view:arrayColumn title="${detailLabel}" sortable="false"/>
    <view:arrayLines var="entity" items="${readingState}">
      <view:arrayLine>
        <view:arrayCellText><view:username user="${entity.user}"/></view:arrayCellText>
        <view:arrayCellText text="${entity.lastAccess != null
                                ?  silfn:formatDateAndHour(entity.lastAccess, currentUserLanguage)
                                : ''}"/>
        <view:arrayCellText text="${entity.nbAccess}"/>
        <view:arrayCellText>
          <c:if test="${entity.lastAccess != null}">
            <view:icons>
              <view:icon iconName="${infoIconUrl}"
                         action="javascript:editDetail('${entity.user.id}','${entity.user.displayedName}')"
                         altText="${detailLabel}"/>
            </view:icons>
          </c:if>
        </view:arrayCellText>
      </view:arrayLine>
    </view:arrayLines>
  </view:arrayPane>
  <script type="text/javascript">
    whenSilverpeasReady(function() {
      sp.arrayPane.ajaxControls('#dynamic-container');
    });
  </script>
</div>