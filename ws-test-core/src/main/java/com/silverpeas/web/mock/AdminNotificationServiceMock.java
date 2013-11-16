package com.silverpeas.web.mock;

import javax.inject.Named;

import com.silverpeas.admin.notification.AdminNotificationService;
import com.silverpeas.admin.notification.ComponentJsonPatch;

@Named("adminNotificationService")
public class AdminNotificationServiceMock implements AdminNotificationService {

  @Override
  public void notifyOnDeletionOf(String spaceId, String userId) {
  }

  @Override
  public void notifyOfComponentConfigurationChange(String componentId, String userId,
      ComponentJsonPatch changes) {
  }

}
