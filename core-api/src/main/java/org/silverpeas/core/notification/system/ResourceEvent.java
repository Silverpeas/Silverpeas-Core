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

import java.io.Serializable;

/**
 * An event implying a resource in Silverpeas. The resource can be either a contribution,
 * a content, an organizational object or any bean managed by Silverpeas.
 * <p>
 * This event is about a change on a resource in Silverpeas. A such change can be a creation (the
 * resource is spawn to life), an update, a removing, a deletion and so on. A change is then
 * manifested by a transition between two states of the related resource. Hence, the event is
 * characterized by a type that indicates the nature of the change occurring on
 * the resource, and by the state transition of the resource implied by this change. This state
 * transition carries an instance of the resource as it was before the change and an instance of
 * the resource as it is actually.
 * </p>
 * @param <T> the type of the resource concerned by this event.
 * @author mmoquillon
 */
public interface ResourceEvent<T extends Serializable> extends Serializable {

  /**
   * Gets the type of the event.
   * The type of the event carries the cause: a creation, an update, a removing or a deletion of
   * the related resource. It defines then the state in the life-cycle of the resource.
   * @return the type of the event (creation, update, deletion, ...)
   */
  Type getType();

  /**
   * Gets the state transition implying by a change on the resource. The transition carries two
   * instances of the resource: the resource before the change at the origin of this event, the
   * the resource after the change has occurred. According to the type of the event, one of the
   * state can be null or the two instances can be identical:
   * <ul>
   *   <li>for a creation, the instance before the transition is null whereas the instance after
   *   the transition is the created resource,</li>
   *   <li>for an update, the instance before the transition is the resource before the update
   *   whereas the instance after the transition is the resource after the update,</li>
   *   <li>for a removing, the two instances are identical (as the resource is existent and
   *   recoverable),</li>
   *   <li> for a deletion, the instance before the transition is the resource whereas the instance
   *   after the deletion is null.</li>
   * </ul>
   * @return a state transition with two instances of the resource related by this event: one being
   * the resource before the change, the second being the resource after the change.
   */
  StateTransition<T> getTransition();

  /**
   * Is the event on the creation of a resource.
   * @return true if the event is about a resource creation. False otherwise.
   */
  default boolean isOnCreation() {
    return getType() == Type.CREATION;
  }

  /**
   * Is the event on the update of a resource.
   * @return true if the event is about a resource update. False otherwise.
   */
  default boolean isOnUpdate() {
    return getType() == Type.UPDATE;
  }

  /**
   * Is the event on the removing of a resource.
   * @return true if the event is about a resource removing. False otherwise.
   */
  default boolean isOnRemoving() {
    return getType() == Type.REMOVING;
  }

  /**Is the event on the deletion of a resource.
   * @return true if the event is about a resource deletion. False otherwise.
   */
  default boolean isOnDeletion() {
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
     * The notification is about the removing of a resource in Silverpeas. Some types of resources
     * are never directly deleted but first removed in a trash. They are then again existent and
     * recoverable. This event is about this state of the life-cycle of a such resource.
     */
    REMOVING,
    /**
     * The notification is about the deletion of a resource in Silverpeas. When a resource is
     * deleted, it is then nonexistent and nonrecoverable.
     */
    DELETION;

  }
}
