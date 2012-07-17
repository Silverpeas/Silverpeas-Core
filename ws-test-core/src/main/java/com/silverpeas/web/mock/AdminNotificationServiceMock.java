package com.silverpeas.web.mock;

import com.silverpeas.admin.notification.AdminNotificationService;
import javax.inject.Named;

@Named("adminNotificationService")
public class AdminNotificationServiceMock implements AdminNotificationService {

  @Override
  public void notifyOnDeletionOf(String spaceId, String userId) {
    
  }

}