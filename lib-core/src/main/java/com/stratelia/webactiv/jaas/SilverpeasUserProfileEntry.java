package com.stratelia.webactiv.jaas;

public class SilverpeasUserProfileEntry {

  public static final String READER_PROFILE = "reader";
  public static final String SUBSCRIBER_PROFILE = "subscriber";
  public static final String MODERATOR_PROFILE = "moderator";
  public static final String ROOT_PROFILE = "admin";
  public static final String PUBLISHER_PROFILE = "publisher";
  public static final String WRITER_PROFILE = "writer";
  public static final String USER_PROFILE = "user";
  private String componentId;

  private String profile;

  public SilverpeasUserProfileEntry(String componentId, String profile) {
    super();
    this.componentId = componentId;
    this.profile = profile;
  }

  public String getComponentId() {
    return componentId;
  }

  public String getProfile() {
    return profile;
  }

  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result
        + ((componentId == null) ? 0 : componentId.hashCode());
    result = prime * result + ((profile == null) ? 0 : profile.hashCode());
    return result;
  }

  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    final SilverpeasUserProfileEntry other = (SilverpeasUserProfileEntry) obj;
    if (componentId == null) {
      if (other.componentId != null)
        return false;
    } else if (!componentId.equals(other.componentId))
      return false;
    if (profile == null) {
      if (other.profile != null)
        return false;
    } else if (!profile.equals(other.profile))
      return false;
    return true;
  }



}
