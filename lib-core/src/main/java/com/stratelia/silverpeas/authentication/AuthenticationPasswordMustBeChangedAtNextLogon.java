package com.stratelia.silverpeas.authentication;

import com.stratelia.webactiv.util.exception.SilverpeasException;

public class AuthenticationPasswordMustBeChangedAtNextLogon extends AuthenticationException {
  public AuthenticationPasswordMustBeChangedAtNextLogon(String extraParams) {
    super(null, SilverpeasException.ERROR, "authentication.EX_PASSWORD_MUST_BE_CHANGED_AT_NEXT_LOGON", extraParams);
  }
}
