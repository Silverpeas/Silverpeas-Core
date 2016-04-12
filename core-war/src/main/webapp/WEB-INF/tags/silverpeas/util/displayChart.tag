<%--
  Copyright (C) 2000 - 2015 Silverpeas

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
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>

<%@ attribute name="chart" required="true"
              type="org.silverpeas.core.chart.Chart"
              description="The chart to display" %>

<%@ attribute name="colors" required="false"
              type="java.lang.String"
              description="The main colors separated by a comma" %>

<%@ attribute name="displayAsBars" required="false"
              type="java.lang.Boolean"
              description="Indicates if the given chart must be displayed as bars" %>

<%@ attribute name="chartClass" required="false"
              type="java.lang.String"
              description="Permits to specify classes for class container" %>

<%@ attribute name="formatToolTipValue" required="false"
              type="java.lang.String"
              description="The name of javascript function that will be called in order to format the value presented into tooltip" %>

<%@ attribute name="onItemClickHelp" required="false"
              type="java.lang.String"
              description="The name of a callback that returns a boolean value or directly a formatted help as string.
              If a boolean value is returned, true indicates that the help must be displayed and false indicates the contrary." %>
<%@ attribute name="onItemClick" required="false"
              type="java.lang.String"
              description="Callback to perform on a click on an item" %>

<c:if test="${chart != null}">

  <c:choose>
    <c:when test="${empty requestScope._idCount4displayChart}">
      <c:set var="_idCount4displayChart" value="${0}" scope="request"/>
    </c:when>
    <c:otherwise>
      <c:set var="_idCount4displayChart" value="${_idCount4displayChart + 1}" scope="request"/>
    </c:otherwise>
  </c:choose>
  <c:set var="chartDomId" value="chart${_idCount4displayChart}"/>
  <c:set var="isDisplayAsBarsRequired" value="${displayAsBars != null and displayAsBars}"/>
  <c:set var="chartClass" value="${empty chartClass ? 'chart' : chartClass}"/>

  <view:includePlugin name="chart"/>

  <div class="flex-container">
    <div id="${chartDomId}Container" class="chart-area">
      <h3 class="txttitrecol">${chart.title}</h3>

      <div id="${chartDomId}" class="${chartClass}"></div>
    </div>
  </div>

  <script type="text/JavaScript">
    jQuery(function() {
      var options = {
        downloadSelector : "#${chartDomId}Container",
        chartSelector : "#${chartDomId}",
        isDisplayAsBars : ${isDisplayAsBarsRequired},
        chart : <%=chart.asJson()%>
      };
      <c:if test="${not empty formatToolTipValue}">
      options.formatToolTipValue = ${formatToolTipValue};
      </c:if>
      <c:if test="${not empty onItemClick}">
      options.onItemClick = ${onItemClick};
      </c:if>
      <c:if test="${not empty onItemClickHelp}">
      options.onItemClickHelp = ${onItemClickHelp};
      </c:if>
      <c:if test="${not empty colors}">
      options.colors = [<c:forTokens items="${colors}" delims="," var="color" varStatus="status"><c:if test="${not status.first}">, </c:if>"${color}"</c:forTokens>];
      </c:if>
      new ChartManager(options);
    });
  </script>

</c:if>