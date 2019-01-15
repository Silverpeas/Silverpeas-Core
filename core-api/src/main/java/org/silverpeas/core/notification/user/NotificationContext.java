package org.silverpeas.core.notification.user;

import org.silverpeas.core.util.StringUtil;

import java.util.HashMap;
import java.util.List;

import static java.util.Arrays.asList;
import static org.apache.commons.lang3.StringUtils.split;

/**
 * Context about a user notification. The context a dictionary of notification properties from
 * which a user notification can be built.
 * @author mmoquillon
 */
public class NotificationContext extends HashMap<String, String> {
  private static final long serialVersionUID = 341715544034127254L;

  /**
   * The predefined key in the context mapped with the unique identifier of a Silverpeas component
   * instance.
   */
  public static final String COMPONENT_ID = "componentId";

  /**
   * The predefined key in the context mapped with the unique identifier of a contribution in
   * Silverpeas. If the contribution is managed by a given component instance, then the key
   * {@link NotificationContext#COMPONENT_ID} must be defined. This key is used by the user
   * notification mechanism to get any attachments of such a contribution in order to automatically
   * indicate them in the notification message. (Those links to attachment can be or not processed
   * by the notification service at the endpoint.)
   */
  public static final String CONTRIBUTION_ID = "contributionId";

  /**
   * The predefined key in the context mapped with the unique identifier of a publication. Used to
   * specify the unique identifier of a contribution with attachments. In the case the contributions
   * managed by a Silverpeas component don't have attachments in themselves but another resource
   * mapped with them, this key is a way to specify the identifier of that resource in order to get
   * the attachments to automatically indicate in the notification message. (Those links to
   * attachment can be or not processed by the notification service at the endpoint.)
   */
  public static final String PUBLICATION_ID = "publicationId";

  public List<String> getAsList(final String key) {
    final String value = get(key);
    return asList(split(value, ","));
  }

  public boolean getAsBoolean(final String key) {
    final String value = get(key);
    return StringUtil.getBooleanValue(value);
  }
}
  