package org.silverpeas.core.notification.user.delayed.repository;

import org.silverpeas.core.notification.user.delayed.model.DelayedNotificationUserSetting;
import org.silverpeas.core.persistence.datasource.model.identifier.UniqueIntegerIdentifier;
import org.silverpeas.core.persistence.datasource.repository.jpa.JpaBasicEntityManager;

import javax.inject.Singleton;
import java.util.List;

@Singleton
public class DelayedNotificationUserSettingJpaManager
    extends JpaBasicEntityManager<DelayedNotificationUserSetting, UniqueIntegerIdentifier>
    implements DelayedNotificationUserSettingManager {

  @Override
  public List<DelayedNotificationUserSetting> findByUserId(final int userId) {
    return listFromNamedQuery("DelayedNotificationUserSetting.findByUserId",
        newNamedParameters().add("userId", userId));
  }

  @Override
  public List<DelayedNotificationUserSetting> findByUserIdAndChannel(final int userId,
      final int channel) {
    return listFromNamedQuery("DelayedNotificationUserSetting.findByUserIdAndChannel",
        newNamedParameters().add("userId", userId).add("channel", channel));
  }

  public void deleteById(String id) {
    delete(getById(id));
  }
}
