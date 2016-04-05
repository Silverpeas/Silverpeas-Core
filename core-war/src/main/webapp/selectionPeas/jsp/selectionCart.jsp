<%@ page import="org.silverpeas.core.util.Pair" %>
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

<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<%@ include file="check.jsp" %>

<%
  String setText = (String) request.getAttribute("setText");
  String elementText = (String) request.getAttribute("elementText");
  String componentURL = (String) request.getAttribute("myComponentURL");
  PanelOperation[] operationsToDisplay = (PanelOperation[]) request.getAttribute(
      "operationsToDisplay");
  boolean isMultiSelect = ((Boolean) request.getAttribute("isMultiSelect")).booleanValue();
  boolean isZoomToSetValid = ((Boolean) request.getAttribute("isZoomToSetValid")).booleanValue();
  boolean isZoomToElementValid = ((Boolean) request.getAttribute("isZoomToElementValid")).booleanValue();

  boolean isSetSelectable = ((Boolean) request.getAttribute("isSetSelectable")).booleanValue();
  boolean isElementSelectable = ((Boolean) request.getAttribute("isElementSelectable")).booleanValue();
  PanelLine[] elementsToDisplay = (PanelLine[]) request.getAttribute("elementsToDisplay");
  PanelLine[] setsToDisplay = (PanelLine[]) request.getAttribute("setsToDisplay");
  String[] setsColumnsHeader = (String[]) request.getAttribute("setsColumnsHeader");
  String[] elementsColumnsHeader = (String[]) request.getAttribute("elementsColumnsHeader");
  String setMiniFilterSelect = (String) request.getAttribute("setMiniFilterSelect");
  String elementMiniFilterSelect = (String) request.getAttribute("elementMiniFilterSelect");
  boolean toPopup = ((Boolean) request.getAttribute("ToPopup")).booleanValue();

  Pair<String, String> hostComponentName = (Pair<String, String>) request.getAttribute("HostComponentName");
  String hostSpaceName = (String) request.getAttribute("HostSpaceName");
  Pair<String, String>[] hostPath = (Pair<String, String>[]) request.getAttribute("HostPath");

  int i, j, c;
  ArrayPane arrayPane;
  ArrayColumn arrayColumn;
  String isChecked = "";

  browseBar.setDomainName(hostSpaceName);
  //pour construire le path
  StringBuffer strPath = new StringBuffer("");
  if (toPopup) {
    browseBar.setComponentName((String) hostComponentName.getFirst());
    for (i = 0; i < hostPath.length; i++) {
      strPath.append((String) (hostPath[i].getFirst()));
      if ((i + 1) < hostPath.length) {
        strPath.append(" &gt ");
      }
    }
  } else {
    String theLink = (String) (hostComponentName.getSecond());
    if ((theLink == null) || (theLink.trim().length() <= 0)) {
      browseBar.setComponentName((String) (hostComponentName.getFirst()));
    } else {
      browseBar.setComponentName((String) (hostComponentName.getFirst()),
          (String) (hostComponentName.getSecond()));
    }
    for (i = 0; i < hostPath.length; i++) {
      theLink = (String) (hostPath[i].getSecond());
      if ((theLink == null) || (theLink.trim().length() <= 0)) {
        strPath.append((String) (hostPath[i].getFirst()));
        if ((i + 1) < hostPath.length) {
          strPath.append(" &gt ");
        }
      } else {
        strPath.append("<a href=\"");
        strPath.append(m_context);
        strPath.append((String) (hostPath[i].getSecond()));
        strPath.append("\">");
        strPath.append((String) (hostPath[i].getFirst()));
        strPath.append("</a>");
        if ((i + 1) < hostPath.length) {
          strPath.append(" &gt ");
        }
      }
    }
  }
  browseBar.setPath(strPath.toString());

  if (operationsToDisplay != null) {
    for (c = 0; c < operationsToDisplay.length; c++) {
      if ((operationsToDisplay[c].m_command == null) || (operationsToDisplay[c].m_command.length() <= 0)) {
        operationPane.addLine();
      } else {
        operationPane.addOperation(operationsToDisplay[c].m_icon,
            operationsToDisplay[c].m_helpString,
            "javascript:submitOperation('" + operationsToDisplay[c].m_command + "','" + operationsToDisplay[c].m_confirmation + "')");
      }
    }
  }

