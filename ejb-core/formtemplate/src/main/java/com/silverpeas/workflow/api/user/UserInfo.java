package com.silverpeas.workflow.api.user;

public interface UserInfo {
  /**
   * For persistence in database Get this object id
   * 
   * @return this object id
   */
  public String getId();

  /**
   * Get the info name
   * 
   * @return info name
   */
  public String getName();

  /**
   * Get the info value
   * 
   * @return info value
   */
  public String getValue();

  /**
   * Get the user settings to which this info is for
   * 
   * @return user settings
   */
  public UserSettings getUserSettings();
}