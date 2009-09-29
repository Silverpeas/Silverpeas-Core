<%--

    Copyright (C) 2000 - 2009 Silverpeas

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    As a special exception to the terms and conditions of version 3.0 of
    the GPL, you may redistribute this Program in connection with Free/Libre
    Open Source Software ("FLOSS") applications as described in Silverpeas's
    FLOSS exception.  You should have recieved a copy of the text describing
    the FLOSS exception, and it is also available here:
    "http://repository.silverpeas.com/legal/licensing"

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

--%>
<%@ include file="check.jsp" %>

<%
    String setText = (String)request.getAttribute("setText");
    String elementText = (String)request.getAttribute("elementText");
    String componentURL = (String)request.getAttribute("myComponentURL");
    PanelOperation[] operationsToDisplay = (PanelOperation[])request.getAttribute("operationsToDisplay");
    boolean isMultiSelect = ((Boolean)request.getAttribute("isMultiSelect")).booleanValue();
    boolean isZoomToSetValid = ((Boolean)request.getAttribute("isZoomToSetValid")).booleanValue();
    boolean isZoomToElementValid = ((Boolean)request.getAttribute("isZoomToElementValid")).booleanValue();

    boolean[] pageElementNavigation = (boolean[])request.getAttribute("pageElementNavigation");
    boolean toPrintElementBack = pageElementNavigation[0];
	boolean toPrintElementNext = pageElementNavigation[1];
    boolean[] pageSetNavigation = (boolean[])request.getAttribute("pageSetNavigation");
    boolean toPrintSetBack = pageSetNavigation[0];
	boolean toPrintSetNext = pageSetNavigation[1];

    boolean isSetSelectable = ((Boolean)request.getAttribute("isSetSelectable")).booleanValue();
    boolean isElementSelectable = ((Boolean)request.getAttribute("isElementSelectable")).booleanValue();
    PanelLine[] elementsToDisplay = (PanelLine[])request.getAttribute("elementsToDisplay");
    PanelLine[] setsToDisplay = (PanelLine[])request.getAttribute("setsToDisplay");
    String[] setsColumnsHeader = (String[])request.getAttribute("setsColumnsHeader");
    String[] elementsColumnsHeader = (String[])request.getAttribute("elementsColumnsHeader");
    String setMiniFilterSelect = (String)request.getAttribute("setMiniFilterSelect");
    String elementMiniFilterSelect = (String)request.getAttribute("elementMiniFilterSelect");

	boolean          toPopup = ((Boolean)request.getAttribute("ToPopup")).booleanValue();

    PairObject       hostComponentName = (PairObject)request.getAttribute("HostComponentName");
    String           hostSpaceName = (String)request.getAttribute("HostSpaceName");
	PairObject[]     hostPath = (PairObject[])request.getAttribute("HostPath");

    int              i,j,c;
    ArrayPane        arrayPane;
    ArrayColumn      arrayColumn;
    String           isChecked = "";

    browseBar.setDomainName(hostSpaceName);
    //pour construire le path
    StringBuffer strPath = new StringBuffer("");
    if(toPopup){
        browseBar.setComponentName((String)hostComponentName.getFirst());
        for(i =0; i<hostPath.length;i++){
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
        for(i =0; i<hostPath.length;i++){
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
<HTML>
<HEAD>
<TITLE><%=resource.getString("GML.popupTitle")%></TITLE>
<%
   out.println(gef.getLookStyleSheet());
%>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/animation.js"></script>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/checkForm.js"></script>
<script language="JavaScript">
function popupZoomToItem(theHref)
{
	var theWindow = SP_openWindow('about:blank', 'OpenZoomSelection', '500', '500', 'menubar=no,scrollbars=yes,statusbar=no');
    theWindow.location.href = theHref;
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

    eval("nbElmt = window.document.operationForm." + type + "NB.value;");
    eval("isChecked = window.document.operationForm." + type + "All.checked;");
    if(isChecked)
    {
        for(var k=0; k<nbElmt; k++)
        {
            eval("window.document.operationForm." + type + k + ".checked=true;");
        }
    }
    else
    {
        for(var k=0; k<nbElmt; k++)
        {
            eval("window.document.operationForm." + type + k + ".checked=false;");
        }
    }
}

</script>
</HEAD>
<BODY>
<%
    out.println(window.printBefore());
    out.println(frame.printBefore());
%>
<br>
<form action="<%=componentURL%>CartDoOperation" name="operationForm">
<input type="hidden" name="SpecificOperation" value="">
<input type="hidden" name="setNB" value="<%=setsToDisplay.length%>">
<input type="hidden" name="elementNB" value="<%=elementsToDisplay.length%>">
<% // ......................................................................................................................................... %>
<% //.........................................................Display Sets..................................................................... %>
<% //.......................................................................................................................................... %>
<%	if(isSetSelectable)
    {
          if (toPrintSetBack || toPrintSetNext)
          {
    %>
              <table width="98%" border="0" cellspacing="0" cellpadding="0" class="ArrayColumn" align="center">
                <tr align="center" class="buttonColorDark"> 
                  <td><img src="<%=resource.getIcon("selectionPeas.px")%>" width="1" height="1"></td>
                </tr>
                <tr align="center" class="intfdcolor4"> 
                  <td><img src="<%=resource.getIcon("selectionPeas.px")%>" width="1" height="1"></td>
                </tr>
                <tr align="center"> 
                  <td class="ArrayNavigation"> 
                    <%
          if(toPrintSetBack)
              out.println("<a href=\"javascript:submitOperation('GENERICPANELPREVIOUSSET','')\" class=\"ArrayNavigation\"><< "+resource.getString("GML.previous")+"&nbsp;</a>");
          if(toPrintSetNext)
              out.println("<a href=\"javascript:submitOperation('GENERICPANELNEXTSET','')\" class=\"ArrayNavigation\">&nbsp;"+resource.getString("GML.next")+" >></a>");
        %>
                  </td>
                </tr>
                <tr align="center" class="buttonColorDark"> 
                  <td><img src="<%=resource.getIcon("selectionPeas.px")%>" width="1" height="1"></td>
                </tr>
                <tr align="center" class="intfdcolor1"> 
                  <td><img src="<%=resource.getIcon("selectionPeas.px")%>" width="1" height="1"></td>
                </tr>
              </table>
    <%
          }
		  // Affiche un tableau
          arrayPane = gef.getArrayPane("List", "#", request,session);
		  // Affiche le nombre de ligne total du tableau dans la même page
		  arrayPane.setVisibleLineNumber(-1);
		  arrayPane.setTitle(setText);
          
          arrayColumn = null;
		  // Définition des entêtes de colonnes
          arrayColumn = arrayPane.addArrayColumn(setMiniFilterSelect + "&nbsp;");
          arrayColumn.setSortable(false);
          arrayColumn.setWidth("40px");
          arrayColumn.setAlignement("center");
          for (c = 0; c < setsColumnsHeader.length; c++)
          {
              arrayColumn = arrayPane.addArrayColumn(setsColumnsHeader[c]);
              arrayColumn.setSortable(false);
          }

          for (i=0; i < setsToDisplay.length; i++)
		  {
                ArrayLine arrayLine = arrayPane.addArrayLine();

                if (setsToDisplay[i].m_Selected)
                    isChecked = " checked";
                else
                    isChecked = "";
                ArrayCellText cell0 = arrayLine.addArrayCellText("<input type=checkbox name=set" + i + " value='" + setsToDisplay[i].m_Id + "'" + isChecked + ">");
                cell0.setAlignement("center");
                if (setsToDisplay[i].m_HighLight)
                    arrayLine.setStyleSheet("ArrayCellHot");
                for (c = 0; c < setsColumnsHeader.length; c++)
                {
                    arrayLine.addArrayCellLink(setsToDisplay[i].m_Values[c], "javascript:popupZoomToItem('" + componentURL + "ZoomToSetInfos?elementId=" + setsToDisplay[i].m_Id + "')");
                }
          }
        out.println(arrayPane.print());

        if (toPrintSetBack || toPrintSetNext)
        {
    %>
              <table width="98%" border="0" cellspacing="0" cellpadding="0" class="ArrayColumn" align="center">
                <tr align="center" class="buttonColorDark"> 
                  <td><img src="<%=resource.getIcon("selectionPeas.px")%>" width="1" height="1"></td>
                </tr>
                <tr align="center" class="intfdcolor4"> 
                  <td><img src="<%=resource.getIcon("selectionPeas.px")%>" width="1" height="1"></td>
                </tr>
                <tr align="center"> 
                  <td class="ArrayNavigation"> 
                    <%
        if(toPrintSetBack)
            out.println("<a href=\"javascript:submitOperation('GENERICPANELPREVIOUSSET','')\" class=\"ArrayNavigation\"><< "+resource.getString("GML.previous")+"&nbsp;</a>");
        if(toPrintSetNext)
            out.println("<a href=\"javascript:submitOperation('GENERICPANELNEXTSET','')\" class=\"ArrayNavigation\">&nbsp;"+resource.getString("GML.next")+" >></a>");
                    %>
                  </td>
                </tr>
                <tr align="center" class="buttonColorDark"> 
                  <td><img src="<%=resource.getIcon("selectionPeas.px")%>" width="1" height="1"></td>
                </tr>
                <tr align="center" class="intfdcolor1"> 
                  <td><img src="<%=resource.getIcon("selectionPeas.px")%>" width="1" height="1"></td>
                </tr>
              </table>
    <%
        }
	}
    %> 
&nbsp;
<br>
<% // ......................................................................................................................................... %>
<% //.........................................................Display Elements................................................................. %>
<% //.......................................................................................................................................... %>
    <%
	if(isElementSelectable)
    {
        if (toPrintElementBack || toPrintElementNext)
        {
    %>
              <table width="98%" border="0" cellspacing="0" cellpadding="0" class="ArrayColumn" align="center">
                <tr align="center" class="buttonColorDark"> 
                  <td><img src="<%=resource.getIcon("selectionPeas.px")%>" width="1" height="1"></td>
                </tr>
                <tr align="center" class="intfdcolor4"> 
                  <td><img src="<%=resource.getIcon("selectionPeas.px")%>" width="1" height="1"></td>
                </tr>
                <tr align="center"> 
                  <td class="ArrayNavigation"> 
                    <%
        if(toPrintElementBack)
            out.println("<a href=\"javascript:submitOperation('GENERICPANELPREVIOUSELEMENT','')\" class=\"ArrayNavigation\"><< "+resource.getString("GML.previous")+"&nbsp;</a>");
        if(toPrintElementNext)
            out.println("<a href=\"javascript:submitOperation('GENERICPANELNEXTELEMENT','')\" class=\"ArrayNavigation\">&nbsp;"+resource.getString("GML.next")+" >></a>");
        %>
                  </td>
                </tr>
                <tr align="center" class="buttonColorDark"> 
                  <td><img src="<%=resource.getIcon("selectionPeas.px")%>" width="1" height="1"></td>
                </tr>
                <tr align="center" class="intfdcolor1"> 
                  <td><img src="<%=resource.getIcon("selectionPeas.px")%>" width="1" height="1"></td>
                </tr>
              </table>
              <%
        }

        // Affiche un tableau
        arrayPane = gef.getArrayPane("List", "#", request,session);
        // Affiche le nombre de ligne total du tableau dans la même page
        arrayPane.setVisibleLineNumber(-1);
        arrayPane.setTitle(elementText);

        // Définition des entêtes de colonnes
        arrayColumn = arrayPane.addArrayColumn(elementMiniFilterSelect + "&nbsp;");
        arrayColumn.setSortable(false);
        arrayColumn.setWidth("40px");
        arrayColumn.setAlignement("center");
        for (c = 0; c < elementsColumnsHeader.length; c++)
        {
          arrayColumn = arrayPane.addArrayColumn(elementsColumnsHeader[c]);
          arrayColumn.setSortable(false);
        }

        for (i=0; i < elementsToDisplay.length; i++)
        {
            ArrayLine arrayLine = arrayPane.addArrayLine();

            if (elementsToDisplay[i].m_Selected)
                isChecked = " checked";
            else
                isChecked = "";
            ArrayCellText cell1 = arrayLine.addArrayCellText("<input type=checkbox name=element" + i + " value='" + elementsToDisplay[i].m_Id + "'" + isChecked + ">");
            cell1.setAlignement("center");
            if (elementsToDisplay[i].m_HighLight)
                arrayLine.setStyleSheet("ArrayCellHot");
            for (c = 0; c < elementsColumnsHeader.length; c++)
            {
                arrayLine.addArrayCellLink(elementsToDisplay[i].m_Values[c], "javascript:popupZoomToItem('" + componentURL + "ZoomToElementInfos?elementId=" + elementsToDisplay[i].m_Id + "')");
            }
        }
        out.println(arrayPane.print());

        if (toPrintElementBack || toPrintElementNext)
        {
    %>
              <table width="98%" border="0" cellspacing="0" cellpadding="0" class="ArrayColumn" align="center">
                <tr align="center" class="buttonColorDark"> 
                  <td><img src="<%=resource.getIcon("selectionPeas.px")%>" width="1" height="1"></td>
                </tr>
                <tr align="center" class="intfdcolor4"> 
                  <td><img src="<%=resource.getIcon("selectionPeas.px")%>" width="1" height="1"></td>
                </tr>
                <tr align="center"> 
                  <td class="ArrayNavigation"> 
                    <%
        if(toPrintElementBack)
            out.println("<a href=\"javascript:submitOperation('GENERICPANELPREVIOUSELEMENT','')\" class=\"ArrayNavigation\"><< "+resource.getString("GML.previous")+"&nbsp;</a>");
        if(toPrintElementNext)
            out.println("<a href=\"javascript:submitOperation('GENERICPANELNEXTELEMENT','')\" class=\"ArrayNavigation\">&nbsp;"+resource.getString("GML.next")+" >></a>");
                    %>
                  </td>
                </tr>
                <tr align="center" class="buttonColorDark"> 
                  <td><img src="<%=resource.getIcon("selectionPeas.px")%>" width="1" height="1"></td>
                </tr>
                <tr align="center" class="intfdcolor1"> 
                  <td><img src="<%=resource.getIcon("selectionPeas.px")%>" width="1" height="1"></td>
                </tr>
              </table>
              <%
        }
	}
    %>
</form>

<center>
<%
    ButtonPane buttonPane = gef.getButtonPane();
	buttonPane.addButton((Button) gef.getFormButton(resource.getString("GML.validate"), "javascript:submitOperation('Validate','')", false));	
	buttonPane.addButton((Button) gef.getFormButton(resource.getString("GML.cancel"), "javascript:submitOperation('Cancel','')", false));
	out.println(buttonPane.print());
%>
</center>
<%
out.println(frame.printAfter());
out.println(window.printAfter());
%>

</BODY>
</HTML>

