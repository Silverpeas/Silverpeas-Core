package com.silverpeas.peasUtil;

import com.stratelia.webactiv.util.exception.SilverpeasException;

public class AccessForbiddenException extends SilverpeasException {
  public AccessForbiddenException(String callingClass, int errorLevel,
      String message) {
    super(callingClass, errorLevel, message, null, null);
  }

  public String getModule() {
    return "peasUtil";
  }
}
