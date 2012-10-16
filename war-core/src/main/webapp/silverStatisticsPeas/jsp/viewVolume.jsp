<%--

    Copyright (C) 2000 - 2012 Silverpeas

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

<%@page import="com.silverpeas.util.StringUtil"%>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%@ include file="checkSilverStatistics.jsp" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>

<%
	Collection<String[]> cMonthBegin = (Collection<String[]>)request.getAttribute("MonthBegin");
	String monthBegin = "";
	Collection<String[]> cYearBegin = (Collection<String[]>)request.getAttribute("YearBegin");
	String yearBegin = "";
	String filterLibGroup = (String)request.getAttribute("FilterLibGroup");
	if (!StringUtil.isDefined(filterLibGroup)) {
	  filterLibGroup = "";
	}
    String filterIdGroup = (String) request.getAttribute("FilterIdGroup");
    String filterLibUser = (String)request.getAttribute("FilterLibUser");
    if (!StringUtil.isDefined(filterLibUser)) {
      filterLibUser = "";
  	}
	String filterIdUser = (String) request.getAttribute("FilterIdUser");
	String spaceId = (String) request.getAttribute("SpaceId");
	Vector<String[]> vPath = (Vector<String[]>) request.getAttribute("Path");
    Vector<String[]> vStatsData = (Vector<String[]>)request.getAttribute("StatsData");
    String userProfile = (String)request.getAttribute("UserProfile");
    
    int totalNumberOfContributions = 0;

	TabbedPane tabbedPane = gef.getTabbedPane();
	if (userProfile.equals("A")) {
		tabbedPane.addTab(resources.getString("silverStatisticsPeas.JobPeas"), m_context+"/RsilverStatisticsPeas/jsp/ViewVolumeServices",false);
	}
	tabbedPane.addTab(resources.getString("silverStatisticsPeas.volumes.tab.contributions"), m_context+"/RsilverStatisticsPeas/jsp/ViewVolumePublication",true);
	tabbedPane.addTab(resources.getString("GML.attachments"), m_context+"/RsilverStatisticsPeas/jsp/ViewVolumeServer",false);
	
	String optionsMonthBegin = "";
	for (String[] item : cMonthBegin) {
    	String theValue = item[0];
		optionsMonthBegin += "<option value="+ theValue +">"+resources.getString(item[1])+"</option>";
  		int indexOfSelected = theValue.indexOf("selected");
  		if (indexOfSelected != -1) {
  			monthBegin = theValue.substring(0, indexOfSelected - 1);
  		}
  	}
  	
  	String optionsYearBegin = "";
	for (String[] item : cYearBegin) {
    	String theValue = item[0];
  		optionsYearBegin += "<option value="+ theValue +">"+item[1]+"</option>";
  		int indexOfSelected = theValue.indexOf("selected");
  		if(indexOfSelected != -1) {
  			yearBegin = theValue.substring(0, indexOfSelected - 1);
  		}
  	}
	
	ArrayPane arrayPane = gef.getArrayPane("List", "", request,session);
  	arrayPane.setVisibleLineNumber(50);

  	ArrayColumn arrayColumn1 = arrayPane.addArrayColumn(resources.getString("silverStatisticsPeas.organisation"));
  	arrayColumn1.setWidth("300px");
  	ArrayColumn arrayColumn2 = arrayPane.addArrayColumn(resources.getString("GML.allMP"));

  	ArrayColumn arrayColumn3 = null;
	if(StringUtil.isDefined(filterIdGroup)) {
	  	arrayColumn3 = arrayPane.addArrayColumn(resources.getString("silverStatisticsPeas.group"));
	}

  	ArrayColumn arrayColumn4 = null;
	if(StringUtil.isDefined(filterIdUser)) {
	  	arrayColumn4 = arrayPane.addArrayColumn(resources.getString("GML.user"));
	}

	if (vStatsData != null) {
		for (String[] item : vStatsData) {
		  	ArrayLine arrayLine = arrayPane.addArrayLine();
	    	if ("SPACE".equals(item[0])) {
	  			arrayLine.addArrayCellLink("<b>"+item[2]+"</b>", "ValidateViewVolume?MonthBegin="+monthBegin+"&YearBegin="+yearBegin+"&FilterLibGroup="+filterLibGroup+"&FilterIdGroup="+filterIdGroup+"&FilterLibUser="+filterLibUser+"&FilterIdUser="+filterIdUser+"&SpaceId="+item[1]);
	    	} else {
	  			arrayLine.addArrayCellText(item[2]);
	    	}
	  			
	  		ArrayCellText cellTextCount = arrayLine.addArrayCellText(item[3]);
	  		int nbContributions = new Integer(item[3]);
	  		totalNumberOfContributions += nbContributions;
	  		cellTextCount.setCompareOn(nbContributions);
	  		
	  		if(StringUtil.isDefined(filterIdGroup)) {
	    		cellTextCount = arrayLine.addArrayCellText(item[4]);
	    		cellTextCount.setCompareOn(new Integer(item[4]));
	    	}
	    	
	  		if(StringUtil.isDefined(filterIdUser)) {
	    		cellTextCount = arrayLine.addArrayCellText(item[5]);	
	    		cellTextCount.setCompareOn(new Integer(item[5]));
	    	}
		}
	}
