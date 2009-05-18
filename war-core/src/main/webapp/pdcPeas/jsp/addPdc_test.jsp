<%@ include file="checkPdc.jsp"%>

<%@ page import="java.net.URLEncoder"%>

<%
	 ArrayList result = new ArrayList();
	 for (int i=1; i<16 ; i++) {
		result.add("Acteur#"+i);
	 }
	 ArrayLine arrayLine = null;
%>
<html>
<head>
<%
  out.println(gef.getLookStyleSheet());
%>
<script language="JavaScript">
function SP_openWindow(page,nom,largeur,hauteur,options) {
	var top=(screen.height-hauteur)/2;
	var left=(screen.width-largeur)/2;
	var popup=window.open(page,nom,"top="+top+",left="+left+",width="+largeur+",height="+hauteur+","+options);
	popup.focus();
	return popup;
}

function openSPWindow(){
	var list,item,bb,len,cnt=0,res="";
	list = document.all.selAxis;
	if (list == null) return;
	bb = 0;
	len = parseInt(list.length,10);
	if (isNaN(len)) { len = 1; bb = 1}

	for (var i=0; i < len; i++) {
		if (bb==0) item = list[i];
		else item = list;
		if (item.checked) {
			cnt=1;
			res = res+item.value+";";
		}
	}
	if (cnt >0 ) {
		var url = "<%=pdcContext%>vsicAddList?Values="+res;
		SP_openWindow(url, "Tree", '700', '450','scrollbars=yes, resizable, alwaysRaised');
	} else {
		alert("No checked items");
	}
}
</script>
</head>
<body>
<%
	browseBar.setDomainName("Espace");
	browseBar.setComponentName("Composant");
	browseBar.setPath(" Ciblage > Acteurs ");

	operationPane.addOperation(resource.getIcon("pdcPeas.icoAddPosition"), "Add selected to PDC", "javascript:openSPWindow();");
    out.println(window.printBefore());

	TabbedPane tabbedPane = gef.getTabbedPane();
	tabbedPane.addTab("Tab1","#",false);
    tabbedPane.addTab("Actors","#",true);
    tabbedPane.addTab("Themes","#",false);
    tabbedPane.addTab("Tab4","#",false);
	out.println(tabbedPane.print());
    out.println(frame.printBefore());
%>

<center>
<!--<table width="98%" cellspacing="0" cellpadding="0" border="0"><tr>
<td><select size=1>
<option value="" selected> Tous les pppppppp
<option value=""> Tous les dddddddd
</select></td></tr>
</table><br>-->
<%
    ArrayPane pane = gef.getArrayPane("PdcTest","",request,session);
    pane.addArrayColumn("Acteurs").setAlignement("center");
    pane.addArrayColumn(resource.getString("GML.description")).setAlignement("center");
    pane.addArrayColumn(resource.getString("pdcPeas.axisOperation")).setAlignement("center");
    pane.setVisibleLineNumber(15);
    pane.setSortable(false);

    for (int i=0; i<result.size(); i++) {
        arrayLine = pane.addArrayLine();
        arrayLine.addArrayCellText("<span class=textePetitBold>"+Encode.javaStringToHtmlParagraphe((String) result.get(i))+"</span>").setAlignement("center");
        arrayLine.addArrayCellText("<span class=textePetitBold>&nbsp;</span>").setAlignement("center");
        arrayLine.addArrayCellText("<input type=checkbox name=selAxis value=\""+URLEncoder.encode((String) result.get(i))+"\">").setAlignement("center");
    }
    out.println(pane.print());
%>
</center>
<%
out.println(frame.printAfter());
out.println(window.printAfter());
%>
</body>
</html>