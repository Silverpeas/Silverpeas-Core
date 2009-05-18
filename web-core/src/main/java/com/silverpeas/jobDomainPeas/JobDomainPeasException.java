/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/

/*
 * JobDomainPeasException.java
 */

package com.silverpeas.jobDomainPeas;

import com.stratelia.webactiv.util.exception.SilverpeasException;

/*
 * CVS Informations
 * 
 * $Id: JobDomainPeasException.java,v 1.2 2004/09/28 12:45:27 neysseri Exp $
 * 
 * $Log: JobDomainPeasException.java,v $
 * Revision 1.2  2004/09/28 12:45:27  neysseri
 * Extension de la longueur du login (de 20 à 50 caractères) + nettoyage sources
 *
 * Revision 1.1.1.1  2002/08/06 14:47:55  nchaix
 * no message
 *
 * Revision 1.1  2002/03/25 10:41:16  tleroi
 * Add jobDomainPeas
 *
 *
 */
 
/**
 * Class declaration
 *
 *
 * @author
 */
public class JobDomainPeasException extends SilverpeasException
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
    public JobDomainPeasException(String callingClass, int errorLevel, String message)
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
    public JobDomainPeasException(String callingClass, int errorLevel, String message, String extraParams)
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
    public JobDomainPeasException(String callingClass, int errorLevel, String message, Exception nested)
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
    public JobDomainPeasException(String callingClass, int errorLevel, String message, String extraParams, Exception nested)
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
        return "jobDomainPeas";
    }

}
