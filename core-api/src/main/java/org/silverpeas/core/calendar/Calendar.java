/*
 * Copyright (C) 2000 - 2016 Silverpeas
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

import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.calendar.repository.CalendarRepository;
import org.silverpeas.core.persistence.Transaction;
import org.silverpeas.core.persistence.datasource.model.identifier.UuidIdentifier;
import org.silverpeas.core.persistence.datasource.model.jpa.AbstractJpaEntity;
import org.silverpeas.core.persistence.datasource.repository.OperationContext;
import org.silverpeas.core.security.Securable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.NamedQuery;
import javax.persistence.PostLoad;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.util.List;

/**
 * A calendar is a particular system for scheduling and organizing events and activities that occur
 * at different times or on different dates throughout the year.
 *
 * Before adding any events or activities into a calendar, it requires to be saved into the
 * Silverpeas data source (use the {@code save} method for doing). Once saved, a store for events
 * is initialized for its event management.
 * @author mmoquillon
 */
@Entity
@NamedQuery(
    name = "calendarsByComponentInstanceId",
    query = "from Calendar c where c.componentInstanceId = :componentInstanceId " +
            "order by c.componentInstanceId, c.title, c.id")
@Table(name = "sb_cal_calendar")
public class Calendar extends AbstractJpaEntity<Calendar, UuidIdentifier> implements Securable {

  @Column(name = "instanceId", nullable = false)
  private String componentInstanceId;

  @Column(name = "title")
  private String title;

  @Transient
  private CalendarEventStore events;

  /**
   * Necessary for JPA management.
   */
  protected Calendar() {
  }

  /**
   * Creates a new calendar with the specified component instance identifier.
   * @param instanceId an identifier identifying an instance of a component in Silverpeas.
   * Usually, this identifier is the identifier of the component instance to which it belongs
   * (for example almanach32) or the identifier of the user personal calendar.
   */
  public Calendar(String instanceId) {
    this.componentInstanceId = instanceId;
  }

  /**
   * Gets a calendar by its identifier.
   * @param id the identifier of the aimed calendar.
   * @return the instance of the aimed calendar or null if it does not exist.
   */
  public static Calendar getById(final String id) {
    CalendarRepository calendarRepository = CalendarRepository.get();
    return calendarRepository.getById(id);
  }

  /**
   * Gets the calendars represented by the specified component instance.  For instance, the
   * component can be a collaborative application or a personal one.
   * @param instanceId the unique identifier identifying an instance of a Silverpeas
   * component.
   * @return a list containing the calendar instances which matched if any, empty list otherwise.
   */
  public static List<Calendar> getByComponentInstanceId(String instanceId) {
    CalendarRepository calendarRepository = CalendarRepository.get();
    return calendarRepository.getByComponentInstanceId(instanceId);
  }

  @Override
  public String getComponentInstanceId() {
    return componentInstanceId;
  }

  public String getTitle() {
    return this.title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  /**
   * Saves the calendar into the persistence context.
   */
  public void save() {
    Transaction.performInOne(() -> {
      CalendarRepository calendarRepository = CalendarRepository.get();
      calendarRepository.save(OperationContext.fromUser(User.getCurrentRequester()), this);
      initEventStore();
      return null;
    });
  }

  /**
   * Deletes the calendar in the persistence context.
   */
  public void delete() {
    Transaction.performInOne(() -> {
      CalendarRepository calendarRepository = CalendarRepository.get();
      calendarRepository.delete(this);
      this.events = null;
      return null;
    });
  }

  /**
   * Gets the events that were added into this calendar. This will be available only if the calendar
   * is persisted. Otherwise an {@link IllegalStateException} is thrown.
   * @return the {@link CalendarEventStore} instance of this calendar.
   */
  public CalendarEventStore getEvents() {
    if (!isPersisted()) {
      throw new IllegalStateException(
          "The calendar isn't persisted and then no event store was set up with the persistence " +
              "context of the calendar");
    }
    return events;
  }

  @PostLoad
  private final void initEventStore() {
    this.events = new CalendarEventStore(this);
  }
}
