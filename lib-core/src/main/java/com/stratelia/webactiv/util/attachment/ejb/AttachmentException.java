/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/

/**
 * Titre : Silverpeas<p>
 * Description : This object provides the attachment execption<p>
 * Copyright : Copyright (c) Stratelia<p>
 * Société : Stratelia<p>
 * @author Jean-Claude Groccia
 * @version 1.0
 * Created on 20 août 2001, 14:07
 */


package com.stratelia.webactiv.util.attachment.ejb;


import com.stratelia.webactiv.util.exception.SilverpeasException;

/*
 * CVS Informations
 * 
 * $Id: AttachmentException.java,v 1.1 2003/09/17 09:18:21 neysseri Exp $
 * 
 * $Log: AttachmentException.java,v $
 * Revision 1.1  2003/09/17 09:18:21  neysseri
 * no message
 *
 * Revision 1.1.1.1  2002/08/06 14:47:40  nchaix
 * no message
 *
 * Revision 1.5  2002/01/21 18:00:31  mguillem
 * Stabilisation Lot2
 * Réorganisation des Router et SessionController
 * Suppression dans les fichiers *Exception de 'implements FromModule'
 *
 * Revision 1.4  2002/01/21 17:53:34  mguillem
 * Stabilisation Lot2
 * Réorganisation des Router et SessionController
 * Suppression dans les fichiers *Exception de 'implements FromModule'
 *
 * Revision 1.3  2002/01/08 10:28:09  groccia
 * no message
 *
 * Revision 1.2  2001/12/31 15:43:54  groccia
 * stabilisation
 *
 */

/**
 * Class declaration
 * 
 * 
 * @author
 */
public class AttachmentException extends SilverpeasException
{

    /**
     * Creates new AttachmentException
     */
    public AttachmentException(String callingClass, int errorLevel, String message)
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
    public AttachmentException(String callingClass, int errorLevel, String message, String extraParams)
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
    public AttachmentException(String callingClass, int errorLevel, String message, Exception nested)
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
    public AttachmentException(String callingClass, int errorLevel, String message, String extraParams, Exception nested)
    {
        super(callingClass, errorLevel, message, extraParams, nested);
    }

    /**
     * method of interface FromModule
     */
    public String getModule()
    {
        return "attachment";
    }

}


