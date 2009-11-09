package com.silverpeas.workflowdesigner.servlets;

import javax.servlet.http.HttpServletRequest;

import com.silverpeas.workflow.api.WorkflowException;
import com.silverpeas.workflowdesigner.control.WorkflowDesignerSessionController;
import com.silverpeas.workflowdesigner.model.WorkflowDesignerException;

/**
 * This interface describes a handler of an atomic function of the Workflow Designer Request Router
 */
public interface FunctionHandler
{
    /**
     * Handle the function do the processing and return the URL of the response
     * 
     * @param function            the name of the function to handle
     * @param workflowDesignerSC  the session controller
     * @param request             the HTTP request
     * @return the name of the destination JSP, without the path part  
     * @throws WorkflowDesignerException when something goes wrong
     * @throws WorkflowException when something goes wrong
     */
    public String getDestination(String                            function, 
                                 WorkflowDesignerSessionController workflowDesignerSC, 
                                 HttpServletRequest                request) throws WorkflowDesignerException, WorkflowException;
}
