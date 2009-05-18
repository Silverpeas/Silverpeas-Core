package com.silverpeas.tagcloud.ejb;

import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;


public class TagCloudRuntimeException extends SilverpeasRuntimeException
{

    public String getModule()
    {
        return "tagCloud";
    }

    public TagCloudRuntimeException(String callingClass, int errorLevel, String message)
    {
        super(callingClass, errorLevel, message);
    }
    
    public TagCloudRuntimeException(String callingClass, int errorLevel, String message,
    	String extraParams)
    {
        super(callingClass, errorLevel, message, extraParams);
    }
    
    public TagCloudRuntimeException(String callingClass, int errorLevel, String message,
    	Exception nested)
    {
        super(callingClass, errorLevel, message, nested);
    }
    
    public TagCloudRuntimeException(String callingClass, int errorLevel, String message,
    	String extraParams, Exception nested)
    {
        super(callingClass, errorLevel, message, extraParams, nested);
    }

}