/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/

/*
 * JobStartPagePeasException.java
 */

package com.silverpeas.jobStartPagePeas;

import com.stratelia.webactiv.util.exception.SilverpeasException;


/*
 * CVS Informations
 * 
 * $Id: JobStartPagePeasException.java,v 1.2 2003/07/09 16:47:29 cbonin Exp $
 * 
 * $Log: JobStartPagePeasException.java,v $
 * Revision 1.2  2003/07/09 16:47:29  cbonin
 * Applet Admin -> JSP
 *
 * Revision 1.1.1.1  2003/01/14 13:41:18  lbertin
 * no message
 *
 * Revision 1.1  2002/03/25 09:58:15  emouchel
 * ajout de JobStartPage
 *
 * Revision 1.1  2002/03/20 17:19:57  tleroi
 * Add jobStartPagePeas
 *
 */
 
/**
 * Class declaration
 *
 *
 * @author
 */
public class JobStartPagePeasException extends SilverpeasException
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
    public JobStartPagePeasException(String callingClass, int errorLevel, String message)
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
    public JobStartPagePeasException(String callingClass, int errorLevel, String message, String extraParams)
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
    public JobStartPagePeasException(String callingClass, int errorLevel, String message, Exception nested)
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
    public JobStartPagePeasException(String callingClass, int errorLevel, String message, String extraParams, Exception nested)
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
        return "jobStartPagePeas";
    }

}
