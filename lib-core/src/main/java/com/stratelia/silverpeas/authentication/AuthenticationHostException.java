/*
 * AuthenticationHostException.java
 *
 * Created on 6 aout 2001
 */

package com.stratelia.silverpeas.authentication;

/**
 * 
 * @author tleroi
 * @version
 */
public class AuthenticationHostException extends AuthenticationException {
  /**
   * --------------------------------------------------------------------------
   * constructor constructor
   */
  public AuthenticationHostException(String callingClass, int errorLevel,
      String message) {
    super(callingClass, errorLevel, message);
  }

  public AuthenticationHostException(String callingClass, int errorLevel,
      String message, String extraParams) {
    super(callingClass, errorLevel, message, extraParams);
  }

  public AuthenticationHostException(String callingClass, int errorLevel,
      String message, Exception nested) {
    super(callingClass, errorLevel, message, nested);
  }

  public AuthenticationHostException(String callingClass, int errorLevel,
      String message, String extraParams, Exception nested) {
    super(callingClass, errorLevel, message, extraParams, nested);
  }
}
