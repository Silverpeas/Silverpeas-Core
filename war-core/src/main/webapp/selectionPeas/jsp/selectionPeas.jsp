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

    boolean[] pageElementNavigation = (boolean[])request.getAttribute("pageElementNavigation");
    boolean toPrintElementBack = pageElementNavigation[0];
	boolean toPrintElementNext = pageElementNavigation[1];
    boolean[] pageSetNavigation = (boolean[])request.getAttribute("pageSetNavigation");
    boolean toPrintSetBack = pageSetNavigation[0];
	boolean toPrintSetNext = pageSetNavigation[1];

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

    PairObject       hostComponentName = (PairObject)request.getAttribute("HostComponentName");
    String           hostSpaceName = (String)request.getAttribute("HostSpaceName");
	PairObject[]     hostPath = (PairObject[])request.getAttribute("HostPath");

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
	        strPath.append(Encode.javaStringToHtmlString(setPath[i].m_Values[0]));
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
<%
out.println(gef.getLookStyleSheet());
%>
<script language="JavaScript">
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
	    submitOperation(theOperation,confirmMsg,'','');
	}
	
	function submitOperation(theOperation,confirmMsg,setId,elementId)
	{
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
  <input type="hidden" name="SpecificOperation" value="">
  <input type="hidden" name="setId" value="">
  <input type="hidden" name="elementId" value="">
  <input type="hidden" name="setNB" value="<%=setsToDisplay.length%>">
  <input type="hidden" name="elementNB" value="<%=elementsToDisplay.length%>">

<% // ......................................................................................................................................... %>
<% //.........................................................Display Header................................................................... %>
<% //.......................................................................................................................................... %>
  <%
        if (isMultiSelect)
        {
  %>
  <table border="0" cellspacing="0" cellpadding="2" width="98%" class="contourintfdcolor">
      <tr> 
        <td class="intfdcolor4"> 
          <table cellpadding="5" cellspacing="0" border="0">
            <tr> 
              <td> 
                <table cellpadding=2 cellspacing=0 border=0 width="100%">
                  <tr> 
                    <td ><span class="txtlibform"><%=resource.getString("selectionPeas.selectedNumber")%>&nbsp;: 
                      &nbsp;</span> </td>
                    <td> 
                      <input type="text" value ="<%=selectedNumber%>" name="selectedNumber" size="7" maxlength="10">
                    </td>
                  </tr>
                </table>
              </td>
            </tr>
          </table>
        </td>
      </tr>
  </table>
  <br>
  <br>
  <%
        }
  %>
<% // ......................................................................................................................................... %>
<% //.........................................................Display Sets..................................................................... %>
<% //.......................................................................................................................................... %>
          <%
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
          
      ArrayPane arrayPane;
      ArrayColumn arrayColumn = null;
      String isChecked = "";
          
      if (isDisplaySets)
      {
		  // Affiche un tableau
          arrayPane = gef.getArrayPane("List", "#", request,session);
		  // Affiche le nombre de ligne total du tableau dans la même page
		  arrayPane.setVisibleLineNumber(-1);
		  arrayPane.setTitle(setText);
          
		  // Définition des entêtes de colonnes
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
                        ArrayCellText cell0 = arrayLine.addArrayCellText("<input type=checkbox name=set" + i + " value='" + setsToDisplay[i].m_Id + "'" + isChecked + " onclick=\"javascript:selectUnselect('set" + i + "')\">");
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
<%
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
%>&nbsp;
<% // ......................................................................................................................................... %>
<% //.........................................................Display Elements................................................................. %>
<% //.......................................................................................................................................... %>

<br>
          <%
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
%>
          <%
		  // Affiche un tableau
          arrayPane = gef.getArrayPane("List", "#", request, session);
		  // Affiche le nombre de ligne total du tableau dans la même page
		  arrayPane.setVisibleLineNumber(-1);
		  arrayPane.setTitle(elementText);
          
		  // Définition des entêtes de colonnes
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
                        ArrayCellText cell1 = arrayLine.addArrayCellText("<input type=checkbox name=element" + i + " value='" + elementsToDisplay[i].m_Id + "'" + isChecked + " onclick=\"javascript:selectUnselect('element" + i + "')\">");
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
          <%
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
%>&nbsp;
<% // ......................................................................................................................................... %>
<% //.........................................................Display End...................................................................... %>
<% //.......................................................................................................................................... %>
  <br><br>
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
