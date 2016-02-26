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
List<UsedAxis> axisList = (List<UsedAxis>) request.getAttribute("AxisList"); // a list of used axis
String componentId = (String) request.getAttribute("ComponentId");

// initialisation of variables of main loop (show all axes)
String usedAxisId = null;
String usedAxisType = null;
int usedAxisMandatory = -1;
int usedAxisVariant = -1;
ArrayLine arrayLine = null;
IconPane iconPane1 = null;
Icon aspiIcon = null;

%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<view:looknfeel withCheckFormScript="true"/>
<script type="text/javascript">

	// IE / Netscape compliant

	// this method opens a pop-up which warns the user
	function areYouSure(){
		return confirm("<%=resource.getString("pdcPeas.confirmDeleteAxis")%>");
	}

	// this function get all checked boxes by the user and sent
	// data to the router
	function getSelectedItems(){
		var boxItems = document.usedAxis.deleteAxis;
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
				document.usedAxis.Ids.value = selectItems;
				document.usedAxis.action = "<%=pdcUtilizationContext%>UtilizationDeleteAxis";
				document.usedAxis.submit();
			}
		}
	}

	// This function open a silverpeas window
	function openSPWindow(fonction,windowName){
		SP_openWindow(fonction, windowName, '600', '300','scrollbars=yes, resizable, alwaysRaised');
	}

</script>
</head>
<body>
	<form name="usedAxis" action="<%=pdcUtilizationContext%>UtilizationView" method="post">
	<input type="hidden" name="Ids"/>
<%
	browseBar.setComponentId(componentId);
	browseBar.setPath(resource.getString("pdcPeas.paramUsedAxis"));

	operationPane.addOperation(resource.getIcon("pdcPeas.icoCreateParamAxis"),resource.getString("pdcPeas.paramChooseAxis"), pdcUtilizationContext+"UtilizationViewAxis");
	if (axisList != null && axisList.size() != 0) // do not show this icone if no axes
		operationPane.addOperation(resource.getIcon("pdcPeas.icoDeleteParamAxis"),resource.getString("pdcPeas.deleteAxis"), "javascript:getSelectedItems()");

    out.println(window.printBefore());
%>
<view:frame>
<view:areaOfOperationOfCreation/>
<%
    ArrayPane arrayPane = gef.getArrayPane("PdcPeas", "", request, session);
	ArrayColumn arrayColumn1 = arrayPane.addArrayColumn(resource.getString("GML.type"));
	arrayColumn1.setSortable(false);
    ArrayColumn arrayColumn2 = arrayPane.addArrayColumn(resource.getString("GML.name"));
	arrayColumn2.setSortable(false);
	ArrayColumn arrayColumn3 = arrayPane.addArrayColumn(resource.getString("pdcPeas.baseValue"));
	arrayColumn3.setSortable(false);
	ArrayColumn arrayColumn4 = arrayPane.addArrayColumn(resource.getString("GML.requiredField"));
	arrayColumn4.setSortable(false);
    if (isAxisInvarianceUsed) {
      ArrayColumn arrayColumn5 = arrayPane.addArrayColumn(resource.getString("pdcPeas.variant"));
      arrayColumn5.setSortable(false);
    }
	ArrayColumn arrayColumn6 = arrayPane.addArrayColumn(resource.getString("pdcPeas.axisOperation"));
    arrayColumn6.setSortable(false);

	// main loop to show all axis
	for (UsedAxis usedAxis : axisList) {
			usedAxisId = usedAxis.getPK().getId();
			usedAxisType = usedAxis._getAxisType();
			usedAxisMandatory = usedAxis.getMandatory();
			usedAxisVariant = usedAxis.getVariant();

            arrayLine = arrayPane.addArrayLine();

			if (usedAxisType.equals("P"))
				arrayLine.addArrayCellText(cellCenterStart+"<img src=\""+resource.getIcon("pdcPeas.icoPrimaryAxis")+"\" alt=\""+resource.getString("pdcPeas.primaryAxis")+"\" title=\""+resource.getString("pdcPeas.primaryAxis")+"\">"+cellCenterEnd);
	        else
				arrayLine.addArrayCellText(cellCenterStart+"<img src=\""+resource.getIcon("pdcPeas.icoSecondaryAxis")+"\" alt=\""+resource.getString("pdcPeas.secondaryAxis")+"\" title=\""+resource.getString("pdcPeas.secondaryAxis")+"\">"+cellCenterEnd);

			arrayLine.addArrayCellText("<a href=\""+pdcUtilizationContext+"UtilizationEditAxis?Id="+usedAxisId+"\" title=\""+resource.getString("pdcPeas.axisUtilizationParameter")+" : "+EncodeHelper.javaStringToHtmlString(usedAxis._getAxisName())+"\"><span class=textePetitBold>"+EncodeHelper.javaStringToHtmlString(usedAxis._getAxisName())+"</a></span>");

			arrayLine.addArrayCellText(usedAxis._getBaseValueName());

			if (usedAxisMandatory == 1)
				arrayLine.addArrayCellText(cellCenterStart+"<img src=\""+resource.getIcon("pdcPeas.bulet")+"\" alt=\""+resource.getString("GML.requiredField")+"\" title=\""+resource.getString("GML.requiredField")+"\">"+cellCenterEnd);
	        else
				arrayLine.addArrayCellText("");

            if (isAxisInvarianceUsed) {
		    if (usedAxisVariant == 1)
				arrayLine.addArrayCellText(cellCenterStart+"<img src=\""+resource.getIcon("pdcPeas.bulet")+"\" alt=\""+resource.getString("pdcPeas.variant")+"\" title=\""+resource.getString("pdcPeas.variant")+"\">"+cellCenterEnd);
                else
			arrayLine.addArrayCellText("");
            }

			arrayLine.addArrayCellText(cellCenterStart+"<input type=checkbox name=deleteAxis value="+usedAxisId+">"+cellCenterEnd);

	}

    out.println(arrayPane.print());

    ButtonPane buttonPane = gef.getButtonPane();
	buttonPane.addButton(gef.getFormButton(resource.getString("GML.close"), "javascript:window.close()", false));
    out.println("<br/>"+buttonPane.print());
%>
	</view:frame>
<%
	out.println(window.printAfter());
%>
</form>
<form name="refresh" action="<%=pdcUtilizationContext%>Main" method="post"></form>
</body>
</html>