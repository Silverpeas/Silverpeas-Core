<%--

    Copyright (C) 2000 - 2013 Silverpeas

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

<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<%@ include file="checkPdc.jsp"%>

<%@ page import="java.net.URLEncoder"%>
<%@ page import="org.silverpeas.core.util.EncodeHelper" %>

<%
	 ArrayList result = new ArrayList();
	 for (int i=1; i<16 ; i++) {
		result.add("Acteur#"+i);
	 }
	 ArrayLine arrayLine = null;
%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<view:looknfeel/>
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
        arrayLine.addArrayCellText("<span class=textePetitBold>"+
            EncodeHelper.javaStringToHtmlParagraphe((String) result.get(i))+"</span>").setAlignement("center");
        arrayLine.addArrayCellText("<span class=textePetitBold>&nbsp;</span>").setAlignement("center");
        arrayLine.addArrayCellText("<input type=checkbox name=selAxis value=\""+URLEncoder.encode((String) result.get(i), "UTF-8")+"\">").setAlignement("center");
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