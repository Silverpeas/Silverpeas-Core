package com.silverpeas.portlets;

import java.io.IOException;
import java.util.Iterator;

import javax.portlet.GenericPortlet;
import javax.portlet.PortletException;
import javax.portlet.PortletRequestDispatcher;
import javax.portlet.PortletSession;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import com.silverpeas.interestCenter.util.InterestCenterUtil;
import com.silverpeas.util.StringUtil;
import com.stratelia.silverpeas.peasCore.MainSessionController;

public class FavoriteQueriesPortlet extends GenericPortlet implements FormNames {
	
	public void doView(RenderRequest request, RenderResponse response) throws PortletException, IOException
	{
		PortletSession session = request.getPortletSession();
		MainSessionController 	m_MainSessionCtrl 	= (MainSessionController) session.getAttribute("SilverSessionController", PortletSession.APPLICATION_SCOPE);
		
		Iterator iCentersList = (new InterestCenterUtil()).getICByUserId(Integer.parseInt(m_MainSessionCtrl.getUserId())).iterator();
	    
	    request.setAttribute("Queries", iCentersList);
		
		include(request, response, "portlet.jsp");
	}
	
	public void doEdit(RenderRequest request, RenderResponse response) throws PortletException 
	{
        include(request, response, "edit.jsp");
    }
    
    /** Include "help" JSP. */
    public void doHelp(RenderRequest request, RenderResponse response) throws PortletException 
    {
        include(request, response, "help.jsp");
    }
    
    /** Include a page. */
    private void include(RenderRequest request, RenderResponse response, String pageName) throws PortletException 
    {
        response.setContentType(request.getResponseContentType());
        if (!StringUtil.isDefined(pageName)) {
            // assert
            throw new NullPointerException("null or empty page name");
        }
        try {
            PortletRequestDispatcher dispatcher = getPortletContext().getRequestDispatcher("/portlets/jsp/favoriteQueries/"+pageName);
            dispatcher.include(request, response);
        } catch (IOException ioe) {
            throw new PortletException(ioe);
        }
    }   
}