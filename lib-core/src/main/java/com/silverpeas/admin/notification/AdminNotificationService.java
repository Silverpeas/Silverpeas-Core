package com.silverpeas.admin.notification;

public interface AdminNotificationService {

  /**
   * Notifies the registered beans a given space comes to be deleted.
   * @param nodes the nodes that are deleted.
   */
  public abstract void notifyOnDeletionOf(final String spaceId, String userId);

}