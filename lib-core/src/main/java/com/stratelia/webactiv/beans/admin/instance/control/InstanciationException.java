/** 
 *
 * @author  akhadrou
 * @version 
 */

package com.stratelia.webactiv.beans.admin.instance.control;

import com.stratelia.webactiv.util.exception.SilverpeasException;

public  class InstanciationException extends SilverpeasException
{
	/*
	 * @Deprecated
	 */
     public InstanciationException (String sMessage)
     {
         super("NoClass", SilverpeasException.ERROR, sMessage);
     }

	/**
	 * constructor
	 */
    public InstanciationException(String callingClass, int errorLevel, String message) 
    {
        super(callingClass, errorLevel, message);
    }
    public InstanciationException(String callingClass, int errorLevel, String message, String extraParams) 
    {
        super(callingClass, errorLevel, message, extraParams);
    }
    public InstanciationException(String callingClass, int errorLevel, String message, Exception nested) 
    {
        super(callingClass, errorLevel, message, nested);
    }
    public InstanciationException(String callingClass, int errorLevel, String message, String extraParams, Exception nested) 
    {
        super(callingClass, errorLevel, message, extraParams, nested);
    }

	/**
	 * getModule
	 */
	public String getModule() {
	   return "admin";
	}
}