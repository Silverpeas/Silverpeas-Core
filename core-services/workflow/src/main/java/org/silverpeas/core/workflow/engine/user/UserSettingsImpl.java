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

import org.silverpeas.core.contribution.content.form.DataRecord;
import org.silverpeas.core.contribution.content.form.Field;
import org.silverpeas.core.contribution.content.form.FormException;
import org.silverpeas.core.contribution.content.form.RecordTemplate;
import org.silverpeas.core.workflow.api.WorkflowException;
import org.silverpeas.core.workflow.api.user.UserInfo;
import org.silverpeas.core.workflow.api.user.UserSettings;
import org.silverpeas.core.workflow.engine.jdo.WorkflowJDOManager;
import org.silverpeas.core.silvertrace.SilverTrace;
import org.exolab.castor.jdo.Database;
import org.exolab.castor.jdo.OQLQuery;
import org.exolab.castor.jdo.PersistenceException;
import org.exolab.castor.jdo.QueryResults;

import java.util.Vector;

/**
 * @table SB_Workflow_UserSettings
 * @key-generator MAX
 */
public class UserSettingsImpl implements UserSettings {
  /**
   * Used for persistence
   * @primary-key
   * @field-name settingsId
   * @field-type string
   * @sql-type integer
   */
  private String settingsId = null;

  /**
   * @field-name userId
   */
  private String userId = null;

  /**
   * @field-name peasId
   */
  private String peasId = null;

  /**
   * Vector of all user informations
   * @field-name userInfos
   * @field-type UserInfoImpl
   * @many-key settingsId
   * @set-method castor_setUserInfos
   * @get-method castor_getUserInfos
   */
  private Vector<UserInfo> userInfos = null;

  /**
   * Default Constructor
   */
  public UserSettingsImpl() {
    reset();
  }

  /**
   * UserSettingsImpl can be constructed with given user Id and peas Id
   */
  public UserSettingsImpl(String userId, String peasId) {
    this.userId = userId;
    this.peasId = peasId;
    reset();
  }

  /**
   * return true if userInfos is not empty
   */
  public boolean isValid() {
    return (userInfos != null && userInfos.size() > 0);
  }

  /**
   * For persistence in database Get this object id
   * @return this object id
   */
  public String getSettingsId() {
    return settingsId;
  }

  /**
   * For persistence in database Set this object id
   * @param settingsId this object id
   */
  public void setSettingsId(String settingsId) {
    this.settingsId = settingsId;
  }

  /**
   * Get the user id
   * @return user id
   */
  public String getUserId() {
    return userId;
  }

  /**
   * Set the user id
   * @param userId user id
   */
  public void setUserId(String userId) {
    this.userId = userId;
  }

  /**
   * Get the peas id
   * @return peas id
   */
  public String getPeasId() {
    return peasId;
  }

  /**
   * Set the peas id
   * @param peasId peas id
   */
  public void setPeasId(String peasId) {
    this.peasId = peasId;
  }

  /**
   * Remove all user infos
   */
  public void reset() {
    userInfos = new Vector<>();
  }

  /**
   * @return UserInfo[]
   */
  public UserInfo[] getUserInfos() {
    if (userInfos != null)
      return userInfos.toArray(new UserInfo[0]);
    else
      return null;
  }

  /**
   * @return UserInfo
   */
  public UserInfo getUserInfo(String name) {
    UserInfoImpl userInfo = new UserInfoImpl(name, "");
    int index = userInfos.indexOf(userInfo);

    if (index == -1)
      return null;
    else
      return userInfos.get(index);
  }

