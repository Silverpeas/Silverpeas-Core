<%@ include file="../../pdcPeas/jsp/checkAdvancedSearch.jsp"%>
<%!

String getValueIdFromPdcSearchContext(int axisId, SearchContext searchContext)
{
	SearchCriteria criteria = searchContext.getCriteriaOnAxis(axisId);
	if (criteria != null)
		return criteria.getValue();
	else
		return null;
}

boolean someAxisPertinent(List axis)
{
	SearchAxis	searchAxis	= null;
	int			nbPositions	= -1;
	if (axis != null)
	{
        for (int i=0; i<axis.size(); i++)
        {
			searchAxis		= (SearchAxis) axis.get(i);
            nbPositions 	= searchAxis.getNbObjects();
            
            if (nbPositions > 0)
            	return true;
        }
	}
	return false;
}

void displayAxisByType(boolean showAllAxis, String axisLabel, List axis, SearchContext searchContext, Boolean activeThesaurus, Jargon jargon, ResourcesWrapper resource, String axisTypeIcon, JspWriter out) throws ThesaurusException, IOException {
	SearchAxis	searchAxis			= null;
	int			axisId				= -1;
	String		axisName			= null;
	int			nbPositions			= -1;
	String		valueInContext		= null;
	Value		value				= null;
	String		increment			= "";
	String		selected			= "";
	String		sNbObjects			= "";
	String		language			= resource.getLanguage();
	
    //out.println("<table width=\"10px\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\">");
		// il peut y avoir aucun axe primaire dans un 1er temps
		if (axis != null && axis.size()>0){
		//	out.println("<tr>");
            for (int i=0; i<axis.size(); i++){
				searchAxis		= (SearchAxis) axis.get(i);
                axisId			= searchAxis.getAxisId();
                axisName		= Encode.javaStringToHtmlString(searchAxis.getAxisName(language));
                nbPositions 	= searchAxis.getNbObjects();
                valueInContext 	= getValueIdFromPdcSearchContext(axisId, searchContext);
                if (nbPositions != 0)
                {
                	if (i > 0)
                		out.println("<td nowrap=\"nowrap\" width=\"30px\">&nbsp;</td>");
                	
                    out.println("<td nowrap=\"nowrap\" width=\"10px\">");
                    if (axisTypeIcon != null)
                    	out.println("<img src=\""+axisTypeIcon+"\" alt=\""+axisLabel+"\" align=\"absmiddle\">&nbsp;");
                    out.println("<nobr>"+axisName+"&nbsp;:&nbsp;</nobr></td>");
                    if (showAllAxis)
                    	out.println("<td width=\"10px\"><select name=\"Axis"+axisId+"\" size=\"1\">");
                    else
                    	out.println("<td width=\"10px\"><select name=\"Axis"+axisId+"\" size=\"1\" onChange=\"javascript:addValue(this, '"+axisId+"');\">");
                    out.println("<option value=\"\"></option>");
                    List values = searchAxis.getValues();
                    for (int v=0; v<values.size(); v++)
                    {
                    	value = (Value) values.get(v);
                    	
                    	for (int inc=0; inc<value.getLevelNumber(); inc++)
                    	{
                    		increment += "&nbsp;&nbsp;&nbsp;&nbsp;";
                    	}
                    	
                    	if (searchContext.isEmpty())
                    	{
                    		sNbObjects = " ("+value.getNbObjects()+")";
                    	}
                    	else if (valueInContext == null)
                    	{
                    		sNbObjects = " ("+value.getNbObjects()+")";
                    	}
                    	else if (value.getFullPath().equals(valueInContext))
                    	{
                    		selected = " selected";
                    		sNbObjects = " ("+value.getNbObjects()+")";
                    	}
                    	else if (value.getFullPath().indexOf(valueInContext)!=-1)
                    		sNbObjects = " ("+value.getNbObjects()+")";
                    	
                    	out.println("<option value=\""+value.getFullPath()+"\""+selected+">"+increment+value.getName(language));
                    	if (!showAllAxis) {
	                    	out.println(sNbObjects);
	                    }
                    	out.println("</option>");
                    	
                    	increment 	= "";
                    	selected	= "";
                    	sNbObjects	= "";
                    }
                    out.println("</select></td>");
               }
			}// fin du for
            //out.println("</tr>");
		} else {
			//out.println("<tr><td width=\"100%\" class=\"txtnav\" bgcolor=\"EDEDED\">&nbsp;</td></tr>");
		} // fin du else
    //out.println("</table>");
}

