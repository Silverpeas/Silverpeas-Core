<%@ page import="org.silverpeas.core.util.Pair" %>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.iconpanes.IconPane" %>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.buttons.Button" %>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.icons.Icon" %>
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
    String pageName = (String)request.getAttribute("pageName");
    String pageSubTitle = (String)request.getAttribute("pageSubTitle");
    String searchNumber = (String)request.getAttribute("searchNumber");
    String selectedNumber = (String)request.getAttribute("selectedNumber");
    String componentURL = (String)request.getAttribute("myComponentURL");
    PanelOperation[] operationsToDisplay = (PanelOperation[])request.getAttribute("operationsToDisplay");
    PanelSearchToken[] searchTokens = (PanelSearchToken[])request.getAttribute("searchTokens");
    PanelLine[] elementsToDisplay = (PanelLine[])request.getAttribute("elementsToDisplay");
    String[] columnsHeader = (String[])request.getAttribute("columnsHeader");
    String miniFilterSelect = (String)request.getAttribute("miniFilterSelect");
    boolean isMultiSelect = ((Boolean)request.getAttribute("isMultiSelect")).booleanValue();
    boolean isSelectable = ((Boolean)request.getAttribute("isSelectable")).booleanValue();
    boolean isFilterValid = ((Boolean)request.getAttribute("isFilterValid")).booleanValue();
    boolean isZoomToItemValid = ((Boolean)request.getAttribute("isZoomToItemValid")).booleanValue();
    boolean[] pageNavigation = (boolean[])request.getAttribute("pageNavigation");
    boolean toPrintBack = pageNavigation[0];
	boolean toPrintNext = pageNavigation[1];
	boolean toPopup = ((Boolean)request.getAttribute("ToPopup")).booleanValue();

    Pair<String, String> hostComponentName = (Pair<String, String>)request.getAttribute("HostComponentName");
    String           hostSpaceName = (String)request.getAttribute("HostSpaceName");
	Pair<String, String>[]     hostPath = (Pair<String, String>[])request.getAttribute("HostPath");

    int c;

    browseBar.setDomainName(hostSpaceName);

    //pour construire le path
    StringBuffer strPath = new StringBuffer("");

    if(toPopup){
        browseBar.setComponentName((String)hostComponentName.getFirst());
        for(int i =0; i<hostPath.length;i++){
            strPath.append((String)(hostPath[i].getFirst()));
            if ((i+1)<hostPath.length)
                strPath.append(" &gt ");
        }
    }
    else{
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
                if ((i+1)<hostPath.length)
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
                if ((i+1)<hostPath.length)
                    strPath.append(" &gt ");
            }
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
function selectUnselect(objName)
{
    var bSelected;

    eval("bSelected = document.operationForm." + objName + ".checked;");
    if (bSelected)
    {
        document.operationForm.selectedNumber.value = parseInt(document.operationForm.selectedNumber.value) + 1;
    }
    else
    {
        document.operationForm.selectedNumber.value = parseInt(document.operationForm.selectedNumber.value) - 1;
    }
}

function submitOperation(theOperation,confirmMsg)
{
    if (confirmMsg.length > 0)
    {
        if (window.confirm(confirmMsg))
        {
            document.operationForm.SpecificOperation.value = theOperation;
            document.operationForm.submit();
        }
    }
    else
    {
        document.operationForm.SpecificOperation.value = theOperation;
        document.operationForm.submit();
    }
}

function selectAll(type)
{
	var nbElmt = 0;
	var isChecked = false;
	var isElemtChecked = false;

    eval("nbElmt = window.document.operationForm." + type + "NB.value;");
    eval("isChecked = window.document.operationForm." + type + "All.checked;");
    if(isChecked)
    {
        for(var k=0; k<nbElmt; k++)
        {
            eval("isElemtChecked = window.document.operationForm." + type + k + ".checked;");
            if (!isElemtChecked)
            {
                document.operationForm.selectedNumber.value = parseInt(document.operationForm.selectedNumber.value) + 1;
            }
            eval("window.document.operationForm." + type + k + ".checked=true;");
        }
    }
    else
    {
        for(var k=0; k<nbElmt; k++)
        {
            eval("isElemtChecked = window.document.operationForm." + type + k + ".checked;");
            if (isElemtChecked)
            {
                document.operationForm.selectedNumber.value = parseInt(document.operationForm.selectedNumber.value) - 1;
            }
            eval("window.document.operationForm." + type + k + ".checked=false;");
        }
    }
}

function popupZoomToItem(theHref)
{
	var theWindow = SP_openWindow('about:blank', 'OpenZoomGenericPanel', '500', '500', 'menubar=no,scrollbars=yes,statusbar=no');
    theWindow.location.href = theHref;
}

