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

<%@ include file="checkSilverStatistics.jsp" %>

<%
//Recuperation des parametres
	ArrayLine  arrayLine = null;
	Iterator   iter = null;
	Collection cMonthBegin = (Collection)request.getAttribute("MonthBegin");
	String monthBegin = "";
	Collection cYearBegin = (Collection)request.getAttribute("YearBegin");
	String yearBegin = "";
	Collection cMonthEnd = (Collection)request.getAttribute("MonthEnd");	
	String monthEnd = "";
	Collection cYearEnd = (Collection)request.getAttribute("YearEnd");
	String yearEnd = "";
	Collection cActorDetail = (Collection)request.getAttribute("ActorDetail");	
	String actorDetail = "";
	String filterType = (String)request.getAttribute("FilterType");
	String filterLib = (String)request.getAttribute("FilterLib");
	String filterId = (String)request.getAttribute("FilterId");
    Collection cResultData = (Collection)request.getAttribute("ResultData");
    String userProfile = (String)request.getAttribute("UserProfile");
    
    String[] item = null;
    String theValue = null;
    int indexOfSelected;

%>

<%
	browseBar.setDomainName(resources.getString("silverStatisticsPeas.statistics"));
    browseBar.setComponentName(resources.getString("silverStatisticsPeas.Connections"));
    browseBar.setPath(resources.getString("silverStatisticsPeas.LoginNumber"));

	TabbedPane tabbedPane = gef.getTabbedPane();
	tabbedPane.addTab(resources.getString("silverStatisticsPeas.usersWithSession"), m_context+"/RsilverStatisticsPeas/jsp/Main",false);
	tabbedPane.addTab(resources.getString("silverStatisticsPeas.connectionNumber"), m_context+"/RsilverStatisticsPeas/jsp/ViewConnections",true);
	tabbedPane.addTab(resources.getString("silverStatisticsPeas.connectionFrequence"), m_context+"/RsilverStatisticsPeas/jsp/ViewFrequence",false);
%>

<html>
<HEAD>
<TITLE><%=resources.getString("GML.popupTitle")%></TITLE>
<%
   out.println(gef.getLookStyleSheet());
%>
<!--[ JAVASCRIPT ]-->
<script type="text/javascript" src="<%=m_context%>/util/javaScript/animation.js"></script>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/checkForm.js"></script>

<SCRIPT LANGUAGE="JAVASCRIPT">

	// This function open a silverpeas window
	function openSPWindow(fonction,windowName){
		fonction = fonction + "?MonthBegin=" + connexionFormulaire.MonthBegin.value;
		fonction = fonction + "&YearBegin=" + connexionFormulaire.YearBegin.value;				
		fonction = fonction + "&MonthEnd=" + connexionFormulaire.MonthEnd.value;		
		fonction = fonction + "&YearEnd=" + connexionFormulaire.YearEnd.value;						
		fonction = fonction + "&ActorDetail=" + connexionFormulaire.ActorDetail.value;		
		fonction = fonction + "&FilterLib=" + connexionFormulaire.FilterLib.value;		
		fonction = fonction + "&FilterType=" + connexionFormulaire.FilterType.value;		
		fonction = fonction + "&FilterId=" + connexionFormulaire.FilterId.value;		
		SP_openWindow(fonction, windowName, '750', '550','scrollbars=yes, resizable, alwaysRaised');
	}
	
	function changeDetail() {
		if(connexionFormulaire.ActorDetail.value == "0") {
			clearFilter();
		}
	}
	
	function clearFilter(){
		connexionFormulaire.FilterLib.value = "";
		connexionFormulaire.FilterType.value = "";
		connexionFormulaire.FilterId.value = "";
	}
	
	function validerForm(){
		connexionFormulaire.FilterLib.disabled = false;
		document.connexionFormulaire.submit();
	}
	

