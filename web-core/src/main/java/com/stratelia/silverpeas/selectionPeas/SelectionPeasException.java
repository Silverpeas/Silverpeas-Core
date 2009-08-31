/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/

package com.stratelia.silverpeas.selectionPeas;

import com.stratelia.webactiv.util.exception.SilverpeasException;

/**
 * Class declaration
 *
 *
 * @author
 */
public class SelectionPeasException extends SilverpeasException
{

    /**
     * Constructor declaration
     *
     *
     * @param callingClass
     * @param errorLevel
     * @param message
     *
     * @see
     */
    public SelectionPeasException(String callingClass, int errorLevel, String message)
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
    public SelectionPeasException(String callingClass, int errorLevel, String message, String extraParams)
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
    public SelectionPeasException(String callingClass, int errorLevel, String message, Exception nested)
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
    public SelectionPeasException(String callingClass, int errorLevel, String message, String extraParams, Exception nested)
    {
        super(callingClass, errorLevel, message, extraParams, nested);
    }

    /**
     * Method declaration
     *
     *
     * @return
     *
     * @see
     */
    public String getModule()
    {
        return "selectionPeas";
    }

}