  /**
   * Saves this settings in database
   * @return the newly created settings id
   */
  public void save() throws WorkflowException {
    Database db = null;
    OQLQuery query = null;
    QueryResults results;
    UserSettingsImpl settings = null;

    if (this.settingsId == null) {
      this.create();
    }

    else
      try {
        // Constructs the query
        db = WorkflowJDOManager.getDatabase();
        db.begin();

        query =
            db
                .getOQLQuery("SELECT settings FROM UserSettingsImpl settings"
                    + " WHERE userId = $1 AND peasId = $2");

        // Execute the query
        query.bind(userId);
        query.bind(peasId);
        results = query.execute();

        if (results.hasMore()) {
          settings = (UserSettingsImpl) results.next();
          settings.reset();

          for (int i = 0; i < this.userInfos.size(); i++) {
            UserInfo info = this.userInfos.get(i);
            settings.addUserInfo(info.getName(), info.getValue());
          }
        }

        db.commit();
      } catch (PersistenceException pe) {
        throw new WorkflowException("UserManagerImpl.getUserSettings",
            "EX_ERR_CASTOR_GET_USER_SETTINGS", pe);
      } finally {
        WorkflowJDOManager.closeDatabase(db);
      }

    if (settings == null) {
      this.create();
    }

  }

  /**
   * Update the settings with a given DataRecord
   * @param data the data record
   * @param template the record template
   */
  public void update(DataRecord data, RecordTemplate template) {
    String[] fieldNames = template.getFieldNames();

    for (int i = 0; fieldNames != null && i < fieldNames.length; i++) {
      try {
        Field field = data.getField(fieldNames[i]);
        if (field == null) {
          SilverTrace.warn("workflowEngine", "UserSettingsImpl.update",
              "workflowEngine.EX_ERR_GET_FIELD", fieldNames[i]);
          continue;
        }

        String value = field.getStringValue();
        if (value != null) {
          this.addUserInfo(fieldNames[i], value);
        }
      } catch (FormException e) {
        SilverTrace.warn("workflowEngine", "UserSettingsImpl.update",
            "workflowEngine.EX_ERR_GET_FIELD", "fieldName:" + fieldNames[i] +
                " and data:" + ((data != null)? data.getId() : "null"));
      }
    }
  }

  /**
   * Fill the given data record with user information
   * @param data the data record
   * @param template the record template
   */
  public void load(DataRecord data, RecordTemplate template) {
    String[] fieldNames = template.getFieldNames();

    for (int i = 0; fieldNames != null && i < fieldNames.length; i++) {
      UserInfoImpl userInfo = new UserInfoImpl(fieldNames[i], "");
      int index = userInfos.indexOf(userInfo);
      if (index != -1) {
        try {
          Field field = data.getField(fieldNames[i]);
          if (field == null) {
            SilverTrace.warn("workflowEngine", "UserSettingsImpl.update",
                "workflowEngine.EX_ERR_GET_FIELD", fieldNames[i]);
          } else {
            userInfo = (UserInfoImpl) userInfos.get(index);
            String value = userInfo.getValue();
            if (value != null) {
              field.setStringValue(value);
            }
          }
        } catch (FormException e) {
          SilverTrace.warn("workflowEngine", "UserSettingsImpl.update",
              "workflowEngine.EX_ERR_GET_FIELD", fieldNames[i]);
        }
      }
    }
  }

  /**
   * Add an user information for this setting
   * @param name key of user information
   * @param value value of user information
   */
  private void addUserInfo(String name, String value) {
    UserInfoImpl userInfo = new UserInfoImpl(name, value);
    userInfo.setUserSettings(this);

    int index = userInfos.indexOf(userInfo);
    if (index == -1) {
      userInfos.add(userInfo);
    } else {
      userInfos.set(index, userInfo);
    }
  }

  /**
   * Creates this settings in database
   */
  private void create() throws WorkflowException {
    Database db = null;
    try {
      db = WorkflowJDOManager.getDatabase();
      synchronized (db) {
        db.begin();
        db.create(this);
        db.commit();
      }
    } catch (PersistenceException pe) {
      throw new WorkflowException("UserSettingsImpl.create",
          "workflowEngine.EX_ERR_CASTOR_CREATE_USER_SETTINGS", pe);
    } finally {
      WorkflowJDOManager.closeDatabase(db);
    }
  }

  // METHODS FOR CASTOR

  /**
   * Set the settings user informations
   * @param userInfos user informations
   */
  public void castor_setUserInfos(Vector<UserInfo> userInfos) {
    this.userInfos = userInfos;
  }

  /**
   * Get the settings user informations
   * @return user informations as a Vector
   */
  public Vector<UserInfo> castor_getUserInfos() {
    return userInfos;
  }

}