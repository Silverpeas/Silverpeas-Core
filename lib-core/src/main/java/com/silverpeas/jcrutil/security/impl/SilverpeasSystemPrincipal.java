package com.silverpeas.jcrutil.security.impl;

import java.security.Principal;

public class SilverpeasSystemPrincipal implements Principal {
  public static final String SYSTEM = "system";

  public String getName() {
    return SYSTEM;
  }

}
