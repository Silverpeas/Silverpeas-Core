/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/

package com.stratelia.silverpeas.peasCore;


import com.stratelia.webactiv.util.exception.*;

/*
 * CVS Informations
 * 
 * $Id: PeasCoreRuntimeException.java,v 1.2 2002/10/28 16:45:37 neysseri Exp $
 * 
 * $Log: PeasCoreRuntimeException.java,v $
 * Revision 1.2  2002/10/28 16:45:37  neysseri
 * Branch "InterestCenters" merging
 *
 * Revision 1.1.1.1  2002/08/06 14:47:52  nchaix
 * no message
 *
 * Revision 1.1  2002/01/28 15:28:21  tleroi
 * Split clipboard and personalization
 *
 * Revision 1.2  2002/01/18 18:04:07  tleroi
 * Centralize URLS + Stabilisation Lot 2 - SilverTrace et Exceptions
 *
 */
 
/**
 * Class declaration
 *
 *
 * @author t.leroi
 */
public class PeasCoreRuntimeException extends SilverpeasRuntimeException
{
	/**--------------------------------------------------------------------------constructor
	 * constructor
	 */
    public PeasCoreRuntimeException(String callingClass, int errorLevel, String message) 
    {
        super(callingClass, errorLevel, message);
    }
    public PeasCoreRuntimeException(String callingClass, int errorLevel, String message, String extraParams) 
    {
        super(callingClass, errorLevel, message, extraParams);
    }
    public PeasCoreRuntimeException(String callingClass, int errorLevel, String message, Exception nested) 
    {
        super(callingClass, errorLevel, message, nested);
    }
    public PeasCoreRuntimeException(String callingClass, int errorLevel, String message, String extraParams, Exception nested) 
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
        return "peasCore";
    }
}

