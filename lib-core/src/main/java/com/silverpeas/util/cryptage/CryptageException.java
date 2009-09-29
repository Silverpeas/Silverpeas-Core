package com.silverpeas.util.cryptage;

import com.stratelia.webactiv.util.exception.SilverpeasException;

public class CryptageException extends SilverpeasException {
  public CryptageException(String callingClass, int errorLevel, String message,
      Exception nested) {
    super(callingClass, errorLevel, message, nested);
  }

  public CryptageException(String callingClass, int errorLevel, String message) {
    super(callingClass, errorLevel, message);
  }

  public String getModule() {
    return "util";
  }
}
