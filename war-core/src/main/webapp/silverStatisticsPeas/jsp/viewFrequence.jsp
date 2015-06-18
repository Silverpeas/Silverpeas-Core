<%@ page import="org.silverpeas.admin.user.constant.UserAccessLevel" %>
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
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<%
//Recuperation des parametres
	ArrayLine  arrayLine = null;
	Iterator   iter = null;
	Collection cMonthBegin = (Collection)request.getAttribute("MonthBegin");
	Collection cYearBegin = (Collection)request.getAttribute("YearBegin");
	Collection cMonthEnd = (Collection)request.getAttribute("MonthEnd");	
	Collection cYearEnd = (Collection)request.getAttribute("YearEnd");
	Collection cFrequenceDetail = (Collection)request.getAttribute("FrequenceDetail");
  UserAccessLevel userProfile = (UserAccessLevel)request.getAttribute("UserProfile");
  ChartVO chart = (ChartVO) request.getAttribute("Chart");
%>

<%
	browseBar.setDomainName(resources.getString("silverStatisticsPeas.statistics"));
    browseBar.setComponentName(resources.getString("silverStatisticsPeas.Connections"));
    browseBar.setPath(resources.getString("silverStatisticsPeas.connectionFrequence"));
    
	TabbedPane tabbedPane = gef.getTabbedPane();
	tabbedPane.addTab(resources.getString("silverStatisticsPeas.usersWithSession"), m_context+"/RsilverStatisticsPeas/jsp/Main",false);
	tabbedPane.addTab(resources.getString("silverStatisticsPeas.connectionNumber"), m_context+"/RsilverStatisticsPeas/jsp/ViewConnections",false);
	tabbedPane.addTab(resources.getString("silverStatisticsPeas.connectionFrequence"), m_context+"/RsilverStatisticsPeas/jsp/ViewFrequence",true);
%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html>
<head>
<title><%=resources.getString("GML.popupTitle")%></title>
<view:looknfeel />
<view:includePlugin name="chart"/>
  <style type="text/css">
    .chart {
      width: 800px;
      height: 400px;
    }
  </style>
<script type="text/javascript">
  $(function() {

    var data = [
      <% if (chart != null) {
        List<String> x = chart.getX();
        List<Long> y = chart.getY();
        for (int i = 0; i<x.size(); i++) {
          out.print("["+DateUtil.parse(x.get(i).replace('-', '/')).getTime()+", "+y.get(i)+"]");
          if (i < x.size()) {
            out.println(",");
          }
        }
      } %>
    ];

    $.plot(".chart", [data], {
       bars: {
          show: true,
          barWidth: 24 * 60 * 60 * 1000 * 30
        }
      ,
      xaxis: {
        mode: "time",
        minTickSize: [1, "month"]
      }
    });

  });
</script>
</head>
<body class="admin stats">
<%
          out.println(window.printBefore());          
          if (UserAccessLevel.ADMINISTRATOR.equals(userProfile)) {
			out.println(tabbedPane.print());
    	  }
          out.println(frame.printBefore());
          out.println(board.printBefore());
%>
  <table width="100%" border="0" cellspacing="0" cellpadding="4">
    <form name="frequenceFormulaire" action="ValidateViewFrequence" method="post">
      <tr> 
        <td width="300" nowrap class=txtlibform><%=resources.getString("silverStatisticsPeas.since")%>&nbsp;:</td>
        <td nowrap> 
          <select name="MonthBegin" size="1">
		    <%
        	iter = cMonthBegin.iterator();
        	while (iter.hasNext())
        	{
            	String[] item = (String[]) iter.next();
          		out.print("<option value="+ item[0] +">"+resources.getString(item[1])+"</option>");
          	}
          	%>
          </select>
	      &nbsp;&nbsp;
          <select name="YearBegin" size="1">
		    <%
        	iter = cYearBegin.iterator();
        	while (iter.hasNext())
        	{
            	String[] item = (String[]) iter.next();
          		out.print("<option value="+ item[0] +">"+item[1]+"</option>");
          	}
          	%>
          </select>
        </td>
      </tr>
      <tr> 
        <td nowrap class=txtlibform><%=resources.getString("silverStatisticsPeas.to")%>&nbsp;:</td>
        <td nowrap> 
          <select name="MonthEnd" size="1">
		    <%
        	iter = cMonthEnd.iterator();
        	while (iter.hasNext())
        	{
            	String[] item = (String[]) iter.next();
          		out.print("<option value="+ item[0] +">"+resources.getString(item[1])+"</option>");
          	}
          	%>
          </select>
        	&nbsp;&nbsp;
          <select name="YearEnd" size="1">
		    <%
        	iter = cYearEnd.iterator();
        	while (iter.hasNext())
        	{
            	String[] item = (String[]) iter.next();
          		out.print("<option value="+ item[0] +">"+item[1]+"</option>");
          	}
          	%>
          </select>
        </td>
      </tr>
      <tr> 
        <td nowrap class=txtlibform><%=resources.getString("silverStatisticsPeas.Frequence")%>&nbsp;:</td>
        <td nowrap> 
          <select name="FrequenceDetail" size="1">
		    <%
        	iter = cFrequenceDetail.iterator();
        	while (iter.hasNext())
        	{
            	String[] item = (String[]) iter.next();
          		out.print("<option value="+ item[0] +">"+item[1]+"</option>");
          	}
          	%>
          </select> <%=resources.getString("silverStatisticsPeas.Connections")%>
        </td> 
      </tr>
	</form>    
  </table>
  
  <%
  	out.println(board.printAfter());
  
    ButtonPane buttonPane = gef.getButtonPane();
    buttonPane.addButton(gef.getFormButton(resources.getString("GML.validate"), "javascript:$.progressMessage();document.frequenceFormulaire.submit()", false));
	  buttonPane.addButton(gef.getFormButton(resources.getString("GML.cancel"), "javascript:document.cancelFrequenceForm.submit()", false));
    out.println("<br><center>"+buttonPane.print()+"</center><br>");
  %>

  <% if (chart != null) { %>
  <div class="flex-container">
    <div class="chart-area">
      <h3 class="txttitrecol"><%=chart.getTitle()%></h3>
      <div class="chart"></div>
    </div>
  </div>
  <% } %>

<%
out.println(frame.printAfter());
out.println(window.printAfter());
%>
	
<form name="cancelFrequenceForm" action="ViewFrequence" method="post">
</form>
<view:progressMessage/>
</body>
</html>