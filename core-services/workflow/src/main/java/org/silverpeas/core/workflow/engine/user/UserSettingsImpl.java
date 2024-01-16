/*
 * Copyright (C) 2000 - 2024 Silverpeas
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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.workflow.engine.user;

import org.silverpeas.core.contribution.content.form.DataRecord;
import org.silverpeas.core.contribution.content.form.Field;
import org.silverpeas.core.contribution.content.form.FormException;
import org.silverpeas.core.contribution.content.form.RecordTemplate;
import org.silverpeas.core.persistence.datasource.model.identifier.UniqueIntegerIdentifier;
import org.silverpeas.core.persistence.datasource.model.jpa.BasicJpaEntity;
import org.silverpeas.core.silvertrace.SilverTrace;
import org.silverpeas.core.util.CollectionUtil;
import org.silverpeas.core.workflow.api.user.UserInfo;
import org.silverpeas.core.workflow.api.user.UserSettings;

import javax.persistence.AttributeOverride;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "sb_workflow_usersettings")
@AttributeOverride(name = "id", column = @Column(name = "settingsid"))
@NamedQueries({
    @NamedQuery(name = "findByUserAndComponent",
        query = "from UserSettingsImpl where userId = :userId and peasId = :componentId")})
public class UserSettingsImpl extends BasicJpaEntity<UserSettingsImpl, UniqueIntegerIdentifier>
    implements UserSettings {

  @Column
  private String userId = null;
  @Column
  private String peasId = null;

  @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, mappedBy = "userSettings")
  private List<UserInfoImpl> userInfos = null;

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
    return CollectionUtil.isNotEmpty(userInfos);
  }

  public void reset() {
    userInfos = new ArrayList<>();
  }

  @Override
  public String getUserId() {
    return userId;
  }

  @Override
  public String getComponentId() {
    return peasId;
  }

  /**
   * @return UserInfo
   */
  public UserInfo getUserInfo(String name) {
    UserInfoImpl userInfo = new UserInfoImpl(name, "");
    int index = userInfos.indexOf(userInfo);
    if (index != -1) {
      return userInfos.get(index);
    }
    return null;
  }

  @SuppressWarnings("unchecked")
  public List<UserInfo> getUserInfos() {
    return (List) userInfos;
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

}