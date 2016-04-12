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

// recuperation des parametres
String 	viewType 		= (String) request.getAttribute("ViewType"); // type d'axes � visualiser P(rimaire) ou S(econdaire)
List 	axisList 		= (List) request.getAttribute("AxisList"); // a list of axis header
String 	creationAllowed = (String) request.getAttribute("CreationAllowed"); // String to authorize the creation
String 	displayLanguage = (String) request.getAttribute("DisplayLanguage");

boolean isAdmin 		= ((Boolean) request.getAttribute("IsAdmin")).booleanValue();
List	manageableAxis 	= (List) request.getAttribute("ManageableAxis");

// initialisation of variables of main loop (show all axes)
AxisHeader axisHeader = null;
String axisId = null;
Iterator it = axisList.iterator();
ArrayLine arrayLine = null;

%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title><%=resource.getString("GML.popupTitle")%></title>
<view:looknfeel withCheckFormScript="true"/>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/i18n.js"></script>
<script type="text/javascript">
	// this method opens a pop-up which warns the user
	function areYouSure(){
		return confirm("<%=resource.getString("pdcPeas.confirmDeleteAxis")%>");
	}

	// this function get all checked boxes by the user and sent
	// data to the router
	function getSelectedItems(){
		var boxItems = document.viewAxis.deleteAxis;
		var  selectItems = "";
		if (boxItems != null){
			// au moins une checkbox exist
			var nbBox = boxItems.length;
			if ( (nbBox == null) && (boxItems.checked == true) ){
				// il n'y a qu'une checkbox selectionn�e
				selectItems += boxItems.value;
			} else{
				// search checked boxes
				for (i=0;i<boxItems.length ;i++ ){
					if (boxItems[i].checked == true){
						selectItems += boxItems[i].value+",";
					}
				}
				selectItems = selectItems.substring(0,selectItems.length-1); // erase the last coma
			}
			if ( (selectItems.length > 0) && (areYouSure())  ){
				// an axis has been selected !
				document.viewAxis.Ids.value = selectItems;
				document.viewAxis.action = "DeleteAxis";
				document.viewAxis.submit();
			}
		}
	}

	// This function open a silverpeas window
	function openSPWindow(fonction,windowName){
		SP_openWindow(fonction, windowName, '700', '600','scrollbars=yes, resizable, alwaysRaised');
	}
</script>
</head>
<body>
	<form name="viewAxis" action="Main" method="post">
	<input type="hidden" name="Ids"/>
<%
	browseBar.setDomainName(resource.getString("pdcPeas.pdc"));
    browseBar.setComponentName(resource.getString("pdcPeas.pdcDefinition"));
    browseBar.setI18N("ChangeLanguage", displayLanguage);

	if(isAdmin && creationAllowed.equals("1")) {
		operationPane.addOperationOfCreation(resource.getIcon("pdcPeas.icoCreateAxis"),resource.getString("pdcPeas.createAxis"), "javascript:openSPWindow('NewAxis','newaxis')");
	}
	if (isAdmin && axisList != null && axisList.size() != 0) {
		// do not show this icone if no axes
		operationPane.addOperation(resource.getIcon("pdcPeas.icoDeleteAxis"),resource.getString("pdcPeas.deleteAxis"), "javascript:getSelectedItems()");
	}

    out.println(window.printBefore());
%>
<view:areaOfOperationOfCreation/>
<%
	TabbedPane tabbedPane = gef.getTabbedPane();
	tabbedPane.addTab(resource.getString("pdcPeas.primaryAxis"),"ChangeViewType?ViewType=P",viewType.equals("P"));
    tabbedPane.addTab(resource.getString("pdcPeas.secondaryAxis"),"ChangeViewType?ViewType=S",viewType.equals("S"));
	out.println(tabbedPane.print());
%>
<view:frame>
<center>
    <%
    ArrayPane arrayPane = gef.getArrayPane("PdcPeas", "Main", request, session);

    ArrayColumn arrayColumn0 =  arrayPane.addArrayColumn("&nbsp;");
	arrayColumn0.setSortable(false);

    ArrayColumn arrayColumn1 =  arrayPane.addArrayColumn(resource.getString("pdcPeas.axisName"));
	arrayColumn1.setSortable(false);

	ArrayColumn arrayColumn3 =  arrayPane.addArrayColumn(resource.getString("pdcPeas.definition"));
	arrayColumn3.setSortable(false);

	ArrayColumn arrayColumn2 =  arrayPane.addArrayColumn(resource.getString("pdcPeas.axisOperation"));
	arrayColumn2.setSortable(false);

	String name = "";
	String description = "";

	// main loop to show all axis
	while (it.hasNext()){
			axisHeader = (AxisHeader) it.next();
			axisId = axisHeader.getPK().getId();
			name = axisHeader.getName(displayLanguage);
			description = axisHeader.getDescription(displayLanguage);

            arrayLine = arrayPane.addArrayLine();

			arrayLine.addArrayCellLink("<div align=center><img src=\""+resource.getIcon("pdcPeas.icoComponent")+"\" alt=\""+resource.getString("pdcPeas.viewAxis")+" : "+EncodeHelper.javaStringToHtmlString(name)+"\" title=\""+resource.getString("pdcPeas.viewAxis")+" : "+EncodeHelper.javaStringToHtmlString(name)+"\"/></div>", "ViewAxis?Id="+axisId).setValignement("top");
		arrayLine.addArrayCellText("<a href=\"ViewAxis?Id="+axisId+"\" title=\""+resource.getString("pdcPeas.viewAxis")+" : "+EncodeHelper.javaStringToHtmlString(name)+"\"><span class=\"textePetitBold\">"+EncodeHelper.javaStringToHtmlString(name)+"</span></a>").setValignement("top");

			arrayLine.addArrayCellText("<span class=\"textePetitBold\">"+EncodeHelper.javaStringToHtmlParagraphe(description)+"</span>").setValignement("top");

			if (isAdmin)
			{
				IconPane iconPane = gef.getIconPane();
			Icon updateIcon = iconPane.addIcon();
			updateIcon.setProperties(resource.getIcon("pdcPeas.update"), resource.getString("pdcPeas.editAxis")+ " : "+EncodeHelper.javaStringToHtmlString(name) , "javascript:openSPWindow('EditAxis?Id="+axisId+"&Translation="+axisHeader.getLanguage()+"','editaxis')");
			arrayLine.addArrayCellText(updateIcon.print()+"&nbsp;&nbsp;&nbsp;&nbsp;<input type=\"checkbox\" name=\"deleteAxis\" value=\""+axisId+"\"/>");
			}
			else if (manageableAxis != null && manageableAxis.contains(axisId))
			{
				IconPane iconPane = gef.getIconPane();
			Icon updateIcon = iconPane.addIcon();
			updateIcon.setProperties(resource.getIcon("pdcPeas.update"), resource.getString("pdcPeas.editAxis")+ " : "+EncodeHelper.javaStringToHtmlString(name) , "javascript:openSPWindow('EditAxis?Id="+axisId+"&Translation="+axisHeader.getLanguage()+"','editaxis')");
			arrayLine.addArrayCellText(updateIcon.print());
			}
			else
			{
				arrayLine.addArrayEmptyCell();
			}
	}

    out.println(arrayPane.print());
    %>

</center>
</view:frame>
<%
out.println(window.printAfter());
%>
</form>
<form name="refresh" action="Main" method="post"></form>
</body>
</html>