/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) 
 ---*/

package com.stratelia.silverpeas.contentManager;

import com.stratelia.webactiv.util.exception.*;

/*
 * CVS Informations
 * 
 * $Id: ContentManagerException.java,v 1.1.1.1 2002/08/06 14:47:47 nchaix Exp $
 * 
 * $Log: ContentManagerException.java,v $
 * Revision 1.1.1.1  2002/08/06 14:47:47  nchaix
 * no message
 *
 * Revision 1.1  2002/02/25 09:05:40  nchaix
 * no message
 *
 * Revision 1.1  2002/02/21 16:32:24  nchaix
 * no message
 *
 * Revision 1.1  2002/02/19 14:05:12  nchaix
 * no message
 *
 * Revision 1.1  2002/01/18 18:04:07  tleroi
 * Centralize URLS + Stabilisation Lot 2 - SilverTrace et Exceptions
 *
 */

/**
 * Class declaration
 * 
 * 
 * @author n.chaix
 */
public class ContentManagerException extends SilverpeasException {
  /**
   * --------------------------------------------------------------------------
   * constructor constructor
   */
  public ContentManagerException(String callingClass, int errorLevel,
      String message) {
    super(callingClass, errorLevel, message);
  }

  public ContentManagerException(String callingClass, int errorLevel,
      String message, String extraParams) {
    super(callingClass, errorLevel, message, extraParams);
  }

  public ContentManagerException(String callingClass, int errorLevel,
      String message, Exception nested) {
    super(callingClass, errorLevel, message, nested);
  }

  public ContentManagerException(String callingClass, int errorLevel,
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
    return "contentManager";
  }
}
