package org.silverpeas.core.workflow.engine.user;

import org.silverpeas.core.SilverpeasExceptionMessages;
import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.contribution.content.form.DataRecord;
import org.silverpeas.core.contribution.content.form.Field;
import org.silverpeas.core.contribution.content.form.FormException;
import org.silverpeas.core.contribution.content.form.RecordTemplate;
import org.silverpeas.core.util.logging.SilverLogger;
import org.silverpeas.core.workflow.api.user.UserInfo;
import org.silverpeas.core.workflow.api.user.UserSettings;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.transaction.Transactional;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by Nicolas on 30/05/2017.
 */
@Service
@Singleton
public class DefaultUserSettingsService implements UserSettingsService {

  public static final String FIELD_TEXT = "field {0}";
  @Inject
  private UserSettingsRepository repository;

  private static final ConcurrentMap<String, UserSettings> userSettings = new ConcurrentHashMap<>();

  @Override
  @Transactional
  public void update(final UserSettings userSettings, DataRecord data, RecordTemplate template) {

    String[] fieldNames = template.getFieldNames();

    for (int i = 0; fieldNames != null && i < fieldNames.length; i++) {
      try {
        Field field = data.getField(fieldNames[i]);
        if (field == null) {
          SilverLogger.getLogger(this)
              .warn(SilverpeasExceptionMessages.failureOnGetting(FIELD_TEXT, fieldNames[i]));
          continue;
        }

        String value = field.getStringValue();
        if (value != null) {
          addUserInfo(userSettings, fieldNames[i], value);
        }
      } catch (FormException e) {
        SilverLogger.getLogger(this)
            .error(SilverpeasExceptionMessages.failureOnGetting(FIELD_TEXT,
                data.getId()), e);
      }
    }

    repository.save((UserSettingsImpl) userSettings);
    resetUserSettings(userSettings.getUserId(), userSettings.getComponentId());
  }

  /**
   * Add an user information for this setting
   * @param name key of user information
   * @param value value of user information
   */
  private void addUserInfo(UserSettings userSettings, String name, String value) {
    UserInfoImpl userInfo = new UserInfoImpl(name, value);
    List<UserInfo> userInfos = userSettings.getUserInfos();
    int index = userInfos.indexOf(userInfo);
    if (index == -1) {
      userInfo.setUserSettings(userSettings);
      userInfos.add(userInfo);
    } else {
      ((UserInfoImpl) userInfos.get(index)).setValue(value);
    }
  }

  public UserSettings get(String userId, String componentId) {
    UserSettings settings = userSettings.get(userId + "_" + componentId);
    if (settings == null) {
      settings = repository.getByUserIdAndComponentId(userId, componentId);
      if (settings == null) {
        settings = new UserSettingsImpl(userId, componentId);
      }
      userSettings.put(userId + "_" + componentId, settings);
    }
    return settings;
  }

  private void resetUserSettings(String userId, String peasId) {
    userSettings.remove(userId + "_" + peasId);
  }

}