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

<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<%@ include file="check.jsp" %>

<%
    String setText = (String)request.getAttribute("setText");
    String elementText = (String)request.getAttribute("elementText");
    String componentURL = (String)request.getAttribute("myComponentURL");
    PanelOperation[] operationsToDisplay = (PanelOperation[])request.getAttribute("operationsToDisplay");
    boolean isMultiSelect = ((Boolean)request.getAttribute("isMultiSelect")).booleanValue();
    boolean isZoomToSetValid = ((Boolean)request.getAttribute("isZoomToSetValid")).booleanValue();
    boolean isZoomToElementValid = ((Boolean)request.getAttribute("isZoomToElementValid")).booleanValue();
    String selectedNumber = (String)request.getAttribute("selectedNumber");
    int c;

    boolean isSetSelectable = ((Boolean)request.getAttribute("isSetSelectable")).booleanValue();
    boolean isElementSelectable = ((Boolean)request.getAttribute("isElementSelectable")).booleanValue();
    PanelLine[] elementsToDisplay = (PanelLine[])request.getAttribute("elementsToDisplay");
    boolean isDisplaySets = ((Boolean)request.getAttribute("isDisplaySets")).booleanValue();
    PanelLine[] setsToDisplay = (PanelLine[])request.getAttribute("setsToDisplay");
    String[] setsColumnsHeader = (String[])request.getAttribute("setsColumnsHeader");
    String[] elementsColumnsHeader = (String[])request.getAttribute("elementsColumnsHeader");
    String setMiniFilterSelect = (String)request.getAttribute("setMiniFilterSelect");
    String elementMiniFilterSelect = (String)request.getAttribute("elementMiniFilterSelect");

	boolean          toPopup = ((Boolean)request.getAttribute("ToPopup")).booleanValue();

    Pair<String, String> hostComponentName = (Pair<String, String>)request.getAttribute("HostComponentName");
    String           hostSpaceName = (String)request.getAttribute("HostSpaceName");
	Pair<String, String>[]     hostPath = (Pair<String, String>[])request.getAttribute("HostPath");

    PanelLine[]      setPath = (PanelLine[])request.getAttribute("SetPath");

    browseBar.setDomainName(hostSpaceName);
    //pour construire le path
    StringBuffer strPath = new StringBuffer("");

    if (toPopup)
    {
        browseBar.setComponentName((String)hostComponentName.getFirst());
        if (hostPath != null)
        {
	        for (int i = 0; i < hostPath.length; i++)
	        {
	            strPath.append((String)(hostPath[i].getFirst()));
	            strPath.append(" &gt ");
	        }
        }
    }
    else
    {
        String theLink = (String)(hostComponentName.getSecond());
        if ((theLink == null) || (theLink.trim().length() <= 0))
        {
            browseBar.setComponentName((String)(hostComponentName.getFirst()));
        }
        else
        {
            browseBar.setComponentName((String)(hostComponentName.getFirst()), (String)(hostComponentName.getSecond()));
        }
        for(int i =0; i<hostPath.length;i++){
            theLink = (String)(hostPath[i].getSecond());
            if ((theLink == null) || (theLink.trim().length() <= 0))
            {
                strPath.append((String)(hostPath[i].getFirst()));
                strPath.append(" &gt ");
            }
            else
            {
                strPath.append("<a href=\"");
                strPath.append(m_context);
                strPath.append((String)(hostPath[i].getSecond()));
                strPath.append("\">");
                strPath.append((String)(hostPath[i].getFirst()));
                strPath.append("</a>");
                strPath.append(" &gt ");
            }
        }
    }
    if (isZoomToSetValid)
    {
	    strPath.append("<a href=\"");
	    strPath.append("javascript:submitOperation('GENERICPANELZOOMTOSET','','','')");
	    strPath.append("\">");
	    strPath.append(resource.getString("selectionPeas.rootName"));
	    strPath.append("</a>");
	    for(int i =0; i<setPath.length;i++){
	        strPath.append(" &gt ");
	        strPath.append("<a href=\"");
	        strPath.append("javascript:submitOperation('GENERICPANELZOOMTOSET','','" + setPath[i].m_Id + "','')");
	        strPath.append("\">");
	        strPath.append(setPath[i].m_Values[0]);
	        strPath.append("</a>");
	    }
    }

    browseBar.setPath(strPath.toString());

    if (operationsToDisplay != null)
    {
        for (c = 0; c < operationsToDisplay.length; c++)
        {
            if ((operationsToDisplay[c].m_command == null) || (operationsToDisplay[c].m_command.length() <= 0))
                operationPane.addLine();
            else
                operationPane.addOperation(operationsToDisplay[c].m_icon,operationsToDisplay[c].m_helpString,"javascript:submitOperation('" + operationsToDisplay[c].m_command + "','" + operationsToDisplay[c].m_confirmation + "')");
        }
    }
