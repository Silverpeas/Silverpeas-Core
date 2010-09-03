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
    
    String entite = (String) request.getAttribute("Entite");
	String entiteId = (String) request.getAttribute("Id");
    Collection cMonthBegin = (Collection)request.getAttribute("MonthBegin");
	String monthBegin = "";
	Collection cYearBegin = (Collection)request.getAttribute("YearBegin");
	String yearBegin = "";
	String filterLibGroup = (String)request.getAttribute("FilterLibGroup");
	String filterIdGroup = (String) request.getAttribute("FilterIdGroup");
	String filterLibUser = (String)request.getAttribute("FilterLibUser");
	String filterIdUser = (String) request.getAttribute("FilterIdUser");
    String spaceId = (String) request.getAttribute("SpaceId");
	Vector vPath = (Vector) request.getAttribute("Path");
	Vector vStatsData = (Vector)request.getAttribute("StatsData");
	String userProfile = (String)request.getAttribute("UserProfile");
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

	Iterator iter1 = cMonthBegin.iterator();
	while (iter1.hasNext())
	{
    	String[] item = (String[]) iter1.next();
    	String theValue = item[0];
  		int indexOfSelected = theValue.indexOf("selected");
  		if(indexOfSelected != -1)
  			monthBegin = theValue.substring(0, indexOfSelected - 1);
  	}
  	
  	iter1 = cYearBegin.iterator();
	while (iter1.hasNext())
	{
    	String[] item = (String[]) iter1.next();
    	String theValue = item[0];
  		int indexOfSelected = theValue.indexOf("selected");
  		if(indexOfSelected != -1)
  			yearBegin = theValue.substring(0, indexOfSelected - 1);
  	}
     
          	
    browseBar.setDomainName(resources.getString("silverStatisticsPeas.statistics"));
    browseBar.setComponentName(resources.getString("silverStatisticsPeas.Access"), "ValidateViewAccess?MonthBegin="+monthBegin+"&YearBegin="+yearBegin+"&FilterLibGroup="+filterLibGroup+"&FilterIdGroup="+filterIdGroup+"&FilterLibUser="+filterLibUser+"&FilterIdUser="+filterIdUser+"&SpaceId=");
    
    String path = "";
	if (spaceId != null && ! "".equals(spaceId))
	{		
		String separator = "";
		Iterator i = vPath.iterator();
		while ( i.hasNext() )
		{
			String[] pathItem = (String[]) i.next();
			if (userProfile.equals("A")) {
				path += separator + "<a href=\"ValidateViewAccess?MonthBegin="+monthBegin+"&YearBegin="+yearBegin+"&FilterLibGroup="+filterLibGroup+"&FilterIdGroup="+filterIdGroup+"&FilterLibUser="+filterLibUser+"&FilterIdUser="+filterIdUser+( (pathItem[0]==null) ? "" : ("&SpaceId="+pathItem[0]) )+"\">"+pathItem[1]+ "</a>";
			} else {
				path += separator + pathItem[1];
			}
			separator = " > ";
		}
		path += separator; 
	}
	
	path += resources.getString("silverStatisticsPeas.Historique");
	browseBar.setPath(path);
	
	out.println(window.printBefore());
    out.println(frame.printBefore());
%>

<BR>
	
<%
	if (vStatsData != null && vStatsData.size() > 0) {
%>
<!-- Graphique -->
<div align="center">
	<img src="<%=m_context%>/ChartServlet/?chart=EVOLUTION_USER_CHART&random=<%=(new Date()).getTime()%>">
</div>

<%
	}
%>

<br>

<%

		// Tableau
      	ArrayPane arrayPane = gef.getArrayPane("List", "ViewEvolutionAccess?Entite="+entite+"&Id="+entiteId, request,session);
		arrayPane.setVisibleLineNumber(50);
      	if (arrayPane.getColumnToSort()==0)
      		arrayPane.setColumnToSort(1);
      	
      	ArrayColumn arrayColumn1 = arrayPane.addArrayColumn(resources.getString("GML.date"));
        ArrayColumn arrayColumn3 = arrayPane.addArrayColumn(resources.getString("silverStatisticsPeas.Access"));
      	
      	if (vStatsData != null)
        {
        	iter1 = vStatsData.iterator();
        	ArrayLine arrayLine;
        	String[] item;
        	ArrayCellText cellText;
        	
        	while (iter1.hasNext())
        	{
				arrayLine = arrayPane.addArrayLine();
				
            	item = (String[]) iter1.next();

          		//transformation de la date 2007-02-01 -> F�v. 2007            	
				cellText = arrayLine.addArrayCellText(formatDate(resources, item[0]));
          		cellText.setCompareOn(item[0]);

				cellText = arrayLine.addArrayCellText(item[1]);
          		cellText.setCompareOn(new Integer(item[1]));
        	}
		
			out.println(arrayPane.print());
	        out.println("");
    }
	    
	out.println(frame.printAfter());
	out.println(window.printAfter());
%>
</BODY>
</HTML>