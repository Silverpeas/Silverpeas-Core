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
