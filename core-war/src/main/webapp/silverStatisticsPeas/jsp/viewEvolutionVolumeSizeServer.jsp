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
<%@ page import="org.silverpeas.core.admin.user.constant.UserAccessLevel" %>
<%@ page import="org.silverpeas.core.util.memory.MemoryUnit" %>

<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%@ include file="checkSilverStatistics.jsp" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<%@ taglib tagdir="/WEB-INF/tags/silverpeas/util" prefix="viewTags" %>
<%!

	private String formatDate(MultiSilverpeasBundle resources, String date)
	{//date au format AAAA-MM-jj

		String dateFormate = "";

		String annee = date.substring(0, 4);
		int mois = new Integer(date.substring(5, 7)).intValue();

		if(mois == 1) {
			dateFormate = resources.getString("silverStatisticsPeas.January");
		} else if(mois == 2) {
			dateFormate = resources.getString("silverStatisticsPeas.February");
		} else if(mois == 3) {
			dateFormate = resources.getString("silverStatisticsPeas.March");
		} else if(mois == 4) {
			dateFormate = resources.getString("silverStatisticsPeas.April");
		} else if(mois == 5) {
			dateFormate = resources.getString("silverStatisticsPeas.May");
		} else if(mois == 6) {
			dateFormate = resources.getString("silverStatisticsPeas.June");
		} else if(mois == 7) {
			dateFormate = resources.getString("silverStatisticsPeas.July");
		} else if(mois == 8) {
			dateFormate = resources.getString("silverStatisticsPeas.August");
		} else if(mois == 9) {
			dateFormate = resources.getString("silverStatisticsPeas.September");
		} else if(mois == 10) {
			dateFormate = resources.getString("silverStatisticsPeas.October");
		} else if(mois == 11) {
			dateFormate = resources.getString("silverStatisticsPeas.November");
		} else if(mois == 12) {
			dateFormate = resources.getString("silverStatisticsPeas.December");
		}

		dateFormate += " "+annee;

		return dateFormate;
	}
%>

<%
  Vector<String[]> vStatsData = (Vector<String[]>)request.getAttribute("StatsData");
  UserAccessLevel userProfile = (UserAccessLevel)request.getAttribute("UserProfile");

	TabbedPane tabbedPane = gef.getTabbedPane();
	if (UserAccessLevel.ADMINISTRATOR.equals(userProfile)) {
		tabbedPane.addTab(resources.getString("silverStatisticsPeas.JobPeas"), m_context+"/RsilverStatisticsPeas/jsp/ViewVolumeServices",false);
	}
	tabbedPane.addTab(resources.getString("silverStatisticsPeas.volumes.tab.contributions"), m_context+"/RsilverStatisticsPeas/jsp/ViewVolumePublication",false);
	tabbedPane.addTab(resources.getString("GML.attachments"), "javascript:displayVolumes();",true);
%>

<c:set var="periodChart" value="${requestScope.Chart}"/>

<html>
<head>
<title><%=resources.getString("GML.popupTitle")%></title>
<view:looknfeel />
<script type="text/javascript">
	function changeDisplay() {
		document.volumeServerFormulaire.action = document.volumeServerFormulaire.Display.value;
		document.volumeServerFormulaire.submit();
	}

  function displayVolumes() {
    $.progressMessage();
    location.href = '<c:url value="/RsilverStatisticsPeas/jsp/ViewVolumeServer" />';
  }

  function formatChartToolTipValue(value) {
    return value + " <%=MemoryUnit.MB.getLabel()%>";
  }
</script>
</head>
<body class="admin stats">
<form name="volumeServerFormulaire" action="ViewEvolutionVolumeSizeServer" method="post">
<%
	browseBar.setDomainName(resources.getString("silverStatisticsPeas.statistics") + " > "+resources.getString("silverStatisticsPeas.Volumes"));
    browseBar.setComponentName(resources.getString("GML.attachments"));
    browseBar.setPath(resources.getString("silverStatisticsPeas.AttachmentsTotalSize"));

	out.println(window.printBefore());
    out.println(tabbedPane.print());
    out.println(frame.printBefore());
%>

<br/>

	<div align="right">
		<%=resources.getString("silverStatisticsPeas.Display")%>&nbsp;:
		<select name="Display" size="1" onchange="changeDisplay()">
			<option value="ViewVolumeServer"><%=resources.getString("silverStatisticsPeas.AttachmentsNumber")%></option>
			<option value="ViewVolumeSizeServer"><%=resources.getString("silverStatisticsPeas.AttachmentsSize")%></option>
			<% if (UserAccessLevel.ADMINISTRATOR.equals(userProfile)) { %>
				<option value="ViewEvolutionVolumeSizeServer" selected><%=resources.getString("silverStatisticsPeas.AttachmentsTotalSize")%></option>
			<% } %>
		</select>
	</div>
<% if (vStatsData != null) { %>
  <div class="flex-container">
    <viewTags:displayChart chart="${periodChart}" formatToolTipValue="formatChartToolTipValue"/>
  </div>
<% } %>
<br/>
<%
		ArrayPane arrayPane = gef.getArrayPane("List", "", request,session);
			arrayPane.setVisibleLineNumber(50);
		if (arrayPane.getColumnToSort()==0) {
			arrayPane.setColumnToSort(1);
		}

		ArrayColumn arrayColumn1 = arrayPane.addArrayColumn(resources.getString("GML.date"));
            ArrayColumn arrayColumn2 = arrayPane.addArrayColumn(resources.getString("GML.name"));
            ArrayColumn arrayColumn3 = arrayPane.addArrayColumn(resources.getString("GML.size"));

		if (vStatsData != null) {
			ArrayCellText cellText;

			for (String[] item : vStatsData) {
					ArrayLine arrayLine = arrayPane.addArrayLine();

			//transformation de la date 2007-02-01 -> Fév. 2007
					cellText = arrayLine.addArrayCellText(formatDate(resources, item[0]));
					cellText.setCompareOn(item[0]);

				arrayLine.addArrayCellText(item[1]);

				Long size = new Long(item[2]);
				cellText = arrayLine.addArrayCellText(FileRepositoryManager.formatFileSize(size * 1024));
				cellText.setCompareOn(size);
			}

				out.println(arrayPane.print());
		        out.println("");
	    }
  %>

 </form>
<%
out.println(frame.printAfter());
out.println(window.printAfter());
%>
</body>
</html>