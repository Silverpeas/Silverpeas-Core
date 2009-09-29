/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) 
 ---*/

package com.stratelia.silverpeas.notificationserver;

import com.stratelia.webactiv.util.exception.SilverpeasException;

/**
 * Titre : Description : Copyright : Copyright (c) 2001 Société :
 * 
 * @author eDurand
 * @version 1.0
 */

public class NotificationServerException extends SilverpeasException {

  /**
   * --------------------------------------------------------------------------
   * constructor constructor
   */
  public NotificationServerException(String callingClass, int errorLevel,
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
  public NotificationServerException(String callingClass, int errorLevel,
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
  public NotificationServerException(String callingClass, int errorLevel,
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
  public NotificationServerException(String callingClass, int errorLevel,
      String message, String extraParams, Exception nested) {
    super(callingClass, errorLevel, message, extraParams, nested);
  }

  /**
   * --------------------------------------------------------------------------
   * getModule getModule
   */
  public String getModule() {
    return "notificationServer";
  }

}
