/*
 * AuthenticationBadCredentialException.java
 *
 * Created on 6 aout 2001
 */

package com.stratelia.silverpeas.authentication;

/**
 * 
 * @author tleroi
 * @version
 */
public class AuthenticationBadCredentialException extends
    AuthenticationException {
  /**
   * --------------------------------------------------------------------------
   * constructor constructor
   */
  public AuthenticationBadCredentialException(String callingClass,
      int errorLevel, String message) {
    super(callingClass, errorLevel, message);
  }

  public AuthenticationBadCredentialException(String callingClass,
      int errorLevel, String message, String extraParams) {
    super(callingClass, errorLevel, message, extraParams);
  }

  public AuthenticationBadCredentialException(String callingClass,
      int errorLevel, String message, Exception nested) {
    super(callingClass, errorLevel, message, nested);
  }

  public AuthenticationBadCredentialException(String callingClass,
      int errorLevel, String message, String extraParams, Exception nested) {
    super(callingClass, errorLevel, message, extraParams, nested);
  }
}
