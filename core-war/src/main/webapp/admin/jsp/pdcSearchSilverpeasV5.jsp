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

<%@page import="org.silverpeas.core.pdc.pdc.model.SearchCriteria"%>
<%@page import="org.silverpeas.core.pdc.pdc.model.Value"%>
<%@page import="java.io.IOException"%>
<%@page import="org.silverpeas.core.pdc.thesaurus.model.ThesaurusException"%>
<%@page import="org.silverpeas.core.pdc.thesaurus.model.Jargon"%>
<%@page import="org.silverpeas.core.util.EncodeHelper"%>
<%@page import="org.silverpeas.core.web.util.viewgenerator.html.GraphicElementFactory"%>
<%@page import="org.silverpeas.core.web.util.viewgenerator.html.buttons.Button"%>
<%@page import="org.silverpeas.core.pdc.pdc.model.SearchAxis"%>
<%@page import="org.silverpeas.core.pdc.pdc.model.QueryParameters"%>
<%@page import="org.silverpeas.core.pdc.pdc.model.SearchContext"%>
<%@page import="java.util.List"%>
<%@page import="org.silverpeas.core.util.MultiSilverpeasBundle"%>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%@page import="org.silverpeas.web.pdc.control.PdcSearchSessionController"%>
<%@ page import="org.silverpeas.core.util.ResourceLocator" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>

<%-- Retrieve user menu display mode --%>
<c:set var="curHelper" value="${sessionScope.Silverpeas_LookHelper}" />
<%-- Set resource bundle --%>
<fmt:setLocale value="${sessionScope['SilverSessionController'].favoriteLanguage}" />
<view:setBundle basename="org.silverpeas.lookSilverpeasV5.multilang.lookBundle"/>

<%!

String getValueIdFromPdcSearchContext(int axisId, SearchContext searchContext)
{
	SearchCriteria criteria = searchContext.getCriteriaOnAxis(axisId);
	if (criteria != null)
		return criteria.getValue();
	else
		return null;
}

boolean someAxisPertinent(List<SearchAxis> axis)
{
	SearchAxis	searchAxis	= null;
	int			nbPositions	= -1;
	if (axis != null)
	{
        for (int i=0; i<axis.size(); i++)
        {
			searchAxis		= axis.get(i);
            nbPositions 	= searchAxis.getNbObjects();

            if (nbPositions > 0)
		return true;
        }
	}
	return false;
}

