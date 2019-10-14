package org.silverpeas.core.workflow.engine.user;

import org.silverpeas.core.contribution.content.form.DataRecord;
import org.silverpeas.core.contribution.content.form.RecordTemplate;
import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.core.workflow.api.user.UserSettings;

/**
 * Created by Nicolas on 30/05/2017.
 */
public interface UserSettingsService {

  static UserSettingsService get() {
    return ServiceProvider.getSingleton(UserSettingsService.class);
  }

  UserSettings get(String userId, String componentId);

  void update(UserSettings userSettings, DataRecord data, RecordTemplate template);

}