%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html>
<head>
<title><%=resources.getString("GML.popupTitle")%></title>
<view:looknfeel />
<script type="text/javascript" src="<%=m_context%>/util/javaScript/animation.js"></script>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/checkForm.js"></script>
<script type="text/javascript">
	// This function open a silverpeas window
	function openSPWindow(fonction,windowName){
		fonction = fonction + "?MonthBegin=" + document.volumePublication.MonthBegin.value;
		fonction = fonction + "&YearBegin=" + document.volumePublication.YearBegin.value;		
		fonction = fonction + "&FilterLibGroup=" + document.volumePublication.FilterLibGroup.value;		
		fonction = fonction + "&FilterIdGroup=" + document.volumePublication.FilterIdGroup.value;		
		fonction = fonction + "&FilterLibUser=" + document.volumePublication.FilterLibUser.value;		
		fonction = fonction + "&FilterIdUser=" + document.volumePublication.FilterIdUser.value;	
		fonction = fonction + "&SpaceId=";	
		SP_openWindow(fonction, windowName, '750', '550','scrollbars=yes, menubar=yes, resizable, alwaysRaised');
	}
	
	function clearFilterGroup(){
		document.volumePublication.FilterLibGroup.value = "";
		document.volumePublication.FilterIdGroup.value = "";
	}
	
	function clearFilterUser(){
		document.volumePublication.FilterLibUser.value = "";
		document.volumePublication.FilterIdUser.value = "";
	}
	
	function validerForm(){
		$.progressMessage();
		document.volumePublication.submit()
	}
	
</script>
</head>
<body class="admin stats volume contributions">
<%	
	browseBar.setDomainName(resources.getString("silverStatisticsPeas.statistics") + " > "+resources.getString("silverStatisticsPeas.Volumes"));
    browseBar.setComponentName(resources.getString("silverStatisticsPeas.volumes.tab.contributions"), "ValidateViewVolume?MonthBegin="+monthBegin+"&YearBegin="+yearBegin+"&FilterLibGroup="+filterLibGroup+"&FilterIdGroup="+filterIdGroup+"&FilterLibUser="+filterLibUser+"&FilterIdUser="+filterIdUser+"&SpaceId=");
    
	if (spaceId != null && !"".equals(spaceId)) {
		String path = "";
		String separator = "";
		for (String[] pathItem : vPath) {
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
<form name="volumePublication" action="ValidateViewVolume" method="post">
  <table width="100%" border="0" cellspacing="0" cellpadding="4">
      <tr> 
        <td class="txtlibform"><%=resources.getString("GML.date")%>&nbsp;:&nbsp;</td>
        <td> 
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
        <td class=txtlibform><%=resources.getString("silverStatisticsPeas.group")%>&nbsp;:&nbsp;</td>
        <td> 
          <input type="text" name="FilterLibGroup" value="<%=filterLibGroup%>" size="25" disabled="disabled"/>
		  <input type="hidden" name="FilterIdGroup" value="<%=filterIdGroup%>"/>
          <a href=javascript:openSPWindow('VolumeCallUserPanelGroup','')><img src="<%=resources.getIcon("silverStatisticsPeas.icoAccessGroupPanelPeas")%>" align="absmiddle" alt="<%=resources.getString("silverStatisticsPeas.openUserPanelPeas")%>" border="0" title="<%=resources.getString("silverStatisticsPeas.openUserPanelPeas")%>"/></a> 
          <a href=javascript:clearFilterGroup()><img src="<%=resources.getIcon("silverStatisticsPeas.icoClearGroupUser")%>" align="absmiddle" alt="<%=resources.getString("silverStatisticsPeas.ClearUserPanelPeas")%>" border="0" title="<%=resources.getString("silverStatisticsPeas.ClearUserPanelPeas")%>"/></a> 
        </td>
      </tr>
      <tr> 
        <td class=txtlibform><%=resources.getString("GML.user")%>&nbsp;:&nbsp;</td>
        <td> 
          <input type="text" name="FilterLibUser" value="<%=filterLibUser%>" size="25" disabled="disabled"/>
		  <input type="hidden" name="FilterIdUser" value="<%=filterIdUser%>"/>
          <a href=javascript:openSPWindow('VolumeCallUserPanelUser','')><img src="<%=resources.getIcon("silverStatisticsPeas.icoAccessUserPanelPeas")%>" align="absmiddle" alt="<%=resources.getString("silverStatisticsPeas.openUserPanelPeas")%>" border="0" title="<%=resources.getString("silverStatisticsPeas.openUserPanelPeas")%>"/></a> 
          <a href=javascript:clearFilterUser()><img src="<%=resources.getIcon("silverStatisticsPeas.icoClearGroupUser")%>" align="absmiddle" alt="<%=resources.getString("silverStatisticsPeas.ClearUserPanelPeas")%>" border="0" title="<%=resources.getString("silverStatisticsPeas.ClearUserPanelPeas")%>"/></a> 
        </td>
      </tr>
      <input type="hidden" name="SpaceId" value="<%=(spaceId==null) ? "" : spaceId%>"/>
  </table>
  </form>
  <%
  	out.println(board.printAfter());
  	
    ButtonPane buttonPane = gef.getButtonPane();
    buttonPane.addButton(gef.getFormButton(resources.getString("GML.validate"), "javascript:validerForm()", false));
	buttonPane.addButton(gef.getFormButton(resources.getString("GML.cancel"), "ViewVolumePublication", false));
    out.println("<br/><center>"+buttonPane.print()+"</center><br/>");

	//Graphiques
   	if (vStatsData != null) {
  %>
  		<center>
  		<div align="center" id="chart">
			<img src="<%=m_context%>/ChartServlet/?chart=PUBLI_VENTIL_CHART&random=<%=(new Date()).getTime()%>"/>
		</div>
		<div align="center" id="total">
			<span><span class="number"><%=totalNumberOfContributions %></span> <%=resources.getString("silverStatisticsPeas.sums.contributions") %></span>
		</div>
  <% } %>
  <br/>
  <%
	if (vStatsData != null) {
		out.println(arrayPane.print());
    }
  %>
</center>
<%
out.println(frame.printAfter());
out.println(window.printAfter());
%>
<view:progressMessage/>
</body>
</html>