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
String viewType = (String) request.getAttribute("ViewType"); // type d'axes � visualiser P(rimaire) ou S(econdaire)
List axisList = (List) request.getAttribute("AxisList"); // a list of axis header
AxisHeader axisHeader = null;
String axisId = null;
Iterator it = axisList.iterator();
ArrayLine arrayLine = null;
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<HEAD>
<TITLE><%=resource.getString("GML.popupTitle")%></TITLE>
<view:looknfeel/>
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

                arrayLine.addArrayCellLink("<div align=left><img src=\""+resource.getIcon("pdcPeas.icoComponent")+"\" border=0 alt=\""+resource.getString("pdcPeas.viewAxis")+" : "+EncodeHelper.javaStringToHtmlString(axisHeader.getName())+"\" title=\""+resource.getString("pdcPeas.viewAxis")+" : "+EncodeHelper.javaStringToHtmlString(axisHeader.getName())+"\"></div>", "vsicAddTree?Id="+axisId);
                arrayLine.addArrayCellText("<span class=textePetitBold>"+EncodeHelper.javaStringToHtmlString(axisHeader.getName())+"</span>");
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