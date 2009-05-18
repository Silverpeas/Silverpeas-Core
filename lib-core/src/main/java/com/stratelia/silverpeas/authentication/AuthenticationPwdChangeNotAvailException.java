/*
 * AuthenticationPwdChangeNotAvailException
 *
 * Created on 6 aout 2001
 */

package com.stratelia.silverpeas.authentication;

/**
 * 
 * @author Ludovic Bertin
 *
 */
public class AuthenticationPwdChangeNotAvailException extends AuthenticationException
{
    /**--------------------------------------------------------------------------constructor
	 * constructor
	 */
    public AuthenticationPwdChangeNotAvailException(String callingClass, int errorLevel, String message) 
    {
        super(callingClass, errorLevel, message);
    }
    public AuthenticationPwdChangeNotAvailException(String callingClass, int errorLevel, String message, String extraParams) 
    {
        super(callingClass, errorLevel, message, extraParams);
    }
    public AuthenticationPwdChangeNotAvailException(String callingClass, int errorLevel, String message, Exception nested) 
    {
        super(callingClass, errorLevel, message, nested);
    }
    public AuthenticationPwdChangeNotAvailException(String callingClass, int errorLevel, String message, String extraParams, Exception nested) 
    {
        super(callingClass, errorLevel, message, extraParams, nested);
    }
}
