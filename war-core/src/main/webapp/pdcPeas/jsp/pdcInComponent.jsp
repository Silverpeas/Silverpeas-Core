<%@page import="com.silverpeas.thesaurus.model.Jargon"%>
<%@page import="com.stratelia.silverpeas.peasCore.ComponentContext"%>
<%@page import="com.stratelia.silverpeas.peasCore.MainSessionController"%>
<%@page import="com.stratelia.silverpeas.pdcPeas.control.PdcSearchSessionController"%>
<%@ include file="checkPdc.jsp"%>

<%!

PdcSearchSessionController setComponentSessionController(HttpSession session, MainSessionController mainSessionCtrl) {
    //ask to MainSessionController to create the ComponentContext
    ComponentContext componentContext = mainSessionCtrl.createComponentContext(null, null);
    //instanciate a new CSC
    PdcSearchSessionController component = new PdcSearchSessionController(mainSessionCtrl, componentContext, "com.stratelia.silverpeas.pdcPeas.multilang.pdcBundle", "com.stratelia.silverpeas.pdcPeas.settings.pdcPeasIcons");
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

void displayAxisByType(boolean showAllAxis, String axisLabel, List axis, SearchContext searchContext, Boolean activeThesaurus, Jargon jargon, ResourcesWrapper resource, String axisTypeIcon, JspWriter out) throws IOException {
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
	
	if (searchContext == null)
		searchContext = new SearchContext();
	
    out.println("<table width=\"100%\" border=\"0\" cellspacing=\"0\" cellpadding=\"5\">");
		// il peut y avoir aucun axe primaire dans un 1er temps
		if (axis != null && axis.size()>0){
            for (int i=0; i<axis.size(); i++){
				searchAxis		= (SearchAxis) axis.get(i);
                axisId			= searchAxis.getAxisId();
                axisName		= Encode.javaStringToHtmlString(searchAxis.getAxisName(language));
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
		MainSessionController mainSessionCtrl = (MainSessionController) session.getAttribute("SilverSessionController");
		pdcSC = setComponentSessionController(session, mainSessionCtrl);
	}
	
	resource = new ResourcesWrapper(pdcSC.getMultilang(), pdcSC.getIcon(), pdcSC.getSettings(), pdcSC.getLanguage());
	
	pdcSC.buildComponentListWhereToSearch("dummy", componentId);
	
	SearchAxis axis = null;

	// we get primary and eventually secondary axis
    List searchAxis	= pdcSC.getAxis("P");
    for (int p=0; p<searchAxis.size(); p++)
    {
    	axis = (SearchAxis) searchAxis.get(p);
    	axis.setValues(pdcSC.getDaughterValues(Integer.toString(axis.getAxisId()), "0"));
    }
	if (searchAxis.size()>0)
	{
    	String axisIcon = resource.getIcon("pdcPeas.icoPrimaryAxis");
		displayAxisByType(false, "", searchAxis, searchContext, Boolean.FALSE, null, resource, axisIcon, out);
	}
	
	searchAxis	= pdcSC.getAxis("S");
    for (int p=0; p<searchAxis.size(); p++)
    {
    	axis = (SearchAxis) searchAxis.get(p);
    	axis.setValues(pdcSC.getDaughterValues(Integer.toString(axis.getAxisId()), "0"));
    }
    if (searchAxis.size()>0)
    {
    	String axisIcon = resource.getIcon("pdcPeas.icoSecondaryAxis");
    	displayAxisByType(false, "", searchAxis, searchContext, Boolean.FALSE, null, resource, axisIcon, out);
    }
%>