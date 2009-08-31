/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/

/*
 * @author Dominique Blot
 * @version 1.0
 */

package com.silverpeas.util.clipboard;


/*
 * CVS Informations
 * 
 * $Id: SKDException.java,v 1.1.1.1 2002/08/06 14:47:46 nchaix Exp $
 * 
 * $Log: SKDException.java,v $
 * Revision 1.1.1.1  2002/08/06 14:47:46  nchaix
 * no message
 *
 * Revision 1.2  2002/01/04 14:03:48  mmarengo
 * Stabilisation Lot 2
 * SilverTrace
 * Exception
 *
 */

/**
 * Class declaration
 * 
 * 
 * @author
 */
public class SKDException extends ClipboardException
{

    /**
     * --------------------------------------------------------------------------constructor
     * constructor
     */
    public SKDException(String callingClass, int errorLevel, String message)
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
    public SKDException(String callingClass, int errorLevel, String message, String extraParams)
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
    public SKDException(String callingClass, int errorLevel, String message, Exception nested)
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
    public SKDException(String callingClass, int errorLevel, String message, String extraParams, Exception nested)
    {
        super(callingClass, errorLevel, message, extraParams, nested);
    }

    /**
     * --------------------------------------------------------------------------getModule
     * getModule
     */
    public String getModule()
    {
        return "clipboard";
    }

}
