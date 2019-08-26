package org.silverpeas.core.notification.user;

import org.silverpeas.core.admin.user.model.User;
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
public class NotificationContext extends HashMap<String, Object> {
  private static final long serialVersionUID = 341715544034127254L;

  /**
   * The predefined key in the context mapped with the unique identifier of a Silverpeas component
   * instance. This key is used to identify the component instance for which a user notification
   * has to be sent. It is used further in the user selection to filter the users that can access
   * the component instance.
   */
  public static final String COMPONENT_ID = "componentId";

  /**
   * The predefined key in the context mapped with the unique identifier of a resource in
   * Silverpeas. The mapped resource identifier should be a concat of the type and of the
   * true identifier of the resource. Such a resource can be for example a node within which some
   * contributions are put. If the resource is managed by a given component instance,
   * then the key {@link NotificationContext#COMPONENT_ID} must be defined. This key is
   * used in the user selection to filter the users that can access the specified resource.
   */
  public static final String RESOURCE_ID = "resourceId";

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

  /**
   * The predefined key in the context mapped with the unique identifier of a node. Used to
   * specify the unique identifier of a node or a folder with contributions.
   */
  public static final String NODE_ID = "nodeId";

  private final User sender;

  public NotificationContext(final User sender) {
    this.sender = sender;
  }

  public String getComponentId() {
    return get(COMPONENT_ID);
  }

  public String getResourceId() {
    return get(RESOURCE_ID);
  }

  public String getNodeId() {
    return get(NODE_ID);
  }

  public String getPublicationId() {
    return get(PUBLICATION_ID);
  }

  public String getContributionId() {
    return get(CONTRIBUTION_ID);
  }

  public String getTitle() {
    return get("title");
  }

  public String getContent() {
    return get("content");
  }

  public List<String> getAsList(final String key) {
    final String value = get(key);
    return asList(split(value, ","));
  }

  public boolean getAsBoolean(final String key) {
    final String value = get(key);
    return StringUtil.getBooleanValue(value);
  }

  public User getSender() {
    return sender;
  }

  @Override
  public String get(final Object key) {
    return (String) super.get(key);
  }

  @SuppressWarnings("unchecked")
  public <T> T getObject(final Object key) {
    return (T) super.get(key);
  }

  @Override
  public boolean equals(final Object o) {
    return super.equals(o);
  }

  @Override
  public int hashCode() {
    return super.hashCode();
  }
}
  