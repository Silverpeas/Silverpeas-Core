package com.silverpeas.portlets;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.GenericPortlet;
import javax.portlet.PortletException;
import javax.portlet.PortletMode;
import javax.portlet.PortletRequestDispatcher;
import javax.portlet.PortletSession;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import com.silverpeas.util.StringUtil;
import com.stratelia.silverpeas.notificationserver.channel.silvermail.SILVERMAILUtil;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.viewGenerator.html.arrayPanes.ArrayPane;

public class MyNotificationsPortlet extends GenericPortlet implements FormNames {
	
	public void doView(RenderRequest request, RenderResponse response) throws PortletException, IOException
	{
		PortletSession session = request.getPortletSession();
		MainSessionController 	m_MainSessionCtrl 	= (MainSessionController) session.getAttribute("SilverSessionController", PortletSession.APPLICATION_SCOPE);
		
		SILVERMAILUtil silvermailUtil = new SILVERMAILUtil(m_MainSessionCtrl.getUserId(), null);
		
	    List messages = new ArrayList();
		try {
			messages = (List) silvermailUtil.getFolderMessageList("INBOX");
		} catch (Exception e) {
			SilverTrace.error("portlet", "MyNotificationsPortlet", "portlet.ERROR", e);
		}

	    request.setAttribute("Messages", messages.iterator());
		
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
    
    public void processAction(ActionRequest request, ActionResponse response) throws PortletException 
    {
    	response.setRenderParameter(ArrayPane.ACTION_PARAMETER_NAME, request.getParameter(ArrayPane.ACTION_PARAMETER_NAME));
    	response.setRenderParameter(ArrayPane.COLUMN_PARAMETER_NAME, request.getParameter(ArrayPane.COLUMN_PARAMETER_NAME));
    	response.setRenderParameter(ArrayPane.INDEX_PARAMETER_NAME, request.getParameter(ArrayPane.INDEX_PARAMETER_NAME));
    	response.setRenderParameter(ArrayPane.TARGET_PARAMETER_NAME, request.getParameter(ArrayPane.TARGET_PARAMETER_NAME));
    	
    	response.setPortletMode(PortletMode.VIEW);
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
            PortletRequestDispatcher dispatcher = getPortletContext().getRequestDispatcher("/portlets/jsp/myNotifications/"+pageName);
            dispatcher.include(request, response);
        } catch (IOException ioe) {
            throw new PortletException(ioe);
        }
    }
}
