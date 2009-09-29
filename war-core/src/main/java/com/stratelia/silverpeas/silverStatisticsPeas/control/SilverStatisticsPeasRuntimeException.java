package com.stratelia.silverpeas.silverStatisticsPeas.control;

import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;

public class SilverStatisticsPeasRuntimeException extends
    SilverpeasRuntimeException {

  public SilverStatisticsPeasRuntimeException(String callingClass,
      int errorLevel, String message) {
    super(callingClass, errorLevel, message);
  }

  public SilverStatisticsPeasRuntimeException(String callingClass,
      int errorLevel, String message, String extraParams) {
    super(callingClass, errorLevel, message, extraParams);
  }

  public SilverStatisticsPeasRuntimeException(String callingClass,
      int errorLevel, String message, Exception nested) {
    super(callingClass, errorLevel, message, nested);
  }

  public SilverStatisticsPeasRuntimeException(String callingClass,
      int errorLevel, String message, String extraParams, Exception nested) {
    super(callingClass, errorLevel, message, extraParams, nested);
  }

  public String getModule() {
    return "silverStatisticsPeas";
  }

}
