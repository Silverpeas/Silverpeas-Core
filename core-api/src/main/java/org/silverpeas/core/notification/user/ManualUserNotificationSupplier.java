package org.silverpeas.core.notification.user;

import java.util.Map;

/**
 * A supplier of a manual user notifications. Such notifications are messages sent explicitly by
 *  a given user to a set of one or more other users in order to notify them about an information,
 *  usually the availability or the content of a contribution that could interest them.
 * @author mmoquillon
 */
@FunctionalInterface
public interface ManualUserNotificationSupplier {

  /**
   * Gets a {@link UserNotification} object that matches the specified notification context.
   * @param context a {@link Map} of key-values describing the context within which the user
   * notification has to be built.
   * @return a {@link UserNotification} object.
   */
  UserNotification get(final NotificationContext context);
}
