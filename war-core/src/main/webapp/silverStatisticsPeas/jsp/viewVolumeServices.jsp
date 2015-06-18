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

<%@ include file="checkSilverStatistics.jsp" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%
//Recuperation des parametres
  ChartVO chart = (ChartVO) request.getAttribute("Chart");

  int totalNumberOfInstances = 0;

  TabbedPane tabbedPane = gef.getTabbedPane();
  tabbedPane.addTab(resources.getString("silverStatisticsPeas.JobPeas"), m_context
        + "/RsilverStatisticsPeas/jsp/ViewVolumeServices", true);
  tabbedPane.addTab(resources.getString("silverStatisticsPeas.volumes.tab.contributions"),
        m_context + "/RsilverStatisticsPeas/jsp/ViewVolumePublication", false);
  tabbedPane.addTab(resources.getString("GML.attachments"), "javascript:displayVolumes();", false);
%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html>
  <head>
    <title><%=resources.getString("GML.popupTitle")%></title>
    <view:looknfeel />
    <view:includePlugin name="chart"/>
    <script type="text/javascript">
      function displayVolumes() {
        $.progressMessage();
        $.get('<c:url value="/RsilverStatisticsPeas/jsp/ViewVolumeServer" />', function(newContent) {
          var newPage = document.open("text/html", "replace");
          newPage.write(newContent);
          newPage.close();
        });
      }

      // A custom label formatter used by several of the plots
      function labelFormatter(label, series) {
        return "<div style='font-size:8pt; text-align:center; padding:2px; color:white;'>" + Math.round(series.percent) + "%</div>";
      }

      $(function() {

        var data = [
          <% if (chart != null) {
            List<String> x = chart.getX();
            List<Long> y = chart.getY();
            for (int i = 0; i<x.size(); i++) {
              out.print("{ label: \""+x.get(i)+"\",  data: "+y.get(i)+"}");
              if (i < x.size()) {
                out.println(",");
              }
            }
          } %>
        ];

        $.plot(".chart", data, {
          series: {
            pie: {
              show: true,
              combine: {
                color: '#999',
                threshold: 0.03
              },
              radius: 1,
              label: {
                show: true,
                radius: 3/4,
                formatter: labelFormatter,
                background: {
                  opacity: 0.5
                }
              }
            }
          },
          legend: {
            show: true
          },
          colors: ["#1c94d4", "#7eb73b", "#ec9c01", "#f7d723", "#11c3d8", "#e03183", "#639784",
            "#88376", "#8d5788", "#f79992", "#ce6f6f", "#f63333"]
        });

      });
    </script>
  </head>
  <body class="admin stats volume applications">
    <%
      browseBar.setDomainName(resources.getString("silverStatisticsPeas.statistics"));
      browseBar.setComponentName(resources.getString("silverStatisticsPeas.Volumes"));
      browseBar.setPath(resources.getString("silverStatisticsPeas.JobPeas"));

      out.println(window.printBefore());
        out.println(tabbedPane.print());
        out.println(frame.printBefore());

      ArrayPane arrayPane = gef.getArrayPane("List", "", request, session);
        arrayPane.setVisibleLineNumber(50);

      arrayPane.addArrayColumn(resources.getString("GML.jobPeas"));
        arrayPane.addArrayColumn(resources.getString("silverStatisticsPeas.InstancesNumber"));

  if (chart != null) {
    List<String> x = chart.getX();
    List<Long> y = chart.getY();
    for (int i=0; i<x.size(); i++) {
      ArrayLine arrayLine = arrayPane.addArrayLine();
      arrayLine.addArrayCellText(x.get(i));

      String nb = String.valueOf(y.get(i));
      ArrayCellText cellText = arrayLine.addArrayCellText(nb);
      cellText.setCompareOn(Integer.parseInt(nb));
      totalNumberOfInstances += Integer.parseInt(nb);
    }
  }
    %>

    <div class="flex-container">
      <% if (chart != null) { %>
      <div class="chart-area">
        <h3 class="txttitrecol"><%=chart.getTitle()%></h3>
        <div class="chart"></div>
      </div>
      <% } %>
      <div align="center" id="total">
        <span><span class="number"><%=totalNumberOfInstances%></span> <%=resources.getString(
            "silverStatisticsPeas.sums.applications")%></span>
      </div>
    </div>


      <%
      if (chart != null) {
        out.println(arrayPane.print());
      }

  out.println(frame.printAfter());
    out.println(window.printAfter());
    %>
    <view:progressMessage/>
  </body>
</html>