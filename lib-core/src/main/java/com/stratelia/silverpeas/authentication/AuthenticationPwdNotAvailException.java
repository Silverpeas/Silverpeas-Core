/*
 * AuthenticationPwdNotAvailException.java
 *
 * Created on 6 aout 2001
 */

package com.stratelia.silverpeas.authentication;

/**
 * 
 * @author tleroi
 * @version
 */
public class AuthenticationPwdNotAvailException extends AuthenticationException {
  /**
   * --------------------------------------------------------------------------
   * constructor constructor
   */
  public AuthenticationPwdNotAvailException(String callingClass,
      int errorLevel, String message) {
    super(callingClass, errorLevel, message);
  }

  public AuthenticationPwdNotAvailException(String callingClass,
      int errorLevel, String message, String extraParams) {
    super(callingClass, errorLevel, message, extraParams);
  }

  public AuthenticationPwdNotAvailException(String callingClass,
      int errorLevel, String message, Exception nested) {
    super(callingClass, errorLevel, message, nested);
  }

  public AuthenticationPwdNotAvailException(String callingClass,
      int errorLevel, String message, String extraParams, Exception nested) {
    super(callingClass, errorLevel, message, extraParams, nested);
  }
}
