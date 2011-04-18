/*
 * Copyright (C) 2000 - 2009 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.notification;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * A notification emitted within the Silverpeas system.
 *
 * A notification informs about an event or an action that has occured in a Silverpeas component
 * instance and in what a given object or resource is involved. For example, the creation of a
 * publication can be notified with the publication (or its unique identifier) as object of the
 * announcement.
 * @param <T> the type of the object involved in the notification. The object must be serializable
 * in order to be transported into the subscribers through the underlying messaging system.
 */
public class SilverpeasNotification<T extends Serializable> implements Serializable {
  private static final long serialVersionUID = -570734131630845982L;
  private final NotificationSource source;
  private final T object;
  private Map<String, String> parameters = new HashMap<String, String>();

  /**
   * Constructs a new notification involving the specified object and triggered by the specified
   * source.
   * @param source the source of the notification.
   * @param object the object on which the notification is focusing.
   */
  public SilverpeasNotification(final NotificationSource source, final T object) {
    this.source = source;
    this.object = object;
  }

  /**
   * Gets the object focusing by this notification.
   * @return the object involving in an event or an action this notification informs.
   */
  public T getObject() {
    return object;
  }

  /**
   * Gets the source of this notification.
   * @return the notification source.
   */
  public NotificationSource getSource() {
    return this.source;
  }

  /**
   * Adds a new parameter to this notification.
   * Parameters in notification carries additional information and ahs to be simple.
   * @param name the parameter name.
   * @param value the parameter value.
   */
  public void addParameter(String name, String value) {
    parameters.put(name, value);
  }

  /**
   * Gets the value of the specified parameter. If the parameter isn't set in the notification,
   * then null is returned.
   * @param name the name of the parameter to get.
   * @return the value of the parameter or null if no such parameter is set in this notification.
   */
  public String getParameterValue(String name) {
    return parameters.get(name);
  }

  /**
   * Gets the set of this notification's parameters.
   * @return an unmodifiable set of parameter names.
   */
  public Set<String> getParameters() {
    return Collections.unmodifiableSet(parameters.keySet());
  }
}
