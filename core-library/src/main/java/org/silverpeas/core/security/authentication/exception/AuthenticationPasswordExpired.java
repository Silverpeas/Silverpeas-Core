package org.silverpeas.core.security.authentication.exception;

public class AuthenticationPasswordExpired extends AuthenticationException {
  public AuthenticationPasswordExpired(String extraParams) {
    super(null, ERROR, "authentication.EX_PASSWORD_EXPIRED", extraParams);
  }
}
