<%--

    Copyright (C) 2000 - 2018 Silverpeas

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
<%@ page import="org.silverpeas.core.admin.user.constant.UserAccessLevel" %>
<%@ page import="org.silverpeas.core.chart.pie.PieChart" %>
<%@ page import="org.silverpeas.core.chart.pie.PieChartItem" %>
<%@ page import="org.silverpeas.core.util.memory.MemoryUnit" %>

<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%@ include file="checkSilverStatistics.jsp" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<%@ taglib tagdir="/WEB-INF/tags/silverpeas/util" prefix="viewTags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%
  String spaceId = (String) request.getAttribute("SpaceId");
	List<String[]> vPath = (List<String[]>) request.getAttribute("Path");
  List<String[]> vStatsData = (List<String[]>)request.getAttribute("StatsData");
  UserAccessLevel userProfile = (UserAccessLevel)request.getAttribute("UserProfile");
  PieChart chart = (PieChart) request.getAttribute("Chart");

  long totalSize = 0L;

	TabbedPane tabbedPane = gef.getTabbedPane();
	if (UserAccessLevel.ADMINISTRATOR.equals(userProfile)) {
		tabbedPane.addTab(resources.getString("silverStatisticsPeas.JobPeas"), m_context+"/RsilverStatisticsPeas/jsp/ViewVolumeServices",false);
	}
	tabbedPane.addTab(resources.getString("silverStatisticsPeas.volumes.tab.contributions"), m_context+"/RsilverStatisticsPeas/jsp/ViewVolumePublication",false);
	tabbedPane.addTab(resources.getString("GML.attachments"),"javascript:displayVolumes();",true);

	ArrayPane arrayPane = gef.getArrayPane("List", "ViewVolumeSizeServer"+( (spaceId==null) ? "" : ("?SpaceId="+spaceId) ), request,session);
	arrayPane.setVisibleLineNumber(50);
  arrayPane.addArrayColumn(resources.getString("silverStatisticsPeas.organisation"));
  arrayPane.addArrayColumn(resources.getString("silverStatisticsPeas.AttachmentsSize"));

  if (vStatsData != null) {
    Iterator<PieChartItem> itChartItems = chart.getItems().iterator();
    for (String[] item : vStatsData) {
      PieChartItem charItem = itChartItems.next();
      ArrayLine arrayLine = arrayPane.addArrayLine();
      if ("SPACE".equals(item[0])) {
        String url = "ViewVolumeSizeServer?SpaceId=" + item[1];
        charItem.addExtra("spaceStatisticUrl", url);
        arrayLine.addArrayCellLink("<b>" + item[2] + "</b>",
            "javascript:displaySubSpaceStatistics('" + url + "')");
      } else {
        arrayLine.addArrayCellText(item[2]);
      }
      long size = Long.parseLong(item[3]);
      totalSize += size;
      String formattedSize = FileRepositoryManager.formatFileSize(size);
      ArrayCellText cellTextCount = arrayLine.addArrayCellText(formattedSize);
      charItem.addExtra("formattedSize", formattedSize);
      cellTextCount.setCompareOn(size);
    }
  }
%>

<c:set var="pieChart" value="${requestScope.Chart}"/>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html>
<head>
<title><%=resources.getString("GML.popupTitle")%></title>
<view:looknfeel />
<script type="text/javascript">
	function changeDisplay() {
		document.volumeServerFormulaire.action = document.volumeServerFormulaire.Display.value;
		$.progressMessage();
		document.volumeServerFormulaire.submit();
	}

  function displayVolumes() {
    $.progressMessage();
    $.get('<c:url value="/RsilverStatisticsPeas/jsp/ViewVolumeServer" />', function(newContent) {
      var newPage = document.open("text/html", "replace");
      newPage.write(newContent);
      newPage.close();
    });
  }

  function onItemClickHelp(item) {
    return (item.srcData && item.srcData.extra && (typeof item.srcData.extra.spaceStatisticUrl === 'string'));
  }

  function onItemClick(item) {
    if (onItemClickHelp(item)) {
      displaySubSpaceStatistics(item.srcData.extra.spaceStatisticUrl);
    }
  }

  function displaySubSpaceStatistics(url) {
    $.progressMessage();
    document.location.href = url;
  }

  function formatChartToolTipValue(value) {
    return (value / 1024 / 1024).roundDown(2) + " <%=MemoryUnit.MB.getLabel()%>";
  }
</script>
</head>
<body class="admin stats volume attachments page_content_admin">
 <form name="volumeServerFormulaire" action="ViewVolumeSizeServer" method="post">
<%
	browseBar.setDomainName(resources.getString("silverStatisticsPeas.statistics") + " > "+resources.getString("silverStatisticsPeas.Volumes") + " > "+resources.getString("GML.attachments"));
    browseBar.setComponentName(resources.getString("silverStatisticsPeas.AttachmentsSize"), "ViewVolumeSizeServer?SpaceId=");

    if (spaceId != null && !"".equals(spaceId))	{
		String path = "";
		String separator = "";
		for (String[] pathItem : vPath) {
			if (UserAccessLevel.ADMINISTRATOR.equals(userProfile)) {
				path += separator + "<a href=\"ViewVolumeSizeServer"+( (pathItem[0]==null) ? "" : ("?SpaceId="+pathItem[0]) )+"\">"+pathItem[1]+ "</a>";
			} else {
				path += separator + pathItem[1];
			}
			separator = " > ";
		}
		browseBar.setPath(path);
	}

	out.println(window.printBefore());
  out.println(tabbedPane.print());
  out.println(frame.printBefore());
%>

<br/>

	<div align="right">
		<%=resources.getString("silverStatisticsPeas.Display")%>&nbsp;:
		<select name="Display" size="1" onchange="changeDisplay()">
			<option value="ViewVolumeServer"><%=resources.getString("silverStatisticsPeas.AttachmentsNumber")%></option>
			<option value="ViewVolumeSizeServer" selected><%=resources.getString("silverStatisticsPeas.AttachmentsSize")%></option>
			<% if (UserAccessLevel.ADMINISTRATOR.equals(userProfile)) { %>
				<option value="ViewEvolutionVolumeSizeServer"><%=resources.getString("silverStatisticsPeas.AttachmentsTotalSize")%></option>
			<% } %>
		</select>
	</div>

<% if (vStatsData != null) { %>
   <div class="flex-container">
     <viewTags:displayChart chart="${pieChart}" formatToolTipValue="formatChartToolTipValue" onItemClick="onItemClick" onItemClickHelp="onItemClickHelp"/>
     <div align="center" id="total">
        <span><span class="number"><%=FileRepositoryManager.formatFileSize(totalSize) %></span> <%=resources
            .getString("silverStatisticsPeas.sums.attachments.size") %></span>
     </div>
   </div>
<% } %>
<br/>
<%
    if (vStatsData != null) {
		out.println(arrayPane.print());
    }
  %>
 </form>
<%
out.println(frame.printAfter());
out.println(window.printAfter());
%>
<view:progressMessage/>
</body>
</html>