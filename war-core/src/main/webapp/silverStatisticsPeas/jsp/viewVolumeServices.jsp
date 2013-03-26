<%--

    Copyright (C) 2000 - 2012 Silverpeas

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
  Vector<String[]> vStatsData = (Vector<String[]>) request.getAttribute("StatsData");

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
    <script type="text/javascript">
      function displayVolumes() {
        $.progressMessage();
        $.get('<c:url value="/RsilverStatisticsPeas/jsp/ViewVolumeServer" />', function(newContent) {
          var newPage = document.open("text/html", "replace");
          newPage.write(newContent);
          newPage.close();
        });
      }
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

  if (vStatsData != null) {
      for (String[] item : vStatsData) {
        ArrayLine arrayLine = arrayPane.addArrayLine();
        arrayLine.addArrayCellText(item[0]);
        ArrayCellText cellTextCount = arrayLine.addArrayCellText(item[1]);
        Integer nbInstances = new Integer(item[1]);
        totalNumberOfInstances += nbInstances;
        cellTextCount.setCompareOn(nbInstances);
      }
    }
    %>

    <div align="center" id="chart">
      <img src="<%=m_context%>/ChartServlet/?chart=KM_INSTANCES_CHART&random=<%=new Date().getTime()%>"/>
    </div>
    <div align="center" id="total">
      <span><span class="number"><%=totalNumberOfInstances%></span> <%=resources.getString("silverStatisticsPeas.sums.applications")%></span>
    </div>

      <%
      if (vStatsData != null) {
        out.println(arrayPane.print());
      }

  out.println(frame.printAfter());
    out.println(window.printAfter());
    %>
    <view:progressMessage/>
  </body>
</html>