%>

<html>
<head>
<title><%=resource.getString("GML.popupTitle")%></title>
<view:looknfeel/>
<script language="JavaScript">
	function selectAll(type)
	{
		var isChecked = false;
	    eval("isChecked = window.document.operationForm." + type + "All.checked;");

	    var boxItems;
	    eval("boxItems = window.document.operationForm." + type + ";");
	    if (boxItems != null){
		    if(isChecked)
		    {
				for (i=0;i<boxItems.length ;i++ ){
					if (!boxItems[i].checked){
						boxItems[i].checked=true;
						addToSelectedNumber(1);
					}
				}
		    }
		    else
		    {
				for (i=0;i<boxItems.length ;i++ ){
					if (boxItems[i].checked){
						boxItems[i].checked=false;
						addToSelectedNumber(-1);
					}
				}
		    }
	    }
	}

	function selectUnselect(obj)
	{
	    var bSelected = obj.checked;

	    if (bSelected)
	    {
		addToSelectedNumber(1);
	    }
	    else
	    {
		addToSelectedNumber(-1);
	    }
	}

	function addToSelectedNumber(number)
	{
		document.operationForm.selectedNumber.value = parseInt(document.operationForm.selectedNumber.value) + number;
	}

	function submitOperation(theOperation,confirmMsg)
	{
	    submitOperation(theOperation,confirmMsg,'','');
	}

	function submitOperation(theOperation,confirmMsg,setId,elementId)
	{
		document.operationForm.SelectedSets.value = getObjects("set", true);
		document.operationForm.NonSelectedSets.value = getObjects("set", false);

		document.operationForm.SelectedElements.value = getObjects("element", true);
		document.operationForm.NonSelectedElements.value = getObjects("element", false);

	    if (confirmMsg.length > 0)
	    {
	        if (window.confirm(confirmMsg))
	        {
	            document.operationForm.SpecificOperation.value = theOperation;
	            document.operationForm.setId.value = setId;
	            document.operationForm.elementId.value = elementId;
	            document.operationForm.submit();
	        }
	    }
	    else
	    {
	        document.operationForm.SpecificOperation.value = theOperation;
	        document.operationForm.setId.value = setId;
	        document.operationForm.elementId.value = elementId;
	        document.operationForm.submit();
	    }
	}

	function changeSetsPage(index)
	{
		changePage(index, "sets");
	}

	function changeElementsPage(index)
	{
		changePage(index, "elements");
	}

	function changePage(index, arrayName)
	{
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

	function getObjects(type, selected)
	{
		var boxItems;
	    eval("boxItems = window.document.operationForm." + type + ";");

		var  items = "";
		if (boxItems != null){
			// au moins une checkbox exist
			var nbBox = boxItems.length;
			if ( (nbBox == null) && (boxItems.checked == selected) ){
				// il n'y a qu'une checkbox non selectionn�e
				items += boxItems.value+",";
			} else{
				// search not checked boxes
				for (i=0;i<boxItems.length ;i++ ){
					if (boxItems[i].checked == selected){
						items += boxItems[i].value+",";
					}
				}
			}
		}
		return items;
	}

<%
	boolean useExternalFunctionName = false;
	String[] referenceColumnsNames = new String[0];
	String externalFunctionName = (String)request.getAttribute("externalFunctionName");
	if (externalFunctionName != null && externalFunctionName.length() > 0)
	{
		useExternalFunctionName = true;
		int originFormIndex = ((Integer)request.getAttribute("originFormIndex")).intValue();

		String params = (String)request.getAttribute("originFieldsNames");
		StringTokenizer st = new StringTokenizer(params, "#");
		String[] originFieldsNames = new String[st.countTokens()];
		int index = 0;
		while (st.hasMoreTokens())
		{
			originFieldsNames[index] = st.nextToken();
			index++;
		}

		params = (String)request.getAttribute("referenceColumnsNames");
		st = new StringTokenizer(params, "#");
		referenceColumnsNames = new String[st.countTokens()];
		index = 0;
		while (st.hasMoreTokens())
		{
			referenceColumnsNames[index] = st.nextToken();
			index++;
		}
%>
	function updateExternalFields(values)
	{
		if (window.opener != null)
		{<%

		for (int i = 0, n = originFieldsNames.length; i < n; i++)
		{
%>
			window.opener.<%=externalFunctionName%>(<%=originFormIndex%>, "<%=originFieldsNames[i]%>", values[<%=i%>]);<%

		}
%>
			setTimeout("window.close();", 300);
		}
		else
		{
			window.close();
		}
	}

	function checkParentFrame()
	{
		window.setInterval("checkParentFrameInterval()", 1000);
	}

	function checkParentFrameInterval()
	{
		if ((window.opener == null) || (window.opener.<%=externalFunctionName%> == null))
		{
			window.close();
		}
	}
<%
	}
%>
</script>
</head>
<body <%if (useExternalFunctionName) {%>onload="checkParentFrame()"<%}%>>
<%
	out.println(window.printBefore());
	out.println(frame.printBefore());
%>
<center>
<form action="<%=componentURL%>BrowseOperation" name="operationForm" method="POST">
  <input type="hidden" name="SpecificOperation" value=""/>
  <input type="hidden" name="setId" value=""/>
  <input type="hidden" name="elementId" value=""/>
  <input type="hidden" name="setNB" value="<%=SelectionPeasSettings.setByBrowsePage%>"/>
  <input type="hidden" name="elementNB" value="<%=SelectionPeasSettings.elementByBrowsePage%>"/>
  <input type="hidden" name="<%=ArrayPane.INDEX_PARAMETER_NAME%>" value=""/>
  <input type="hidden" name="<%=ArrayPane.TARGET_PARAMETER_NAME%>" value=""/>
  <input type="hidden" name="<%=ArrayPane.ACTION_PARAMETER_NAME%>" value=""/>
  <input type="hidden" name="SelectedSets" value=""/>
  <input type="hidden" name="NonSelectedSets" value=""/>
  <input type="hidden" name="SelectedElements" value=""/>
  <input type="hidden" name="NonSelectedElements" value=""/>

<% // ......................................................................................................................................... %>
<% //.........................................................Display Header................................................................... %>
<% //.......................................................................................................................................... %>
  <%
        if (isMultiSelect)
        {
          Board board = gef.getBoard();
          out.print(board.printBefore());
  %>
		<table cellpadding="2" cellspacing="0" border="0" width="100%">
		<tr>
		<td nowrap="nowrap"><span class="txtlibform"><%=resource.getString("selectionPeas.selectedNumber")%> :</span></td>
                <td width="100%"><input type="text" value ="<%=selectedNumber%>" name="selectedNumber" size="7" maxlength="10"/></td>
            </tr>
        </table>
  <%
			out.print(board.printAfter());
			out.print("<br/><br/>");
        }
  %>
<% // ......................................................................................................................................... %>
<% //.........................................................Display Sets..................................................................... %>
<% //.......................................................................................................................................... %>
<%
      ArrayPane arrayPane;
      ArrayColumn arrayColumn = null;
      String isChecked = "";

      if (isDisplaySets)
      {
		  // Affiche un tableau
          arrayPane = gef.getArrayPane("sets", componentURL+"BrowseOperation", request,session);
		  // Affiche le nombre de ligne total du tableau dans la m�me page
		  arrayPane.setVisibleLineNumber(SelectionPeasSettings.setByBrowsePage);
		  arrayPane.setTitle(setText);
		  arrayPane.setPaginationJavaScriptCallback("changeSetsPage");

		  // D�finition des ent�tes de colonnes
          if (isSetSelectable)
          {
              if (isMultiSelect)
              {
                  arrayColumn = arrayPane.addArrayColumn(setMiniFilterSelect + "&nbsp;");
              }
              else
              {
                  arrayColumn = arrayPane.addArrayColumn("&nbsp;");
              }
              arrayColumn.setSortable(false);
              arrayColumn.setWidth("40px");
              arrayColumn.setAlignement("center");
          }
          for (c = 0; c < setsColumnsHeader.length; c++)
          {
              arrayColumn = arrayPane.addArrayColumn(setsColumnsHeader[c]);
              arrayColumn.setSortable(false);
          }

          for (int i=0; i < setsToDisplay.length; i++)
		  {
                ArrayLine arrayLine = arrayPane.addArrayLine();

                if (setsToDisplay[i].m_HighLight)
                    arrayLine.setStyleSheet("ArrayCellHot");
                if (isSetSelectable)
                {
                    if (isMultiSelect)
                    {
                        if (setsToDisplay[i].m_Selected)
                            isChecked = " checked";
                        else
                            isChecked = "";
                        ArrayCellText cell0 = arrayLine.addArrayCellText("<input type=\"checkbox\" name=\"set\" value=\"" + setsToDisplay[i].m_Id + "\"" + isChecked + " onclick=\"javascript:selectUnselect(this)\">");
                        cell0.setAlignement("center");
                    }
                    else
                    {
                        IconPane iconPane = gef.getIconPane();
                        Icon expleIcon1 = iconPane.addIcon();
                        expleIcon1.setProperties(resource.getIcon("selectionPeas.smallOk"), resource.getString("selectionPeas.quickSelect") , "javascript:submitOperation('','','" + setsToDisplay[i].m_Id + "','')");
                        arrayLine.addArrayCellIconPane(iconPane);
                    }
                }
                for (c = 0; c < setsColumnsHeader.length; c++)
                {
                    arrayLine.addArrayCellLink(setsToDisplay[i].m_Values[c], "javascript:submitOperation('GENERICPANELZOOMTOSET','','" + setsToDisplay[i].m_Id + "','')");
                }
          }

          out.println(arrayPane.print());
      }
%>
<% // ......................................................................................................................................... %>
<% //.........................................................Display Elements................................................................. %>
<% //.......................................................................................................................................... %>
<br/>
         <%
		  // Affiche un tableau
          arrayPane = gef.getArrayPane("elements", componentURL+"BrowseOperation", request, session);
		  // Affiche le nombre de ligne total du tableau dans la m�me page
		  arrayPane.setVisibleLineNumber(SelectionPeasSettings.elementByBrowsePage);
		  arrayPane.setTitle(elementText);
		  arrayPane.setPaginationJavaScriptCallback("changeElementsPage");

		  // D�finition des ent�tes de colonnes
          if (isElementSelectable)
          {
              if (isMultiSelect)
              {
                  arrayColumn = arrayPane.addArrayColumn(elementMiniFilterSelect + "&nbsp;");
              }
              else
              {
                  arrayColumn = arrayPane.addArrayColumn("&nbsp;");
              }
              arrayColumn.setSortable(false);
              arrayColumn.setWidth("40px");
              arrayColumn.setAlignement("center");
          }
		  int refCount = referenceColumnsNames.length;
		  int[] referenceColumnsIndexes = new int[refCount];
		  int refIndex = 0;
          for (c = 0; c < elementsColumnsHeader.length; c++)
          {
              arrayColumn = arrayPane.addArrayColumn(elementsColumnsHeader[c]);
              arrayColumn.setSortable(false);
              if (useExternalFunctionName && refIndex < refCount)
              {
		  for (int j = 0; j < refCount; j++)
		  {
			  if (elementsColumnsHeader[c].equals(referenceColumnsNames[j]))
			  {
				  referenceColumnsIndexes[refIndex] = c;
				  refIndex++;
			  }
		  }
              }
          }

          StringBuffer externalLinkSb = null;
          String externalValue;
          for (int i=0; i < elementsToDisplay.length; i++)
		  {
				ArrayLine arrayLine = arrayPane.addArrayLine();

                if (isElementSelectable)
                {
                    if (isMultiSelect)
                    {
                        if (elementsToDisplay[i].m_Selected)
                            isChecked = " checked";
                        else
                            isChecked = "";
                        ArrayCellText cell1 = arrayLine.addArrayCellText("<input type=\"checkbox\" name=\"element\" value=\"" + elementsToDisplay[i].m_Id + "\"" + isChecked + " onclick=\"javascript:selectUnselect(this)\">");
                        cell1.setAlignement("center");
                    }
                    else
                    {
                        IconPane iconPane = gef.getIconPane();
                        Icon expleIcon1 = iconPane.addIcon();
                        expleIcon1.setProperties(resource.getIcon("selectionPeas.smallOk"), resource.getString("selectionPeas.quickSelect"), "javascript:submitOperation('','','','" + elementsToDisplay[i].m_Id + "')");
                        arrayLine.addArrayCellIconPane(iconPane);
                    }
                }
                if (elementsToDisplay[i].m_HighLight)
                {
                    arrayLine.setStyleSheet("ArrayCellHot");
                }

                if (isZoomToElementValid && useExternalFunctionName)
                {
			externalLinkSb = new StringBuffer("new Array(");
			for (int j = 0, m = referenceColumnsIndexes.length; j < m; j++)
			{
				if (j > 0)
				{
					externalLinkSb.append(", ");
				}
				externalValue = elementsToDisplay[i].m_Values[referenceColumnsIndexes[j]];
				if (externalValue == null)
				{
					externalValue = "";
				}
				externalLinkSb.append("'").append(externalValue).append("'");
			}
			externalLinkSb.append(")");

                }
                for (c = 0; c < elementsColumnsHeader.length; c++)
                {
                    if (isZoomToElementValid)
                    {
			if (useExternalFunctionName)
			{
				String text = elementsToDisplay[i].m_Values[c];
				if (text == null)
				{
					text = "&nbsp;&nbsp;&nbsp;";
				}
				for (int j = 0, m = referenceColumnsIndexes.length; j < m; j++)
				{
					if (c == referenceColumnsIndexes[j])
					{
						text = "<b>" + text + "</b>";
					}
				}
				arrayLine.addArrayCellLink(text,
					"javascript:updateExternalFields(" + externalLinkSb.toString() + ")");
			}
			else
			{
				arrayLine.addArrayCellLink(elementsToDisplay[i].m_Values[c],
					"javascript:submitOperation('GENERICPANELZOOMTOELEMENT','','','" + elementsToDisplay[i].m_Id + "')");
			}
                    }
                    else
                    {
                        arrayLine.addArrayCellText(elementsToDisplay[i].m_Values[c]);
                    }
                }
          }

          out.println(arrayPane.print());
%>
<% // ......................................................................................................................................... %>
<% //.........................................................Display End...................................................................... %>
<% //.......................................................................................................................................... %>
  <br/><br/>
  <%
    ButtonPane buttonPane = gef.getButtonPane();
    if (isMultiSelect)
    {
	    buttonPane.addButton((Button) gef.getFormButton(resource.getString("GML.validate"), "javascript:submitOperation('','')", false));
    }
    if(toPopup){
		buttonPane.addButton((Button) gef.getFormButton(resource.getString("GML.cancel"), "javascript:window.close()", false));
	} else {
		buttonPane.addButton((Button) gef.getFormButton(resource.getString("GML.cancel"), "javascript:submitOperation('Cancel','')", false));
	}
	out.println(buttonPane.print());
%>
</form>
</center>
<%
out.println(frame.printAfter());
out.println(window.printAfter());
%>
</body>
</html>