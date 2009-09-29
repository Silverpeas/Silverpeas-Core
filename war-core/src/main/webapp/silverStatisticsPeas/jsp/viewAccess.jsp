<%--

    Copyright (C) 2000 - 2009 Silverpeas

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    As a special exception to the terms and conditions of version 3.0 of
    the GPL, you may redistribute this Program in connection with Free/Libre
    Open Source Software ("FLOSS") applications as described in Silverpeas's
    FLOSS exception.  You should have recieved a copy of the text describing
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
ArrayLine arrayLine = null;
Iterator   iter1 = null;

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
    String userProfile = (String) request.getAttribute("UserProfile");
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
		fonction = fonction + "?MonthBegin=" + accessFormulaire.MonthBegin.value;
		fonction = fonction + "&YearBegin=" + accessFormulaire.YearBegin.value;		
		fonction = fonction + "&FilterLibGroup=" + accessFormulaire.FilterLibGroup.value;		
		fonction = fonction + "&FilterIdGroup=" + accessFormulaire.FilterIdGroup.value;		
		fonction = fonction + "&FilterLibUser=" + accessFormulaire.FilterLibUser.value;		
		fonction = fonction + "&FilterIdUser=" + accessFormulaire.FilterIdUser.value;
		fonction = fonction + "&SpaceId=";		
		SP_openWindow(fonction, windowName, '750', '550','scrollbars=yes, menubar=yes, resizable, alwaysRaised');
	}
	
	function clearFilterGroup(){
		accessFormulaire.FilterLibGroup.value = "";
		accessFormulaire.FilterIdGroup.value = "";
	}
	
	function clearFilterUser(){
		accessFormulaire.FilterLibUser.value = "";
		accessFormulaire.FilterIdUser.value = "";
	}
	
	
	function validerForm(){
		accessFormulaire.FilterLibGroup.disabled = false;
		accessFormulaire.FilterLibUser.disabled = false;		
		document.accessFormulaire.submit()
	}

</SCRIPT>
</HEAD>
<BODY marginheight=5 marginwidth=5 leftmargin=5 topmargin=5 onLoad="">
<%  

	String optionsMonthBegin = "";
	iter1 = cMonthBegin.iterator();
	while (iter1.hasNext())
	{
    	String[] item = (String[]) iter1.next();
    	String theValue = item[0];
		optionsMonthBegin += "<option value="+ theValue +">"+resources.getString(item[1])+"</option>";
  		int indexOfSelected = theValue.indexOf("selected");
  		if(indexOfSelected != -1)
  			monthBegin = theValue.substring(0, indexOfSelected - 1);
  	}
  	
  	String optionsYearBegin = "";
  	iter1 = cYearBegin.iterator();
	while (iter1.hasNext())
	{
    	String[] item = (String[]) iter1.next();
    	String theValue = item[0];
  		optionsYearBegin += "<option value="+ theValue +">"+item[1]+"</option>";
  		int indexOfSelected = theValue.indexOf("selected");
  		if(indexOfSelected != -1)
  			yearBegin = theValue.substring(0, indexOfSelected - 1);
  	}
          	
	browseBar.setDomainName(resources.getString("silverStatisticsPeas.statistics"));
    browseBar.setComponentName(resources.getString("silverStatisticsPeas.Access"), "ValidateViewAccess?MonthBegin="+monthBegin+"&YearBegin="+yearBegin+"&FilterLibGroup="+filterLibGroup+"&FilterIdGroup="+filterIdGroup+"&FilterLibUser="+filterLibUser+"&FilterIdUser="+filterIdUser+"&SpaceId=");
    
	if (spaceId != null && ! "".equals(spaceId))
	{
		String path = "";
		String separator = "";
		Iterator i = vPath.iterator();
		while ( i.hasNext() )
		{
			String[] pathItem = (String[]) i.next();
			if(userProfile.equals("A")) {//Administrateur
				path += separator + "<a href=\"ValidateViewAccess?MonthBegin="+monthBegin+"&YearBegin="+yearBegin+"&FilterLibGroup="+filterLibGroup+"&FilterIdGroup="+filterIdGroup+"&FilterLibUser="+filterLibUser+"&FilterIdUser="+filterIdUser+( (pathItem[0]==null) ? "" : ("&SpaceId="+pathItem[0]) )+"\">"+pathItem[1]+ "</a>";
			} else {//manager d'espaces
				path += separator + pathItem[1];
			}
			separator = " > ";
		}
		
		browseBar.setPath(path);
	}
	
	operationPane.addOperation(resources.getIcon("silverStatisticsPeas.icoGenExcel"),resources.getString("silverStatisticsPeas.export"),"javascript:openSPWindow('ExportAccess.txt','')");
	
	out.println(window.printBefore());
	out.println(frame.printBefore());
	out.println(board.printBefore());
