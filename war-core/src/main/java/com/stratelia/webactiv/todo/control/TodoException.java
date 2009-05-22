/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/

/*
 * TodoException.java
 * 
 * Created on 18/12/2001
 */

package com.stratelia.webactiv.todo.control;

import com.stratelia.webactiv.util.exception.SilverpeasException;

/**
 * 
 * @author  mmarengo
 * @version 1.0
 */
public class TodoException extends SilverpeasException
{

    /**
     * Creates new TodoException
     */
    public TodoException(String callingClass, int errorLevel, String message)
    {
        super(callingClass, errorLevel, message);
    }

    /**
     * Constructor declaration
     * 
     * 
     * @param callingClass
     * @param errorLevel
     * @param message
     * @param extraParams
     * 
     * @see
     */
    public TodoException(String callingClass, int errorLevel, String message, String extraParams)
    {
        super(callingClass, errorLevel, message, extraParams);
    }

    /**
     * Constructor declaration
     * 
     * 
     * @param callingClass
     * @param errorLevel
     * @param message
     * @param nested
     * 
     * @see
     */
    public TodoException(String callingClass, int errorLevel, String message, Exception nested)
    {
        super(callingClass, errorLevel, message, nested);
    }

    /**
     * Constructor declaration
     * 
     * 
     * @param callingClass
     * @param errorLevel
     * @param message
     * @param extraParams
     * @param nested
     * 
     * @see
     */
    public TodoException(String callingClass, int errorLevel, String message, String extraParams, Exception nested)
    {
        super(callingClass, errorLevel, message, extraParams, nested);
    }

    /**
     * getModule
     */
    public String getModule()
    {
        return "todo";
    }

}
