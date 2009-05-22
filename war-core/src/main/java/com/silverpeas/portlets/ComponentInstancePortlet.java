package com.silverpeas.portlets;

import java.io.IOException;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.GenericPortlet;
import javax.portlet.PortletException;
import javax.portlet.PortletMode;
import javax.portlet.PortletPreferences;
import javax.portlet.PortletRequestDispatcher;
import javax.portlet.PortletSession;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.ValidatorException;

import com.silverpeas.util.StringUtil;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.peasCore.URLManager;

public class ComponentInstancePortlet extends GenericPortlet implements FormNames {
	
	public void doView(RenderRequest request, RenderResponse response) throws PortletException, IOException
	{
		PortletSession session = request.getPortletSession();
		MainSessionController 	m_MainSessionCtrl 	= (MainSessionController) session.getAttribute("SilverSessionController", PortletSession.APPLICATION_SCOPE);
				
		PortletPreferences pref = request.getPreferences();
		String instanceId = pref.getValue("instanceId", "");
		
		if (m_MainSessionCtrl.getOrganizationController().isComponentAvailable(instanceId, m_MainSessionCtrl.getUserId()))
			request.setAttribute("URL", URLManager.getURL(null, null, instanceId)+"portlet");
		
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
    
    /*
     * Process Action.
     */
    public void processAction(ActionRequest request, ActionResponse response) throws PortletException 
    {    
        if (request.getParameter(SUBMIT_FINISHED) != null) {
            //
            // handle "finished" button on edit page
            // return to view mode
            //
            processEditFinishedAction(request, response);
        } else if (request.getParameter(SUBMIT_CANCEL) != null) {
            //
            // handle "cancel" button on edit page
            // return to view mode
            //
            processEditCancelAction(request, response);
        }
    }
    
    /*
     * Process the "cancel" action for the edit page.
     */
    private void processEditCancelAction(ActionRequest request, ActionResponse response) throws PortletException 
    {
        response.setPortletMode(PortletMode.VIEW);
    }
    
    /*
     * Process the "finished" action for the edit page.  Set the "url" to the value specified
     * in the edit page.
     */
    private void processEditFinishedAction(ActionRequest request, ActionResponse response) throws PortletException 
    {
        String instanceId = request.getParameter("instanceId");
        
        //Check if it is a number
    	try {			
			//store preference
            PortletPreferences pref = request.getPreferences();
            try {
                pref.setValue("instanceId", instanceId);
                pref.store();
            } catch (ValidatorException ve) {
                getPortletContext().log("could not set instanceId", ve);
                throw new PortletException("IFramePortlet.processEditFinishedAction", ve);
            } catch (IOException ioe) {
                getPortletContext().log("could not set instanceId", ioe);
                throw new PortletException("IFramePortlet.prcoessEditFinishedAction", ioe);
            }
            response.setPortletMode(PortletMode.VIEW);
			
		} catch (NumberFormatException e) {
			response.setRenderParameter(ERROR_BAD_VALUE, "true");
            response.setPortletMode(PortletMode.EDIT);
		}        
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
            PortletRequestDispatcher dispatcher = getPortletContext().getRequestDispatcher("/portlets/jsp/componentInstance/"+pageName);
            dispatcher.include(request, response);
        } catch (IOException ioe) {
            throw new PortletException(ioe);
        }
    }
}
