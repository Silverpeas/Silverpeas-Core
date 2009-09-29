package com.stratelia.webactiv.util.questionResult.model;

import com.stratelia.webactiv.util.exception.*;

public class QuestionResultRuntimeException extends SilverpeasRuntimeException {

  public QuestionResultRuntimeException(String callingClass, int errorLevel,
      String message) {
    super(callingClass, errorLevel, message);
  }

  public QuestionResultRuntimeException(String callingClass, int errorLevel,
      String message, String extraParams) {
    super(callingClass, errorLevel, message, extraParams);
  }

  public QuestionResultRuntimeException(String callingClass, int errorLevel,
      String message, Exception nested) {
    super(callingClass, errorLevel, message, nested);
  }

  public QuestionResultRuntimeException(String callingClass, int errorLevel,
      String message, String extraParams, Exception nested) {
    super(callingClass, errorLevel, message, extraParams, nested);
  }

  public String getModule() {
    return "questionResult";
  }

}
