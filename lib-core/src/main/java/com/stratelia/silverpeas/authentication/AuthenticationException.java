/*
 * AuthenticationException.java
 *
 * Created on 6 aout 2001
 */

package com.stratelia.silverpeas.authentication;

import com.stratelia.webactiv.util.exception.SilverpeasException;

/** 
 *
 * @author  tleroi
 * @version 
 */
public class AuthenticationException extends SilverpeasException 
{
    /**--------------------------------------------------------------------------constructor
	 * constructor
	 */
    public AuthenticationException(String callingClass, int errorLevel, String message) 
    {
        super(callingClass, errorLevel, message);
    }
    public AuthenticationException(String callingClass, int errorLevel, String message, String extraParams) 
    {
        super(callingClass, errorLevel, message, extraParams);
    }
    public AuthenticationException(String callingClass, int errorLevel, String message, Exception nested) 
    {
        super(callingClass, errorLevel, message, nested);
    }
    public AuthenticationException(String callingClass, int errorLevel, String message, String extraParams, Exception nested) 
    {
        super(callingClass, errorLevel, message, extraParams, nested);
    }

	/**--------------------------------------------------------------------------getModule
	 * getModule
	 */
	public String getModule() {
	   return "authentication";
	}
}
