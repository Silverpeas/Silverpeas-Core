/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) 
 ---*/

package com.stratelia.webactiv.personalization.control.ejb;

import com.stratelia.webactiv.util.exception.*;

/*
 * CVS Informations
 * 
 * $Id: PersonalizationRuntimeException.java,v 1.1.1.1 2002/08/06 14:47:52 nchaix Exp $
 * 
 * $Log: PersonalizationRuntimeException.java,v $
 * Revision 1.1.1.1  2002/08/06 14:47:52  nchaix
 * no message
 *
 * Revision 1.1  2002/01/28 14:36:32  tleroi
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
public class PersonalizationRuntimeException extends SilverpeasRuntimeException {
  /**
   * --------------------------------------------------------------------------
   * constructor constructor
   */
  public PersonalizationRuntimeException(String callingClass, int errorLevel,
      String message) {
    super(callingClass, errorLevel, message);
  }

  public PersonalizationRuntimeException(String callingClass, int errorLevel,
      String message, String extraParams) {
    super(callingClass, errorLevel, message, extraParams);
  }

  public PersonalizationRuntimeException(String callingClass, int errorLevel,
      String message, Exception nested) {
    super(callingClass, errorLevel, message, nested);
  }

  public PersonalizationRuntimeException(String callingClass, int errorLevel,
      String message, String extraParams, Exception nested) {
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
  public String getModule() {
    return "personalization";
  }
}
