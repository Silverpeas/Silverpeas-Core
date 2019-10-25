package org.silverpeas.core.notification.user;

import java.util.Map;
import java.util.Optional;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.silverpeas.core.notification.user.ComponentInstanceManualUserNotification.Constants.NAME_SUFFIX;
import static org.silverpeas.core.util.ServiceProvider.getServiceByComponentInstanceAndNameSuffix;

/**
 * <p>
 * This interface provides the initialization of a manual {@link UserNotification} from a
 * {@link NotificationContext}.
 * </p>
 * <p>
 * Each component dealing with manual {@link UserNotification} has to implement this
 * interface and the implementation has to be qualified with the @{@link javax.inject.Named}
 * annotation by a name satisfying the following convention <code>[COMPONENT
 * NAME]InstanceManualUserNotification</code>. For example, for an application Kmelia, the
 * implementation must be qualified with <code>@Named("kmeliaInstanceManualUserNotification")
 * </code>
 * <p>
 * @author silveryocha
 */
public interface ComponentInstanceManualUserNotification {

  /**
   * Constants are predefined value used by manual user notification to work with and that
   * carries a semantic that has to be shared by all the implementations of this interface.
   */
  class Constants {

    private Constants() {
    }

    /**
     * The predefined suffix that must compound the name of each implementation of this interface.
     * An implementation of this interface by a Silverpeas application named Kmelia must be named
     * <code>kmelia[NAME_SUFFIX]</code> where NAME_SUFFIX is the predefined suffix as defined below.
     */
    public static final String NAME_SUFFIX = "InstanceManualUserNotification";
  }

  /**
   * Gets the implementation of this interface with the qualified name guessed from the given
   * component instance identifier.
   * @param componentNameOrInstanceId the component instance identifier or component name from
   * which the qualified name of
   * the implementation is guessed.
   * @return an optional implementation of this interface.
   */
  static Optional<ComponentInstanceManualUserNotification> get(String componentNameOrInstanceId) {
    Optional<ComponentInstanceManualUserNotification> service;
    try {
      service = of(getServiceByComponentInstanceAndNameSuffix(componentNameOrInstanceId, NAME_SUFFIX));
    } catch (IllegalStateException e) {
      service = empty();
    }
    return service;
  }

  /**
   * Initializes a {@link UserNotification} object with the specified notification context.
   * @param context a {@link Map} of key-values describing the context within which the user
   * notification has to be built.
   * @return a {@link UserNotification} object.
   */
  UserNotification initializesWith(final NotificationContext context);
}
