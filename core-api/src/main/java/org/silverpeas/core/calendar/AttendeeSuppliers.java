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
import org.silverpeas.core.calendar.Attendee.AttendeeSupplier;

/**
 * Providers of suppliers of Attendee objects by encapsulating their concrete implementation.
 *
 * @author mmoquillon
 */
public class AttendeeSuppliers {

  private AttendeeSuppliers() {}

  /**
   * Gets a supplier of attendee representing a user from outside Silverpeas having the specified
   * email address.
   * @param email the email address of the attendee to supply later.
   * @return a supplier of an external attendee.
   */
  public static AttendeeSupplier fromEmail(final String email) {
    return ExternalAttendee.withEmail(email);
  }

  /**
   * Gets a supplier of an attendee representing the specified user in Silverpeas.
   * @param user a user in Silverpeas.
   * @return a supplier of an internal attendee.
   */
  public static AttendeeSupplier fromUser(final User user) {
    return InternalAttendee.fromUser(user);
  }
}
  