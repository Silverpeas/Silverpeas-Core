package com.stratelia.silverpeas.authentication;

import com.stratelia.webactiv.util.exception.SilverpeasException;

public class AuthenticationPasswordExpired extends AuthenticationException {
  public AuthenticationPasswordExpired(String extraParams) {
    super(null, SilverpeasException.ERROR, "authentication.EX_PASSWORD_EXPIRED", extraParams);
  }
}
