/*
 * AuthenticationPwdNotAvailException.java
 *
 * Created on 6 aout 2001
 */

package com.stratelia.silverpeas.authentication;

/** 
 *
 * @author  tleroi
 * @version 
 */
public class AuthenticationPasswordAboutToExpireException extends AuthenticationException
{
    /**--------------------------------------------------------------------------constructor
	 * constructor
	 */
    public AuthenticationPasswordAboutToExpireException(String callingClass, int errorLevel, String message) 
    {
        super(callingClass, errorLevel, message);
    }
    public AuthenticationPasswordAboutToExpireException(String callingClass, int errorLevel, String message, String extraParams) 
    {
        super(callingClass, errorLevel, message, extraParams);
    }
    public AuthenticationPasswordAboutToExpireException(String callingClass, int errorLevel, String message, Exception nested) 
    {
        super(callingClass, errorLevel, message, nested);
    }
    public AuthenticationPasswordAboutToExpireException(String callingClass, int errorLevel, String message, String extraParams, Exception nested) 
    {
        super(callingClass, errorLevel, message, extraParams, nested);
    }
}
