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
	tabbedPane.addTab(resources.getString("GML.publications"), m_context+"/RsilverStatisticsPeas/jsp/ViewVolumePublication",false);
	tabbedPane.addTab(resources.getString("GML.attachments"), m_context+"/RsilverStatisticsPeas/jsp/ViewVolumeServer",true);
%>

<html>
<HEAD>
<TITLE><%=resources.getString("GML.popupTitle")%></TITLE>
<%
   out.println(gef.getLookStyleSheet());
%>
<!--[ JAVASCRIPT ]-->
<SCRIPT LANGUAGE="JAVASCRIPT">

	function changeDisplay() {
		document.volumeServerFormulaire.action = document.volumeServerFormulaire.Display.value;
		document.volumeServerFormulaire.submit();		
	}

</SCRIPT>
</HEAD>
<BODY marginheight=5 marginwidth=5 leftmargin=5 topmargin=5 onLoad="">
 <FORM name="volumeServerFormulaire" action="ViewVolumeServer" method="post">
<%	

	browseBar.setDomainName(resources.getString("silverStatisticsPeas.statistics") + " > "+resources.getString("silverStatisticsPeas.Volumes") + " > "+resources.getString("GML.attachments"));
    browseBar.setComponentName(resources.getString("silverStatisticsPeas.AttachmentsNumber"), "ViewVolumeServer?SpaceId=");
    
    if (spaceId != null && ! "".equals(spaceId))
	{
		String path = "";
		String separator = "";
		Iterator i = vPath.iterator();
		while ( i.hasNext() )
		{
			String[] pathItem = (String[]) i.next();
			if (userProfile.equals("A")) {
				path += separator + "<a href=\"ViewVolumeServer"+( (pathItem[0]==null) ? "" : ("?SpaceId="+pathItem[0]) )+"\">"+pathItem[1]+ "</a>";
			} else {
				path += separator + pathItem[1];
			}
			separator = " > ";
		}
		
		browseBar.setPath(path);
	}
    
	out.println(window.printBefore());
    out.println(tabbedPane.print());
    out.println(frame.printBefore());
%>

<BR>
	<div align="right">
		<%=resources.getString("silverStatisticsPeas.Display")%>&nbsp;:
		<select name="Display" size="1" onChange="changeDisplay()">
			<option value="ViewVolumeServer" selected><%=resources.getString("silverStatisticsPeas.AttachmentsNumber")%></option>
			<option value="ViewVolumeSizeServer"><%=resources.getString("silverStatisticsPeas.AttachmentsSize")%></option>
			<%
			if (userProfile.equals("A"))
    		{
    		%>
				<option value="ViewEvolutionVolumeSizeServer"><%=resources.getString("silverStatisticsPeas.AttachmentsTotalSize")%></option>
			<%
			}
			%>
		</select>
	</div>
		    
<%
	//Graphiques
   	if (vStatsData != null)
   	{
%>
<div align="center">
	<img src="<%=m_context%>/ChartServlet/?chart=DOC_VENTIL_CHART&random=<%=(new Date()).getTime()%>">
</div>
<%
	}
%>
<br>
<%

		  // Tableau

          	ArrayPane arrayPane = gef.getArrayPane("List", "ViewVolumeServer"+( (spaceId==null) ? "" : ("?SpaceId="+spaceId) ), request,session);
			arrayPane.setVisibleLineNumber(50);
          
	      	ArrayColumn arrayColumn1 = arrayPane.addArrayColumn(resources.getString("silverStatisticsPeas.organisation"));
            ArrayColumn arrayColumn2 = arrayPane.addArrayColumn(resources.getString("silverStatisticsPeas.AttachmentsNumber"));
	      	
	      	if (vStatsData != null)
	        {
	        	iter1 = vStatsData.iterator();
	        	
	        	while (iter1.hasNext())
	        	{
					arrayLine = arrayPane.addArrayLine();
					
	            	String[] item = (String[]) iter1.next();
	            	
	            	if ( "SPACE".equals(item[0]) )
	          			arrayLine.addArrayCellLink("<B>"+item[2]+"</B>", "ViewVolumeServer?SpaceId="+item[1]);
	          		else
	          			arrayLine.addArrayCellText(item[2]);
	          			
	          		ArrayCellText cellTextCount = arrayLine.addArrayCellText(item[3]);
	          		cellTextCount.setCompareOn(new Integer(item[3]));
	          		
	        	}
			
				out.println(arrayPane.print());
		        out.println("");
	    }
  %>

 </FORM>
<%
out.println(frame.printAfter());
out.println(window.printAfter());
%>
</BODY>
</HTML>