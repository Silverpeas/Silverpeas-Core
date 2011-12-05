package com.silverpeas.notification.mock;

import javax.inject.Named;

import com.silverpeas.admin.notification.AdminNotificationService;

@Named("adminNotificationService")
public class AdminNotificationServiceMock implements AdminNotificationService {

  @Override
  public void notifyOnDeletionOf(String spaceId, String userId) {
    
  }

}