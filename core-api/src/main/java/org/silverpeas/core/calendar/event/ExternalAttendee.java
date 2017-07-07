/*
 * Copyright (C) 2000 - 2017 Silverpeas
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
package org.silverpeas.core.calendar.event;

import org.silverpeas.core.persistence.datasource.model.jpa.EntityManagerProvider;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.EntityManager;

/**
 * An attendee that is a person external to Silverpeas. It can only be notified, and hence
 * identified, by an email address.
 * @author mmoquillon
 */
@Entity
@DiscriminatorValue("1")
public class ExternalAttendee extends Attendee {

  private ExternalAttendee(final String email, final CalendarEvent event) {
    super(email, event);
  }

  @Override
  public String getFullName() {
    return this.getId();
  }

  protected ExternalAttendee() {
  }

  public static AttendeeSupplier withEmail(final String email) {
    return p -> new ExternalAttendee(email, p);
  }

  @Override
  protected Attendee getFromPersistenceContext() {
    EntityManager entityManager = EntityManagerProvider.get().getEntityManager();
    return entityManager.find(ExternalAttendee.class, getNativeId());
  }

}
