package com.stratelia.webactiv.agenda.control;

import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;

public class AgendaRuntimeException extends SilverpeasRuntimeException {

  /**
   * method of interface FromModule
   */
  public String getModule() {
    return "agenda";
  }

  public AgendaRuntimeException(String callingClass, int errorLevel,
      String message) {
    super(callingClass, errorLevel, message);
  }

  public AgendaRuntimeException(String callingClass, int errorLevel,
      String message, String extraParams) {
    super(callingClass, errorLevel, message, extraParams);
  }

  public AgendaRuntimeException(String callingClass, int errorLevel,
      String message, Exception nested) {
    super(callingClass, errorLevel, message, nested);
  }

  public AgendaRuntimeException(String callingClass, int errorLevel,
      String message, String extraParams, Exception nested) {
    super(callingClass, errorLevel, message, extraParams, nested);
  }
}
