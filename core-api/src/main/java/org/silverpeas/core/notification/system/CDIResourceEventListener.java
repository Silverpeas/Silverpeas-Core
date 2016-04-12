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

import javax.enterprise.event.Observes;

/**
 * A synchronous event listener using the notification bus of CDI. This bus is based on the
 * Observer pattern but by using the annotations in place of code lines to setup listeners and so
 * on.
 * <p>
 * Synchronous events are carried within a specific CDI event and are collected by this
 * abstract class. All concrete listeners have just to extend this abstract class and to implement
 * some of the following methods to transparently receive the events on which they are interested:
 * </p>
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
 *
 * @author mmoquillon
 */
public abstract class CDIResourceEventListener<T extends ResourceEvent>
    implements ResourceEventListener<T> {

  protected final SilverLogger logger = SilverLogger.getLogger(this);

  /**
   * Listens for events related to a resource managed in Silverpeas.
   * <p>
   *   The event is decoded from the specified message and according to the type of the event,
   *   the adequate method is then invoked.
   * </p>
   * @see ResourceEventListener#dispatchEvent(ResourceEvent)
   * @param event an event.
   * @throws Exception if the processing of the event fails.
   */
  public void onEvent(@Observes T event) throws Exception {
    dispatchEvent(event);
  }
}