</script>
</head>
<body>
<%
out.println(window.printBefore());
out.println(frame.printBefore());
%>
<center>
<form action="<%=componentURL%>DoOperation" name="operationForm">
  <input type="hidden" name="SpecificOperation" value="">
  <input type="hidden" name="elementNB" value="<%=elementsToDisplay.length%>">


  <%//*********** Entete ***************%>
  <table border="0" cellspacing="0" cellpadding="0" width="98%" class="contourintfdcolor">
      <tr>
        <td class="intfdcolor4" align=center>
          <table cellpadding="5" cellspacing="0" border="0">
        <%
            if (isFilterValid)
            {
        %>
            <tr>
                  <td><span class="txtlibform"><%=resource.getString("genericPanelPeas.searchCriteres")%>&nbsp;:
                    &nbsp;</span> </td>

            </tr>
            <tr>
              <td valign="top">
                <table cellpadding=2 cellspacing=0 border=0 width="100%">
                    <%
                        for (int i = 0; i < searchTokens.length; i++)
                        {
                            out.print(searchTokens[i].getHTMLDisplay());
                        }
                    %>
                </table>
              </td>

              <td valign="top"align="left">
<%
    ButtonPane buttonPane1 = gef.getButtonPane();
	buttonPane1.addButton((Button) gef.getFormButton(resource.getString("GML.validate"), "javascript:submitOperation('GENERICPANELAPPLYFILTER','')", false));
	out.println(buttonPane1.print());
%>


              </td>
            </tr>
        <%
            }
        %>
			<TR align="center"><td colspan="3">
			<table cellpadding=0 cellspacing=0 border=0 width="70%">
				<tr>
				<TD align="center" class="intfdcolor" height="1"><img src="<%=resource.getIcon("genericPanelPeas.px")%>"></TD>
				</tr>
			</table></td>
			</TR>
			<tr>
              <td valign="top" colspan="3">
                <table cellpadding=0 cellspacing=0 border=0 width="100%">
                  <tr>
                    <td nowrap><span class="txtlibform"><%=resource.getString("genericPanelPeas.searchResult")%>&nbsp;:
                      &nbsp;</span> </td><td>&nbsp;</td>
                    <td>
                      <input type="text" value ="<%=searchNumber%>" name="searchResult" size="7" maxlength="10">
                    </td>

      <%
            if (isMultiSelect)
            {
      %>
                   <td><img src="<%=resource.getIcon("genericPanelPeas.px")%>" width="30" height="1"></td>
                    <td nowrap><span class="txtlibform"><%=resource.getString("genericPanelPeas.selectedNumber")%>&nbsp;:
                      &nbsp;</span> </td>
                    <td>
                      <input type="text" value ="<%=selectedNumber%>" name="selectedNumber" size="7" maxlength="10">
                    </td>
      <%
            }
      %>
                  </tr>
				</table>
                </td>
			</tr>
          </table>
        </td>
      </tr>
  </table><br>
          <%
    if (toPrintBack || toPrintNext)
    {
%>
	<br>
          <table width="98%" border="0" cellspacing="0" cellpadding="0" class="ArrayColumn" align="center">
            <tr align="center" class="buttonColorDark">
              <td><img src="<%=resource.getIcon("genericPanelPeas.px")%>" width="1" height="1"></td>
            </tr>
            <tr align="center" class="intfdcolor4">
              <td><img src="<%=resource.getIcon("genericPanelPeas.px")%>" width="1" height="1"></td>
            </tr>
            <tr align="center">
              <td class="ArrayNavigation">
                <%
    if(toPrintBack)
		out.println("<a href=\"javascript:submitOperation('GENERICPANELPREVIOUSUSER','')\" class=\"ArrayNavigation\"><< "+resource.getString("GML.previous")+"&nbsp;</a>");
	if(toPrintNext)
		out.println("<a href=\"javascript:submitOperation('GENERICPANELNEXTUSER','')\" class=\"ArrayNavigation\">&nbsp;"+resource.getString("GML.next")+" >></a>");
	%>
              </td>
            </tr>
            <tr align="center" class="buttonColorDark">
              <td><img src="<%=resource.getIcon("genericPanelPeas.px")%>" width="1" height="1"></td>
            </tr>
            <tr align="center" class="intfdcolor1">
              <td><img src="<%=resource.getIcon("genericPanelPeas.px")%>" width="1" height="1"></td>
            </tr>
          </table>
          <%
    }
