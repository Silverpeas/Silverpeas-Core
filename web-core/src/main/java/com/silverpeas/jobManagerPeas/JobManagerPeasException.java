/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/

/*
 * JobManagerPeasException.java
 */

package com.silverpeas.jobManagerPeas;

import com.stratelia.webactiv.util.exception.*;

/*
 * CVS Informations
 * 
 * $Id: JobManagerPeasException.java,v 1.1.1.1 2002/08/06 14:47:55 nchaix Exp $
 * 
 * $Log: JobManagerPeasException.java,v $
 * Revision 1.1.1.1  2002/08/06 14:47:55  nchaix
 * no message
 *
 * Revision 1.1  2002/03/20 17:19:57  tleroi
 * Add jobManagerPeas
 *
 */
 
/**
 * Class declaration
 *
 *
 * @author
 */
public class JobManagerPeasException extends SilverpeasException
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
    public JobManagerPeasException(String callingClass, int errorLevel, String message)
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
    public JobManagerPeasException(String callingClass, int errorLevel, String message, String extraParams)
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
    public JobManagerPeasException(String callingClass, int errorLevel, String message, Exception nested)
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
    public JobManagerPeasException(String callingClass, int errorLevel, String message, String extraParams, Exception nested)
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
        return "jobManagerPeas";
    }

}
