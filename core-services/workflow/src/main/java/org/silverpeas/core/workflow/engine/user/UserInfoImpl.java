/*
 * Copyright (C) 2000 - 2014 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.core.workflow.engine.user;

import org.silverpeas.core.workflow.api.user.UserInfo;
import org.silverpeas.core.workflow.api.user.UserSettings;
import org.silverpeas.core.workflow.engine.AbstractReferrableObject;

/**
 * @table SB_Workflow_UserInfo
 * @depends UserSettingsImpl
 * @key-generator MAX
 */
public class UserInfoImpl extends AbstractReferrableObject implements UserInfo {
  /**
   * Used for persistence
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
   * @field-type UserSettingsImpl
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
   * @return this object id
   */
  public String getId() {
    return id;
  }

  /**
   * For persistence in database Set this object id
   * @param this object id
   */
  public void setId(String id) {
    this.id = id;
  }

  /**
   * Get the info name
   * @return info name
   */
  public String getName() {
    return name;
  }

  /**
   * Get the info name
   * @return info name
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Get the info value
   * @return info value
   */
  public String getValue() {
    return value;
  }

  /**
   * Get the info value
   * @return info value
   */
  public void setValue(String value) {
    this.value = value;
  }

  /**
   * Get the user settings to which this info is for
   * @return user settings
   */
  public UserSettings getUserSettings() {
    return userSettings;
  }

  /**
   * Set the user settings to which this info is for
   * @param user settings
   */
  public void setUserSettings(UserSettings userSettings) {
    this.userSettings = (UserSettingsImpl) userSettings;
  }

  /**
   * This method has to be implemented by the referrable object it has to compute the unique key
   * @return The unique key.
   */
  public String getKey() {
    return this.getName();
  }
}