%>
          <%
		  // Affiche un tableau
          ArrayPane arrayPane = gef.getArrayPane("List", "#", request,session);
          String    isChecked = "";
		  // Affiche le nombre de ligne total du tableau dans la m�me page
		  arrayPane.setVisibleLineNumber(-1);
		  arrayPane.setTitle(pageName);

          ArrayColumn arrayColumn = null;
		  // D�finition des ent�tes de colonnes
          if (isMultiSelect || isSelectable)
          {
              if (isMultiSelect)
              {
                  arrayColumn = arrayPane.addArrayColumn(miniFilterSelect + "&nbsp;");
              }
              else
              {
                  arrayColumn = arrayPane.addArrayColumn("&nbsp;");
              }
              arrayColumn.setSortable(false);
              arrayColumn.setWidth("40px");
              arrayColumn.setAlignement("center");
          }
          for (c = 0; c < columnsHeader.length; c++)
          {
              arrayColumn = arrayPane.addArrayColumn(columnsHeader[c]);
              arrayColumn.setSortable(false);
          }

          for (int i=0; i < elementsToDisplay.length; i++)
		  {
                ArrayLine arrayLine = arrayPane.addArrayLine();

                if (elementsToDisplay[i].m_HighLight)
                    arrayLine.setStyleSheet("ArrayCellHot");
                if (isMultiSelect)
                {
                    if (elementsToDisplay[i].m_Selected)
                        isChecked = " checked";
                    else
                        isChecked = "";
				ArrayCellText cell0 = arrayLine.addArrayCellText("<input type=checkbox name=element" + i + " value='" + elementsToDisplay[i].m_Id + "'" + isChecked + " onclick=\"javascript:selectUnselect('element" + i + "')\">");
				cell0.setAlignement("center");
                }
                else if (isSelectable)
                {
                    IconPane iconPane = gef.getIconPane();
                    Icon expleIcon1 = iconPane.addIcon();
                    expleIcon1.setProperties(resource.getIcon("genericPanelPeas.smallOk"), resource.getString("genericPanelPeas.quickSelect") , componentURL + "DoOperation?userId=" + elementsToDisplay[i].m_Id + "&SpecificOperation=Validate");
                    arrayLine.addArrayCellIconPane(iconPane);
                }
                for (c = 0; c < columnsHeader.length; c++)
                {
                    if (isZoomToItemValid)
                    {
//                        arrayLine.addArrayCellLink(elementsToDisplay[i].m_Values[c], componentURL + "DoOperation?userId=" + elementsToDisplay[i].m_Id + "&SpecificOperation=GENERICPANELZOOMTOITEM");
                        arrayLine.addArrayCellLink(elementsToDisplay[i].m_Values[c], "javascript:popupZoomToItem('" + componentURL + "DoOperation?userId=" + elementsToDisplay[i].m_Id + "&SpecificOperation=GENERICPANELZOOMTOITEM')");
                    }
                    else
                    {
                        arrayLine.addArrayCellText(elementsToDisplay[i].m_Values[c]);
                    }
                }
          }

          out.println(arrayPane.print());
%>
          <%
	if (toPrintBack || toPrintNext)
    {
%>
          <table width="98%" border="0" cellspacing="0" cellpadding="0" class="ArrayColumn" align="center">
            <tr align="center" class="buttonColorDark">
              <td><img src="<%=resource.getIcon("genericPanelPeas.px")%>" width="1" height="1"></td>
            </tr>
            <tr align="center" class="intfdcolor4">
              <td><img src="<%=resource.getIcon("genericPanelPeas.px")%>" width="1" height="1"></td>
            </tr>
            <tr align="center">
              <td class="ArrayNavigation">
                <%
    if(toPrintBack)
		out.println("<a href=\"javascript:submitOperation('GENERICPANELPREVIOUSUSER','')\" class=\"ArrayNavigation\"><< "+resource.getString("GML.previous")+"&nbsp;</a>");
	if(toPrintNext)
		out.println("<a href=\"javascript:submitOperation('GENERICPANELNEXTUSER','')\" class=\"ArrayNavigation\">&nbsp;"+resource.getString("GML.next")+" >></a>");
                %>
              </td>
            </tr>
            <tr align="center" class="buttonColorDark">
              <td><img src="<%=resource.getIcon("genericPanelPeas.px")%>" width="1" height="1"></td>
            </tr>
            <tr align="center" class="intfdcolor1">
              <td><img src="<%=resource.getIcon("genericPanelPeas.px")%>" width="1" height="1"></td>
            </tr>
          </table>
          <%
    }
%>&nbsp;
  <br><br>

  <%
    ButtonPane buttonPane = gef.getButtonPane();
    if (isMultiSelect)
    {
	    buttonPane.addButton((Button) gef.getFormButton(resource.getString("GML.validate"), "javascript:submitOperation('GENERICPANELVALIDATE','')", false));
    }
	buttonPane.addButton((Button) gef.getFormButton(resource.getString("GML.cancel"), "javascript:submitOperation('GENERICPANELCANCEL','')", false));
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