</SCRIPT>

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
    <FORM name="connexionFormulaire" action="ValidateViewConnection" method="post">
      <tr> 
        <td width="300" nowrap class=txtlibform><%=resources.getString("silverStatisticsPeas.since")%>&nbsp;:</td>
        <td nowrap> 
          <select name="MonthBegin" size="1">
		    <%
        	iter = cMonthBegin.iterator();
        	while (iter.hasNext())
        	{
            	item = (String[]) iter.next();
            	theValue = item[0];
          		out.print("<option value="+ theValue +">"+resources.getString(item[1])+"</option>");
          		indexOfSelected = theValue.indexOf("selected");
          		if(indexOfSelected != -1)
          			monthBegin = theValue.substring(0, indexOfSelected - 1);
          	}
          	%>
          </select>
	      &nbsp;&nbsp;
          <select name="YearBegin" size="1">
		    <%
        	iter = cYearBegin.iterator();
        	while (iter.hasNext())
        	{
            	item = (String[]) iter.next();
            	theValue = item[0];
          		out.print("<option value="+ theValue +">"+item[1]+"</option>");
          		indexOfSelected = theValue.indexOf("selected");
          		if(indexOfSelected != -1)
          			yearBegin = theValue.substring(0, indexOfSelected - 1);
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
            	item = (String[]) iter.next();
            	theValue = item[0];
          		out.print("<option value="+ theValue +">"+resources.getString(item[1])+"</option>");
          		indexOfSelected = theValue.indexOf("selected");
          		if(indexOfSelected != -1)
          			monthEnd = theValue.substring(0, indexOfSelected - 1);
          	}
          	%>
          </select>
        	&nbsp;&nbsp;
          <select name="YearEnd" size="1">
		    <%
        	iter = cYearEnd.iterator();
        	while (iter.hasNext())
        	{
            	item = (String[]) iter.next();
            	theValue = item[0];
          		out.print("<option value="+ theValue +">"+item[1]+"</option>");
          		indexOfSelected = theValue.indexOf("selected");
          		if(indexOfSelected != -1)
          			yearEnd = theValue.substring(0, indexOfSelected - 1);
          	}
          	%>
          </select>
        </td>
      </tr>
      <tr> 
        <td nowrap class=txtlibform><%=resources.getString("GML.detail")%>&nbsp;:</td>
        <td nowrap> 
          <select name="ActorDetail" size="1" onChange="changeDetail()">
		    <%
        	iter = cActorDetail.iterator();
        	while (iter.hasNext())
        	{
            	item = (String[]) iter.next();
            	theValue = item[0];
          		out.print("<option value="+ theValue +">"+resources.getString(item[1])+"</option>");
          		indexOfSelected = theValue.indexOf("selected");
          		if(indexOfSelected != -1)
          			actorDetail = theValue.substring(0, indexOfSelected - 1);
          	}
          	%>
          </select>
        </td>
      </tr>
      <tr> 
        <td nowrap class=txtlibform><%=resources.getString("silverStatisticsPeas.filter")+ " ("+resources.getString("silverStatisticsPeas.group")+ " - " + resources.getString("GML.user") + ")"%>&nbsp;:</td>
        <td nowrap> 
          <input type="text" name="FilterLib" value="<%=( filterLib == null ? "" : filterLib )%>" size="25" disabled>
		  <input type="hidden" name="FilterType" value="<%=filterType%>">
		  <input type="hidden" name="FilterId" value="<%=filterId%>">
          <a href=javascript:openSPWindow('CallUserPanel','')><img src="<%=resources.getIcon("silverStatisticsPeas.icoAccessGroupPanelPeas")%>" align="absmiddle" alt="<%=resources.getString("silverStatisticsPeas.openUserPanelPeas")%>" border=0 title="<%=resources.getString("silverStatisticsPeas.openUserPanelPeas")%>"></a> 
          <a href=javascript:clearFilter()><img src="<%=resources.getIcon("silverStatisticsPeas.icoClearGroupUser")%>" align="absmiddle" alt="<%=resources.getString("silverStatisticsPeas.ClearUserPanelPeas")%>" border=0 title="<%=resources.getString("silverStatisticsPeas.ClearUserPanelPeas")%>"></a> 
        </td>
      </tr>
	</FORM>    
  </table>
  
  <%
  	out.println(board.printAfter());
  
    ButtonPane buttonPane = gef.getButtonPane();
    buttonPane.addButton((Button) gef.getFormButton(resources.getString("GML.validate"), "javascript:validerForm()", false));
	buttonPane.addButton((Button) gef.getFormButton(resources.getString("GML.cancel"), "javascript:document.cancelConnectionForm.submit()", false));
    out.println("<br><center>"+buttonPane.print()+"</center><br>");
  
  	  // Tableau

          ArrayPane arrayPane = gef.getArrayPane("List", "", request,session);

          arrayPane.addArrayColumn(resources.getString("GML.name"));
          arrayPane.addArrayColumn(resources.getString("silverStatisticsPeas.connectionNumber"));
          arrayPane.addArrayColumn(resources.getString("silverStatisticsPeas.durationAvg"));
          
          if(("1".equals(actorDetail) || "2".equals(actorDetail)) &&
			(filterId == null || "".equals(filterId)) ) {//ajoute une colonne dans le tableau
			
				ArrayColumn arrayColumn = arrayPane.addArrayColumn(resources.getString("silverStatisticsPeas.Actions"));
		  		arrayColumn.setSortable(false);
		  }
          
          ArrayCellText cellTextCount;
		  ArrayCellText cellTextDuration;
        
        if (cResultData != null)
        {
      %>
      
      <div align=center>
      
      <%
        	if(request.getAttribute("GraphicDistinctUser") != null && Boolean.TRUE.equals(request.getAttribute("GraphicDistinctUser"))) 
        	{
      %>
		<img src="<%=m_context%>/ChartServlet/?chart=LOGIN_CHART&random=<%=(new Date()).getTime()%>">
	  <%
			}
	  %>
		<img src="<%=m_context%>/ChartServlet/?chart=USER_CHART&random=<%=(new Date()).getTime()%>">
	  </div>
      <%
        	iter = cResultData.iterator();
        	if(iter.hasNext()) {
        		String title;
	        	while (iter.hasNext())
	        	{
	            	item = (String[]) iter.next();
	
	          		arrayLine = arrayPane.addArrayLine();
	
	          		arrayLine.addArrayCellText(item[0]);
									
	              	cellTextCount = arrayLine.addArrayCellText(item[1]);
	              	cellTextCount.setCompareOn(new Integer(item[1]));
	
					long duration	= Long.valueOf(item[2]).longValue();
					
					String formattedDuration = DateUtil.formatDuration(duration);
					cellTextDuration = arrayLine.addArrayCellText(formattedDuration);
					cellTextDuration.setCompareOn(new Long(duration));
					
					if(("1".equals(actorDetail) || "2".equals(actorDetail)) &&
						(filterId == null || "".equals(filterId)) ) 
					{
						title = resources.getString("silverStatisticsPeas.GraphConnections")+" ";
						if("1".equals(actorDetail)) {
							title += resources.getString("GML.groupe")+" ";
						}
						else if("2".equals(actorDetail)) {
							title += resources.getString("GML.user")+" ";
						}
						title += item[0];
						
						arrayLine.addArrayCellText("<div align=left><a href=\"ValidateViewConnection?EntiteId="+item[3]+"&MonthBegin="+monthBegin+"&YearBegin="+yearBegin+"&MonthEnd="+monthEnd+"&YearEnd="+yearEnd+"&ActorDetail="+actorDetail+"&FilterType="+filterType+"&FilterLib="+filterLib+"&FilterId="+filterId+"\"><img src=\""+resources.getIcon("silverStatisticsPeas.icoHistoAccess")+"\" align=absmiddle alt=\""+title+"\" border=0 title=\""+title+"\"></a></div>");
					}
	        	}
	        } else {
				arrayLine = arrayPane.addArrayLine();
				
				arrayLine.addArrayCellText(filterLib == null ? "*" : filterLib);
				arrayLine.addArrayCellText("0");
				arrayLine.addArrayCellText("0");
	        }
        	out.println(arrayPane.print());
		}
  %> 
</CENTER>
<%
out.println(frame.printAfter());
out.println(window.printAfter());
%>
	
<form name="cancelConnectionForm" action="ViewConnections" method="post">
</form>
</BODY>
</HTML>