package com.stratelia.webactiv.util.questionContainer.model;

import com.stratelia.webactiv.util.exception.*;

public class QuestionContainerRuntimeException extends
    SilverpeasRuntimeException {

  public QuestionContainerRuntimeException(String callingClass, int errorLevel,
      String message) {
    super(callingClass, errorLevel, message);
  }

  public QuestionContainerRuntimeException(String callingClass, int errorLevel,
      String message, String extraParams) {
    super(callingClass, errorLevel, message, extraParams);
  }

  public QuestionContainerRuntimeException(String callingClass, int errorLevel,
      String message, Exception nested) {
    super(callingClass, errorLevel, message, nested);
  }

  public QuestionContainerRuntimeException(String callingClass, int errorLevel,
      String message, String extraParams, Exception nested) {
    super(callingClass, errorLevel, message, extraParams, nested);
  }

  public String getModule() {
    return "questionContainer";
  }

}
