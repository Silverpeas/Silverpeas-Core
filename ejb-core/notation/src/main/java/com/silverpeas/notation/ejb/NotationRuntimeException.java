package com.silverpeas.notation.ejb;

import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;


public class NotationRuntimeException extends SilverpeasRuntimeException
{

    public String getModule()
    {
        return "notation";
    }

    public NotationRuntimeException(String callingClass, int errorLevel, String message)
    {
        super(callingClass, errorLevel, message);
    }
    
    public NotationRuntimeException(String callingClass, int errorLevel, String message,
    	String extraParams)
    {
        super(callingClass, errorLevel, message, extraParams);
    }
    
    public NotationRuntimeException(String callingClass, int errorLevel, String message,
    	Exception nested)
    {
        super(callingClass, errorLevel, message, nested);
    }
    
    public NotationRuntimeException(String callingClass, int errorLevel, String message,
    	String extraParams, Exception nested)
    {
        super(callingClass, errorLevel, message, extraParams, nested);
    }

}