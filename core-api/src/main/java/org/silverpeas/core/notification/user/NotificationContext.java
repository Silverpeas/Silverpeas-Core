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
   * {@link NotificationContext#COMPONENT_ID} must be defined.
   */
  public static final String CONTRIBUTION_ID = "contributionId";

  public List<String> getAsList(final String key) {
    final String value = get(key);
    return asList(split(value, ","));
  }

  public boolean getAsBoolean(final String key) {
    final String value = get(key);
    return StringUtil.getBooleanValue(value);
  }
}
  