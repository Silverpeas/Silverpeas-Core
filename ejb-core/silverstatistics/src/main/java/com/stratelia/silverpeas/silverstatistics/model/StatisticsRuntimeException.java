package com.stratelia.silverpeas.silverstatistics.model;

import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;

public class StatisticsRuntimeException extends SilverpeasRuntimeException {

  public StatisticsRuntimeException(String callingClass, int errorLevel,
      String message) {
    super(callingClass, errorLevel, message);
  }

  public StatisticsRuntimeException(String callingClass, int errorLevel,
      String message, String extraParams) {
    super(callingClass, errorLevel, message, extraParams);
  }

  public StatisticsRuntimeException(String callingClass, int errorLevel,
      String message, Exception nested) {
    super(callingClass, errorLevel, message, nested);
  }

  public StatisticsRuntimeException(String callingClass, int errorLevel,
      String message, String extraParams, Exception nested) {
    super(callingClass, errorLevel, message, extraParams, nested);
  }

  public String getModule() {
    return "statistics";
  }

}
