<%@ include file="checkPdc.jsp"%>

<%

// recuperation des parametres
String 	viewType = (String) request.getAttribute("ViewType"); // type d'axes à visualiser P(rimaire) ou S(econdaire)
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
            
			arrayLine.addArrayCellText("<div align=right><img src=\""+resource.getIcon("pdcPeas.icoComponent")+"\" border=0 alt=\""+Encode.javaStringToHtmlString(axisHeader.getName(language))+"\" title=\""+Encode.javaStringToHtmlString(axisHeader.getName(language))+"\"></div>");
            
			arrayLine.addArrayCellText("<a href=\""+pdcUtilizationContext+"UtilizationChooseAxis?Id="+axisId+"\" title=\""+resource.getString("pdcPeas.axisUse")+"&nbsp;:&nbsp;"+Encode.javaStringToHtmlString(axisHeader.getName(language))+"\"><span class=textePetitBold>"+Encode.javaStringToHtmlString(axisHeader.getName(language))+"</span></a>");
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