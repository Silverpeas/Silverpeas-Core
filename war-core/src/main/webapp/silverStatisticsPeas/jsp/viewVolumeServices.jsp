<%--

    Copyright (C) 2000 - 2009 Silverpeas

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    As a special exception to the terms and conditions of version 3.0 of
    the GPL, you may redistribute this Program in connection with Free/Libre
    Open Source Software ("FLOSS") applications as described in Silverpeas's
    FLOSS exception.  You should have received a copy of the text describing
    the FLOSS exception, and it is also available here:
    "http://repository.silverpeas.com/legal/licensing"

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

--%>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%@ include file="checkSilverStatistics.jsp" %>

<%
//Recuperation des parametres
ArrayLine arrayLine = null;
Iterator   iter1 = null;

	 Vector vStatsData = (Vector)request.getAttribute("StatsData");
%>

<%
	TabbedPane tabbedPane = gef.getTabbedPane();
	
	tabbedPane.addTab(resources.getString("silverStatisticsPeas.JobPeas"), m_context+"/RsilverStatisticsPeas/jsp/ViewVolumeServices",true);
	tabbedPane.addTab(resources.getString("GML.publications"), m_context+"/RsilverStatisticsPeas/jsp/ViewVolumePublication",false);
	tabbedPane.addTab(resources.getString("GML.attachments"), m_context+"/RsilverStatisticsPeas/jsp/ViewVolumeServer",false);
%>

<html>
<HEAD>
<TITLE><%=resources.getString("GML.popupTitle")%></TITLE>
<%
   out.println(gef.getLookStyleSheet());
%>
</HEAD>
<BODY marginheight=5 marginwidth=5 leftmargin=5 topmargin=5 onLoad="">
<%

	browseBar.setDomainName(resources.getString("silverStatisticsPeas.statistics"));
    browseBar.setComponentName(resources.getString("silverStatisticsPeas.Volumes"));
    browseBar.setPath(resources.getString("silverStatisticsPeas.JobPeas"));
    
    out.println(window.printBefore());
    out.println(tabbedPane.print());
    out.println(frame.printBefore());
%>

<div align=center>
<img src="<%=m_context%>/ChartServlet/?chart=KM_INSTANCES_CHART&random=<%=(new Date()).getTime()%>">
</div>

<%
		  // Tableau

          ArrayPane arrayPane = gef.getArrayPane("List", "", request,session);
          arrayPane.setVisibleLineNumber(50);

          arrayPane.addArrayColumn(resources.getString("GML.jobPeas"));
          arrayPane.addArrayColumn(resources.getString("silverStatisticsPeas.InstancesNumber"));

        if (vStatsData != null)
        {
        	iter1 = vStatsData.iterator();
        	
        	while (iter1.hasNext())
        	{        		
				arrayLine = arrayPane.addArrayLine();
				
				String[] item = (String[]) iter1.next();
				
       			arrayLine.addArrayCellText(item[0]);
              	ArrayCellText cellTextCount = arrayLine.addArrayCellText(item[1]);
              	cellTextCount.setCompareOn(new Integer(item[1]));
        	}
        	out.println(arrayPane.print());
		}
  %>
<%
out.println(frame.printAfter());
out.println(window.printAfter());
%>
</BODY>
</HTML>