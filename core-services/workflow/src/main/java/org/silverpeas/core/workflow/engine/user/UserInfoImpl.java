/*
 * Copyright (C) 2000 - 2021 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "https://www.silverpeas.org/legal/floss_exception.html"
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

import org.silverpeas.core.persistence.datasource.model.identifier.UniqueIntegerIdentifier;
import org.silverpeas.core.persistence.datasource.model.jpa.BasicJpaEntity;
import org.silverpeas.core.workflow.api.user.UserInfo;
import org.silverpeas.core.workflow.api.user.UserSettings;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "sb_workflow_userinfo")
public class UserInfoImpl extends BasicJpaEntity<UserInfoImpl, UniqueIntegerIdentifier>
    implements UserInfo {

  @Column
  private String name = null;
  @Column
  private String value = null;

  @ManyToOne
  @JoinColumn(name = "settingsid", nullable = false)
  private UserSettingsImpl userSettings = null;

  /**
   * Default Constructor
   */
  protected UserInfoImpl() {
  }

  /**
   * UserInfoImpl can be constructed with given name and value
   */
  public UserInfoImpl(String name, String value) {
    this.name = name;
    this.value = value;
  }

  public String getName() {
    return name;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  /**
   * Set the user settings to which this info is for
   * @param userSettings settings
   */
  public void setUserSettings(UserSettings userSettings) {
    this.userSettings = (UserSettingsImpl) userSettings;
  }

  @Override
  public boolean equals(Object theOther) {
    if (theOther instanceof UserInfoImpl) {
      return getName().equals(((UserInfoImpl) theOther).getName());
    }
    return false;
  }

  @Override
  public int hashCode() {
    return getName().hashCode();
  }

}