%>
<html>
<head>
  <title><%=resource.getString("GML.popupTitle")%></title>
  <view:looknfeel withCheckFormScript="true"/>
  <script language="JavaScript">
    function popupZoomToItem(theHref) {
      var theWindow = SP_openWindow('about:blank', 'OpenZoomSelection', '500', '500',
          'menubar=no,scrollbars=yes,statusbar=no');
      theWindow.location.href = theHref;
    }

    function submitOperation(theOperation, confirmMsg) {
      document.operationForm.SelectedSets.value = getObjects("set", true);
      document.operationForm.NonSelectedSets.value = getObjects("set", false);

      document.operationForm.SelectedElements.value = getObjects("element", true);
      document.operationForm.NonSelectedElements.value = getObjects("element", false);

      if (confirmMsg.length > 0) {
        if (window.confirm(confirmMsg)) {
          document.operationForm.SpecificOperation.value = theOperation;
          document.operationForm.submit();
        }
      }
      else {
        document.operationForm.SpecificOperation.value = theOperation;
        document.operationForm.submit();
      }
    }

    function selectAll(type) {
      var isChecked = false;
      eval("isChecked = window.document.operationForm." + type + "All.checked;");

      var boxItems;
      eval("boxItems = window.document.operationForm." + type + ";");
      if (boxItems != null) {
        if (isChecked) {
          for (i = 0; i < boxItems.length; i++) {
            if (!boxItems[i].checked) {
              boxItems[i].checked = true;
            }
          }
        }
        else {
          for (i = 0; i < boxItems.length; i++) {
            if (boxItems[i].checked) {
              boxItems[i].checked = false;
            }
          }
        }
      }
    }

    function changeSetsPage(index) {
      changePage(index, "sets");
    }

    function changeElementsPage(index) {
      changePage(index, "elements");
    }

    function changePage(index, arrayName) {
      document.operationForm.SelectedSets.value = getObjects("set", true);
      document.operationForm.NonSelectedSets.value = getObjects("set", false);

      document.operationForm.SelectedElements.value = getObjects("element", true);
      document.operationForm.NonSelectedElements.value = getObjects("element", false);

      document.operationForm.SpecificOperation.value = "GENERICPANELChangePage";
      document.operationForm.<%=ArrayPane.TARGET_PARAMETER_NAME%>.value = arrayName;
      document.operationForm.<%=ArrayPane.INDEX_PARAMETER_NAME%>.value = index;
      document.operationForm.<%=ArrayPane.ACTION_PARAMETER_NAME%>.value = "ChangePage";

      document.operationForm.submit();
    }

    function getObjects(type, selected) {
      var boxItems;
      eval("boxItems = window.document.operationForm." + type + ";");

      var items = "";
      if (boxItems != null) {
        // au moins une checkbox exist
        var nbBox = boxItems.length;
        if ((nbBox == null) && (boxItems.checked == selected)) {
          // il n'y a qu'une checkbox non selectionn�e
          items += boxItems.value + ",";
        } else {
          // search not checked boxes
          for (i = 0; i < boxItems.length; i++) {
            if (boxItems[i].checked == selected) {
              items += boxItems[i].value + ",";
            }
          }
        }
      }
      return items;
    }
  </script>
</HEAD>
<BODY>
<%
  out.println(window.printBefore());
  out.println(frame.printBefore());
%>
<br/>

