/*
 * Aliaksei_Budnikau
 * Date: Oct 14, 2002
 */
package com.silverpeas.pdcSubscription;

import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;

public class PdcSubscriptionRuntimeException extends SilverpeasRuntimeException {

  public PdcSubscriptionRuntimeException(String callingClass, int level,
      String message) {
    super(callingClass, ERROR, message);
  }

  public PdcSubscriptionRuntimeException(String callingClass, int level,
      String message, Object extraParams) {
    super(callingClass, ERROR, message, makeString(extraParams));
  }

  public PdcSubscriptionRuntimeException(String callingClass, int level,
      String message, Exception nested) {
    super(callingClass, ERROR, message, nested);
  }

  public PdcSubscriptionRuntimeException(String callingClass, int level,
      String message, Object extraParams, Exception nested) {
    super(callingClass, ERROR, message, makeString(extraParams), nested);
  }

  public String getModule() {
    return "pdc";
  }

  protected static String makeString(Object arg) {
    return (arg != null) ? arg.toString() : "null";
  }
}
