/*
 * Copyright (C) 2000 - 2022 Silverpeas
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.calendar;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

/**
 * An attendee that is a person external to Silverpeas. It can only be notified, and hence
 * identified, by an email address.
 * @author mmoquillon
 */
@Entity
@DiscriminatorValue("1")
public class ExternalAttendee extends Attendee {

  /**
   * Constructs an empty external attendee. It is dedicated to the persistence engine.
   */
  protected ExternalAttendee() {
    // empty constructor for JPA
  }

  private ExternalAttendee(final String email, final CalendarComponent calendarComponent) {
    super(email, calendarComponent);
  }

  /**
   * Gets a supplier of an external attendee having the specified email address.
   * @param email the email address of the attendee to supply later.
   * @return a supplier of an external attendee.
   */
  static AttendeeSupplier withEmail(final String email) {
    return p -> new ExternalAttendee(email, p);
  }

  /**
   * Gets the email address of this attendee.
   * @return the attendee's email address.
   */
  @Override
  public String getFullName() {
    return this.getId();
  }
}