<form action="<%=componentURL%>CartDoOperation" name="operationForm">
  <input type="hidden" name="SpecificOperation" value=""/>
  <input type="hidden" name="setNB" value="<%=setsToDisplay.length%>"/>
  <input type="hidden" name="elementNB" value="<%=elementsToDisplay.length%>"/>
  <input type="hidden" name="<%=ArrayPane.INDEX_PARAMETER_NAME%>" value=""/>
  <input type="hidden" name="<%=ArrayPane.TARGET_PARAMETER_NAME%>" value=""/>
  <input type="hidden" name="<%=ArrayPane.ACTION_PARAMETER_NAME%>" value=""/>
  <input type="hidden" name="SelectedSets" value=""/>
  <input type="hidden" name="NonSelectedSets" value=""/>
  <input type="hidden" name="SelectedElements" value=""/>
  <input type="hidden" name="NonSelectedElements" value=""/>
  <% // ......................................................................................................................................... %>
  <% //.........................................................Display Sets..................................................................... %>
  <% //.......................................................................................................................................... %>
  <% if (isSetSelectable) {
    // Affiche un tableau
    arrayPane = gef.getArrayPane("sets", componentURL + "CartDoOperation", request, session);
    // Affiche le nombre de ligne total du tableau dans la m�me page
    arrayPane.setVisibleLineNumber(SelectionPeasSettings.setByBrowsePage);
    arrayPane.setTitle(setText);
    arrayPane.setPaginationJavaScriptCallback("changeSetsPage");

    arrayColumn = null;
    // D�finition des ent�tes de colonnes
    arrayColumn = arrayPane.addArrayColumn(setMiniFilterSelect + "&nbsp;");
    arrayColumn.setSortable(false);
    arrayColumn.setWidth("40px");
    arrayColumn.setAlignement("center");
    for (c = 0; c < setsColumnsHeader.length; c++) {
      arrayColumn = arrayPane.addArrayColumn(setsColumnsHeader[c]);
      arrayColumn.setSortable(false);
    }

    for (i = 0; i < setsToDisplay.length; i++) {
      ArrayLine arrayLine = arrayPane.addArrayLine();

      if (setsToDisplay[i].m_Selected) {
        isChecked = " checked";
      } else {
        isChecked = "";
      }
      ArrayCellText cell0 = arrayLine.addArrayCellText(
          "<input type=\"checkbox\" name=\"set\" value=\"" + setsToDisplay[i].m_Id + "\"" + isChecked + "/>");
      cell0.setAlignement("center");
      if (setsToDisplay[i].m_HighLight) {
        arrayLine.setStyleSheet("ArrayCellHot");
      }
      for (c = 0; c < setsColumnsHeader.length; c++) {
        arrayLine.addArrayCellLink(setsToDisplay[i].m_Values[c],
            "javascript:popupZoomToItem('" + componentURL + "ZoomToSetInfos?elementId=" + setsToDisplay[i].m_Id + "')");
      }
    }
    out.println(arrayPane.print());
  }
  %>
  <br/>
  <% // ......................................................................................................................................... %>
  <% //.........................................................Display Elements................................................................. %>
  <% //.......................................................................................................................................... %>
  <%
    if (isElementSelectable) {
      // Affiche un tableau
      arrayPane = gef.getArrayPane("elements", componentURL + "CartDoOperation", request, session);
      // Affiche le nombre de ligne total du tableau dans la m�me page
      arrayPane.setVisibleLineNumber(SelectionPeasSettings.elementByBrowsePage);
      arrayPane.setTitle(elementText);
      arrayPane.setPaginationJavaScriptCallback("changeElementsPage");

      // D�finition des ent�tes de colonnes
      arrayColumn = arrayPane.addArrayColumn(elementMiniFilterSelect + "&nbsp;");
      arrayColumn.setSortable(false);
      arrayColumn.setWidth("40px");
      arrayColumn.setAlignement("center");
      for (c = 0; c < elementsColumnsHeader.length; c++) {
        arrayColumn = arrayPane.addArrayColumn(elementsColumnsHeader[c]);
        arrayColumn.setSortable(false);
      }

      for (i = 0; i < elementsToDisplay.length; i++) {
        ArrayLine arrayLine = arrayPane.addArrayLine();

        if (elementsToDisplay[i].m_Selected) {
          isChecked = " checked";
        } else {
          isChecked = "";
        }
        ArrayCellText cell1 = arrayLine.addArrayCellText(
            "<input type=\"checkbox\" name=\"element\" value=\"" + elementsToDisplay[i].m_Id + "\"" + isChecked + "/>");
        cell1.setAlignement("center");
        if (elementsToDisplay[i].m_HighLight) {
          arrayLine.setStyleSheet("ArrayCellHot");
        }
        for (c = 0; c < elementsColumnsHeader.length; c++) {
          arrayLine.addArrayCellLink(elementsToDisplay[i].m_Values[c],
              "javascript:popupZoomToItem('" + componentURL + "ZoomToElementInfos?elementId=" + elementsToDisplay[i].m_Id + "')");
        }
      }
      out.println(arrayPane.print());
    }
  %>
</form>
  <%
    ButtonPane buttonPane = gef.getButtonPane();
    buttonPane.addButton((Button) gef.getFormButton(resource.getString("GML.validate"),
        "javascript:submitOperation('Validate','')", false));
    buttonPane.addButton((Button) gef.getFormButton(resource.getString("GML.cancel"),
        "javascript:submitOperation('Cancel','')", false));
    out.println(buttonPane.print());
  %>
<%
  out.println(frame.printAfter());
  out.println(window.printAfter());
%>
</body>
</html>