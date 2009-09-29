package com.stratelia.silverpeas.authentication.security;

public class SecurityData {

  private String userId;
  private String domainId;

  public SecurityData(String userId, String domainId) {
    this.userId = userId;
    this.domainId = domainId;
  }

  public String getUserId() {
    return userId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }

  public String getDomainId() {
    return domainId;
  }

  public void setDomainId(String domainId) {
    this.domainId = domainId;
  }

}
