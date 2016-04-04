/*
 * Copyright (C) 2000 - 2014 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.core.notification.system;

import org.silverpeas.core.util.logging.SilverLogger;

/**
 * A resource event listener. This interface defines the common properties all listeners should
 * implement. The event dispatching method is already implemented here so that the implementers
 * have just to override one of the following method to perform their task:
 * <ul>
 *   <li>{@code org.silverpeas.core.notification.system.ResourceEventListener#onCreation(ResourceEvent} to
 *   receive events about the creation of a resource,</li>
 *   <li>{@code org.silverpeas.core.notification.system.ResourceEventListener#onUpdate(ResourceEvent} to
 *   receive events about the update of a resource,</li>
 *   <li>{@code org.silverpeas.core.notification.system.ResourceEventListener#onRemoving(ResourceEvent} to
 *   receive events about the removing of a resource,</li>
 *   <li>{@code org.silverpeas.core.notification.system.ResourceEventListener#onDeletion(ResourceEvent} to
 *   receive events about the deletion of a resource,</li>
 * </ul>
 * @author mmoquillon
 */
public interface ResourceEventListener<T extends ResourceEvent> {

  /**
   * An event on the deletion of a resource has be listened. A deleted resource is nonexistent and
   * nonrecoverable. By default, this method does nothing.
   * @param event the event on the deletion of a resource.
   * @throws java.lang.Exception if an error occurs while treating the event.
   */
  default void onDeletion(final T event) throws Exception {
  }

  /**
   * An event on the removing of a resource has be listened. A removed resource is again existent
   * and it is recoverable; it is usually put in a trash. By default, this method does nothing.
   * @param event the event on the removing of a resource.
   * @throws java.lang.Exception if an error occurs while treating the event.
   */
  default void onRemoving(final T event) throws Exception {
  }

  /**
   * An event on the update of a resource has be listened. By default, this method does nothing.
   * @param event the event on the update of a resource.
   * @throws java.lang.Exception if an error occurs while treating the event.
   */
  default void onUpdate(final T event) throws Exception {
  }

  /**
   * An event on the creation of a resource has be listened. By default, this method does nothing.
   * @param event the event on the creation of a resource.
   * @throws java.lang.Exception if an error occurs while treating the event.
   */
  default void onCreation(final T event) throws Exception {
  }

  /**
   * Dispatches the treatment of the specified event to the correct method according to its type:
   * <ul>
   *   <li>{@code org.silverpeas.core.notification.system.ResourceEventListener#onCreation(ResourceEvent} for
   *   events about the creation of a resource,</li>
   *   <li>{@code org.silverpeas.core.notification.system.ResourceEventListener#onUpdate(ResourceEvent} for
   *   events about the update of a resource,</li>
   *   <li>{@code org.silverpeas.core.notification.system.ResourceEventListener#onRemoving(ResourceEvent} for
   *   events about the removing of a resource,</li>
   *   <li>{@code org.silverpeas.core.notification.system.ResourceEventListener#onDeletion(ResourceEvent} for
   *   events about the deletion of a resource,</li>
   * </ul>
   * <p>
   *   This method shouldn't be overridden as the dispatch mechanism is already implemented here.
   * </p>
   * @param event the event to dispatch.
   * @throws java.lang.Exception if an error occurs while treating the event.
   */
  default void dispatchEvent(final T event) throws Exception {
    switch (event.getType()) {
      case CREATION:
        onCreation(event);
        break;
      case UPDATE:
        onUpdate(event);
        break;
      case REMOVING:
        onRemoving(event);
        break;
      case DELETION:
        onDeletion(event);
        break;
      default:
        SilverLogger.getLogger(this).
            warn("Event type {0} not yet supported", event.getType().toString());
        break;
    }
  }
}
