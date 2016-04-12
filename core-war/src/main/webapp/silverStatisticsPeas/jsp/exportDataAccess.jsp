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

<%
response.setContentType("text/html");
//Recuperation des parametres

	Iterator   iter1 = null;
	String filterIdGroup = (String) request.getAttribute("FilterIdGroup");
		String filterIdUser = (String) request.getAttribute("FilterIdUser");
        Vector vStatsData = (Vector) request.getAttribute("StatsData");
		String separatorCSV = ",";

        String Organisation = resources.getString("silverStatisticsPeas.organisation");
        String Tous = resources.getString("GML.allMP");
        String Groupe = resources.getString("silverStatisticsPeas.group");
        String Utilisateur = resources.getString("GML.user");


        out.println(Organisation+separatorCSV+Tous+separatorCSV+Groupe+separatorCSV+Utilisateur);
        out.println("<BR>");
        if (vStatsData != null)
        {
		iter1 = vStatsData.iterator();

		while (iter1.hasNext())
		{
			String[] item = (String[]) iter1.next();

			out.println("<BR>");
			out.print(item[2]+separatorCSV);
			out.print(item[3]+separatorCSV);
			if(filterIdGroup != null && ! "".equals(filterIdGroup)) {
				out.print(item[4]+separatorCSV);
			} else {
				out.print(" "+separatorCSV);
			}
			if(filterIdUser != null && ! "".equals(filterIdUser)) {
				out.print(item[5]);
			} else {
				out.print(" ");
			}
		}
		}
        out.println("<script language=\"Javascript\">alert(\""+resources.getString("silverStatisticsPeas.conseilTXT")+"\");</script>");
%>
