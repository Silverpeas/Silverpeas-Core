package com.silverpeas.jobDomainPeas;

import com.stratelia.webactiv.util.exception.*;

public class JobDomainPeasRuntimeException extends SilverpeasRuntimeException {

  /**
	 * 
	 */
  private static final long serialVersionUID = 1L;

  public JobDomainPeasRuntimeException(String callingClass, int errorLevel,
      String message) {
    super(callingClass, errorLevel, message);
  }

  public JobDomainPeasRuntimeException(String callingClass, int errorLevel,
      String message, String extraParams) {
    super(callingClass, errorLevel, message, extraParams);
  }

  public JobDomainPeasRuntimeException(String callingClass, int errorLevel,
      String message, Exception nested) {
    super(callingClass, errorLevel, message, nested);
  }

  public JobDomainPeasRuntimeException(String callingClass, int errorLevel,
      String message, String extraParams, Exception nested) {
    super(callingClass, errorLevel, message, extraParams, nested);
  }

  public String getModule() {
    return "jobDomainPeas";
  }

}
