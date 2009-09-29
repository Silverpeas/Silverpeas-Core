package com.silverpeas.workflow.engine.user;

import com.silverpeas.workflow.api.user.UserInfo;
import com.silverpeas.workflow.api.user.UserSettings;
import com.silverpeas.workflow.engine.AbstractReferrableObject;

/**
 * @table SB_Workflow_UserInfo
 * @depends com.silverpeas.workflow.engine.user.UserSettingsImpl
 * @key-generator MAX
 */
public class UserInfoImpl extends AbstractReferrableObject implements UserInfo {
  /**
   * Used for persistence
   * 
   * @primary-key
   * @field-name id
   * @field-type string
   * @sql-type integer
   */
  private String id = null;

  /**
   * @field-name name
   */
  private String name = null;

  /**
   * @field-name value
   */
  private String value = null;

  /**
   * @field-name userSettings
   * @field-type com.silverpeas.workflow.engine.user.UserSettingsImpl
   * @sql-name settingsId
   */
  private UserSettingsImpl userSettings = null;

  /**
   * Default Constructor
   */
  public UserInfoImpl() {
  }

  /**
   * UserInfoImpl can be constructed with given name and value
   */
  public UserInfoImpl(String name, String value) {
    this.name = name;
    this.value = value;
  }

  /**
   * For persistence in database Get this object id
   * 
   * @return this object id
   */
  public String getId() {
    return id;
  }

  /**
   * For persistence in database Set this object id
   * 
   * @param this object id
   */
  public void setId(String id) {
    this.id = id;
  }

  /**
   * Get the info name
   * 
   * @return info name
   */
  public String getName() {
    return name;
  }

  /**
   * Get the info name
   * 
   * @return info name
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Get the info value
   * 
   * @return info value
   */
  public String getValue() {
    return value;
  }

  /**
   * Get the info value
   * 
   * @return info value
   */
  public void setValue(String value) {
    this.value = value;
  }

  /**
   * Get the user settings to which this info is for
   * 
   * @return user settings
   */
  public UserSettings getUserSettings() {
    return (UserSettings) userSettings;
  }

  /**
   * Set the user settings to which this info is for
   * 
   * @param user
   *          settings
   */
  public void setUserSettings(UserSettings userSettings) {
    this.userSettings = (UserSettingsImpl) userSettings;
  }

  /**
   * This method has to be implemented by the referrable object it has to
   * compute the unique key
   * 
   * @return The unique key.
   */
  public String getKey() {
    return this.getName();
  }
}