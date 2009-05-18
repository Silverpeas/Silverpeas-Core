<%@ include file="checkPdc.jsp"%>
<%
	Axis axis = (Axis) request.getAttribute("Axis");
	AxisHeader header = axis.getAxisHeader();
	String axisId = header.getPK().getId();
	ArrayList axisValues = (ArrayList) axis.getValues();
	String valueName = "";
	String valueId = "";
	int valueLevel = -1;
	Value value = null;
	String increment = "";
%>
<HTML>
<HEAD>
<TITLE><%=resource.getString("GML.popupTitle")%></TITLE>
<%
out.println(gef.getLookStyleSheet());
%>
<style type="text/css">
<!--

.axe:hover {
	font-size: 10px;
	font-weight: normal;
	color: White;
	background-color : navy;
	text-decoration: none;
	border:1 solid  rgb(255,150,0);
}

.axe {
	font-size: 10px;
	font-weight: normal;
	color: navy;
	background-color : White;
	text-decoration: none;
	border:1 solid  rgb(150,150,150);
}
-->
</style>
<script language="JavaScript">
function addValues(valId) {
    location.replace("vsicAddAxis?valueId="+valId+"&axisId=<%=axisId%>");
    window.close();
}
</script>
</HEAD>
<BODY>
<%
    browseBar.setDomainName(resource.getString("pdcPeas.DomainSelect"));
    browseBar.setComponentName("Composants");
	browseBar.setPath(" Ciblage > Acteurs > Ajout au plan de classement");

    out.println(window.printBefore());
    out.println(frame.printBefore());
%>
<CENTER>
<%=boardStart%>
<table border=0 cellpadding=0 cellspacing=0>
<tr><td background="<%=resource.getIcon("pdcPeas.trame")%>">
<%
  for (int i = 0; i<axisValues.size(); i++) {
     value = (Value) axisValues.get(i);
     valueName = value.getName();
     valueId = value.getPK().getId();
     valueLevel = value.getLevelNumber();
     increment = "";
     for (int j = 0; j < valueLevel; j++) {
        increment += "<img src="+resource.getIcon("pdcPeas.noColorPix")+" width=\"18\" align=\"absmiddle\">";
     }
     out.println(increment+"<img src="+resource.getIcon("pdcPeas.target")+" width=\"15\" align=\"absmiddle\">&nbsp;<a class=\"axe\" href=\"javaScript:addValues('"+valueId+"');\">&nbsp;&nbsp;&nbsp;"+Encode.javaStringToHtmlString(valueName)+"&nbsp;&nbsp;&nbsp;</a><img src="+resource.getIcon("pdcPeas.noColorPix")+" width=\"18\" align=\"absmiddle\"><BR>");
 }
%>
</td></tr>
</table>
<br>
<%
    ButtonPane buttonPane = gef.getButtonPane();
    buttonPane.addButton((Button) gef.getFormButton("Fermer", "javascript:window.close()", false));
    out.println(buttonPane.print());
%>
<%=boardEnd%>
<br>
</CENTER>
<%
	out.println(frame.printAfter());
    out.println(window.printAfter());
%>
</BODY>
</HTML>