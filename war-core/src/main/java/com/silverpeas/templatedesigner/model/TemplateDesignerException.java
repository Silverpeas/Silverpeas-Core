package com.silverpeas.templatedesigner.model;

import com.stratelia.webactiv.util.exception.SilverpeasException;

public class TemplateDesignerException extends SilverpeasException
{ 
  /**--------------------------------------------------------------------------constructors
     * constructors
     */
    public TemplateDesignerException (String callingClass, int errorLevel, String message)
    {
       super(callingClass, errorLevel, message);
    }

    public TemplateDesignerException (String callingClass, int errorLevel, String message, String extraParams)
    {
        super(callingClass, errorLevel, message, extraParams);
    }

    public TemplateDesignerException (String callingClass, int errorLevel, String message, Exception nested)
    {
        super(callingClass, errorLevel, message, nested);
    }

    public TemplateDesignerException (String callingClass, int errorLevel, String message, String extraParams, Exception nested)
    {
        super(callingClass, errorLevel, message, extraParams, nested);
    }

    /**--------------------------------------------------------------------------getModule
     * getModule
     */
    public String getModule() {
       return "TemplateDesigner";
    }

}