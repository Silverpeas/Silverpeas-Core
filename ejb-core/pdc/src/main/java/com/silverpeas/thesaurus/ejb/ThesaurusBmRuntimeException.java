package com.silverpeas.thesaurus.ejb;

import com.stratelia.webactiv.util.exception.*;

public class ThesaurusBmRuntimeException extends SilverpeasRuntimeException {

  public ThesaurusBmRuntimeException(String callingClass, int errorLevel,
      String message) {
    super(callingClass, errorLevel, message);
  }

  public ThesaurusBmRuntimeException(String callingClass, int errorLevel,
      String message, String extraParams) {
    super(callingClass, errorLevel, message, extraParams);
  }

  public ThesaurusBmRuntimeException(String callingClass, int errorLevel,
      String message, Exception nested) {
    super(callingClass, errorLevel, message, nested);
  }

  public ThesaurusBmRuntimeException(String callingClass, int errorLevel,
      String message, String extraParams, Exception nested) {
    super(callingClass, errorLevel, message, extraParams, nested);
  }

  public String getModule() {
    return "thesaurus";
  }

}