package com.stratelia.webactiv.util.exception;

public class UtilTrappedException extends SilverpeasTrappedException {

  private static final long serialVersionUID = 1L;

  public UtilTrappedException(String callingClass, int errorLevel,
      String message) {
    super(callingClass, errorLevel, message);
  }

  public UtilTrappedException(String callingClass, int errorLevel,
      String message, String extraParams) {
    super(callingClass, errorLevel, message, extraParams);
  }

  public UtilTrappedException(String callingClass, int errorLevel,
      String message, Exception nested) {
    super(callingClass, errorLevel, message, nested);
  }

  public UtilTrappedException(String callingClass, int errorLevel,
      String message, String extraParams, Exception nested) {
    super(callingClass, errorLevel, message, extraParams, nested);
  }

  public String getModule() {
    return "util";
  }

}
