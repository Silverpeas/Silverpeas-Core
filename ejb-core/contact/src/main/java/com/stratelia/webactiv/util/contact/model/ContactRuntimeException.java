package com.stratelia.webactiv.util.contact.model;

import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;

public class ContactRuntimeException extends SilverpeasRuntimeException {

  /**
   * --------------------------------------------------------------------------
   * constructors constructors
   */
  public ContactRuntimeException(String callingClass, int errorLevel,
      String message) {
    super(callingClass, errorLevel, message);
  }

  public ContactRuntimeException(String callingClass, int errorLevel,
      String message, String extraParams) {
    super(callingClass, errorLevel, message, extraParams);
  }

  public ContactRuntimeException(String callingClass, int errorLevel,
      String message, Exception nested) {
    super(callingClass, errorLevel, message, nested);
  }

  public ContactRuntimeException(String callingClass, int errorLevel,
      String message, String extraParams, Exception nested) {
    super(callingClass, errorLevel, message, extraParams, nested);
  }

  /**
   * --------------------------------------------------------------------------
   * getModule getModule
   */
  public String getModule() {
    return "contact";
  }

}
