/*
 * Copyright (C) 2000 - 2025 Silverpeas
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
 * "http://www.silverpeas.com/legal/licensing"
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
 * Result of a business operation that was performed against a calendar event.
 */
public class EventOperationResult {

  private CalendarEvent updated;
  private CalendarEvent created;
  private CalendarEventOccurrence instance;

  /**
   * Sets to this result the calendar event that was updated in the scope of the business operation.
   * @param updated the updated calendar event.
   * @return itself.
   */
  public EventOperationResult withUpdated(CalendarEvent updated) {
    this.updated = updated;
    return this;
  }

  /**
   * Sets to this result the calendar event that was created in the scope of the business operation.
   * @param created the created calendar event.
   * @return itself.
   */
  public EventOperationResult withCreated(CalendarEvent created) {
    this.created = created;
    return this;
  }

  /**
   * Sets to this result an occurrence of the calendar event that was updated in the scope of the
   * business operation.
   * @param instance the updated occurrence of the calendar event.
   * @return itself.
   */
  public EventOperationResult withInstance(CalendarEventOccurrence instance) {
    this.instance = instance;
    return this;
  }

  /**
   * Gets the optional updated calendar event.
   * @return an option with the calendar event that was updated within an operation. The option
   * is empty if no such event was updated.
   */
  public Optional<CalendarEvent> updated() {
    return Optional.ofNullable(updated);
  }

  /**
   * Gets the optional created calendar event.
   * @return an option with the calendar event that was created within an operation. The option
   * is empty if no such event was created.
   */
  public Optional<CalendarEvent> created() {
    return Optional.ofNullable(created);
  }

  /**
   * Gets the optional updated occurrence of a calendar event.
   * @return an option with the occurrence of a calendar event that was updated within an operation.
   * The option is empty if no such instance was updated.
   */
  public Optional<CalendarEventOccurrence> instance() {
    return Optional.ofNullable(instance);
  }

  /**
   * Is this operation result empty? A result is empty if the operation concludes in no update nor
   * creation of a calendar event or occurrence of it. This is can be for a deletion for example.
   * @return true if there is no result, false otherwise.
   */
  public boolean isEmpty() {
    return updated == null && created == null && instance == null;
  }
}
  