package com.silverpeas.jcrutil.security.impl;

import javax.jcr.Credentials;

public class SilverpeasCredentials implements Credentials {

  private String userId;

  public SilverpeasCredentials(String userId) {
    this.userId = userId;
  }

  public String getUserId() {
    return this.userId;
  }
}
