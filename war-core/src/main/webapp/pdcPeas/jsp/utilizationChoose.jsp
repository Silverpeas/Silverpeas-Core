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

<%@ include file="checkPdc.jsp"%>

<%

// recuperation des parametres
String 	viewType = (String) request.getAttribute("ViewType"); // type d'axes � visualiser P(rimaire) ou S(econdaire)
List 	axisList = (List) request.getAttribute("AxisList"); // a list of axis header

// initialisation of variables of main loop (show all axes)
AxisHeader axisHeader = null;
String axisId = null;
Iterator it = axisList.iterator();
ArrayLine arrayLine = null;
IconPane iconPane1 = null;
Icon aspiIcon = null;

%>

<HTML>
<HEAD>
<TITLE><%=resource.getString("GML.popupTitle")%></TITLE>
<%
   out.println(gef.getLookStyleSheet());
%>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/animation.js"></script>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/checkForm.js"></script>
<script type="text/javascript" src="<%=m_context%>/pdcPeas/jsp/javascript/formUtil.js"></script>
<script language="javascript">
function goBack(){
	document.goBack.submit();
}
</script>
</HEAD>
<BODY marginheight="5" marginwidth="5" leftmargin="5" topmargin="5" bgcolor="#FFFFFF">
<%
	browseBar.setDomainName(spaceLabel);
	browseBar.setComponentName(componentLabel);
	browseBar.setExtraInformation(resource.getString("pdcPeas.paramChooseAxis"));

    out.println(window.printBefore());

	TabbedPane tabbedPane = gef.getTabbedPane();
	tabbedPane.addTab(resource.getString("pdcPeas.primaryAxis"),pdcUtilizationContext+"ChangeViewType?ViewType=P",viewType.equals("P"));
    tabbedPane.addTab(resource.getString("pdcPeas.secondaryAxis"),pdcUtilizationContext+"ChangeViewType?ViewType=S",viewType.equals("S"));
	out.println(tabbedPane.print());

    out.println(frame.printBefore());
%>
<CENTER>
    <%
    ArrayPane arrayPane = gef.getArrayPane("PdcPeas", "", request, session);    

    ArrayColumn arrayColumn0 = arrayPane.addArrayColumn("&nbsp;");
	arrayColumn0.setSortable(false);

	ArrayColumn arrayColumn = arrayPane.addArrayColumn(resource.getString("pdcPeas.axisName"));
	arrayColumn.setSortable(false);

	// main loop to show all axis
	while (it.hasNext()){
			axisHeader = (AxisHeader) it.next();
			axisId = axisHeader.getPK().getId();

            arrayLine = arrayPane.addArrayLine();
            
			arrayLine.addArrayCellText("<div align=right><img src=\""+resource.getIcon("pdcPeas.icoComponent")+"\" border=0 alt=\""+EncodeHelper.javaStringToHtmlString(axisHeader.getName(language))+"\" title=\""+EncodeHelper.javaStringToHtmlString(axisHeader.getName(language))+"\"></div>");
            
			arrayLine.addArrayCellText("<a href=\""+pdcUtilizationContext+"UtilizationChooseAxis?Id="+axisId+"\" title=\""+resource.getString("pdcPeas.axisUse")+"&nbsp;:&nbsp;"+EncodeHelper.javaStringToHtmlString(axisHeader.getName(language))+"\"><span class=textePetitBold>"+EncodeHelper.javaStringToHtmlString(axisHeader.getName(language))+"</span></a>");
	}
	
    out.println(arrayPane.print());
    %>
	<%=separator%>
    <%
    ButtonPane buttonPane = gef.getButtonPane();
	buttonPane.addButton((Button) gef.getFormButton(resource.getString("GML.back"), "javascript:goBack()", false));
    out.println(buttonPane.print());
    %>

</CENTER>
<%
out.println(frame.printAfter());
out.println(window.printAfter());
%>
</form>
<form name="goBack" action="<%=pdcUtilizationContext%>Main" method="post">
</form>
</BODY>
</HTML>