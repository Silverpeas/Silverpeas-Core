package com.silverpeas.workflow.api.user;

import com.silverpeas.form.DataRecord;
import com.silverpeas.form.RecordTemplate;
import com.silverpeas.workflow.api.WorkflowException;

public interface UserSettings {
  /**
   * return true if userInfos is not empty
   */
  public boolean isValid();

  /**
   * For persistence in database Get this object id
   * 
   * @return this object id
   */
  public String getSettingsId();

  /**
   * Get the user id
   * 
   * @return user id
   */
  public String getUserId();

  /**
   * Get the peas id
   * 
   * @return peas id
   */
  public String getPeasId();

  /**
   * @return UserInfo[]
   */
  public UserInfo[] getUserInfos();

  /**
   * @return UserInfo
   */
  public UserInfo getUserInfo(String name);

  /**
   * Fill the given data record with user information
   * 
   * @param data
   *          the data record
   * @param template
   *          the record template
   */
  public void load(DataRecord data, RecordTemplate template);

  /**
   * Saves this settings in database
   * 
   * @return the newly created settings id
   */
  public void save() throws WorkflowException;

  /**
   * Update the settings with a given DataRecord
   * 
   * @param data
   *          the data record
   * @param template
   *          the record template
   */
  public void update(DataRecord data, RecordTemplate template);

}