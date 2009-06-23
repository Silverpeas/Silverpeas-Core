package com.stratelia.webactiv.util.readingControl.model;

import com.stratelia.webactiv.util.exception.*;

public class ReadingControlRuntimeException extends SilverpeasRuntimeException
{

    /**--------------------------------------------------------------------------constructors
     * constructors
     */
    public ReadingControlRuntimeException (String callingClass, int errorLevel, String message)
    {
       super(callingClass, errorLevel, message);
    }

    public ReadingControlRuntimeException (String callingClass, int errorLevel, String message, String extraParams)
    {
        super(callingClass, errorLevel, message, extraParams);
    }

    public ReadingControlRuntimeException (String callingClass, int errorLevel, String message, Exception nested)
    {
        super(callingClass, errorLevel, message, nested);
    }

    public ReadingControlRuntimeException (String callingClass, int errorLevel, String message, String extraParams, Exception nested)
    {
        super(callingClass, errorLevel, message, extraParams, nested);
    }

    public String getModule() {
        return "readingControl";
    }

}