package com.stratelia.webactiv.jaas;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

public class SilverpeasUserPrincipal implements Principal {

  private String userId;
  private Map entries;

  public SilverpeasUserPrincipal(String userId) {
    this.userId = userId;
    this.entries = new HashMap(100);
  }

  public void addUserProfile(SilverpeasUserProfileEntry entry) {
    entries.put(entry.getComponentId(), entry);
  }

  public SilverpeasUserProfileEntry getUserProfile(String componentId) {
    return (SilverpeasUserProfileEntry) this.entries.get(componentId);
  }

  public String getUserId() {
    return userId;
  }

  public String getName() {
    return userId;
  }
}
