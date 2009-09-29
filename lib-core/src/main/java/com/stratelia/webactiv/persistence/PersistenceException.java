package com.stratelia.webactiv.persistence;

import com.stratelia.webactiv.util.exception.SilverpeasException;

/**
 * Title: Description: Copyright: Copyright (c) 2001 Company:
 * 
 * @author cbonin
 * @version 1.0
 */

public class PersistenceException extends SilverpeasException {

  /**
   * --------------------------------------------------------------------------
   * constructor constructor
   */
  public PersistenceException(String callingClass, int errorLevel,
      String message) {
    super(callingClass, errorLevel, message);
  }

  public PersistenceException(String callingClass, int errorLevel,
      String message, String extraParams) {
    super(callingClass, errorLevel, message, extraParams);
  }

  public PersistenceException(String callingClass, int errorLevel,
      String message, Exception nested) {
    super(callingClass, errorLevel, message, nested);
  }

  public PersistenceException(String callingClass, int errorLevel,
      String message, String extraParams, Exception nested) {
    super(callingClass, errorLevel, message, extraParams, nested);
  }

  /**
   * --------------------------------------------------------------------------
   * getModule getModule
   */
  public String getModule() {
    return "persistence";
  }
}