%>
<CENTER>
  <table width="100%" border="0" cellspacing="0" cellpadding="4">
    <form name="accessFormulaire" action="ValidateViewAccess" method="post">
      <tr> 
        <td width="300" nowrap class=txtlibform><%=resources.getString("GML.date")%>&nbsp;:</td>
        <td nowrap> 
          <select name="MonthBegin" size="1">
		    <%
		    out.print(optionsMonthBegin);
          	%>
          </select>
          &nbsp;&nbsp; 
          <select name="YearBegin" size="1">
		    <%
        	out.print(optionsYearBegin);
          	%>
          </select>
        </td>
      </tr>
      <tr> 
        <td nowrap class=txtlibform><%=resources.getString("silverStatisticsPeas.group")%>&nbsp;:</td>
        <td nowrap colspan="2"> 
          <input type="text" name="FilterLibGroup" value="<%=filterLibGroup%>" size="25" disabled>
		  <input type="hidden" name="FilterIdGroup" value="<%=filterIdGroup%>">
          <a href=javascript:openSPWindow('AccessCallUserPanelGroup','')><img src="<%=resources.getIcon("silverStatisticsPeas.icoAccessGroupPanelPeas")%>" align="absmiddle" alt="<%=resources.getString("silverStatisticsPeas.openUserPanelPeas")%>" border=0 title="<%=resources.getString("silverStatisticsPeas.openUserPanelPeas")%>"></a> 
          <a href=javascript:clearFilterGroup()><img src="<%=resources.getIcon("silverStatisticsPeas.icoClearGroupUser")%>" align="absmiddle" alt="<%=resources.getString("silverStatisticsPeas.ClearUserPanelPeas")%>" border=0 title="<%=resources.getString("silverStatisticsPeas.ClearUserPanelPeas")%>"></a> 
        </td>
      </tr>
      <tr> 
        <td nowrap class=txtlibform><%=resources.getString("GML.user")%>&nbsp;:</td>
        <td nowrap colspan="2"> 
          <input type="text" name="FilterLibUser" value="<%=filterLibUser%>" size="25" disabled>
		  <input type="hidden" name="FilterIdUser" value="<%=filterIdUser%>">
          <a href=javascript:openSPWindow('AccessCallUserPanelUser','')><img src="<%=resources.getIcon("silverStatisticsPeas.icoAccessUserPanelPeas")%>" align="absmiddle" alt="<%=resources.getString("silverStatisticsPeas.openUserPanelPeas")%>" border=0 title="<%=resources.getString("silverStatisticsPeas.openUserPanelPeas")%>"></a> 
          <a href=javascript:clearFilterUser()><img src="<%=resources.getIcon("silverStatisticsPeas.icoClearGroupUser")%>" align="absmiddle" alt="<%=resources.getString("silverStatisticsPeas.ClearUserPanelPeas")%>" border=0 title="<%=resources.getString("silverStatisticsPeas.ClearUserPanelPeas")%>"></a> 
        </td>
      </tr>
      <input type="hidden" name="SpaceId" value="<%=(spaceId==null) ? "" : spaceId%>">
      </form>
  </table>
  <%
  
	out.println(board.printAfter());
  	
    ButtonPane buttonPane = gef.getButtonPane();
    buttonPane.addButton((Button) gef.getFormButton(resources.getString("GML.validate"), "javascript:validerForm()", false));
	buttonPane.addButton((Button) gef.getFormButton(resources.getString("GML.cancel"), "javascript:document.cancelAccessForm.submit()", false));
    out.println("<br><center>"+buttonPane.print()+"</center><br>");
  
   //Graphiques
   if (vStatsData != null)
   {
  %>
		   	<div align="center">
				<img src="<%=m_context%>/ChartServlet/?chart=USER_VENTIL_CHART&random=<%=(new Date()).getTime()%>">
			</div>
  <%
  }
  %>
  <br>
  
  <%
 	
	// Tableau

          ArrayPane arrayPane = gef.getArrayPane("List", "ValidateViewAccess?MonthBegin="+monthBegin+"&YearBegin="+yearBegin+"&FilterLibGroup="+filterLibGroup+"&FilterIdGroup="+filterIdGroup+"&FilterLibUser="+filterLibUser+"&FilterIdUser="+filterIdUser+"&SpaceId="+spaceId, request,session);
          arrayPane.setVisibleLineNumber(50);
		  
          ArrayColumn arrayColumn1 = arrayPane.addArrayColumn(resources.getString("silverStatisticsPeas.organisation"));
          ArrayColumn arrayColumn2 = arrayPane.addArrayColumn(resources.getString("GML.allMP"));
          ArrayColumn arrayColumn3 = arrayPane.addArrayColumn(resources.getString("silverStatisticsPeas.group"));
          if(filterIdGroup == null || "".equals(filterIdGroup)) {
			  arrayColumn3.setSortable(false);
		  }
          ArrayColumn arrayColumn4 = arrayPane.addArrayColumn(resources.getString("GML.user"));
          if(filterIdUser == null || "".equals(filterIdUser)) {
			  arrayColumn4.setSortable(false);
		  }
		  
		  ArrayColumn arrayColumn5 = arrayPane.addArrayColumn(resources.getString("silverStatisticsPeas.Actions")+"</A>");
          arrayColumn5.setSortable(false);

        if (vStatsData != null)
        {
        	iter1 = vStatsData.iterator();
        	String title;
        	        	
        	while (iter1.hasNext())
        	{
				arrayLine = arrayPane.addArrayLine();
				
            	String[] item = (String[]) iter1.next();
            	
            	title = resources.getString("silverStatisticsPeas.Historique");
            	if ( "SPACE".equals(item[0]) ) {
          			arrayLine.addArrayCellLink("<B>"+item[2]+"</B>", "ValidateViewAccess?MonthBegin="+monthBegin+"&YearBegin="+yearBegin+"&FilterLibGroup="+filterLibGroup+"&FilterIdGroup="+filterIdGroup+"&FilterLibUser="+filterLibUser+"&FilterIdUser="+filterIdUser+"&SpaceId="+item[1]);
          			title += " ["+item[2]+"]";
          		} else {
          			arrayLine.addArrayCellText(item[2]);
          			title += " "+item[2];
          		}
          			
          		ArrayCellText cellTextCount = arrayLine.addArrayCellText(item[3]);
          		cellTextCount.setCompareOn(new Integer(item[3]));
          		
          		if(filterIdGroup != null && ! "".equals(filterIdGroup)) {
	          		cellTextCount = arrayLine.addArrayCellText(item[4]);
    	      		cellTextCount.setCompareOn(new Integer(item[4]));
    	      		if(filterIdUser == null || "".equals(filterIdUser)) {
    	      			title += " "+resources.getString("silverStatisticsPeas.For")+ " "+filterLibGroup;
    	      		}
    	      	} else {
	    	      	arrayLine.addArrayCellText("");
    	      	}
    	      	
    	      	if(filterIdUser != null && ! "".equals(filterIdUser)) {
	          		cellTextCount = arrayLine.addArrayCellText(item[5]);	
    	      		cellTextCount.setCompareOn(new Integer(item[5]));
    	      		title += " "+resources.getString("silverStatisticsPeas.For")+ " "+filterLibUser;
    	      	} else {
	    	      	arrayLine.addArrayCellText("");
    	      	}
    	      	
    	      	arrayLine.addArrayCellText("<div align=left><a href=\"ViewEvolutionAccess?Entite="+item[0]+"&Id="+item[1]+"\"><img src=\""+resources.getIcon("silverStatisticsPeas.icoComponent")+"\" align=absmiddle alt=\""+title+"\" border=0 title=\""+title+"\"></a></div>");
		}

        out.println(arrayPane.print());
        out.println("");
    }
    %>
</CENTER>
<%
out.println(frame.printAfter());
out.println(window.printAfter());
%>
<form name="cancelAccessForm" action="ViewAccess" method="post">
</form>
</BODY>
</HTML>