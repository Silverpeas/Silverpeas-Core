package org.silverpeas.authentication;

public class AuthenticationPasswordMustBeChangedAtNextLogon extends AuthenticationException {
  public AuthenticationPasswordMustBeChangedAtNextLogon(String extraParams) {
    super(null, ERROR, "authentication.EX_PASSWORD_MUST_BE_CHANGED_AT_NEXT_LOGON", extraParams);
  }
}