void displayAxisByType(boolean showAllAxis, String axisLabel, List<SearchAxis> axis, SearchContext searchContext, Boolean activeThesaurus, Jargon jargon, MultiSilverpeasBundle resource, String axisTypeIcon, JspWriter out) throws ThesaurusException, IOException {
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

		// il peut y avoir aucun axe primaire dans un 1er temps
		if (axis != null && axis.size()>0){
            for (int i=0; i<axis.size(); i++){
				searchAxis		= (SearchAxis) axis.get(i);
                axisId			= searchAxis.getAxisId();
                axisName		= EncodeHelper.javaStringToHtmlString(searchAxis.getAxisName(language));
                nbPositions 	= searchAxis.getNbObjects();
                valueInContext 	= getValueIdFromPdcSearchContext(axisId, searchContext);
                if (nbPositions != 0)
                {
			if (i > 0)
				out.println("<td nowrap=\"nowrap\" width=\"30\">&nbsp;</td>");

                    out.println("<td nowrap=\"nowrap\">");
                    if (axisTypeIcon != null)
			out.println("<img src=\""+axisTypeIcon+"\" alt=\""+axisLabel+"\" align=\"middle\" alt=\"\" nowrap=\"nowrap\"/>&nbsp;");
                    out.println(axisName+"&nbsp;:&nbsp;</td>");
                    if (showAllAxis)
			out.println("<td width=\"10\"><select name=\"Axis"+axisId+"\" size=\"1\">");
                    else
			out.println("<td width=\"10\"><select name=\"Axis"+axisId+"\" size=\"1\" onchange=\"javascript:addValue(this, '"+axisId+"');\">");
                    out.println("<option value=\"\"></option>");
                    List<Value> values = searchAxis.getValues();
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
		}
}
%>
<%
MultiSilverpeasBundle resource = (MultiSilverpeasBundle) request.getAttribute("resources");
GraphicElementFactory gef = (GraphicElementFactory) session.getAttribute("SessionGraphicElementFactory");
String m_context = ResourceLocator.getGeneralSettingBundle().getString("ApplicationURL");

//recuperation des parametres pour le PDC
List<SearchAxis> primaryAxis		= (List) request.getAttribute("ShowPrimaryAxis");
List<SearchAxis> secondaryAxis		= (List) request.getAttribute("ShowSecondaryAxis");
SearchContext	 searchContext		= (SearchContext) request.getAttribute("SearchContext");
QueryParameters	 parameters			= (QueryParameters) request.getAttribute("QueryParameters");

Button searchButton = gef.getFormButton(resource.getString("pdcPeas.search"), "javascript:onClick=sendQuery()", false);
%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title><%=resource.getString("GML.popupTitle")%></title>
<view:looknfeel />

<script type="text/javascript">
function addValue(selectItem, axisId) {
  $.progressMessage();
  var valuePath = selectItem.value;
  if (valuePath.length > 0) {
	document.AdvancedSearch.AxisId.value = axisId;
	document.AdvancedSearch.ValueId.value = valuePath;
	document.AdvancedSearch.action = "GlobalAddCriteria";
  } else {
	document.AdvancedSearch.Ids.value = axisId;
	document.AdvancedSearch.action = "GlobalDeleteCriteria";
  }
  document.AdvancedSearch.target = "_self";
  document.AdvancedSearch.submit();
}

function sendQuery() {
  document.AdvancedSearch.action = "AdvancedSearch";
  document.AdvancedSearch.target = "MyMain";
  document.AdvancedSearch.submit();
}

function raz() {
  $.progressMessage();
  document.AdvancedSearch.action = "ResetPDCContext";
  document.AdvancedSearch.target = "_self";
  document.AdvancedSearch.submit();
}

function init() {
<%
  if (someAxisPertinent(primaryAxis) || someAxisPertinent(secondaryAxis)) {
    out.println("parent.showPdcFrame();");
  } else {
    out.println("parent.hidePdcFrame();");
  }
%>
}
</script>
</head>
<body id="pdcFrame" onload="init()">
<center>
<form name="AdvancedSearch" action="ViewAdvancedSearch" method="post">
  <input type="hidden" name="AxisId"/>
  <input type="hidden" name="ValueId"/>
  <input type="hidden" name="Ids"/>
  <input type="hidden" name="mode"/>
  <input type="hidden" name="FromPDCFrame" value="true"/>
  <input type="hidden" name="ShowResults" value="<%=PdcSearchSessionController.SHOWRESULTS_OnlyPDC %>"/>
  <input type="hidden" name="ResultPage" value=""/>
  <input type="hidden" name="SearchPage" value="/admin/jsp/pdcSearchSilverpeasV5.jsp"/>
  <input type="hidden" name="spaces" value="<%=EncodeHelper.javaStringToHtmlString(parameters.getSpaceId())%>"/>
  <input type="hidden" name="componentSearch" value="<%=EncodeHelper.javaStringToHtmlString(parameters.getInstanceId())%>"/>

  <table width="100%" border="0" cellpadding="0" cellspacing="0">
	<tr>
		<td class="viewGeneratorLines" width="100%"><img src="<%=resource.getIcon("pdcPeas.1px")%>" width="1" height="1" alt=""/></td>
	</tr>
  </table>

  <table border="0" align="center">
	<tr>
	<td><img src="<%=resource.getIcon("pdcPeas.noColorPix")%>" width="20" height="1" alt=""/></td>
<%

	String axisIcon = resource.getIcon("pdcPeas.icoPrimaryAxis");
	displayAxisByType(false, resource.getString("pdcPeas.primaryAxis"), primaryAxis, searchContext, Boolean.FALSE, null, resource, null, out);
	if (secondaryAxis != null){
		axisIcon = resource.getIcon("pdcPeas.icoSecondaryAxis");
		displayAxisByType(false, resource.getString("pdcPeas.secondaryAxis"), secondaryAxis, searchContext, Boolean.FALSE, null, resource, null, out);
	}
%>
	<td><img src="<%=resource.getIcon("pdcPeas.noColorPix")%>" width="20" height="1" alt=""/></td>
	<td><%=searchButton.print()%></td><td><img src="<%=resource.getIcon("pdcPeas.1px")%>" width="0" height="1" alt=""/></td><td><a href="javaScript:raz()" title="<%=resource.getString("GML.reset")%>"><img src="<%=m_context%>/admin/jsp/icons/silverpeasV5/refresh.gif" border="0" alt="<%=resource.getString("GML.reset")%>"/></a></td>
	</tr></table>
</form>
</center>
<view:progressMessage/>
</body>
</html>