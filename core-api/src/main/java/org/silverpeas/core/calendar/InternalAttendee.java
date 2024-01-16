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
package org.silverpeas.core.calendar;

import org.silverpeas.core.admin.user.model.User;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

/**
 * An attendee that is a user in Silverpeas.
 * @author mmoquillon
 */
@Entity
@DiscriminatorValue("0")
public class InternalAttendee extends Attendee {

  /**
   * Constructs an empty internal attendee. It is dedicated to the persistence engine.
   */
  protected InternalAttendee() {
    // empty constructor for JPA
  }

  private InternalAttendee(final User user, final CalendarComponent calendarComponent) {
    super(user.getId(), calendarComponent);
  }

  /**
   * Gets a supplier of an internal attendee representing the specified user in Silverpeas.
   * @param user a user in Silverpeas.
   * @return a supplier of an internal attendee.
   */
  static AttendeeSupplier fromUser(final User user) {
    return p -> new InternalAttendee(user, p);
  }

  /**
   * Gets the full name of this attendee. The full name is made up of its first name followed of
   * its last name.
   * @return the attendee full name.
   */
  @Override
  public String getFullName() {
    return getUser().getDisplayedName();
  }

  /**
   * Gets the user in Silverpeas that is behind this attendee.
   * @return the user corresponding to this internal attendee.
   */
  public User getUser() {
    return User.getById(getId());
  }
}
