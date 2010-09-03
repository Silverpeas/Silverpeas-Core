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

<%!

	private String formatDate(ResourcesWrapper resources, String date) 
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
//Recuperation des parametres
ArrayLine arrayLine = null;
Iterator   iter1 = null;
    
    Vector vStatsData = (Vector)request.getAttribute("StatsData");
    String userProfile = (String)request.getAttribute("UserProfile");
%>

<%
	TabbedPane tabbedPane = gef.getTabbedPane();
	if (userProfile.equals("A"))
    {
		tabbedPane.addTab(resources.getString("silverStatisticsPeas.JobPeas"), m_context+"/RsilverStatisticsPeas/jsp/ViewVolumeServices",false);
	}
	tabbedPane.addTab(resources.getString("GML.publications"), m_context+"/RsilverStatisticsPeas/jsp/ViewVolumePublication",false);
	tabbedPane.addTab(resources.getString("GML.attachments"), m_context+"/RsilverStatisticsPeas/jsp/ViewVolumeServer",true);
%>

<html>
<HEAD>
<TITLE><%=resources.getString("GML.popupTitle")%></TITLE>
<!--[ JAVASCRIPT ]-->
<SCRIPT LANGUAGE="JAVASCRIPT">

	function changeDisplay() {
		document.volumeServerFormulaire.action = document.volumeServerFormulaire.Display.value;
		document.volumeServerFormulaire.submit();		
	}

</SCRIPT>
<%
   out.println(gef.getLookStyleSheet());
%>
</HEAD>
<BODY marginheight=5 marginwidth=5 leftmargin=5 topmargin=5 onLoad="">
 <FORM name="volumeServerFormulaire" action="ViewEvolutionVolumeSizeServer" method="post">
<%

	browseBar.setDomainName(resources.getString("silverStatisticsPeas.statistics") + " > "+resources.getString("silverStatisticsPeas.Volumes"));
    browseBar.setComponentName(resources.getString("GML.attachments"));
    browseBar.setPath(resources.getString("silverStatisticsPeas.AttachmentsTotalSize"));
    
	out.println(window.printBefore());
    out.println(tabbedPane.print());
    out.println(frame.printBefore());
%>

<BR>

	<div align="right">
		<%=resources.getString("silverStatisticsPeas.Display")%>&nbsp;:
		<select name="Display" size="1" onChange="changeDisplay()">
			<option value="ViewVolumeServer"><%=resources.getString("silverStatisticsPeas.AttachmentsNumber")%></option>
			<option value="ViewVolumeSizeServer"><%=resources.getString("silverStatisticsPeas.AttachmentsSize")%></option>
			<%
			if (userProfile.equals("A"))
    		{
    		%>
				<option value="ViewEvolutionVolumeSizeServer" selected><%=resources.getString("silverStatisticsPeas.AttachmentsTotalSize")%></option>
			<%
			}
			%>
		</select>
	</div>
<%
	//Graphiques
   	if (vStatsData != null)
   	{
%>
<div align="center">
	<img src="<%=m_context%>/ChartServlet/?chart=EVOLUTION_DOCSIZE_CHART&random=<%=(new Date()).getTime()%>">
</div>
<%
	}
%>
<br>
<%
		  // Tableau

          	ArrayPane arrayPane = gef.getArrayPane("List", "", request,session);
			arrayPane.setVisibleLineNumber(50);
          	if (arrayPane.getColumnToSort()==0)
	      		arrayPane.setColumnToSort(1);
	      	
	      	ArrayColumn arrayColumn1 = arrayPane.addArrayColumn(resources.getString("GML.date"));
            ArrayColumn arrayColumn2 = arrayPane.addArrayColumn(resources.getString("GML.name"));
            ArrayColumn arrayColumn3 = arrayPane.addArrayColumn(resources.getString("GML.size"));
	      	
	      	if (vStatsData != null)
	        {
	        	iter1 = vStatsData.iterator();
	        	ArrayCellText cellText;
	        	
	        	while (iter1.hasNext())
	        	{
					arrayLine = arrayPane.addArrayLine();
					
	            	String[] item = (String[]) iter1.next();
	            	
	            	//transformation de la date 2007-02-01 -> F�v. 2007            	
					cellText = arrayLine.addArrayCellText(formatDate(resources, item[0]));
					cellText.setCompareOn(item[0]);

	          		arrayLine.addArrayCellText(item[1]);
	          		
	          		cellText = arrayLine.addArrayCellText(FileRepositoryManager.formatFileSize(new Long(item[2]).longValue() * 1024));
	          		cellText.setCompareOn(new Integer(item[2]));
	        	}
			
				out.println(arrayPane.print());
		        out.println("");
	    }
  %>
  
 </FORM>
<%
out.println(frame.printAfter());
out.println(window.printAfter());
%>
</BODY>
</HTML>