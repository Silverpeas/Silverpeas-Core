/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/

package com.stratelia.webactiv.util.statistic.model;

import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;

/*
 * CVS Informations
 * 
 * $Id: StatisticRuntimeException.java,v 1.2 2007/06/14 08:37:55 neysseri Exp $
 * 
 * $Log: StatisticRuntimeException.java,v $
 * Revision 1.2  2007/06/14 08:37:55  neysseri
 * no message
 *
 * Revision 1.1.1.1.20.1  2007/06/14 08:22:38  neysseri
 * no message
 *
 * Revision 1.1.1.1  2002/08/06 14:47:53  nchaix
 * no message
 *
 * Revision 1.3  2002/01/22 09:25:48  mguillem
 * Stabilisation Lot2
 * Réorganisation des Router et SessionController
 * Suppression dans les fichiers *Exception de 'implements FromModule'
 *
 * Revision 1.2  2001/12/26 12:01:47  nchaix
 * no message
 *
 */
 
/**
 * Class declaration
 *
 *
 * @author
 */
public class StatisticRuntimeException extends SilverpeasRuntimeException
{

    public StatisticRuntimeException(String callingClass, int errorLevel, String message)
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
    public StatisticRuntimeException(String callingClass, int errorLevel, String message, String extraParams)
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
    public StatisticRuntimeException(String callingClass, int errorLevel, String message, Exception nested)
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
    public StatisticRuntimeException(String callingClass, int errorLevel, String message, String extraParams, Exception nested)
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
        return "statistic";
    }

}
