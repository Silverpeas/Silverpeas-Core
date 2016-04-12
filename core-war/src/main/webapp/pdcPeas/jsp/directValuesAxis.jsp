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
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<HEAD>
<TITLE><%=resource.getString("GML.popupTitle")%></TITLE>
<view:looknfeel/>
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
     out.println(increment+"<img src="+resource.getIcon("pdcPeas.target")+" width=\"15\" align=\"absmiddle\">&nbsp;<a class=\"axe\" href=\"javaScript:addValues('"+valueId+"');\">&nbsp;&nbsp;&nbsp;"+EncodeHelper.javaStringToHtmlString(valueName)+"&nbsp;&nbsp;&nbsp;</a><img src="+resource.getIcon("pdcPeas.noColorPix")+" width=\"18\" align=\"absmiddle\"><BR>");
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