package com.silverpeas.web.mock;

import javax.inject.Named;

import com.silverpeas.admin.notification.AdminNotificationService;

@Named("adminNotificationService")
public class AdminNotificationServiceMock implements AdminNotificationService {

  @Override
  public void notifyOnDeletionOf(String spaceId, String userId) {
    
  }

}