 /*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/

package com.stratelia.silverpeas.notificationserver.channel.silvermail;

/**
 * Titre : SILVERMAILRequestRouter.java
 * Description :
 * Copyright :    Copyright (c) 2001
 * Société :
 * @author eDurand
 * @version 1.0
 */

import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;

import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.ComponentSessionController;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.peasCore.servlets.ComponentRequestRouter;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.exception.SilverpeasException;

/**
 * Class declaration
 *
 *
 * @author
 * @version %I%, %G%
 */
public class SILVERMAILRequestRouter extends ComponentRequestRouter
{

	/**
	 * Package from which to attempt to load RequestHandler objects
	 */
	private static final String REQUEST_HANDLER_PACKAGE = "com.stratelia.silverpeas.notificationserver.channel.silvermail.requesthandlers";

	/**
	 * Name of the session bean that will be used for this application.
	 * This must be matched by the useBean actions in the JSPs.
	 */
	private static final String SESSION_BEAN_NAME = "SILVERMAIL";

	/**
	 * Hash table of RequestHandler instances, keyed by class name.
	 * This is used for performance optimization, to avoid the need
	 * to load a class by name to process each request.
	 */
	private HashMap				handlerHash = new HashMap();

    
    public SILVERMAILRequestRouter() {}

    public ComponentSessionController createComponentSessionController(MainSessionController mainSessionCtrl, ComponentContext context)
    {
        ComponentSessionController component = (ComponentSessionController) new SILVERMAILSessionController(mainSessionCtrl, context);
        return component;
    }

	/**
	 * This method has to be implemented in the component request router class.
	 * returns the session control bean name to be put in the request object
	 * ex : for almanach, returns "almanach"
	 */
	public String getSessionControlBeanName()
	{
		return SESSION_BEAN_NAME;
	}

	public String getDestination(String action, ComponentSessionController componentSC, HttpServletRequest request)
	{
		String					 destination = "/SILVERMAIL/jsp/" + action;
		String					 function = extractFunctionName(action);

		// If we have an action parameter, obtain the RequestHandler that implements
		// the action. This method will throw a exception if no handler is
		// associated with the action.
		SILVERMAILRequestHandler requestHandler;

		try
		{
			requestHandler = getHandlerInstance(function);
			((SILVERMAILSessionController) componentSC).setCurrentFunction(function);

			// Return the URL of the view the RequestHandler instance
			// chooses after doing the work of processing the request
			destination = requestHandler.handleRequest(componentSC, request);
		}
		catch (SILVERMAILException e)
		{
			SilverTrace.error("silvermail", "SILVERMAILRequestRouter.getDestination()", "root.EX_IGNORED", "", e);
		}

		return destination;
	}

	/**
	 * Locate and return the RequestHandler instance for this action.
	 * Instances are stored in a hash table once instantiated, so after the initial
	 * use of Class.forName() this method is very fast and does not rely on
	 * reflection.
	 */
	protected SILVERMAILRequestHandler getHandlerInstance(String action) throws SILVERMAILException
	{
		String					 handlerName = REQUEST_HANDLER_PACKAGE + "." + action;
		SILVERMAILRequestHandler requestHandler = (SILVERMAILRequestHandler) handlerHash.get(handlerName);

		if (requestHandler == null)
		{
			// We don't have a handler instance associated with this action,
			// so we need to instantiate one and put in in our hash table
			try
			{
				// Use reflection to load the class by name
				Class handlerClass = Class.forName(handlerName);

				// Check the class we obtained implements the RequestHandler interface
				if (!SILVERMAILRequestHandler.class.isAssignableFrom(handlerClass))
				{
					throw new SILVERMAILException("SILVERMAILRequestRouter.getHandlerInstance()",SilverpeasException.ERROR,"silvermail.EX_NOT_A_REQUESTHANDLER","Class=" + handlerName);
				}
				// Instantiate the request handler object
				requestHandler = (SILVERMAILRequestHandler) handlerClass.newInstance();
				// Save the instance so we don't have to load it dynamically to process
				// further requests from this user
				handlerHash.put(handlerName, requestHandler);
			}
			catch (ClassNotFoundException ex)
			{
				throw new SILVERMAILException("SILVERMAILRequestRouter.getHandlerInstance()",SilverpeasException.ERROR,"silvermail.EX_NO_HANDLER","Class=" + handlerName,ex);
			}
			catch (InstantiationException ex)
			{
				// It probably doesn't have a no-argument constructor
				throw new SILVERMAILException("SILVERMAILRequestRouter.getHandlerInstance()",SilverpeasException.ERROR,"silvermail.EX_CANT_BE_INSTANCIATED","Class=" + handlerName,ex);
			}
			catch (IllegalAccessException ex)
			{
				throw new SILVERMAILException("SILVERMAILRequestRouter.getHandlerInstance()",SilverpeasException.ERROR,"silvermail.EX_CANT_BE_INSTANCIATED","Class=" + handlerName,ex);
			}
		}

		// If we get to here, we have a valid RequestHandler instance,
		// whether it came from the hash table or from dynamical class loading
		return requestHandler;
	}

	/**
	 * Method declaration
	 *
	 *
	 * @param action
	 *
	 * @return
	 *
	 * @see
	 */
	protected String extractFunctionName(String action)
	{
		String result = action;

		if (action.endsWith(".jsp") == true)
		{
			result = action.substring(0, action.length() - 4);
		}
		return result;
	}

}
