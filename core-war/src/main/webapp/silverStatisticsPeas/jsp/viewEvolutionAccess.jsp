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
<%@ page import="org.silverpeas.core.date.DateTime" %>
<%@ page import="org.silverpeas.core.admin.user.constant.UserAccessLevel" %>
<%@ page import="org.silverpeas.core.chart.period.PeriodChart" %>
<%@ page import="static java.util.Calendar.getInstance" %>
<%@ page import="org.silverpeas.core.chart.period.PeriodChartItem" %>

<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<%@ taglib tagdir="/WEB-INF/tags/silverpeas/util" prefix="viewTags" %>
<%@ include file="checkSilverStatistics.jsp" %>

<%!

	private String formatDate(MultiSilverpeasBundle resources, DateTime date)
	{//date au format AAAA-MM-jj
    Calendar calendar = getInstance();
    calendar.setTime(date);

		String dateFormate = "";

		int annee = calendar.get(Calendar.YEAR);
		int mois = calendar.get(Calendar.MONTH) + 1;

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
	PeriodChart chart = (PeriodChart) request.getAttribute("Chart");
	UserAccessLevel userProfile = (UserAccessLevel)request.getAttribute("UserProfile");
%>

<c:set var="periodChart" value="${requestScope.Chart}"/>

<html>
<HEAD>
<TITLE><%=resources.getString("GML.popupTitle")%></TITLE>
<view:looknfeel/>
</HEAD>
<BODY class="admin stats">
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
			if (UserAccessLevel.ADMINISTRATOR.equals(userProfile)) {
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

<div class="flex-container">
  <viewTags:displayChart chart="${periodChart}"/>
</div>

<br>

<%

		// Tableau
	ArrayPane arrayPane = gef.getArrayPane("List", "ViewEvolutionAccess?Entite="+entite+"&Id="+entiteId, request,session);
		    arrayPane.setVisibleLineNumber(50);
	if (arrayPane.getColumnToSort()==0)
		arrayPane.setColumnToSort(1);

	ArrayColumn arrayColumn1 = arrayPane.addArrayColumn(resources.getString("GML.date"));
        ArrayColumn arrayColumn3 = arrayPane.addArrayColumn(resources.getString("silverStatisticsPeas.Access"));

	if (chart != null) {
		for (PeriodChartItem item : chart.getItems()) {
            ArrayLine arrayLine = arrayPane.addArrayLine();
            ArrayCellText cellText = arrayLine.addArrayCellText(formatDate(resources,
                item.getX().getBeginDatable()));
            cellText.setCompareOn(item.getX().getBeginDatable().getTime());

            cellText = arrayLine.addArrayCellText(String.valueOf(item.getYValues().iterator().next()));
            cellText.setCompareOn(Long.valueOf(item.getYValues().iterator().next().toString()));
          }
			    out.println(arrayPane.print());
        }

	out.println(frame.printAfter());
	out.println(window.printAfter());
%>
</BODY>
</HTML>