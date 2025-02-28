/*
 * Copyright (C) 2000 - 2024 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.notification.system;

import org.silverpeas.kernel.logging.SilverLogger;

/**
 * A resource event listener. This interface defines the common properties all listeners should
 * implement. The event dispatching method is already implemented here so that the implementers have
 * just to override one of the following method to perform their task:
 * <ul>
 *   <li>{@code org.silverpeas.core.notification.system.ResourceEventListener#onCreation
 *   (ResourceEvent} to
 *   receive events about the creation of a resource,</li>
 *   <li>{@code org.silverpeas.core.notification.system.ResourceEventListener#onUpdate
 *   (ResourceEvent} to
 *   receive events about the update of a resource,</li>
 *   <li>{@code org.silverpeas.core.notification.system.ResourceEventListener#onRemoving
 *   (ResourceEvent} to
 *   receive events about the removing of a resource,</li>
 *   <li>{@code org.silverpeas.core.notification.system.ResourceEventListener#onDeletion
 *   (ResourceEvent} to
 *   receive events about the deletion of a resource,</li>
 * </ul>
 *
 * @author mmoquillon
 */
public interface ResourceEventListener<T extends ResourceEvent<?>> {

  /**
   * An event on the deletion of a resource has be listened. A deleted resource is nonexistent and
   * nonrecoverable. By default, this method does nothing.
   *
   * @param event the event on the deletion of a resource.
   */
  default void onDeletion(final T event) {
  }

  /**
   * An event on the removing of a resource has be listened. A removed resource is again existent
   * and it is recoverable; it is usually put in a trash. By default, this method does nothing.
   *
   * @param event the event on the removing of a resource.
   */
  default void onRemoving(final T event) {
  }

  /**
   * An event on the recovery of a removed resource has be listened. By default, this method does
   * nothing.
   *
   * @param event the event on the removing of a resource.
   */
  default void onRecovery(final T event) {
  }

  /**
   * An event on the update of a resource has be listened. By default, this method does nothing.
   *
   * @param event the event on the update of a resource.
   */
  default void onUpdate(final T event) {
  }

  /**
   * An event on the move of a resource has be listened. By default, this method does nothing.
   *
   * @param event the event on the move of a resource.
   */
  default void onMove(final T event) {
  }

  /**
   * An event on the creation of a resource has be listened. By default, this method does nothing.
   *
   * @param event the event on the creation of a resource.
   */
  default void onCreation(final T event) {
  }

  /**
   * An event on the unlock of a resource has be listened. By default, this method does nothing.
   *
   * @param event the event on the unlock of a resource.
   */
  default void onUnlock(final T event) {
  }

  /**
   * Dispatches the treatment of the specified event to the correct method according to its type:
   * <ul>
   *   <li>{@code org.silverpeas.core.notification.system.ResourceEventListener#onCreation
   *   (ResourceEvent} for
   *   events about the creation of a resource,</li>
   *   <li>{@code org.silverpeas.core.notification.system.ResourceEventListener#onUpdate
   *   (ResourceEvent} for
   *   events about the update of a resource,</li>
   *   <li>{@code org.silverpeas.core.notification.system.ResourceEventListener#onRemoving
   *   (ResourceEvent} for
   *   events about the removing of a resource,</li>
   *   <li>{@code org.silverpeas.core.notification.system.ResourceEventListener#onDeletion
   *   (ResourceEvent} for
   *   events about the deletion of a resource,</li>
   * </ul>
   * <p>
   *   This method shouldn't be overridden as the dispatch mechanism is already implemented here.
   * </p>
   *
   * @param event the event to dispatch.
   */
  default void dispatchEvent(final T event) {
    if (isEnabled()) {
      switch (event.getType()) {
        case CREATION:
          onCreation(event);
          break;
        case UPDATE:
          onUpdate(event);
          break;
        case MOVE:
          onMove(event);
          break;
        case UNLOCK:
          onUnlock(event);
          break;
        case REMOVING:
          onRemoving(event);
          break;
        case DELETION:
          onDeletion(event);
          break;
        case RECOVERY:
          onRecovery(event);
          break;
        default:
          SilverLogger.getLogger(this).
              warn("Event type {0} not yet supported", event.getType().toString());
          break;
      }
    }
  }

  /**
   * Is this listener enabled? When a listener is enabled, it processes then all the incoming events
   * it listens for. Otherwise, nothing the event isn't consumed.
   * <p>
   * By default, the listener is enabled. If the listener has to be enabled according to some
   * conditions, then overrides this method.
   *
   * @return true if this listener has to consume the events it listens for. False otherwise.
   * Returns true by default.
   */
  default boolean isEnabled() {
    return true;
  }
}
