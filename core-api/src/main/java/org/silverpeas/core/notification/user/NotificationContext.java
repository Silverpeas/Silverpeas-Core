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

  public List<String> getAsList(final String key) {
    final String value = get(key);
    return asList(split(value, ","));
  }

  public boolean getAsBoolean(final String key) {
    final String value = get(key);
    return StringUtil.getBooleanValue(key);
  }
}
  