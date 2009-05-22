package com.silverpeas.portlets;

import java.io.IOException;

import javax.portlet.GenericPortlet;
import javax.portlet.PortletException;
import javax.portlet.PortletRequestDispatcher;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import com.silverpeas.util.StringUtil;

public class HelloSilverpeasPortlet extends GenericPortlet {
	
	public void doView(RenderRequest request, RenderResponse response) throws PortletException, IOException
	{
        include(request, response, "portlet.jsp");
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
            PortletRequestDispatcher dispatcher = getPortletContext().getRequestDispatcher("/portlets/jsp/helloSilverpeas/"+pageName);
            dispatcher.include(request, response);
        } catch (IOException ioe) {
            throw new PortletException(ioe);
        }
    }
}
