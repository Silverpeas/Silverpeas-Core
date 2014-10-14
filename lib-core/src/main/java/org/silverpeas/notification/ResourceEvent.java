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

import java.io.Serializable;

/**
 * An event implying a resource in Silverpeas. It is about a change in the life-cycle of the
 * resource in Silverpeas. The resource can be either a contribution, a content, an organizational
 * object or any bean managed by Silverpeas.
 * @param <T> the type of the resource concerned by this event.
 * @author mmoquillon
 */
public interface ResourceEvent<T> extends Serializable {

  /**
   * Gets the type of the event.
   * The type of the event carries the cause: is it about a creation, an update or a deletion?
   * @return the type of the event (creation, update, deletion, ...)
   */
  public Type getType();

  /**
   * Gets the resource related by this event.
   * @return the resource.
   */
  public T getResource();

  /**
   * Is the event on the creation of a resource.
   * @return true if the event is about a resource creation. False otherwise.
   */
  public default boolean isOnCreation() {
    return getType() == Type.CREATION;
  }

  /**
   * Is the event on the update of a resource.
   * @return true if the event is about a resource update. False otherwise.
   */
  public default boolean isOnUpdate() {
    return getType() == Type.UPDATE;
  }

  /**Is the event on the deletion of a resource.
   * @return true if the event is about a resource deletion. False otherwise.
   */
  public default boolean isOnDeletion() {
    return getType() == Type.DELETION;
  }

  /**
   * It defines the more common type of notification in use in Silverpeas. That is to say a
   * notification related about the life-cycle event of a contribution, a content or an
   * organizational object (component instance, space, ...).
   * @author mmoquillon
   */
  enum Type {

    /**
     * The notification is about the creation of a resource in Silverpeas.
     */
    CREATION,
    /**
     * The notification is about the update of a resource in Silverpeas.
     */
    UPDATE,
    /**
     * The notification is about the deletion of a resource in Silverpeas.
     */
    DELETION;

  }
}
