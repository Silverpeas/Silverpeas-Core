<%@ include file="checkSilverStatistics.jsp" %>

<%
//Recuperation des parametres
	ArrayLine  arrayLine = null;
	Iterator   iter = null;
	Collection cMonthBegin = (Collection)request.getAttribute("MonthBegin");
	Collection cYearBegin = (Collection)request.getAttribute("YearBegin");
	Collection cMonthEnd = (Collection)request.getAttribute("MonthEnd");	
	Collection cYearEnd = (Collection)request.getAttribute("YearEnd");
	Collection cFrequenceDetail = (Collection)request.getAttribute("FrequenceDetail");		
    String userProfile = (String)request.getAttribute("UserProfile");

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

<html>
<HEAD>
<TITLE><%=resources.getString("GML.popupTitle")%></TITLE>
<%
   out.println(gef.getLookStyleSheet());
%>
</HEAD>
<BODY marginheight=5 marginwidth=5 leftmargin=5 topmargin=5 onLoad="">
<%
          out.println(window.printBefore());          
          if (userProfile.equals("A")) {
			out.println(tabbedPane.print());
    	  }
          out.println(frame.printBefore());
          out.println(board.printBefore());
%>
<CENTER>
  <table width="100%" border="0" cellspacing="0" cellpadding="4">
    <FORM name="frequenceFormulaire" action="ValidateViewFrequence" method="post">
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
	</FORM>    
  </table>
  
  <%
  	out.println(board.printAfter());
  
    ButtonPane buttonPane = gef.getButtonPane();
    buttonPane.addButton((Button) gef.getFormButton(resources.getString("GML.validate"), "javascript:document.frequenceFormulaire.submit()", false));
	buttonPane.addButton((Button) gef.getFormButton(resources.getString("GML.cancel"), "javascript:document.cancelFrequenceForm.submit()", false));
    out.println("<br><center>"+buttonPane.print()+"</center><br>");
        
        if(request.getAttribute("Graphic") != null && Boolean.TRUE.equals(request.getAttribute("Graphic"))) {
      %>
      
      <div align=center>
		<img src="<%=m_context%>/ChartServlet/?chart=USER_FQ_CHART&random=<%=(new Date()).getTime()%>">
	  </div>
	  
      <%
		}
  %> 
</CENTER>
<%
out.println(frame.printAfter());
out.println(window.printAfter());
%>
	
<form name="cancelFrequenceForm" action="ViewFrequence" method="post">
</form>
</BODY>
</HTML>