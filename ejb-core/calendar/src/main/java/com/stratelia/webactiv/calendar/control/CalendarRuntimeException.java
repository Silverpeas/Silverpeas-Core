package com.stratelia.webactiv.calendar.control;


import com.stratelia.webactiv.util.exception.*;

public class CalendarRuntimeException extends SilverpeasRuntimeException {
  
      /**
     * method of interface FromModule
     */
    public String getModule()
    {
        return "calendar";
    }

  public CalendarRuntimeException(String callingClass, int errorLevel, String message) 
    {
        super(callingClass, errorLevel, message);
    }

    public CalendarRuntimeException(String callingClass, int errorLevel, String message, String extraParams) 
    {
        super(callingClass, errorLevel, message, extraParams);
    }

    public CalendarRuntimeException(String callingClass, int errorLevel, String message, Exception nested) 
    {
        super(callingClass, errorLevel, message, nested);
    }

    public CalendarRuntimeException(String callingClass, int errorLevel, String message, String extraParams, Exception nested) 
    {
      super(callingClass, errorLevel, message, extraParams, nested);
    }
}

