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