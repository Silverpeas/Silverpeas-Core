package com.silverpeas.external.filesharing.model;

import com.stratelia.webactiv.util.exception.*;

public class FileSharingRuntimeException extends SilverpeasRuntimeException {
  /**
   * --------------------------------------------------------------------------
   * constructors constructors
   */
  public FileSharingRuntimeException(String callingClass, int errorLevel,
      String message) {
    super(callingClass, errorLevel, message);
  }

  public FileSharingRuntimeException(String callingClass, int errorLevel,
      String message, String extraParams) {
    super(callingClass, errorLevel, message, extraParams);
  }

  public FileSharingRuntimeException(String callingClass, int errorLevel,
      String message, Exception nested) {
    super(callingClass, errorLevel, message, nested);
  }

  public FileSharingRuntimeException(String callingClass, int errorLevel,
      String message, String extraParams, Exception nested) {
    super(callingClass, errorLevel, message, extraParams, nested);
  }

  /**
   * --------------------------------------------------------------------------
   * getModule getModule
   */
  public String getModule() {
    return "FileSharing";
  }

}