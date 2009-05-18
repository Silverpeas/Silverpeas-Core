<%@ include file="checkPdc.jsp"%>
<%
String viewType = (String) request.getAttribute("ViewType"); // type d'axes à visualiser P(rimaire) ou S(econdaire)
List axisList = (List) request.getAttribute("AxisList"); // a list of axis header
AxisHeader axisHeader = null;
String axisId = null;
Iterator it = axisList.iterator();
ArrayLine arrayLine = null;
%>
<HTML>
<HEAD>
<TITLE><%=resource.getString("GML.popupTitle")%></TITLE>
<%
out.println(gef.getLookStyleSheet());
%>
</HEAD>
<BODY>
<%
    browseBar.setDomainName(resource.getString("pdcPeas.DomainSelect"));
    browseBar.setComponentName("Composants");
	browseBar.setPath(" Ciblage > Acteurs > Ajout au plan de classement");
    out.println(window.printBefore());

	TabbedPane tabbedPane = gef.getTabbedPane();
    tabbedPane.addTab("Axes primaires","vsicChangeType?ViewType=P",viewType.equals("P"));
    tabbedPane.addTab("Axes secondaires","vsicChangeType?ViewType=S",viewType.equals("S"));
    out.println(tabbedPane.print());
    out.println(frame.printBefore());
%>
<center>
 <%
        ArrayPane arrayPane = gef.getArrayPane("List", "#", request, session);
        arrayPane.addArrayColumn("&nbsp;");
        ArrayColumn arrayColumn1 =  arrayPane.addArrayColumn(resource.getString("pdcPeas.axisName"));
        arrayColumn1.setSortable(false);

        // main loop to show all axis
        while (it.hasNext()){
                axisHeader = (AxisHeader) it.next();
                axisId = axisHeader.getPK().getId();

                arrayLine = arrayPane.addArrayLine();

                arrayLine.addArrayCellLink("<div align=left><img src=\""+resource.getIcon("pdcPeas.icoComponent")+"\" border=0 alt=\""+resource.getString("pdcPeas.viewAxis")+" : "+Encode.javaStringToHtmlString(axisHeader.getName())+"\" title=\""+resource.getString("pdcPeas.viewAxis")+" : "+Encode.javaStringToHtmlString(axisHeader.getName())+"\"></div>", "vsicAddTree?Id="+axisId);
                arrayLine.addArrayCellText("<span class=textePetitBold>"+Encode.javaStringToHtmlString(axisHeader.getName())+"</span>");
        }

        out.println(arrayPane.print());
 %>
<br>
<%
		  ButtonPane buttonPane = gef.getButtonPane();
		  buttonPane.addButton((Button) gef.getFormButton("Fermer", "javascript:window.close()", false));
		  out.println(buttonPane.print());
%>
<br>
</center>
<%
out.println(frame.printAfter());
out.println(window.printAfter());
%>
</BODY>
</HTML>