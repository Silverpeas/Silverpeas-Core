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
 * "http://www.silverpeas.org/legal/licensing"
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

import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.persistence.datasource.model.jpa.EntityManagerProvider;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.EntityManager;

/**
 * An attendee that is a user in Silverpeas.
 * @author mmoquillon
 */
@Entity
@DiscriminatorValue("0")
public class InternalAttendee extends Attendee {

  private InternalAttendee(final User user, final CalendarEvent event) {
    super(user.getId(), event);
  }

  protected InternalAttendee() {
  }

  @Override
  public String getFullName() {
    return getUser().getDisplayedName();
  }

  public static AttendeeSupplier fromUser(final User user) {
    return p -> new InternalAttendee(user, p);
  }

  public User getUser() {
    return User.getById(getId());
  }

  @Override
  protected Attendee getFromPersistenceContext() {
    EntityManager entityManager = EntityManagerProvider.get().getEntityManager();
    return entityManager.find(InternalAttendee.class, getNativeId());
  }

}
