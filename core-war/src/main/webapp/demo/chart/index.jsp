<%@ page import="java.util.Date" %>
<%@ page import="org.apache.commons.lang3.time.DateUtils" %>
<%@ page import="org.silverpeas.core.chart.period.PeriodChart" %>
<%@ page import="org.silverpeas.core.date.period.Period" %>
<%@ page import="org.silverpeas.core.date.period.PeriodType" %>
<%@ page import="org.silverpeas.core.chart.pie.PieChart" %>
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

<%@ page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>
<%@ taglib tagdir="/WEB-INF/tags/silverpeas/util" prefix="viewTags" %>

<%
  Date month1 = new Date();
  Date month2 = DateUtils.addMonths(month1, 1);
  Date month3 = DateUtils.addMonths(month2, 1);

  PeriodChart periodLineChart = PeriodChart.fromTitle("Period line chart example");
  periodLineChart.forX(Period.from(month1, PeriodType.month)).add(26);
  periodLineChart.forX(month2, PeriodType.month).add(38);
  periodLineChart.forX(Period.from(month3, PeriodType.month)).add(75);

  PeriodChart periodBarChart = PeriodChart
      .fromTitle("Period bar chart example (same data as the one above), with axis titles");
  periodBarChart.forX(Period.from(month1, PeriodType.month)).add(26);
  periodBarChart.forX(month2, PeriodType.month).add(38);
  periodBarChart.forX(Period.from(month3, PeriodType.month)).add(75);
  periodBarChart.getAxisX().setTitle("Axis X title");
  periodBarChart.getAxisY().setTitle("Axis Y title");

  PieChart pieChart = PieChart.fromTitle("Pie chart example");
  pieChart.add("Part 1", 26);
  pieChart.add("Part 2", 38);
  pieChart.add("Part 3", 75);
%>


<c:set var="periodLineChart" value="<%=periodLineChart%>"/>
<c:set var="periodBarChart" value="<%=periodBarChart%>"/>
<c:set var="pieChart" value="<%=pieChart%>"/>

<html xmlns="http://www.w3.org/1999/xhtml">
<head>
  <title>Demo charts</title>
  <view:looknfeel/>
  <script type="text/javascript">
    function callback(item) {
      notyInfo("Part <b>" + item.srcData.label + "</b> with value " + item.srcData.value +
          " clicked");
    }
  </script>
</head>
<body>
<h1>Usage examples of Silverpeas chart API</h1>
<ul>
  <li><viewTags:displayChart chart="${periodLineChart}"/></li>
  <li><viewTags:displayChart chart="${periodBarChart}" displayAsBars="true"/></li>
  <li><viewTags:displayChart chart="${pieChart}" onItemClick="callback"/></li>
</ul>
</body>
</html>