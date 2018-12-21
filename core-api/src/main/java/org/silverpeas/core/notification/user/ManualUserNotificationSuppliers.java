package org.silverpeas.core.notification.user;

import org.silverpeas.core.SilverpeasRuntimeException;

import javax.inject.Singleton;
import java.util.HashMap;
import java.util.Map;

/**
 * A collection of suppliers of manual user notifications. The mechanism of sending manual user
 * notification is centralized and transverse. This collection is a way for this mechanism to get
 * the {@link UserNotification} object related to a Silverpeas component and that matches a given
 * notification context.
 * <p>
 * Because the way each notifications are built depends both on their context and on the
 * resources or, more specifically, the contributions they are related to, each component in
 * Silverpeas with which a manual notification is available has to specify its own supplier of
 * {@link UserNotification} object to this {@link ManualUserNotificationSuppliers} singleton. So,
 * when a manual notification has to be sent for a given component instance, the mechanism of
 * sending manual notifications will ask for a {@link UserNotification} object to the
 * corresponding supplier's Silverpeas component through this collection.
 * </p>
 * @author mmoquillon
 */
@Singleton
public class ManualUserNotificationSuppliers {

  private final Map<String, ManualUserNotificationSupplier> suppliers =
      new HashMap<>();

  /**
   * Sets for the given Silverpeas component the specified {@link UserNotification} provider.
   * @param componentName the name of a Silverpeas component in lower case (kmelia for example).
   * @param supplier a function that accepts as argument the context of the notification and
   * that returns the {@link UserNotification} object.
   */
  public void set(final String componentName, final ManualUserNotificationSupplier supplier) {
    this.suppliers.putIfAbsent(componentName, supplier);
  }

  /**
   * Gets a {@link UserNotification} object from the specified Silverpeas component and matching
   * the specified notification context.
   * @param componentName the name of a Silverpeas component in lower case (kmelia for example).
   * @param context the context within which the manual notification will be built and then
   * sent.
   * @return a {@link UserNotification} object.
   */
  public UserNotification get(final String componentName, final NotificationContext context) {
    ManualUserNotificationSupplier supplier = this.suppliers.get(componentName);
    if (supplier == null) {
      throw new SilverpeasRuntimeException(
          "No User Notification Supplier defined for component " + componentName);
    }
    return supplier.get(context);
  }
}
  