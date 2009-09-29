package com.stratelia.silverpeas.notificationUser;

import com.stratelia.webactiv.util.exception.*;

public class NotificationUserException extends SilverpeasException {
  /**
   * --------------------------------------------------------------------------
   * constructors constructors
   */
  public NotificationUserException(String callingClass, int errorLevel,
      String message) {
    super(callingClass, errorLevel, message);
  }

  public NotificationUserException(String callingClass, int errorLevel,
      String message, String extraParams) {
    super(callingClass, errorLevel, message, extraParams);
  }

  public NotificationUserException(String callingClass, int errorLevel,
      String message, Exception nested) {
    super(callingClass, errorLevel, message, nested);
  }

  public NotificationUserException(String callingClass, int errorLevel,
      String message, String extraParams, Exception nested) {
    super(callingClass, errorLevel, message, extraParams, nested);
  }

  /**
   * --------------------------------------------------------------------------
   * getModule getModule
   */
  public String getModule() {
    return "notificationUser";
  }

}
