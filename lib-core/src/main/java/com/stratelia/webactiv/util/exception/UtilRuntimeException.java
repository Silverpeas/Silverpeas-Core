package com.stratelia.webactiv.util.exception;

public class UtilRuntimeException extends SilverpeasRuntimeException implements
    FromModule {

  public UtilRuntimeException(String message) {
    super(message);
  }

  public UtilRuntimeException(String message, Exception nested) {
    super(message, nested);
  }

  public String getModule() {
    return "util";
  }

}
