package org.silverpeas.authentication.exception;

public class AuthenticationPasswordExpired extends AuthenticationException {
  public AuthenticationPasswordExpired(String extraParams) {
    super(null, ERROR, "authentication.EX_PASSWORD_EXPIRED", extraParams);
  }
}
