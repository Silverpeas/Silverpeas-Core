/*
 * Aliaksei_Budnikau
 * Date: Oct 14, 2002
 */
package com.silverpeas.interestCenter;

import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;

public class InterestCenterRuntimeException extends SilverpeasRuntimeException {

  public InterestCenterRuntimeException(String callingClass, String message) {
    super(callingClass, ERROR, message);
  }

  public InterestCenterRuntimeException(String callingClass, String message,
      String extraParams) {
    super(callingClass, ERROR, message, extraParams);
  }

  public InterestCenterRuntimeException(String callingClass, String message,
      Exception nested) {
    super(callingClass, ERROR, message, nested);
  }

  public InterestCenterRuntimeException(String callingClass, String message,
      String extraParams, Exception nested) {
    super(callingClass, ERROR, message, extraParams, nested);
  }

  public String getModule() {
    return "InterestCenter";
  }
}
