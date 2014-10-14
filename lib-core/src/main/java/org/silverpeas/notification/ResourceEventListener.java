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

package org.silverpeas.notification;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author mmoquillon
 */
public interface ResourceEventListener<T extends ResourceEvent> {

  /**
   * An event on the deletion of a resource has be listened. By default, this method does nothing.
   * @param event the event on the deletion of a resource.
   * @throws java.lang.Exception if an error occurs while treating the event.
   */
  public default void onDeletion(final T event) throws Exception {
  }

  /**
   * An event on the update of a resource has be listened. By default, this method does nothing.
   * @param event the event on the update of a resource.
   * @throws java.lang.Exception if an error occurs while treating the event.
   */
  public default void onUpdate(final T event) throws Exception {
  }

  /**
   * An event on the creation of a resource has be listened. By default, this method does nothing.
   * @param event the event on the creation of a resource.
   * @throws java.lang.Exception if an error occurs while treating the event.
   */
  public default void onCreation(final T event) throws Exception {
  }

  /**
   * Dispatches the treatment of the specified event to the correct method according to its type:
   * <ul>
   * <li><code>onCreation</code> method is invoked with an event about the creation of a resource,
   * </li>
   * <li><code>onUpdate</code> method is invoked with an event about the update of a resource,
   * </li>
   * <li><code>onDeletion</code> method is invoked with an event about the deletion of a resource.
   * </li>
   * </ul>
   * <p>
   *   This method shouldn't be overriden as the dispatch mechanism is already implemented here.
   * </p>
   * @param event the event to dispatch.
   * @throws java.lang.Exception if an error occurs while treating the event.
   */
  public default void dispatchEvent(final T event) throws Exception {
    switch (event.getType()) {
      case CREATION:
        onCreation(event);
        break;
      case UPDATE:
        onUpdate(event);
        break;
      case DELETION:
        onDeletion(event);
        break;
      default:
        Logger.getLogger(getClass().getSimpleName())
            .log(Level.WARNING, "Event type {0} not yet supported", event.getType());
        break;
    }
  }
}
