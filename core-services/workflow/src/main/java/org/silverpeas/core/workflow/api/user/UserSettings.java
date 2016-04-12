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

package org.silverpeas.core.workflow.api.user;

import org.silverpeas.core.contribution.content.form.DataRecord;
import org.silverpeas.core.contribution.content.form.RecordTemplate;
import org.silverpeas.core.workflow.api.WorkflowException;

public interface UserSettings {
  /**
   * return true if userInfos is not empty
   */
  public boolean isValid();

  /**
   * For persistence in database Get this object id
   * @return this object id
   */
  public String getSettingsId();

  /**
   * Get the user id
   * @return user id
   */
  public String getUserId();

  /**
   * Get the peas id
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
   * @param data the data record
   * @param template the record template
   */
  public void load(DataRecord data, RecordTemplate template);

  /**
   * Saves this settings in database
   * @return the newly created settings id
   */
  public void save() throws WorkflowException;

  /**
   * Update the settings with a given DataRecord
   * @param data the data record
   * @param template the record template
   */
  public void update(DataRecord data, RecordTemplate template);

}