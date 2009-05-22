package com.silverpeas.myLinks;

import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;

public class MyLinksRuntimeException extends SilverpeasRuntimeException
{ 
  /**--------------------------------------------------------------------------constructors
     * constructors
     */
    public MyLinksRuntimeException (String callingClass, int errorLevel, String message)
    {
       super(callingClass, errorLevel, message);
    }

    public MyLinksRuntimeException (String callingClass, int errorLevel, String message, String extraParams)
    {
        super(callingClass, errorLevel, message, extraParams);
    }

    public MyLinksRuntimeException (String callingClass, int errorLevel, String message, Exception nested)
    {
        super(callingClass, errorLevel, message, nested);
    }

    public MyLinksRuntimeException (String callingClass, int errorLevel, String message, String extraParams, Exception nested)
    {
        super(callingClass, errorLevel, message, extraParams, nested);
    }

    /**--------------------------------------------------------------------------getModule
     * getModule
     */
    public String getModule() {
       return "myLinks";
    }

}
