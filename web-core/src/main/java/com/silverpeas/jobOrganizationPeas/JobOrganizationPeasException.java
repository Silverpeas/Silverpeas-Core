/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) 
 ---*/

/*
 * CVS Informations
 * 
 * $Id: JobOrganizationPeasException.java,v 1.1.1.1 2002/08/06 14:47:55 nchaix Exp $
 * 
 * $Log: JobOrganizationPeasException.java,v $
 * Revision 1.1.1.1  2002/08/06 14:47:55  nchaix
 * no message
 *
 * Revision 1.1  2002/04/05 05:42:40  tleroi
 * no message
 *
 *
 */

package com.silverpeas.jobOrganizationPeas;

import com.stratelia.webactiv.util.exception.*;

/**
 * Class declaration
 * 
 * 
 * @author Thierry Leroi, Jean-Christophe Carry
 */
public class JobOrganizationPeasException extends SilverpeasException {

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
  public JobOrganizationPeasException(String callingClass, int errorLevel,
      String message) {
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
  public JobOrganizationPeasException(String callingClass, int errorLevel,
      String message, String extraParams) {
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
  public JobOrganizationPeasException(String callingClass, int errorLevel,
      String message, Exception nested) {
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
  public JobOrganizationPeasException(String callingClass, int errorLevel,
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
    return "jobOrganizationPeas";
  }

}
