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
    String userProfile = (String)request.getAttribute("UserProfile");
%>

<%
	TabbedPane tabbedPane = gef.getTabbedPane();
	if (userProfile.equals("A"))
    {
		tabbedPane.addTab(resources.getString("silverStatisticsPeas.JobPeas"), m_context+"/RsilverStatisticsPeas/jsp/ViewVolumeServices",false);
	}
	tabbedPane.addTab(resources.getString("GML.publications"), m_context+"/RsilverStatisticsPeas/jsp/ViewVolumePublication",true);
	tabbedPane.addTab(resources.getString("GML.attachments"), m_context+"/RsilverStatisticsPeas/jsp/ViewVolumeServer",false);
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
		fonction = fonction + "?MonthBegin=" + volumePublication.MonthBegin.value;
		fonction = fonction + "&YearBegin=" + volumePublication.YearBegin.value;		
		fonction = fonction + "&FilterLibGroup=" + volumePublication.FilterLibGroup.value;		
		fonction = fonction + "&FilterIdGroup=" + volumePublication.FilterIdGroup.value;		
		fonction = fonction + "&FilterLibUser=" + volumePublication.FilterLibUser.value;		
		fonction = fonction + "&FilterIdUser=" + volumePublication.FilterIdUser.value;	
		fonction = fonction + "&SpaceId=";	
		SP_openWindow(fonction, windowName, '750', '550','scrollbars=yes, menubar=yes, resizable, alwaysRaised');
	}
	
	function clearFilterGroup(){
		volumePublication.FilterLibGroup.value = "";
		volumePublication.FilterIdGroup.value = "";
	}
	
	function clearFilterUser(){
		volumePublication.FilterLibUser.value = "";
		volumePublication.FilterIdUser.value = "";
	}
	
	function validerForm(){
		volumePublication.FilterLibGroup.disabled = false;
		volumePublication.FilterLibUser.disabled = false;
		document.volumePublication.submit()
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
  	
	browseBar.setDomainName(resources.getString("silverStatisticsPeas.statistics") + " > "+resources.getString("silverStatisticsPeas.Volumes"));
    browseBar.setComponentName(resources.getString("GML.publications"), "ValidateViewVolume?MonthBegin="+monthBegin+"&YearBegin="+yearBegin+"&FilterLibGroup="+filterLibGroup+"&FilterIdGroup="+filterIdGroup+"&FilterLibUser="+filterLibUser+"&FilterIdUser="+filterIdUser+"&SpaceId=");
    
	if (spaceId != null && ! "".equals(spaceId))
	{
		String path = "";
		String separator = "";
		Iterator i = vPath.iterator();
		while ( i.hasNext() )
		{
			String[] pathItem = (String[]) i.next();
			if (userProfile.equals("A")) {
				path += separator + "<a href=\"ValidateViewVolume?MonthBegin="+monthBegin+"&YearBegin="+yearBegin+"&FilterLibGroup="+filterLibGroup+"&FilterIdGroup="+filterIdGroup+"&FilterLibUser="+filterLibUser+"&FilterIdUser="+filterIdUser+( (pathItem[0]==null) ? "" : ("&SpaceId="+pathItem[0]) )+"\">"+pathItem[1]+ "</a>";
			} else {
				path += separator + pathItem[1];			
			}
			separator = " > ";
		}
		
		browseBar.setPath(path);
	}

	operationPane.addOperation(resources.getIcon("silverStatisticsPeas.icoGenExcel"),resources.getString("silverStatisticsPeas.export"),"javascript:openSPWindow('ExportAccess.txt','')");
		
	out.println(window.printBefore());
	out.println(tabbedPane.print());
	out.println(frame.printBefore());
	out.println(board.printBefore());
%>
<CENTER>
  <table width="100%" border="0" cellspacing="0" cellpadding="4">
    <form name="volumePublication" action="ValidateViewVolume" method="post">
      <tr> 
        <td width="40%" nowrap><span class=txtlibform><%=resources.getString("GML.date")%>&nbsp;:&nbsp;</span></td>
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
        <td nowrap><span class=txtlibform><%=resources.getString("silverStatisticsPeas.group")%>&nbsp;:&nbsp;</span></td>
        <td nowrap colspan="2"> 
          <input type="text" name="FilterLibGroup" value="<%=filterLibGroup%>" size="25" disabled>
		  <input type="hidden" name="FilterIdGroup" value="<%=filterIdGroup%>">
          <a href=javascript:openSPWindow('VolumeCallUserPanelGroup','')><img src="<%=resources.getIcon("silverStatisticsPeas.icoAccessGroupPanelPeas")%>" align="absmiddle" alt="<%=resources.getString("silverStatisticsPeas.openUserPanelPeas")%>" border=0 title="<%=resources.getString("silverStatisticsPeas.openUserPanelPeas")%>"></a> 
          <a href=javascript:clearFilterGroup()><img src="<%=resources.getIcon("silverStatisticsPeas.icoClearGroupUser")%>" align="absmiddle" alt="<%=resources.getString("silverStatisticsPeas.ClearUserPanelPeas")%>" border=0 title="<%=resources.getString("silverStatisticsPeas.ClearUserPanelPeas")%>"></a> 
        </td>
      </tr>
      <tr> 
        <td nowrap><span class=txtlibform><%=resources.getString("GML.user")%>&nbsp;:&nbsp;</span></td>
        <td nowrap colspan="2"> 
          <input type="text" name="FilterLibUser" value="<%=filterLibUser%>" size="25" disabled>
		  <input type="hidden" name="FilterIdUser" value="<%=filterIdUser%>">
          <a href=javascript:openSPWindow('VolumeCallUserPanelUser','')><img src="<%=resources.getIcon("silverStatisticsPeas.icoAccessUserPanelPeas")%>" align="absmiddle" alt="<%=resources.getString("silverStatisticsPeas.openUserPanelPeas")%>" border=0 title="<%=resources.getString("silverStatisticsPeas.openUserPanelPeas")%>"></a> 
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
	buttonPane.addButton((Button) gef.getFormButton(resources.getString("GML.cancel"), "javascript:document.cancelForm.submit()", false));
    out.println("<br><center>"+buttonPane.print()+"</center><br>");

	//Graphiques
   	if (vStatsData != null)
   	{
  %>
		   	<div align="center">
				<img src="<%=m_context%>/ChartServlet/?chart=PUBLI_VENTIL_CHART&random=<%=(new Date()).getTime()%>">
			</div>
  <%
  	}
  %>
  <br>
  <%
		  // Tableau

          ArrayPane arrayPane = gef.getArrayPane("List", "", request,session);
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

        if (vStatsData != null)
        {
        	iter1 = vStatsData.iterator();
        	
        	while (iter1.hasNext())
        	{
				arrayLine = arrayPane.addArrayLine();
				
            	String[] item = (String[]) iter1.next();
            	
            	if ( "SPACE".equals(item[0]) )
          			arrayLine.addArrayCellLink("<B>"+item[2]+"</B>", "ValidateViewVolume?MonthBegin="+monthBegin+"&YearBegin="+yearBegin+"&FilterLibGroup="+filterLibGroup+"&FilterIdGroup="+filterIdGroup+"&FilterLibUser="+filterLibUser+"&FilterIdUser="+filterIdUser+"&SpaceId="+item[1]);
          		else
          			arrayLine.addArrayCellText(item[2]);
          			
          		ArrayCellText cellTextCount = arrayLine.addArrayCellText(item[3]);
          		cellTextCount.setCompareOn(new Integer(item[3]));
          		
          		if(filterIdGroup != null && ! "".equals(filterIdGroup)) {
	          		cellTextCount = arrayLine.addArrayCellText(item[4]);
    	      		cellTextCount.setCompareOn(new Integer(item[4]));
    	      	} else {
	    	      	arrayLine.addArrayCellText("");
    	      	}
    	      	
    	      	if(filterIdUser != null && ! "".equals(filterIdUser)) {
	          		cellTextCount = arrayLine.addArrayCellText(item[5]);	
    	      		cellTextCount.setCompareOn(new Integer(item[5]));
    	      	}
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
<form name="cancelForm" action="ViewVolumePublication" method="post">
</form>
</BODY>
</HTML>