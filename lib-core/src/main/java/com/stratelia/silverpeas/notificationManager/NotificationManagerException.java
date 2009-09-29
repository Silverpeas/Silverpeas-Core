package com.stratelia.silverpeas.notificationManager;

import com.stratelia.webactiv.util.exception.SilverpeasException;

public class NotificationManagerException extends SilverpeasException {
  /**
   * --------------------------------------------------------------------------
   * constructor constructor
   */
  public NotificationManagerException(String callingClass, int errorLevel,
      String message) {
    super(callingClass, errorLevel, message);
  }

  public NotificationManagerException(String callingClass, int errorLevel,
      String message, String extraParams) {
    super(callingClass, errorLevel, message, extraParams);
  }

  public NotificationManagerException(String callingClass, int errorLevel,
      String message, Exception nested) {
    super(callingClass, errorLevel, message, nested);
  }

  public NotificationManagerException(String callingClass, int errorLevel,
      String message, String extraParams, Exception nested) {
    super(callingClass, errorLevel, message, extraParams, nested);
  }

  /**
   * --------------------------------------------------------------------------
   * getModule getModule
   */
  public String getModule() {
    return "notificationManager";
  }

}
