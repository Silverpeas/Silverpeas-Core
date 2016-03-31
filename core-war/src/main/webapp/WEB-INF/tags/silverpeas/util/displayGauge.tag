<%@ tag import="org.silverpeas.core.admin.quota.constant.QuotaType" %>
<%@ tag import="java.util.UUID" %>
<%--
  Copyright (C) 2000 - 2013 Silverpeas

  This program is free software: you can redistribute it and/or modify
  it under the terms of the GNU Affero General Public License as
  published by the Free Software Foundation, either version 3 of the
  License, or (at your option) any later version.

  As a special exception to the terms and conditions of version 3.0 of
  the GPL, you may redistribute this Program in connection with Free/Libre
  Open Source Software ("FLOSS") applications as described in Silverpeas's
  FLOSS exception. You should have recieved a copy of the text describing
  the FLOSS exception, and it is also available here:
  "http://www.silverpeas.org/docs/core/legal/floss_exception.html"

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU Affero General Public License for more details.

  You should have received a copy of the GNU Affero General Public License
  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  --%>

<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://www.silverpeas.com/tld/silverFunctions" prefix="silfn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>
<%@ taglib prefix="plugins" tagdir="/WEB-INF/tags/silverpeas/plugins" %>

<fmt:setLocale value="${requestScope.resources.language}"/>
<view:setBundle basename="org.silverpeas.jobStartPagePeas.multilang.jobStartPagePeasBundle" var="spaceBundle"/>

<plugins:includeGauge/>

<%-- Defaults --%>
<c:set var="_gaugeContainerId" value="<%=UUID.randomUUID()%>"/>
<c:set var="_gaugeTitle" value=""/>
<c:set var="_gaugeCurrentValue" value="0"/>
<c:set var="_gaugeCurrentValueLabel" value=""/>
<c:set var="_gaugeMinValue" value="0"/>
<c:set var="_gaugeMaxValue" value="100"/>

<c:set var="_containerWidth" value="200"/>
<c:set var="_containerHeight" value="150"/>

<%-- TAG attributes --%>
<%@ attribute name="title" required="false" description="A title for the gauge" %>
<%@ attribute name="currentValue" required="false" description="The current value of the gauge" %>
<%@ attribute name="currentValueLabel" required="false" description="The label of the current value of the gauge" %>
<%@ attribute name="minValue" required="false" description="The minimum value of the gauge" %>
<%@ attribute name="maxValue" required="false" description="The maximum value of the gauge" %>

<%@ attribute name="containerWidth" required="false" type="java.lang.Integer" description="The width of the HTML container (div)" %>
<%@ attribute name="containerHeight" required="false" type="java.lang.Integer" description="The height of the HTML container (div)" %>

<%-- A quota bean --%>
<%@ attribute name="quotaBean" required="false" type="org.silverpeas.core.admin.quota.model.Quota"
              description="A quota bean (Quota.java). The label of the current value is handled (but it is possible to overwrite it by filling the currentValueLabel attribute parameter of the tag)." %>

<c:choose>
  <%-- Quota bean case --%>
  <c:when test="${quotaBean != null}">
    <c:set var="_COMPONENTS_IN_SPACE_QUOTA_TYPE" value="<%=QuotaType.COMPONENTS_IN_SPACE%>"/>
    <c:set var="_DATA_STORAGE_IN_SPACE_QUOTA_TYPE" value="<%=QuotaType.DATA_STORAGE_IN_SPACE%>"/>
    <c:choose>
      <c:when test="${_COMPONENTS_IN_SPACE_QUOTA_TYPE == quotaBean.type}">
        <c:set var="_gaugeCurrentValue" value="${quotaBean.count}"/>
        <c:set var="_gaugeMinValue" value="${quotaBean.minCount}"/>
        <c:set var="_gaugeMaxValue" value="${quotaBean.maxCount}"/>
        <fmt:message var="_gaugeCurrentValueLabel" key="JSPP.componentSpaceQuotaCurrentCount" bundle="${spaceBundle}">
          <fmt:param value="${_gaugeCurrentValue}"/>
        </fmt:message>
      </c:when>
      <c:when test="${_DATA_STORAGE_IN_SPACE_QUOTA_TYPE == quotaBean.type}">
        <c:set var="_gaugeMaxMemData" value="${silfn:getMemData(quotaBean.maxCount)}"/>
        <c:set var="_gaugeCurrentValue" value="${silfn:getMemorySizeConvertedTo(silfn:getMemData(quotaBean.count), _gaugeMaxMemData.bestUnit)}"/>
        <c:set var="_gaugeMinValue" value="${silfn:getMemorySizeConvertedTo(silfn:getMemData(quotaBean.minCount), _gaugeMaxMemData.bestUnit)}"/>
        <c:set var="_gaugeMaxValue" value="${_gaugeMaxMemData.bestValue}"/>
        <c:set var="_gaugeCurrentLoadPercentage" value="<%=quotaBean.getLoadPercentage().longValue()%>"/>
        <c:set var="_gaugeCurrentValueLabel" value="${_gaugeMaxMemData.bestUnit.label} (${_gaugeCurrentLoadPercentage} %)"/>
      </c:when>
      <c:otherwise>
        <c:set var="_gaugeCurrentValue" value="${quotaBean.count}"/>
        <c:set var="_gaugeMinValue" value="${quotaBean.minCount}"/>
        <c:set var="_gaugeMaxValue" value="${quotaBean.maxCount}"/>
      </c:otherwise>
    </c:choose>
  </c:when>
  <c:otherwise>
    <c:if test="${currentValue}">
      <c:set var="_gaugeCurrentValue" value="${currentValue}"/>
    </c:if>
    <c:if test="${minValue}">
      <c:set var="_gaugeMinValue" value="${minValue}"/>
    </c:if>
    <c:if test="${maxValue}">
      <c:set var="_gaugeMaxValue" value="${maxValue}"/>
    </c:if>
  </c:otherwise>
</c:choose>

<c:if test="${title != null}">
  <c:set var="_gaugeTitle" value="${title}"/>
</c:if>
<c:if test="${currentValueLabel != null}">
  <c:set var="_gaugeCurrentValueLabel" value="${currentValueLabel}"/>
</c:if>

<c:if test="${containerWidth != null}">
  <c:set var="_containerWidth" value="${containerWidth}"/>
</c:if>
<c:if test="${containerHeight != null}">
  <c:set var="_containerHeight" value="${containerHeight}"/>
</c:if>

<div id="${_gaugeContainerId}" class="gauge-container" style="width:${_containerWidth}px; height:${_containerHeight}px"></div>
<script type="text/javascript">
  createGauge({
    containerId : '${_gaugeContainerId}',
    title : '${_gaugeTitle}',
    currentValue : ${_gaugeCurrentValue},
    currentValueLabel : '${_gaugeCurrentValueLabel}',
    minValue : ${_gaugeMinValue},
    maxValue : ${_gaugeMaxValue}
  });
</script>