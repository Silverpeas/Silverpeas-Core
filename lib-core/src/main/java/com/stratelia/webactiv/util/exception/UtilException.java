package com.stratelia.webactiv.util.exception;

public class UtilException extends SilverpeasException {

  /**
   * @deprecated
   */
  public UtilException(String msg) {
    super(msg);
  }

  /**
   * @deprecated
   */
  public UtilException(String msg, Exception e) {
    super(msg, e);
  }

  public UtilException(String callingClass, String message) {
    super(callingClass, ERROR, message);
  }

  public UtilException(String callingClass, String message, String extraParams) {
    super(callingClass, ERROR, message, extraParams);
  }

  public UtilException(String callingClass, String message, String extraParams,
      Exception nested) {
    super(callingClass, ERROR, message, extraParams, nested);
  }

  public UtilException(String callingClass, String message, Exception nested) {
    super(callingClass, ERROR, message, nested);
  }

  public UtilException(String callingClass, int errorLevel, String message) {
    super(callingClass, errorLevel, message);
  }

  public UtilException(String callingClass, int errorLevel, String message,
      String extraParams) {
    super(callingClass, errorLevel, message, extraParams);
  }

  public UtilException(String callingClass, int errorLevel, String message,
      Exception nested) {
    super(callingClass, errorLevel, message, nested);
  }

  public UtilException(String callingClass, int errorLevel, String message,
      String extraParams, Exception nested) {
    super(callingClass, errorLevel, message, extraParams, nested);
  }

  public String getModule() {
    return "util";
  }

}
