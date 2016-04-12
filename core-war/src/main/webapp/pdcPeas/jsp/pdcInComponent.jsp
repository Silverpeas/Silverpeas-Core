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

<%@page import="org.silverpeas.core.pdc.thesaurus.model.Jargon"%>
<%@page import="org.silverpeas.web.pdc.control.PdcSearchSessionController"%>
<%@page import="org.silverpeas.core.web.mvc.controller.ComponentContext"%>
<%@page import="org.silverpeas.core.web.mvc.controller.MainSessionController"%>
<%@ include file="checkPdc.jsp"%>

<%!

PdcSearchSessionController setComponentSessionController(HttpSession session, MainSessionController mainSessionCtrl) {
    //ask to MainSessionController to create the ComponentContext
    ComponentContext componentContext = mainSessionCtrl.createComponentContext(null, null);
    //instanciate a new CSC
    PdcSearchSessionController component = new PdcSearchSessionController(mainSessionCtrl, componentContext, "org.silverpeas.pdcPeas.multilang.pdcBundle", "org.silverpeas.pdcPeas.settings.pdcPeasIcons");
    session.setAttribute("Silverpeas_pdcSearch", component);
    return component;
}

String getValueIdFromPdcSearchContext(int axisId, SearchContext searchContext)
{
	if (searchContext == null)
		return null;

	SearchCriteria criteria = searchContext.getCriteriaOnAxis(axisId);
	if (criteria != null)
		return criteria.getValue();
	else
		return null;
}

void displayAxisByType(boolean showAllAxis, String axisLabel, List axis, String userId, SearchContext searchContext, Boolean activeThesaurus, Jargon jargon, MultiSilverpeasBundle resource, String axisTypeIcon, JspWriter out) throws IOException {
  SearchAxis searchAxis = null;
  int axisId = -1;
  String axisName = null;
  int nbPositions = -1;
  String valueInContext = null;
  Value value = null;
  String increment = "";
  String selected = "";
  String sNbObjects = "";
  String language = resource.getLanguage();

  if (searchContext == null) {
    searchContext = new SearchContext(userId);
  }

    out.println("<table width=\"100%\" border=\"0\" cellspacing=\"0\" cellpadding=\"5\">");
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
                    out.println("<tr>");
                    out.println("<td class=\"txtlibform\" width=\"200px\" nowrap><img src=\""+axisTypeIcon+"\" alt=\""+axisLabel+"\" align=\"absmiddle\">"+axisName+"&nbsp;:</td>");
                    out.println("<td><select name=\"Axis"+axisId+"\" size=\"1\">");
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

			out.println("<option value=\""+axisId+"_"+value.getFullPath()+"\""+selected+">"+increment+value.getName(language));
			if (!showAllAxis) {
				out.println(sNbObjects);
	                    }
			out.println("</option>");

			increment 	= "";
			selected	= "";
			sNbObjects	= "";
                    }
                    out.println("</select></td>");
                    out.println("</tr>");
               }
			}// fin du for
		} else {
			out.println("<tr><td width=\"100%\" class=\"txtnav\" bgcolor=\"EDEDED\">&nbsp;</td></tr>");
		} // fin du else
    out.println("</table>");
}

%>

<%
	String 			componentId 	= request.getParameter("ComponentId");
	SearchContext 	searchContext 	= (SearchContext) request.getAttribute("PDCSearchContext");

	PdcSearchSessionController pdcSC = (PdcSearchSessionController) session.getAttribute("Silverpeas_pdcSearch");

	if (pdcSC == null) {
		MainSessionController mainSessionCtrl = (MainSessionController) session.getAttribute(MainSessionController.MAIN_SESSION_CONTROLLER_ATT);
		pdcSC = setComponentSessionController(session, mainSessionCtrl);
	}

	resource = new MultiSilverpeasBundle(pdcSC.getMultilang(), pdcSC.getIcon(), pdcSC.getSettings(), pdcSC.getLanguage());

	pdcSC.buildComponentListWhereToSearch("dummy", componentId);

	// we get primary and eventually secondary axis
    List<SearchAxis> searchAxis	= pdcSC.getAxis("P");
	List<SearchAxis> pertinentAxis = new ArrayList<SearchAxis>();
    for (SearchAxis axis : searchAxis) {
	List<Value> values = pdcSC.getDaughterValues(Integer.toString(axis.getAxisId()), "0");
	if (values != null && !values.isEmpty()) {
		axis.setValues(values);
		pertinentAxis.add(axis);
	}
    }
	if (!pertinentAxis.isEmpty()) {
	String axisIcon = resource.getIcon("pdcPeas.icoPrimaryAxis");
		  displayAxisByType(false, "", pertinentAxis, pdcSC.getUserId(), searchContext, Boolean.FALSE, null, resource, axisIcon, out);
	}

	searchAxis	= pdcSC.getAxis("S");
	pertinentAxis.clear();
	for (SearchAxis axis : searchAxis) {
		List<Value> values = pdcSC.getDaughterValues(Integer.toString(axis.getAxisId()), "0");
		if (values != null && !values.isEmpty()) {
		axis.setValues(values);
		pertinentAxis.add(axis);
		}
	}
	if (!pertinentAxis.isEmpty()) {
	String axisIcon = resource.getIcon("pdcPeas.icoSecondaryAxis");
	displayAxisByType(false, "", pertinentAxis, pdcSC.getUserId(), searchContext, Boolean.FALSE, null, resource, axisIcon, out);
    }
%>