%>
<%
//recuperation des parametres pour le PDC
List			primaryAxis			= (List) request.getAttribute("ShowPrimaryAxis");
List			secondaryAxis		= (List) request.getAttribute("ShowSecondaryAxis");
SearchContext	searchContext		= (SearchContext) request.getAttribute("SearchContext");
QueryParameters	parameters			= (QueryParameters) request.getAttribute("QueryParameters");

boolean	isEmptySearchContext = true;
// l'objet SearchContext n'est pas vide
if (searchContext != null && searchContext.getCriterias().size() > 0){
	isEmptySearchContext = false;
}

Button searchButton = (Button) gef.getFormButton(resource.getString("pdcPeas.search"), "javascript:onClick=sendQuery()", false);
%>

<html>
<HEAD>
<TITLE><%=resource.getString("GML.popupTitle")%></TITLE>
<%
   out.println(gef.getLookStyleSheet());
%>
<script language="JavaScript1.2">
function addValue(selectItem, axisId) 
{
	var valuePath = selectItem.value;
	if (valuePath.length > 0)
	{
		document.AdvancedSearch.AxisId.value = axisId;
		document.AdvancedSearch.ValueId.value = valuePath;
		document.AdvancedSearch.action = "GlobalAddCriteria";
	}
	else
	{
		document.AdvancedSearch.Ids.value = axisId;
		document.AdvancedSearch.action = "GlobalDeleteCriteria";
	}
	document.AdvancedSearch.target = "_self";
	document.AdvancedSearch.submit();
}

function sendQuery() 
{		
	document.AdvancedSearch.action = "AdvancedSearch";
	document.AdvancedSearch.target = "MyMain";
		
	//displayStaticMessage();
    //setTimeout("document.AdvancedSearch.submit();", 500);
	document.AdvancedSearch.submit();
}

function raz()
{
	document.AdvancedSearch.mode.value = "clear";
	document.AdvancedSearch.action = "ChangeSearchTypeToExpert";
	document.AdvancedSearch.target = "_self";
	document.AdvancedSearch.submit();
}

function init()
{
	<%
		if (someAxisPertinent(primaryAxis) || someAxisPertinent(secondaryAxis)) {
			out.println("parent.showPdcFrame();");
		} else {
			out.println("parent.hidePdcFrame();");
		}
	%>
}
</SCRIPT>
</HEAD>
<BODY id="pdcFrame" onload="init()">
<CENTER>
<form name="AdvancedSearch" action="ViewAdvancedSearch" method="post">
  <input type="hidden" name="AxisId"/>
  <input type="hidden" name="ValueId"/>
  <input type="hidden" name="Ids"/>
  <input type="hidden" name="mode"/>
  <input type="hidden" name="ResultPage" value="searchDocuments.jsp"/>
  <input type="hidden" name="SearchPage" value="/admin/jsp/pdcSearchSilverpeasV5.jsp"/>
  <input type="hidden" name="spaces" value="<%=parameters.getSpaceId()%>"/>
  <input type="hidden" name="componentSearch" value="<%=parameters.getInstanceId()%>"/>
  
  <table width="100%" border="0" cellpadding="0" cellspacing="0">
  	<tr>
  		<td class="viewGeneratorLines" width="100%"><img src="<%=resource.getIcon("pdcPeas.1px")%>" width="1" height="1"/></td>
  	</tr>
  </table>

  <table border="0" align="center">
  	<tr>
	<td><img src="<%=resource.getIcon("pdcPeas.noColorPix")%>" width="20px" height="1px"></td>
	<!-- <td width="100%" align="center"> -->
<%
	String axisIcon = resource.getIcon("pdcPeas.icoPrimaryAxis");
	displayAxisByType(false, resource.getString("pdcPeas.primaryAxis"), primaryAxis, searchContext, new Boolean(false), null, resource, null, out);
	if (secondaryAxis != null){
		axisIcon = resource.getIcon("pdcPeas.icoSecondaryAxis");
		displayAxisByType(false, resource.getString("pdcPeas.secondaryAxis"), secondaryAxis, searchContext, new Boolean(false), null, resource, null, out);
	}
%>
	<!-- </td> -->
	<td><img src="<%=resource.getIcon("pdcPeas.noColorPix")%>" width="20px" height="1px"></td>
	<td><%=searchButton.print()%></td><td><img src="<%=resource.getIcon("pdcPeas.1px")%>" width="0px" height="1px"></td><td><a href="javaScript:raz()"><img src="<%=m_context%>/admin/jsp/icons/silverpeasV5/refresh.gif" border="0" alt="Remise à zéro"/></a></td>
	</tr></table>
</form>
</CENTER>
</BODY>
</HTML>