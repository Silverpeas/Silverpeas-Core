package com.silverpeas.workflowdesigner.model;

import com.stratelia.webactiv.util.exception.SilverpeasException;

public class WorkflowDesignerException extends SilverpeasException
{ 
  /**--------------------------------------------------------------------------constructors
     * constructors
     */
    public WorkflowDesignerException (String callingClass, int errorLevel, String message)
    {
       super(callingClass, errorLevel, message);
    }

    public WorkflowDesignerException (String callingClass, int errorLevel, String message, String extraParams)
    {
        super(callingClass, errorLevel, message, extraParams);
    }

    public WorkflowDesignerException (String callingClass, int errorLevel, String message, Exception nested)
    {
        super(callingClass, errorLevel, message, nested);
    }

    public WorkflowDesignerException (String callingClass, int errorLevel, String message, String extraParams, Exception nested)
    {
        super(callingClass, errorLevel, message, extraParams, nested);
    }

    /**--------------------------------------------------------------------------getModule
     * getModule
     */
    public String getModule() {
       return "WorkflowDesigner";
    }

}