/*
 * Copyright (C) 2000 - 2020 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.core.calendar;

import java.util.Optional;

/**
 * Result of an operation that was performed against an object in the calendar business model like
 * a calendar event and its occurrences for example.
 *
 * A result is a container of {@link Plannable} objects and/or of one of its {@link Occurrence}
 * object that have been either spawned or updated by a given business operation.
 */
public class OperationResult<T extends Plannable, U extends Occurrence> {
  private T updated;
  private T created;
  private U instance;

  /**
   * Sets to this result the planned object that was updated within the operation.
   * @param updated the updated planned object.
   * @return itself.
   */
  @SuppressWarnings("unchecked")
  public <R extends OperationResult<T, U>> R withUpdated(T updated) {
    this.updated = updated;
    return (R) this;
  }

  /**
   * Sets to this result the planned object that was created within the operation.
   * @param created the created planned object.
   * @return itself.
   */
  @SuppressWarnings("unchecked")
  public <R extends OperationResult<T, U>> R withCreated(T created) {
    this.created = created;
    return (R) this;
  }

  /**
   * Sets to this result an instance of a planned object that was updated within the operation.
   * @param instance the updated instance of a planned object.
   * @return itself.
   */
  @SuppressWarnings("unchecked")
  public <R extends OperationResult<T, U>> R withInstance(U instance) {
    this.instance = instance;
    return (R) this;
  }

  /**
   * Gets the optional updated planned object.
   * @return an option with the planned object that was updated within an operation. The option
   * is empty if no such object was updated.
   */
  public Optional<T> updated() {
    return Optional.ofNullable(updated);
  }

  /**
   * Gets the optional created planned object.
   * @return an option with the planned object that was created within an operation. The option
   * is empty if no such object was created.
   */
  public Optional<T> created() {
    return Optional.ofNullable(created);
  }

  /**
   * Gets the optional updated instance of a planned object.
   * @return an option with the instance of a planned object that was updated within an operation.
   * The option is empty if no such instance was updated.
   */
  public Optional<U> instance() {
    return Optional.ofNullable(instance);
  }

  /**
   * Is this operation result empty? A result is empty if the operation concludes in no update nor
   * creation of a resource. This is can be for a deletion for example.
   * @return true if there is no result, false otherwise.
   */
  public boolean isEmpty() {
    return updated == null && created == null && instance == null;
  }
}
